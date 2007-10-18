package net.sf.taverna.t2.activities.wsdl.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import org.embl.ebi.escience.scufl.Processor;

/**
 *
 * @author Stuart Owen
 */
public class WSDLActivityTranslator extends AbstractActivityTranslator<WSDLActivityConfigurationBean>{
    
    protected WSDLActivityConfigurationBean createConfigType(Processor processor) throws ActivityTranslationException {
        WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
        bean.setWsdl(determineWSDL(processor));
        bean.setOperation(determineOperation(processor));
        return bean;
    }

    public boolean canHandle(Processor processor) {
        return processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor");
    }

    protected Activity<WSDLActivityConfigurationBean> createUnconfiguredActivity() {
        return new WSDLActivity();
    }
    
    private String determineWSDL(Processor processor) throws ActivityTranslationException{
        return (String)invokeMethodWithIntrospection(processor,"getWSDLLocation");
    }
    
    
    private String determineOperation(Processor processor) throws ActivityTranslationException {
        return (String)invokeMethodWithIntrospection(processor,"getOperationName");
    }
    
    private Object invokeMethodWithIntrospection(Processor processor, String operationName) throws ActivityTranslationException {
            try {
                    Method m=processor.getClass().getMethod(operationName);
                    return m.invoke(processor);
            } catch (SecurityException e) {
                    throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getStringValue through introspection",e);
            } catch (NoSuchMethodException e) {
                    throw new ActivityTranslationException("The processor does not have the method getStringValue, an therefore does not conform to being a StringConstant processor",e);
            } catch (IllegalArgumentException e) {
                    throw new ActivityTranslationException("The method getStringValue on the StringConstant processor had unexpected arguments",e);
            } catch (IllegalAccessException e) {
                    throw new ActivityTranslationException("Unable to access the method getStringValue on the StringConstant processor",e);
            } catch (InvocationTargetException e) {
                    throw new ActivityTranslationException("An error occurred invoking the method getStringValue on the StringConstant processor",e);
            }
    }
    
}
