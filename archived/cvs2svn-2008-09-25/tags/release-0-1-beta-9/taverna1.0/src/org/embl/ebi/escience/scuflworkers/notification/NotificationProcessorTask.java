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
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.StringReader;
import java.lang.reflect.Method;
import org.embl.ebi.escience.baclava.DataThing;

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
    
    public Map execute(Map inputMap) throws TaskExecutionException {
        try{
            Set set = inputMap.keySet();
            if(inputMap.size() > 1)
                throw new TaskExecutionException(" This task accepts only 1 input" );
            for(Iterator itr = set.iterator(); itr.hasNext();){
                String key = (String) itr.next();
                DataThing value = (DataThing) inputMap.get(key);
                
                String methodName = getMethodName(key);
                publishProcessorClass = Class.forName(NotificationProcessorConstants.PUBLISH_PROCESSOR);
                String parameterClassName = NotificationProcessorConstants.W3C_DOM_DOCUMENT;
                publishProcessor = (PublishProcessor) publishProcessorClass.newInstance();
                
                Method[] methods = publishProcessorClass.getDeclaredMethods();
                for(int i=0;i<methods.length;i++){
                    Method method = (Method) methods[i];
                    Class[] parameterTypes = method.getParameterTypes();
                    if(parameterTypes.length != 1)
                        continue;
                    if(method.getName().equals(methodName) &&
                        ((Class)parameterTypes[0]).getName().equals(parameterClassName)){
                            
                            org.w3c.dom.Document notificationMessage = convertInputMessage(value);
                            method.invoke(publishProcessor, new Object[] { notificationMessage });
                    }
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
    
    private String getMethodName(String key){
        return key.substring(key.indexOf(portDelimiter)+1);
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
        new NotificationProcessorTask().execute(inputMap);
    }
}