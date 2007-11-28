package net.sf.taverna.t2.cloudone.refscheme.file;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;

public class FileReferenceSchemeFactory extends
		BeanableFactory<FileReferenceScheme, FileReferenceBean> {

	public FileReferenceSchemeFactory() {
		super(FileReferenceScheme.class, FileReferenceBean.class);
	}

}
