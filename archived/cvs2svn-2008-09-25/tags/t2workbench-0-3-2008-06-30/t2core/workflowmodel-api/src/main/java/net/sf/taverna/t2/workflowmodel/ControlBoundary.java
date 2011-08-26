package net.sf.taverna.t2.workflowmodel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the associated type creates a boundary of control within the
 * dataflow. Types marked with this annotation are those which can modify the
 * owning process of data tokens they consume and generally correspond to cases
 * where the control flow bifurcates in some fashion.
 * <p>
 * This annotation doesn't currently define this behaviour but serves as an easy
 * way for us to track which objects might potentially do this, something we
 * need to be able to do to guarantee that the monitor works correctly.
 * 
 * @author Tom Oinn
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ControlBoundary {

}
