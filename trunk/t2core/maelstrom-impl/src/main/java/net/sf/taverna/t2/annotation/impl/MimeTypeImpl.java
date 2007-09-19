package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.annotation.MimeType;

/**
 * Implementation of {@link MimeType}
 * 
 * @author Stuart Owen
 */
public class MimeTypeImpl implements MimeType {

	private String mimeType;
	
	public MimeTypeImpl(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getMIMEType() {
		return mimeType;
	}

}
