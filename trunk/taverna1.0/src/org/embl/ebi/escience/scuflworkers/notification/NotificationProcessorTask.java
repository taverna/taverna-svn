/*
 * NotificationProcessorTaskImpl.java
 *
 * Created on 25 February 2004, 17:56
 */

package org.embl.ebi.escience.scuflworkers.notification;


import uk.ac.soton.ecs.iam.notification.dynamicproxy.DynamicProxy;
import uk.ac.soton.ecs.iam.notification.publishprocessor.infc.PublishProcessor;
import uk.ac.soton.ecs.iam.notification.constants.NotificationProcessorConstants;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.ecs.iam.notification.notifconfig.infc.NotificationProcessorConfiguration;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.StringReader;
import java.lang.reflect.Method;
import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

/**
 *
 * @author  kahs
 * @author Justin Ferris
 */
public class NotificationProcessorTask implements ProcessorTaskWorker {
    private Processor processor;
    private static Class publishProcessorClass;
    private static PublishProcessor publishProcessor;
    private static DynamicProxy proxy;
    private static String portDelimiter = ":";
    private String methodName;
    private DataThing topic;
    private DataThing message;
    private static NotificationProcessorConfiguration configuration;

    /**
     * Creates a new instance of NotificationProcessorTaskImpl
     * The processor used here is a dummy processor just to
     * plug in to taverna for gui purposes.
     */
    public NotificationProcessorTask(Processor p) {
        this.processor = p;
    }

    public NotificationProcessorTask(){

    }

    public Map execute(Map inputMap, ProcessorTask parentTask) throws TaskExecutionException {
        try{
            Set set = inputMap.keySet();
            for(Iterator itr = set.iterator(); itr.hasNext();){
                String portName = (String) itr.next();
                DataThing value = (DataThing) inputMap.get(portName);
				if(portName.equalsIgnoreCase(NotificationProcessorConstants.PUBLISH_METHOD)){
					methodName = portName;
					message = value;
				}else if(portName.equalsIgnoreCase(NotificationProcessorConstants.PUBLISH_TOPIC)){
					topic = value;
				}
	     	}

			publishProcessorClass = Class.forName(NotificationProcessorConstants.PUBLISH_PROCESSOR);
			String parameterClassName = NotificationProcessorConstants.W3C_DOM_DOCUMENT;
			publishProcessor = (PublishProcessor) publishProcessorClass.newInstance();

			configuration = (NotificationProcessorConfiguration)
											proxy.newInstance(NotificationProcessorConstants.NOTIFICATION_PROCESSOR_CONFIG);
			configuration.loadConfigurationDetails(NotificationProcessorConstants.CONFIG_FILE);
			String defaultPublisherName = configuration.getConfigValue(NotificationProcessorConstants.PUBLISHER_NAME);


			Method[] methods = publishProcessorClass.getDeclaredMethods();
			for(int i=0;i<methods.length;i++){
				Method method = (Method) methods[i];
				Class[] parameterTypes = method.getParameterTypes();
				if(parameterTypes.length != 3)
					continue;
				if(method.getName().equalsIgnoreCase(methodName) &&
					((Class)parameterTypes[0]).getName().equals(String.class.getName()) &&
					((Class)parameterTypes[1]).getName().equals(String.class.getName()) &&
					((Class)parameterTypes[2]).getName().equals(parameterClassName)){

						org.w3c.dom.Document notificationMessage = convertInputMessage(message);
						String topicName = convertInputTopic(topic);
						method.invoke(publishProcessor, new Object[] { defaultPublisherName,
						topicName,
						notificationMessage });
				}
			}
        } catch(Exception ex){
            ex.printStackTrace();
            throw new TaskExecutionException(ex.toString());
        }

        return new HashMap();
    }

    private org.w3c.dom.Document convertInputMessage(DataThing value) throws Exception {
        Object object = value.getDataObject();
        if(object instanceof String == false) {
          throw new IllegalArgumentException("NotificationProcessorTask cannot accept illegal DataThing argument of class " +
                                              object.getClass().getName());
        }
        String strValue = (String) object;
        // org.xml.sax.InputSource inputSource = new org.xml.sax.InputSource(new StringReader(strValue));
        javax.xml.parsers.DocumentBuilder docBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // org.w3c.dom.Document doc = docBuilder.parse(inputSource);
        org.w3c.dom.Document doc = docBuilder.newDocument();
        org.w3c.dom.Element element = doc.createElement("message");
        element.appendChild(doc.createTextNode(strValue));
        doc.appendChild(element);
        return doc;
    }

    private String convertInputTopic(DataThing value){
	if(value == null)
		return configuration.getConfigValue(NotificationProcessorConstants.PUBLISHER_TOPIC_NAME);
    	Object object = value.getDataObject();
	if(object == null || object == "")
		return configuration.getConfigValue(NotificationProcessorConstants.PUBLISHER_TOPIC_NAME);
	if(object instanceof String == false) {
		throw new IllegalArgumentException("NotificationProcessorTask cannot accept illegal DataThing argument of class " +
											object.getClass().getName());
	}

	return (String) object;

    }

    /*
     * Main method added to test the working of the processor task
     * along with taverna classes.
     *
     */

    public static void main(String args[]) throws Exception {
        if(args.length != 1){
            System.out.println(" Usage : java <program name> <xmlFileName> ");
            System.exit(0);
        }

        String workflowEventFile = args[0];
        java.io.File messageFile = new java.io.File(workflowEventFile);
        javax.xml.parsers.DocumentBuilder docBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = docBuilder.parse(messageFile);
        Map inputMap = new HashMap();
        inputMap.put("Portname:publishMessage", new DataThing(doc));
        new NotificationProcessorTask().execute(inputMap, null);
    }
}
