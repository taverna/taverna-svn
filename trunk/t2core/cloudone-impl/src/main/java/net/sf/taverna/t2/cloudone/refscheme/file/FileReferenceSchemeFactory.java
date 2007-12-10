package net.sf.taverna.t2.cloudone.refscheme.file;

import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

/**
 * Factory used to associate {@link FileReferenceScheme} with
 * {@link FileReferenceBean} for the purpose of serialising/deserialising
 * 
 * @see BeanSerialiser
 * @see BeanableFactory
 * @see BeanableFactoryRegistry
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileReferenceSchemeFactory extends
		BeanableFactory<FileReferenceScheme, FileReferenceBean> {

	public FileReferenceSchemeFactory() {
		super(FileReferenceScheme.class, FileReferenceBean.class);
	}

}
