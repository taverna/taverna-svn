package net.sf.taverna.service.interfaces;

public abstract class TavernaException extends Exception {

	private String msg;
	
	public TavernaException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public TavernaException(String msg, Exception cause) {
		super(msg, cause);
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
	

}
