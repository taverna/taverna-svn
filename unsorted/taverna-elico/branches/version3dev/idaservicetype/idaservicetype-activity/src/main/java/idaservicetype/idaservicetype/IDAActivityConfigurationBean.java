package idaservicetype.idaservicetype;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

/**
 * Example activity configuration bean.
 * 
 */
public class IDAActivityConfigurationBean implements Serializable {

	/*
	 * TODO: Remove this comment.
	 * 
	 * The configuration specifies the variable options and configurations for
	 * an activity that has been added to a workflow. For instance for a WSDL
	 * activity, the configuration contains the URL for the WSDL together with
	 * the method name. String constant configurations contain the string that
	 * is to be returned, while Beanshell script configurations contain both the
	 * scripts and the input/output ports (by subclassing
	 * ActivityPortsDefinitionBean).
	 * 
	 * Configuration beans are serialised as XML (currently by using XMLBeans)
	 * when Taverna is saving the workflow definitions. Therefore the
	 * configuration beans need to follow the JavaBeans style and only have
	 * fields of 'simple' types such as Strings, integers, etc. Other beans can
	 * be referenced as well, as long as they are part of the same plugin.
	 */
	
	// TODO: Remove the example fields and getters/setters and add your own	
	private String exampleString;
	
	private transient Dataflow dataflow;

	private String selectedTask;
	
	private boolean isTemplate;


	 private void writeObject(java.io.ObjectOutputStream out) throws IOException, SerializationException {
		
		 System.out.println(" STARTING");
		 XMLSerializer serializer = new XMLSerializerImpl();
		 System.out.println(" STARTING got xmlserializer");

		 Element serialized = null;
		 System.out.println(" STARTING constructed");
		 System.out.println(" STARTING serialization...");

		 serialized = serializer.serializeDataflow(dataflow);
		 System.out.println(" SERIALIZED");

		 XMLOutputter outputter = new XMLOutputter();
		 String dfXML = outputter.outputString(serialized);
		 System.out.println(" HERE 1");

		 MyObject obj = new MyObject();
		 obj.setDataflow(dfXML);
		 obj.setExampleString(exampleString);
		 obj.setSelectedTask(selectedTask);
		 System.out.println(" HERE 2");

		 out.writeObject(obj);
		 System.out.println(" HERE 3");

		 out.flush(); 
		 System.out.println(" HERE 4");

	 }
	 
	 private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		 System.out.println(" HERE 5");

		 System.out.println(" HERE 6");

		 XMLDeserializer deserializer  = new XMLDeserializerImpl();
		 System.out.println(" HERE 7");

		 MyObject obj = (MyObject) in.readObject();
		 System.out.println(" HERE 8");

		 Element myElement = new Element("workflow");
		 myElement.addContent(obj.getDataflow());
		 System.out.println(" HERE 9");
		 System.out.println(" ACTIUAL DATAFLOW STRING: " + obj.getDataflow());

		 SAXBuilder builder = new SAXBuilder();
		 Document document = null;
		try {
			document = builder.build(new StringReader(obj.getDataflow()));
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	      Element root = document.getRootElement();
		 
		 try {
			dataflow = deserializer.deserializeDataflow(root);
			 System.out.println(" HERE 10");

		} catch (DeserializationException e) {
			e.printStackTrace();
		} catch (EditException e) {
			e.printStackTrace();
		}
		 System.out.println(" HERE 11");

		exampleString = obj.getExampleString(); 
		selectedTask = obj.getSelectedTask();
		 System.out.println(" HERE 12");

	 }
	
	public String getExampleString() {
		return exampleString;
	}

	public void setExampleString(String exampleString) {
		this.exampleString = exampleString;
	}

	//public URI getExampleUri() {
	//	return exampleUri;
	//}

	//public void setExampleUri(URI exampleUri) {
	//	this.exampleUri = exampleUri;
	//}

	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

	public void setTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	public boolean isTemplate() {
		return isTemplate;
	}

	public void setSelectedTask(String selectedTask) {
		this.selectedTask = selectedTask;
	}

	public String getSelectedTask() {
		return selectedTask;
	}

}
