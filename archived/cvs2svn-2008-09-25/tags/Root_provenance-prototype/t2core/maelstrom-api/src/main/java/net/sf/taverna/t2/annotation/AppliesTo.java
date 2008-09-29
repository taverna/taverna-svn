package net.sf.taverna.t2.annotation;

import java.lang.annotation.*;

/**
 * Annotation to be used on metadata objects to denote which workflow objects
 * they apply to
 * 
 * @author Tom Oinn
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface AppliesTo {

	/**
	 * The class of the metadata object allowed by this annotation
	 */
	Class<?>[] targetObjectType();

	/**
	 * Can you have more than one of these metadata objects in the resolved set?
	 */
	boolean many() default true;

}
