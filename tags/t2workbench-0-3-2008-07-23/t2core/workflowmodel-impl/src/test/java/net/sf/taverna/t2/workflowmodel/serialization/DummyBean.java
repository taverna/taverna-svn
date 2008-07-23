package net.sf.taverna.t2.workflowmodel.serialization;

public class DummyBean {
	private int id;
	private String name;
	private InnerBean innerBean;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public InnerBean getInnerBean() {
		return innerBean;
	}
	public void setInnerBean(InnerBean innerBean) {
		this.innerBean = innerBean;
	}
	
	
}
