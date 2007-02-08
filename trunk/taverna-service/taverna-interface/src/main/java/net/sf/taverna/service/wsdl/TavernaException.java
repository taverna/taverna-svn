package net.sf.taverna.service.wsdl;

public abstract class TavernaException extends Exception {

	private String msg;

	public TavernaException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
	

}
