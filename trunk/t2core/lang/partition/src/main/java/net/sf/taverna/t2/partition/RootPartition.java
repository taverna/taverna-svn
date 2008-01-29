package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Subclass of Partition acting as the public access point for the partition
 * structure. Exposes a TreeModel for use with a UI.
 * 
 * @author Tom Oinn
 * 
 * @param <ItemType>
 *            all items added to this partition must cast to this type
 */
public class RootPartition<ItemType> extends
		Partition<ItemType, Object, Object> implements TreeModel {

	// Used to track where we ended up putting items with the addOrUpdateItem
	// method, this makes checking for duplicate items, reclassification and
	// removal of partitions much faster at the expense of a few bytes of memory
	private Map<ItemType, Partition<ItemType, ?, ?>> itemToLeafPartition;

	private PropertyExtractorRegistry propertyExtractorRegistry;
	
	/**
	 * Build a new empty root partition with the specified list of partition
	 * algorithm implementations used to recursively allocate new data items to
	 * the sub-partitions below this one on addition
	 * 
	 * @param pa
	 */
	public RootPartition(List<PartitionAlgorithmSPI<?>> pa, PropertyExtractorRegistry per) {
		super(null, pa, null, null);
		this.root = this;
		this.propertyExtractorRegistry = per;
		this.itemToLeafPartition = new HashMap<ItemType, Partition<ItemType, ?, ?>>();
	}

	/**
	 * Alter the list of partition algorithms which drive the construction of
	 * the partitions. Calling this effectively forces a complete rebuild of the
	 * tree structure which is an expensive operation so be careful when you use
	 * it.
	 * 
	 * @param pa
	 *            a new list of PartitionAlgorithmSPI instances to use as the
	 *            basis for the partition structure.
	 */
	public synchronized void setPartitionAlgorithmList(
			List<PartitionAlgorithmSPI<?>> pa) {
		if (pa.equals(this.partitionAlgorithms)) {
			// At the least this checks for reference equality, although I'm not
			// sure it does much more than that. TODO - replace this with a
			// smarter check to see whether the list has really changed, doing a
			// full re-build is expensive.
			return;
		}
		// First create a copy of the keyset containing all the items we've
		// added to this point.
		Set<ItemType> itemsToAdd = new HashSet<ItemType>(itemToLeafPartition
				.keySet());
		this.partitionAlgorithms = pa;
		this.children.clear();
		this.itemToLeafPartition.clear();
		treeStructureChanged(new TreeModelEvent(null, getTreePath()));
		for (ItemType item : itemsToAdd) {
			addOrUpdateItem(item);
		}
	}

	/**
	 * Add a new item to the partition structure. If the item already exists
	 * this is interpreted as a request to reclassify according to properties
	 * which may have changed. This is not the same as a reclassification due to
	 * modification of the partition algorithm list, and just refers to the
	 * specified item. If the item exists already and its classification is
	 * changed the model will be notified with a removal event from the previous
	 * location and the item will be added as usual immediately afterwards.
	 */
	public synchronized void addOrUpdateItem(ItemType item) {
		// First check whether the item is already stored
		if (itemToLeafPartition.containsKey(item)) {
			// request to reclassify item.
			List<Partition<ItemType, ?, ?>> partitions = itemToLeafPartition
					.get(item).getPartitionPath();
			// partitions[i].getPartitionValue is the result of running
			// getPartitionAlgorithms[i-1] on the item, we run through the array
			// until either we hit the end (no reclassification) or the item
			// classifies differently in which case we remove and re-add it.
			for (int i = 1; i < partitions.size(); i++) {
				PartitionAlgorithmSPI<?> pa = getPartitionAlgorithms().get(
						i - 1);
				Object existingValue = partitions.get(i).getPartitionValue();
				Object reclassifiedValue = pa.allocate(item, getPropertyExtractorRegistry());
				if (existingValue.equals(reclassifiedValue) == false) {
					// Items classify differently, remove it
					removeItem(item);
					// ...and add it back again, forcing reclassification
					super.addItem(item);
					return;
				}
			}
			// return as the item wasn't changed.
			return;
		} else {
			// Value wasn't already in the map so we just add it as usual
			super.addItem(item);
		}

	}

	/**
	 * Remove an item from the partition structure, if this leaves any
	 * partitions with zero item count they are removed as well to keep things
	 * tidy.
	 * 
	 * @param item
	 *            the item to remove from the partition structure. If this isn't
	 *            present in the structure this method does nothing along the
	 *            lines of the collections API.
	 */
	public synchronized void removeItem(ItemType item) {
		Partition<ItemType, ?, ?> partition = itemToLeafPartition.get(item);
		if (partition != null) {
			// Remove the item from the leaf partition
			partition.removeMember(item);
			// Traverse up the partition path and decrement the item count. If
			// any item count becomes zero we mark this as a partition to
			// remove, then at the end we remove the highest level one (so we
			// only have to send a single delete event to the tree view)
			for (Partition<ItemType, ?, ?> p : partition.getPartitionPath()) {
				synchronized (p) {
					p.itemCount--;
					if (p.getItemCount() == 0 && p != this) {
						// Can remove this node, all nodes after this will by
						// definition have item count of zero. The exception is
						// if this is the root node, in which case we just
						// ignore it and move on to the next child. This avoids
						// deleting the root, which is generally not a smart
						// thing to do to a tree.
						Partition<ItemType, ?, ?> parent = p.getParent();
						int indexInParent = getIndexOfChild(parent, p);
						parent.children.remove(indexInParent);
						treeNodesRemoved(new TreeModelEvent(null, parent
								.getTreePath(), new int[] { indexInParent },
								new Object[] { p }));
						break;
					}
				}
			}
			itemToLeafPartition.remove(item);
		}
	}

	/**
	 * Called by a leaf Partition when it has stored an item in its member set,
	 * used to keep track of where items have been stored to make removal more
	 * efficient.
	 */
	void itemStoredAt(ItemType item, Partition<ItemType, ?, ?> partition) {
		itemToLeafPartition.put(item, partition);
	}

	// ---------------------//
	// TreeModel interfaces //
	// ---------------------//
	private List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();

	@SuppressWarnings("unchecked")
	public Object getChild(Object parent, int index) {
		if (parent instanceof Partition) {
			Partition<ItemType, ?, ?> p = (Partition<ItemType, ?, ?>) parent;
			if (index < 0 || index >= p.getChildren().size()) {
				return null;
			} else {
				return p.getChildren().get(index);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public int getChildCount(Object parent) {
		if (parent instanceof Partition) {
			Partition<ItemType, ?, ?> p = (Partition<ItemType, ?, ?>) parent;
			return p.getChildren().size();
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public int getIndexOfChild(Object parent, Object child) {
		if (parent != null && child != null && parent instanceof Partition
				&& child instanceof Partition) {
			Partition<ItemType, ?, ?> p = (Partition<ItemType, ?, ?>) parent;
			Partition<ItemType, ?, ?> c = (Partition<ItemType, ?, ?>) child;
			if (p.root == c.root && p.root == this) {
				// Parent and child must both be members of this tree structure
				return p.getChildren().indexOf(child);
			}
		}
		return -1;
	}

	public Object getRoot() {
		// The root partition is also the root of the tree model
		return this;
	}

	public boolean isLeaf(Object node) {
		// No leaves at the moment as we're only considering partitions which
		// are by definition not leaves (the items within the last partition are
		// but at the moment we're not including them in the tree model)
		return false;
	}

	public void removeTreeModelListener(TreeModelListener l) {
		treeListeners.remove(l);
	}

	public void addTreeModelListener(TreeModelListener l) {
		if (treeListeners.contains(l) == false) {
			treeListeners.add(l);
		}
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		// Ignore this, the tree values are never changed by the user in this
		// implementation so we don't have to do anything
	}

	// -------------------------------------------------------//
	// Tree event forwarding helper methods used by Partition //
	// -------------------------------------------------------//

	void treeNodesChanged(TreeModelEvent e) {
		for (TreeModelListener listener : new ArrayList<TreeModelListener>(
				treeListeners)) {
			listener.treeNodesChanged(e);
		}
	}

	void treeNodesInserted(TreeModelEvent e) {
		for (TreeModelListener listener : new ArrayList<TreeModelListener>(
				treeListeners)) {
			listener.treeNodesInserted(e);
		}
	}

	void treeNodesRemoved(TreeModelEvent e) {
		for (TreeModelListener listener : new ArrayList<TreeModelListener>(
				treeListeners)) {
			listener.treeNodesRemoved(e);
		}
	}

	void treeStructureChanged(TreeModelEvent e) {
		for (TreeModelListener listener : new ArrayList<TreeModelListener>(
				treeListeners)) {
			listener.treeStructureChanged(e);
		}
	}

	public PropertyExtractorRegistry getPropertyExtractorRegistry() {
		return this.propertyExtractorRegistry;
	}

}
