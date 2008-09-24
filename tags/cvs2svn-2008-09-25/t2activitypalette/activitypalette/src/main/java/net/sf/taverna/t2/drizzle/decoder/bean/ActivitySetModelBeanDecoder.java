/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;

import net.sf.taverna.t2.drizzle.bean.ActivitySetModelBean;
import net.sf.taverna.t2.drizzle.bean.ProcessorFactoryAdapterBean;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoderRegistry;
import net.sf.taverna.t2.drizzle.decoder.processorfactory.DefaultProcessorFactoryDecoder;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.query.DecodeRunIdentification;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Element;

/**
 * @author alanrw
 * 
 */
public final class ActivitySetModelBeanDecoder implements
		PropertyDecoder<ActivitySetModelBean, ProcessorFactoryAdapter> {

	/**
	 * @see net.sf.taverna.t2.drizzle.decoder.PropertyDecoder#canDecode(java.lang.Class, java.lang.Class)
	 */
	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		return (targetClass.isAssignableFrom(ProcessorFactoryAdapter.class) && ActivitySetModelBean.class
				.isAssignableFrom(sourceClass));
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.decoder.PropertyDecoder#decode(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public DecodeRunIdentification<ProcessorFactoryAdapter> decode(
			PropertiedObjectSet<ProcessorFactoryAdapter> targetSet,
			ActivitySetModelBean encodedObject) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (encodedObject == null) {
			throw new NullPointerException("encodedObject cannot be null"); //$NON-NLS-1$
		}
		DecodeRunIdentification<ProcessorFactoryAdapter> ident = new DecodeRunIdentification<ProcessorFactoryAdapter>();
		ident.setAffectedObjects(new HashSet<ProcessorFactoryAdapter> ());
		ident.setPropertyKeyProfile(new HashSet<PropertyKey>());
		ident.setTimeOfRun(System.currentTimeMillis());
		
		for (ProcessorFactoryAdapterBean adapterBean : encodedObject.getAdapterBeans()) {
			byte[] byteArray = adapterBean.getXmlFragment();
			ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(bais);
				Element xmlElement = (Element) ois.readObject();
				ProcessorFactory pf = ProcessorHelper.loadFactoryFromXML(xmlElement);
				PropertyDecoder decoder = PropertyDecoderRegistry.getDecoder(
						pf.getClass(), ProcessorFactoryAdapter.class);
				if (decoder == null) {
					decoder = DefaultProcessorFactoryDecoder.getInstance(pf.getClass());
				}
				if (decoder != null) {
					DecodeRunIdentification<ProcessorFactoryAdapter> subIdent =
						decoder.decode(targetSet, pf);
					ident.getAffectedObjects().addAll(subIdent.getAffectedObjects());
					ident.getPropertyKeyProfile().addAll(subIdent.getPropertyKeyProfile());
				} else {
					throw new NullPointerException ("No decoder found for " + pf.getClass().getName()); //$NON-NLS-1$
				}
			} catch (IOException e) {
				// TODO What to do?
			} catch (ClassNotFoundException e) {
				// TODO What to do?
			} catch (NullPointerException e) {
				// TODO What to do?
			}
			
		}
		return ident;
	}
}