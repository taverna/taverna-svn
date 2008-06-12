package net.sf.taverna.t2.partition;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.partition.PartitionTestApplication.AlwaysOpenJTree;
import net.sf.taverna.t2.partition.PartitionTestApplication.TableTreeNodeColumnImpl;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;
import net.sf.taverna.t2.partition.ui.TableTreeNodeColumn;
import net.sf.taverna.t2.partition.ui.TableTreeNodeRenderer;

public class ActivityPaletteDemo {
	boolean toggle=false;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ActivityPaletteDemo().start();
	}

	private void start() {
		JFrame frame = new JFrame();
		final PropertyExtractorRegistry reg = getReg();
		RootPartition<?> partition = getPartition(reg);
		frame.getContentPane().setLayout(new BorderLayout());
		final JTree tree=addTree(frame.getContentPane(), partition, reg);
		
		final WSDLQuery q = new WSDLQuery();
		q.addSetModelChangeListener((SetModelChangeListener)partition.getSetModelChangeListener());
		
		final SoaplabQuery q2 = new SoaplabQuery();
		q2.addSetModelChangeListener((SetModelChangeListener<SoaplabActivityItem>)partition.getSetModelChangeListener());
		
		final LocalWorkerQuery q3 = new LocalWorkerQuery();
		q3.addSetModelChangeListener((SetModelChangeListener<LocalWorkerActivityItem>)partition.getSetModelChangeListener());
//		final WSDLQuery q2 = new WSDLQuery();
		
		
		frame.setSize(800, 600);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		frame.getContentPane().add(new JButton(new AbstractAction("Press Me!") {

			public void actionPerformed(ActionEvent arg0) {
				q.doQuery();
				q2.doQuery();
				q3.doQuery();
			}
			
		}),BorderLayout.SOUTH);
		frame.getContentPane().add(new JButton(new AbstractAction("Press Me again!") {

			public void actionPerformed(ActionEvent arg0) {
				RootPartition<?> partition;
				if (!toggle) {
					partition = getPartition2(reg);
				}
				else {
					 partition = getPartition(reg);
				}
				toggle=!toggle;
				q.addSetModelChangeListener((SetModelChangeListener)partition.getSetModelChangeListener());
				q2.addSetModelChangeListener((SetModelChangeListener<SoaplabActivityItem>)partition.getSetModelChangeListener());
				q3.addSetModelChangeListener((SetModelChangeListener<LocalWorkerActivityItem>)partition.getSetModelChangeListener());
				tree.setModel(partition);
				q.doQuery();
				q2.doQuery();
				q3.doQuery();
			}
			
		}),BorderLayout.SOUTH);
		

		frame.setVisible(true);
	}

	private JTree addTree(Container contentPane, RootPartition<?> partition,
			PropertyExtractorRegistry reg) {

		JTree partitionTree = new AlwaysOpenJTree(partition);
		partitionTree.setRowHeight(24);
		TableTreeNodeRenderer renderer = getTreeNodeRenderer(partitionTree
				.getCellRenderer(), reg);
		partitionTree.setCellRenderer(new DefaultTreeCellRenderer());

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.add(new JScrollPane(partitionTree));
		
		return partitionTree;
	}

	private TableTreeNodeRenderer getTreeNodeRenderer(
			TreeCellRenderer cellRenderer, final PropertyExtractorRegistry reg) {
		return new TableTreeNodeRenderer(cellRenderer, 250) {
			@Override
			public TableTreeNodeColumn[] getColumns() {
				return new TableTreeNodeColumn[] {
						new TableTreeNodeColumnImpl("type", new Color(150, 150,
								210), 100, reg),
						new TableTreeNodeColumnImpl("name", new Color(150, 150,
								210), 100, reg),
						new TableTreeNodeColumnImpl("category", new Color(150, 150,
								210), 100, reg),
						new TableTreeNodeColumnImpl("operation", new Color(150, 150,
								210), 100, reg) };
			}
		};
	}

	private RootPartition<?> getPartition(PropertyExtractorRegistry reg) {
		return new RootPartition<ActivityItem>(getAlgorithms(), reg);
	}
	
	private RootPartition<?> getPartition2(PropertyExtractorRegistry reg) {
		return new RootPartition<ActivityItem>(getAlgorithms2(), reg);
	}

	private PropertyExtractorRegistry getReg() {
		return new PropertyExtractorRegistry() {

			public Map<String, Object> getAllPropertiesFor(Object target) {
				Map<String, Object> result = new HashMap<String, Object>();
				if (target instanceof ActivityItem) {
					ActivityItem ai = (ActivityItem) target;
					result.put("type", ai.getType());
					result.put("name", ai.getName());
				}
				if (target instanceof WSDLActivityItem) {
					WSDLActivityItem ai = (WSDLActivityItem) target;
					result.put("operation", ai.getOperation());
				}
				if (target instanceof SoaplabActivityItem) {
					SoaplabActivityItem ai = (SoaplabActivityItem)target;
					result.put("category",ai.getCategory());
					result.put("operation",ai.getOperation());
				}
				if (target instanceof LocalWorkerActivityItem) {
					LocalWorkerActivityItem ai = (LocalWorkerActivityItem)target;
					result.put("category",ai.getCategory());
				}

				return result;
			}

		};
	}

	private List<PartitionAlgorithm<?>> getAlgorithms() {
		List<PartitionAlgorithm<?>> list = new ArrayList<PartitionAlgorithm<?>>();
		LiteralValuePartitionAlgorithm alg = new LiteralValuePartitionAlgorithm();
		alg.setPropertyName("type");

		LiteralValuePartitionAlgorithm alg2 = new LiteralValuePartitionAlgorithm();
		alg2.setPropertyName("category");
		
		list.add(alg);
		//list.add(alg2);
		return list;
	}
	
	private List<PartitionAlgorithm<?>> getAlgorithms2() {
		List<PartitionAlgorithm<?>> list = new ArrayList<PartitionAlgorithm<?>>();
		LiteralValuePartitionAlgorithm alg = new LiteralValuePartitionAlgorithm();
		alg.setPropertyName("category");

		LiteralValuePartitionAlgorithm alg2 = new LiteralValuePartitionAlgorithm();
		alg2.setPropertyName("type");
		
		LiteralValuePartitionAlgorithm alg3 = new LiteralValuePartitionAlgorithm();
		alg3.setPropertyName("operation");
		
		list.add(alg);
//		list.add(alg2);
//		list.add(alg3);
		return list;
	}
}
