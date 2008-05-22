package net.sf.taverna.platformtest.api;

public abstract class AbstractDataBean implements DataBeanSPI {

	// Internal private key for use in mapping schemes
	private int id;

	protected String name;
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	/**
	 * Override equals method to allow hibernate's cache to work correctly
	 */
	public boolean equals(Object o) {
		if (o instanceof AbstractDataBean) {
			return ((AbstractDataBean) o).id == id;
		}
		return false;
	}

}
