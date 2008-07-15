package net.sf.taverna.t2.workbench.ui.activitypalette.menu;

import java.awt.Component;
import java.net.URI;

import javax.swing.JMenu;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;
import net.sf.taverna.t2.partition.QueryFactory;
import net.sf.taverna.t2.partition.QueryFactoryRegistry;
import net.sf.taverna.t2.partition.RootPartition;
import net.sf.taverna.t2.partition.SetModelChangeListener;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteComponent;

/**
 * A menu that provides a set up menu actions for adding new Activity queries
 * to the Activity Palette.
 * <br>
 * The Actions are discovered from the ActivityQueryFactory's found through
 * the QueryFactory SPI.
 * 
 * @author Stuart Owen
 * 
 * @see ActivityQueryFactory
 * @see QueryFactory
 *
 */
public class AddActivityMenu extends AbstractMenuCustom {

	public AddActivityMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#activity"),
				30,
				URI.create("http://taverna.sf.net/2008/t2workbench/menu#addActivity"));
	}
	
	@SuppressWarnings("unchecked")
	protected Component createCustomComponent() {
		JMenu addQueryMenu = new JMenu("New Query");
		addQueryMenu.setToolTipText("Open this menu to add a new Query");
		for (QueryFactory factory : QueryFactoryRegistry.getInstance()
				.getInstances()) {
			if (factory instanceof ActivityQueryFactory) {
				ActivityQueryFactory af = (ActivityQueryFactory) factory;
				if (af.hasAddQueryActionHandler()) {
					AddQueryActionHandler handler = af
							.getAddQueryActionHandler();
					RootPartition<?> root = ActivityPaletteComponent.getInstance().getRootPartition();
					SetModelChangeListener<ActivityItem> listener = (SetModelChangeListener<ActivityItem>)root.getSetModelChangeListener();
					handler
							.setSetModelChangeListener(listener);
					addQueryMenu.add(handler);
				}
			}
		}
		return addQueryMenu;
	}

}
