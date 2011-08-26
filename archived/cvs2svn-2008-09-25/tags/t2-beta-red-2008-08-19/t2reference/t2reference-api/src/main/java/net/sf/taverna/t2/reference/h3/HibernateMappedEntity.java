package net.sf.taverna.t2.reference.h3;

/**
 * A marker interface used to denote that the component should be registered
 * with the Hibernate ORM system prior to any ExternalReferenceSPI
 * implementations. This is here to allow implementations of e.g. ReferenceSet
 * to be in the implementation package where they belong and still guarantee
 * that they are registered before any other plugins.
 * <p>
 * This should be used as an SPI marker, and set as the first SPI registry in
 * the spiRegistries property of the SpiRegistryAwareLocalSessionFactoryBean
 * 
 * @author Tom Oinn
 * 
 */
public interface HibernateMappedEntity {

}
