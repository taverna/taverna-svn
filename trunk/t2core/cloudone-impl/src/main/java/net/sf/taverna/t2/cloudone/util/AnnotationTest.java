package net.sf.taverna.t2.cloudone.util;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.bean.EntityListBean;
import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;
import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceBean;
import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;

import org.apache.log4j.Logger;
import org.jboss.jaxb.intros.IntroductionsAnnotationReader;
import org.jboss.jaxb.intros.IntroductionsConfigParser;
import org.jboss.jaxb.intros.configmodel.JaxbIntros;


import com.sun.xml.bind.api.JAXBRIContext;

public class AnnotationTest {
	public static final String DEFAULT_NAMESPACE = "http://taverna.sf.net/t2/cloudone/bean/unknown/";
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AnnotationTest.class);
	private static BeanableFactoryRegistry beanableFactoryRegistry = BeanableFactoryRegistry
			.getInstance();

	
	public static void main(String[] args) throws JAXBException, IOException {
		AnnotationTest annotationTest = new AnnotationTest();
		
		annotationTest.annotateDataDoc();
		annotationTest.annotateEntityList();
		annotationTest.annotateErrorDoc();
		System.exit(0);
	}


	
	public void annotateDataDoc() throws JAXBException, IOException {

		JAXBContext context = makeJAXBContext();

		DataDocumentBean docBean = new DataDocumentBean();
		docBean.setIdentifier("sgsgdgdg");

		HttpReferenceBean refBean = new HttpReferenceBean();
		String url = "http://google.com";
		refBean.setUrl(url);
		List<ReferenceBean> list = new ArrayList<ReferenceBean>();
		list.add(refBean);
		docBean.setReferences(list);

		Marshaller marshaller = context.createMarshaller();
		File file = File.createTempFile("annotationTest", ".xml");
		marshaller.marshal(docBean, System.out);
		marshaller.marshal(docBean, file);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		@SuppressWarnings("unused")
		DataDocumentBean retrDocBean = (DataDocumentBean) unmarshaller
				.unmarshal(file);
	}

	@SuppressWarnings("unchecked")
	public JAXBContext makeJAXBContext() throws JAXBException {
		List<Class> beanClasses = new ArrayList<Class>();
		JaxbIntros mergedConfig = new JaxbIntros();
		for (BeanableFactory beanableFactory : beanableFactoryRegistry
				.getInstances()) {
			InputStream annotationStream = beanableFactory
					.getAnnotationIntroduction();
			beanClasses.add(beanableFactory.getBeanType());
			if (annotationStream == null) {
				logger.info("No annotation introduction found for "
						+ beanableFactory);
				continue;
			}
			// SUPER-HACK
//			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			JaxbIntros beanableConfig = IntroductionsConfigParser
					.parseConfig(annotationStream);
			mergedConfig.getClazz().addAll(beanableConfig.getClazz());
		}

		IntroductionsAnnotationReader reader = new IntroductionsAnnotationReader(
				mergedConfig);

		Map<String, Object> jaxbConfig = new HashMap<String, Object>();
		jaxbConfig.put(JAXBRIContext.ANNOTATION_READER, reader);

		jaxbConfig
				.put(JAXBRIContext.DEFAULT_NAMESPACE_REMAP, DEFAULT_NAMESPACE);

		JAXBContext context = JAXBContext.newInstance(beanClasses
				.toArray(new Class[0]), jaxbConfig);
		return context;
	}


	public void annotateErrorDoc() throws JAXBException {
		
		JAXBContext context = makeJAXBContext();

		ErrorDocumentBean bean = new ErrorDocumentBean();
		bean.setIdentifier("sdfgbdfb");
		bean.setMessage("something broke");
		bean.setStackTrace("stacktrace/n");

		Marshaller marshaller = context.createMarshaller();
		System.out.println();
		marshaller.marshal(bean, System.out);
	}
	
	public void annotateEntityList() throws JAXBException  {
		JAXBContext context = makeJAXBContext();
		EntityListBean bean = new EntityListBean();
		bean.setIdentifier("some-kind_of-identifier");
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("some-other_identifier");
		identifiers.add("the-second one");
		identifiers.add("last <weird> one");
		bean.setContent(identifiers);
		

		Marshaller marshaller = context.createMarshaller();
		System.out.println();
		marshaller.marshal(bean, System.out);
		
	}
}