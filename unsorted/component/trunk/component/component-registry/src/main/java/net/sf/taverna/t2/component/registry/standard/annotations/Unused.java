package net.sf.taverna.t2.component.registry.standard.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a constructor, field, method or parameter as unused. Unused members
 * exist for the purpose of documentation or completeness.
 * 
 * @author Donal Fellows
 */
@Documented
@Target({ CONSTRUCTOR, FIELD, METHOD, PARAMETER, TYPE })
@Retention(CLASS)
public @interface Unused {

}
