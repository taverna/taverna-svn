package net.sf.taverna.t2.annotation;

import java.util.ArrayList;
import java.util.List;

public class AnnotationChainImpl implements AnnotationChain{

	private List<AnnotationAssertion<?>> annotationAssertions = new ArrayList<AnnotationAssertion<?>>();
	
	public List<AnnotationAssertion<?>> getAssertions() {
		return new ArrayList<AnnotationAssertion<?>>(annotationAssertions);
	}
	
	/**
	 * Add an annotation to the chain Added because without the edits stuff how
	 * else can we do it?
	 * 
	 * @param annotationAssertion
	 */
	public void addAnnotationAssertion(AnnotationAssertion annotationAssertion) {
		annotationAssertions.add(annotationAssertion);
	}
	
	public void removeAnnotationAssertion(AnnotationAssertion annotationAssertion) {
		annotationAssertions.remove(annotationAssertion);
	}

}
