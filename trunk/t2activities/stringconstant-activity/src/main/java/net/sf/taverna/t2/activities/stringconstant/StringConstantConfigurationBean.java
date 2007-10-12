package net.sf.taverna.t2.activities.stringconstant;

/**
 * Configuration bean for setting up a StringConstantActivity.<br>
 * The only thing to be configured is the string value, since the ports are fixed.
 * 
 * @author Stuart Owen
 * @see StringConstantActivity
 */
public class StringConstantConfigurationBean {
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
