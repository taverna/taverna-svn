/**
 * 
 */
package uk.org.mygrid.sogsa.sbs.semanticAnnotation;

import java.net.URI;

/**
 * @author paolo
 * SSSD = Simple Semantic Service Descriptor
 */
public class SSSD {

	URI serviceClass = null;
	URI serviceInputMessageClass = null;
	URI serviceOutputMessageClass = null;
	URI serviceOpClass = null;
	/**
	 * @return the serviceClass
	 */
	public URI getServiceClass() {
		return serviceClass;
	}
	/**
	 * @param serviceClass the serviceClass to set
	 */
	public void setServiceClass(URI serviceClass) {
		this.serviceClass = serviceClass;
	}
	/**
	 * @return the serviceInputMessageClass
	 */
	public URI getServiceInputMessageClass() {
		return serviceInputMessageClass;
	}
	/**
	 * @param serviceInputMessageClass the serviceInputMessageClass to set
	 */
	public void setServiceInputMessageClass(URI serviceInputMessageClass) {
		this.serviceInputMessageClass = serviceInputMessageClass;
	}
	/**
	 * @return the serviceOutputMessageClass
	 */
	public URI getServiceOutputMessageClass() {
		return serviceOutputMessageClass;
	}
	/**
	 * @param serviceOutputMessageClass the serviceOutputMessageClass to set
	 */
	public void setServiceOutputMessageClass(URI serviceOutputMessageClass) {
		this.serviceOutputMessageClass = serviceOutputMessageClass;
	}
	/**
	 * @return the serviceOpClass
	 */
	public URI getServiceOpClass() {
		return serviceOpClass;
	}
	/**
	 * @param serviceOpClass the serviceOpClass to set
	 */
	public void setServiceOpClass(URI serviceOpClass) {
		this.serviceOpClass = serviceOpClass;
	}

}
