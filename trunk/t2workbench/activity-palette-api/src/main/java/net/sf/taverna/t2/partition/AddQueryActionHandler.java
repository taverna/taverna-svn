package net.sf.taverna.t2.partition;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * Abstract class that defines the Action handler responsible for adding new ActivityQueries.
 * @author Stuart Owen
 *
 */
public abstract class AddQueryActionHandler extends AbstractAction {
	
	private SetModelChangeListener<ActivityItem> setModelChangeListener;
	
	public AddQueryActionHandler() {
		super();
		putValue(AbstractAction.NAME,getText());
		putValue(AbstractAction.SMALL_ICON,getIcon());
	}

	public AddQueryActionHandler(String name, Icon icon) {
		super(name, icon);
	}

	public AddQueryActionHandler(String name) {
		super(name);
	}
	
	public void setSetModelChangeListener(SetModelChangeListener<ActivityItem> listener) {
		this.setModelChangeListener=listener;
	}
	
	protected SetModelChangeListener<ActivityItem> getSetModelChangeListener() {
		return this.setModelChangeListener;
	}
	
	protected void addQuery(final Query<?> query) {
		query.addSetModelChangeListener((SetModelChangeListener)getSetModelChangeListener());
		Thread t = new Thread("Add Query thread") {
			@Override
			public void run() {
				query.doQuery();
			}
		};
		t.start();
	}

	/**
	 * The implementation should invoke the appropriate action to handle adding a new query
	 */
	public abstract void actionPerformed(ActionEvent actionEvent);
	
	protected abstract String getText();
	protected abstract Icon getIcon();

}
