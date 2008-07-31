package net.sf.taverna.t2.activities.wsdl.xmlsplitter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * A helper class to facilitate in building an XMLSplitter configuration bean
 * from the type descriptor XML, including setting up the ports.
 * 
 * @author Stuart Owen
 * 
 */
public class XMLSplitterConfigurationBeanBuilder {
	
	public static XMLSplitterConfigurationBean buildBeanForInput(TypeDescriptor descriptor) throws JDOMException, IOException {
		Element el = XMLSplitterSerialisationHelper.typeDescriptorToExtensionXML(descriptor);
		String xml = new XMLOutputter().outputString(el);
		return buildBeanForInput(xml);
	}
	
	public static XMLSplitterConfigurationBean buildBeanForOutput(TypeDescriptor descriptor) throws JDOMException, IOException {
		Element el = XMLSplitterSerialisationHelper.typeDescriptorToExtensionXML(descriptor);
		String xml = new XMLOutputter().outputString(el);
		return buildBeanForOutput(xml);
	}

	public static XMLSplitterConfigurationBean buildBeanForInput(String xml) throws JDOMException, IOException {
		XMLSplitterConfigurationBean bean = new XMLSplitterConfigurationBean();
		List<ActivityInputPortDefinitionBean> inputDefinitions = new ArrayList<ActivityInputPortDefinitionBean>();
		List<ActivityOutputPortDefinitionBean> outputDefinitions = new ArrayList<ActivityOutputPortDefinitionBean>();

		Element element = new SAXBuilder().build(new StringReader(xml))
				.getRootElement();
		TypeDescriptor descriptor = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(element);
		ActivityOutputPortDefinitionBean outBean = new ActivityOutputPortDefinitionBean();
		outBean.setName("output");
		outBean.setMimeTypes(Collections.singletonList("'text/xml'"));
		outBean.setDepth(0);
		outBean.setGranularDepth(0);
		outputDefinitions.add(outBean);
		
		if (descriptor instanceof ComplexTypeDescriptor) {
			List<TypeDescriptor> elements = ((ComplexTypeDescriptor) descriptor).getElements();
			String[] names = new String[elements.size()];
			Class<?>[] types = new Class<?>[elements.size()];
			TypeDescriptor.retrieveSignature(elements, names, types);
			for (int i = 0; i < names.length; i++) {
				ActivityInputPortDefinitionBean portBean = new ActivityInputPortDefinitionBean();
				portBean.setName(names[i]);
				portBean.setMimeTypes(Collections.singletonList(TypeDescriptor
						.translateJavaType(types[i])));
				inputDefinitions.add(portBean);
				portBean.setDepth(0);
			}
		} else if (descriptor instanceof ArrayTypeDescriptor) {
			ActivityInputPortDefinitionBean portBean = new ActivityInputPortDefinitionBean();
			portBean.setName(descriptor.getType());
			if (((ArrayTypeDescriptor) descriptor).getElementType() instanceof BaseTypeDescriptor) {
				portBean.setMimeTypes(Collections
						.singletonList("l('text/plain')"));
			} else {
				portBean.setMimeTypes(Collections
						.singletonList("l('text/xml')"));
			}
			inputDefinitions.add(portBean);
		}
		
		bean.setOutputPortDefinitions(outputDefinitions);
		bean.setInputPortDefinitions(inputDefinitions);
		bean.setWrappedTypeXML(xml);
		
		return bean;
	}

	public static XMLSplitterConfigurationBean buildBeanForOutput(String xml)
			throws JDOMException, IOException {
		XMLSplitterConfigurationBean bean = new XMLSplitterConfigurationBean();
		List<ActivityInputPortDefinitionBean> inputDefinitions = new ArrayList<ActivityInputPortDefinitionBean>();
		List<ActivityOutputPortDefinitionBean> outputDefinitions = new ArrayList<ActivityOutputPortDefinitionBean>();

		Element element = new SAXBuilder().build(new StringReader(xml))
				.getRootElement();
		TypeDescriptor descriptor = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(element);

		ActivityInputPortDefinitionBean inBean = new ActivityInputPortDefinitionBean();
		inBean.setName("input");
		inBean.setMimeTypes(Collections.singletonList("'text/xml'"));
		inBean.setDepth(0);
		inBean
				.setHandledReferenceSchemes(new ArrayList<Class<? extends ExternalReferenceSPI>>());
		inBean.setTranslatedElementType(String.class);

		inputDefinitions.add(inBean);

		if (descriptor instanceof ComplexTypeDescriptor) {
			List<TypeDescriptor> elements = ((ComplexTypeDescriptor) descriptor).getElements();
			String[] names = new String[elements.size()];
			Class<?>[] types = new Class<?>[elements.size()];
			TypeDescriptor.retrieveSignature(elements, names, types);
			for (int i = 0; i < names.length; i++) {
				ActivityOutputPortDefinitionBean portBean = new ActivityOutputPortDefinitionBean();
				portBean.setName(names[i]);
				portBean.setMimeTypes(Collections.singletonList(TypeDescriptor
						.translateJavaType(types[i])));
				outputDefinitions.add(portBean);
				portBean.setDepth(0);
			}
		} else if (descriptor instanceof ArrayTypeDescriptor) {
			ActivityOutputPortDefinitionBean portBean = new ActivityOutputPortDefinitionBean();
			portBean.setName(descriptor.getType());
			if (((ArrayTypeDescriptor) descriptor).getElementType() instanceof BaseTypeDescriptor) {
				portBean.setMimeTypes(Collections
						.singletonList("l('text/plain')"));
			} else {
				portBean.setMimeTypes(Collections
						.singletonList("l('text/xml')"));
			}
			outputDefinitions.add(portBean);
		}

		bean.setInputPortDefinitions(inputDefinitions);
		bean.setOutputPortDefinitions(outputDefinitions);
		
		bean.setWrappedTypeXML(xml);

		return bean;
	}

}
