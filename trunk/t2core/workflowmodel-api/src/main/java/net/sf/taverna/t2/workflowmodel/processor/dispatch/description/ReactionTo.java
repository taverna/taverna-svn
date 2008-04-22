package net.sf.taverna.t2.workflowmodel.processor.dispatch.description;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the type of message to which the various DispatchLayerFooReaction
 * classes are referring
 * 
 * @author Tom Oinn
 * 
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReactionTo {

	public DispatchMessageType messageType();

}
