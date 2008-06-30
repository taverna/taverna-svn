package net.sf.taverna.t2.activities.ncbi.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
/**
 * Represents a draggable NCBI service
 * @author Ian Dunlop
 *
 */
public class NCBIActivityItem extends AbstractActivityItem {
	
	private String url;
	private String category;
	private String operation;
	private String wsdlOperation;

	public Object getType() {
		return "Localworker";
	}

	@Override
	protected Object getConfigBean() {
		WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
		bean.setWsdl(this.url);
		bean.setOperation(this.wsdlOperation);
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(NCBIActivityItem.class
				.getResource("/localworker.png"));
	}

	@Override
	protected Activity<?> getUnconfiguredActivity() {
		return new WSDLActivity();
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCategory() {
		return category;
	}
	
	@Override
	public String toString() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation() {
		return operation;
	}

	public String getWsdlOperation() {
		return wsdlOperation;
	}

	public void setWsdlOperation(String wsdlOperation) {
		this.wsdlOperation = wsdlOperation;
	}

}
