package net.sf.taverna.t2.workflowmodel.processor.dispatch.description;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes how a dispatch layer reacts to an error message
 * 
 * @author Tom Oinn
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@ReactionTo(messageType = DispatchMessageType.ERROR)
public @interface DispatchLayerErrorReaction {

	public DispatchLayerStateEffect[] stateEffects();

	public DispatchMessageType[] emits();

	public boolean relaysUnmodified();
	
}
