/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.Comparator;
import java.util.List;

import net.sf.taverna.t2.utility.TypedTreeModel;

/**
 * @author alanrw
 *
 */
public interface PropertiedTreeModel<O> extends TypedTreeModel<PropertiedTreeNode<O>>{
	List<PropertyKeySetting> getPropertyKeySettings();
	void setPropertyKeySettings(List<PropertyKeySetting> settingList);
	
	void setPropertiedGraphView(final PropertiedGraphView<O> propertiedGraphView);
	void setFilter(final PropertiedObjectFilter<O> filter);
	PropertiedObjectFilter<O> getFilter();
	
	void setObjectComparator(final Comparator<O> objectComparator);
	Comparator<O> getObjectComparator();
}
