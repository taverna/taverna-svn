package net.sf.taverna.platform.spring;

/**
 * String constants used by the raven spring support package.
 * 
 * @author Tom Oinn
 * 
 */
public class RavenConstants {

	/**
	 * The name of the property defined on raven-enabled BeanDefinition
	 * instances to point to the name of the repository bean within the bean
	 * factory
	 */
	public static String REPOSITORY_BEAN_ATTRIBUTE_NAME = "ravenRepositoryBean";

	/**
	 * The name of the property defined on raven-enabled BeanDefinition
	 * instances specifying the artifact that should be used to load this bean
	 * definition's associated bean class. The artifact is specified as a
	 * group:artifact:version string.
	 */
	public static String ARTIFACT_BEAN_ATTRIBUTE_NAME = "ravenArtifact";

	/**
	 * The XML attribute name used to decorate the raven-enabled bean definition
	 * in the application context configuration to point to the repository bean
	 * id within that definition
	 */
	public static String REPOSITORY_XML_ATTRIBUTE_NAME = "repository";

	/**
	 * The XML attribute name used to specify the artifact to be used by a
	 * particular raven-enabled bean within the configuration xml.
	 */
	public static String ARTIFACT_XML_ATTRIBUTE_NAME = "artifact";

}