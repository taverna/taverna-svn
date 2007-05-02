package net.sf.taverna.t2.workflowmodel.processor;

/**
 * Trivial property class to configure the echo service. As the echo service
 * actually has no configuration whatsoever I've put some dummy properties
 * in here to test the serialization framework.
 * 
 * @author Tom
 * 
 */
public class EchoConfig {

	private String foo = "wibble!";
	
	public String getFoo() {
		return this.foo;
	}
	
	public void setFoo(String newFoo) {
		this.foo = newFoo;
	}
	
	public EchoConfig() {
		//
	}

	public EchoConfig(String newFoo) {
		super();
		this.foo = newFoo;
	}
	
}