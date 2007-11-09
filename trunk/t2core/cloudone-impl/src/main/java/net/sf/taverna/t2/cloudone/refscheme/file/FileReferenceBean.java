package net.sf.taverna.t2.cloudone.refscheme.file;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

public class FileReferenceBean extends ReferenceBean {

	private String file;
	private String charset;

	@Override
	public Class<FileReferenceScheme> getOwnerClass() {
		return FileReferenceScheme.class;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getCharset() {
		return charset;
	}
	

}
