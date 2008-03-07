/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.Comparator;
import java.util.List;

import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.utility.TypedTreeModel;

/**
 * @author alanrw
 * 
 */
/**
 * @author alanrw
 * 
 * @param <O>
 *            The class of Object that is in the PropertiedObjectSet over which
 *            this is a model.
 */
public interface PropertiedTreeModel<O extends Beanable<?>> extends
		TypedTreeModel<PropertiedTreeNode<O>> {
	/**
	 * Return the list of PropertyKeySetting used to order the tree.
	 * 
	 * @return
	 */
	List<PropertyKeySetting> getPropertyKeySettings();

	/**
	 * Set the list of PropertyKeySetting used to order the tree.
	 * 
	 * @param settingList
	 *            The list of PropertyKeySetting to be used.
	 */
	void setPropertyKeySettings(List<PropertyKeySetting> settingList);

	/**
	 * Set the PropertiedGraphView of the PropertiedObjectSet over which this is
	 * a model.
	 * 
	 * @param propertiedGraphView
	 */
	void setPropertiedGraphView(final PropertiedGraphView<O> propertiedGraphView);

	/**
	 * Stop listening to changes in the PropertiedGraphView and hence in the
	 * PropertiedObjectSet.
	 */
	void detachFromGraphView();

	/**
	 * Set the filter to be used to check if objects within the
	 * PropertiedObjectSet should appear in the tree.
	 * 
	 * @param filter
	 */
	void setFilter(final PropertiedObjectFilter<O> filter);

	/**
	 * Return the filter used to check if objects within the PropertiedObjectSet
	 * should appear in the tree.
	 * 
	 * @return
	 */
	PropertiedObjectFilter<O> getFilter();

	/**
	 * Set the comparator to be used to order objects when they appear as leaves
	 * within the tree.
	 * 
	 * @param objectComparator
	 */
	void setObjectComparator(final Comparator<O> objectComparator);

	/**
	 * Return the comparator (if any) used to order objects when they appear as
	 * leaves within the tree. Null is returned if the objects' natural ordering
	 * is used.
	 * 
	 * @return
	 */
	Comparator<O> getObjectComparator();
}
