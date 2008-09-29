package net.sf.taverna.t2.reference.h3;

/**
 * A marker used to denote that the class should be pre-loaded into hibernate's
 * class mapping. Used for component classes which are not going to be mapped to
 * the RDBMS but which must be loadable for mapped classes to instantiate
 * correctly. Basically if you refer to a class that isn't itself going to be
 * mapped in hibernate within a mapping definition you'll need to add that
 * component class to this SPI or hibernate won't be able to find it as it won't
 * know that it should associate it with the appropriate class loader.
 * <p>
 * This should be used as an SPI marker, and set as the first SPI registry in
 * the preloadRegistries property of the SpiRegistryAwareLocalSessionFactoryBean
 * 
 * @author Tom Oinn
 * 
 */
public interface HibernateComponentClass {

}
