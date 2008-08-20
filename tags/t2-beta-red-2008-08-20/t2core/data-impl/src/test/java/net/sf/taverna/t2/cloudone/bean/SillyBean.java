package net.sf.taverna.t2.cloudone.bean;



/**
 * A very simple bean for testing with {@link BeanTest} and
 * {@link BeanSerialiserTest}.
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class SillyBean {

	private String name;

	public SillyBean() {
		name = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
