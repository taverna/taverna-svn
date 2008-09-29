package net.sf.taverna.t2.cloudone.refscheme.file;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

/**
 * Bean for serialising {@link FileReferenceScheme}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/refscheme/file/", name = "fileReferenceScheme")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/refscheme/file/", name = "fileReferenceScheme")
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
