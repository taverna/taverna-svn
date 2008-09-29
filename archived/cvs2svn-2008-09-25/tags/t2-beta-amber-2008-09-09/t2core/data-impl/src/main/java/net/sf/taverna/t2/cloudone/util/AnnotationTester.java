/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.cloudone.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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

/**
 * For compatibility testing with Raven
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class AnnotationTester {
	public static final String DEFAULT_NAMESPACE = "http://taverna.sf.net/t2/cloudone/bean/unknown/";
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AnnotationTester.class);
	private BeanableFactoryRegistry beanableFactoryRegistry = BeanableFactoryRegistry
			.getInstance();

	public static void main(String[] args) throws JAXBException, IOException {
		AnnotationTester annotationTest = new AnnotationTester();

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
		OutputStream outputStream = new FileOutputStream(file);
		marshaller.marshal(docBean, System.out);
		marshaller.marshal(docBean, outputStream);
		outputStream.close();
		InputStream inputStream = new FileInputStream(file);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		@SuppressWarnings("unused")
		DataDocumentBean retrDocBean = (DataDocumentBean) unmarshaller
				.unmarshal(inputStream);
	}

	@SuppressWarnings("unchecked")
	public JAXBContext makeJAXBContext() throws JAXBException {
		List<Class> beanClasses = new ArrayList<Class>();
		for (BeanableFactory beanableFactory : beanableFactoryRegistry
				.getInstances()) {
			beanClasses.add(beanableFactory.getBeanType());
		}

		JAXBContext context = JAXBContext.newInstance(beanClasses
				.toArray(new Class[0]));
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

	public void annotateEntityList() throws JAXBException {
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
