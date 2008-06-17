package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.awt.datatransfer.Transferable;

/**
 * Used when dragging activities from the palette onto "something". Place it
 * inside a {@link Transferable} when doing a drag operation. Contains an
 * {@link Activity} and its configuration bean.
 * 
 * @author Ian Dunlop
 * 
 */
public class ActivityAndBeanWrapper {
	/** The Activity being dragged */
	private Activity activity;
	/** The bean used to configure the activity */
	private Object bean;

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

}
