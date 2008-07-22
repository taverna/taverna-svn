package net.sf.taverna.t2.workflowmodel.processor.dispatch.description;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a dispatch layer can handle streamed result data correctly, if
 * this annotation is attached to a DispatchLayer implementation that
 * implementation must be able to correctly handle the result completion message
 * type. By default dispatch layers are assumed to not handle this message type.
 * 
 * @author Tom Oinn
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SupportsStreamedResult {

}
