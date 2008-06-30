package net.sf.taverna.t2.annotation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;

public class AnnotationAssertionImpl implements AnnotationAssertion<AnnotationBeanSPI> {
	
	private AnnotationBeanSPI annotationBean;
	private AnnotationRole annotationRole;
	private Date date;
	private List<Person> creators;
	private AnnotationSourceSPI annotationSource;
	private List<CurationEvent<?>> curationEventList;

	public AnnotationAssertionImpl(){
		date = new Date();
		curationEventList = new ArrayList<CurationEvent<?>>();
		creators = new ArrayList<Person>();
		
	}
	
	public AnnotationAssertionImpl(AnnotationBeanSPI freeTextDescription, AnnotationRole annotationRole, List<Person> creators, AnnotationSourceSPI annotationSource) {
		this.annotationBean = freeTextDescription;
		this.annotationRole = annotationRole;
		this.creators = creators;
		this.annotationSource = annotationSource;
	}

	public AnnotationBeanSPI getDetail() {
		return annotationBean;
	}

	public AnnotationRole getRole() {
		return annotationRole;
	}

	public Date getCreationDate() {
		return date;
	}

	public List<? extends Person> getCreators() {
		return creators;
	}
	
	public void addCreator(Person person) {
		creators.add(person);
	}
	
	public void removeCreator(Person person) {
		creators.remove(person);
	}

	public List<CurationEvent<?>> getCurationAssertions() {
		return curationEventList;
	}

	public AnnotationSourceSPI getSource() {
		return annotationSource;
	}

	public void setAnnotationBean(AnnotationBeanSPI annotationBean) {
		this.annotationBean = annotationBean;
	}

	public void setAnnotationRole(AnnotationRole annotationRole) {
		this.annotationRole = annotationRole;
	}
	
	public void removeAnnotationRole() {
		this.annotationRole = null;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setCreators(List<Person> creators) {
		this.creators = creators;
	}

	public void setAnnotationSource(AnnotationSourceSPI annotationSource) {
		this.annotationSource = annotationSource;
	}
	
	public void removeAnnotationSource() {
		this.annotationSource = null;
	}

	public void removeAnnotationBean() {
		annotationBean = null;
	}
	
	public void addCurationEvent(CurationEvent curationEvent) {
		curationEventList.add(curationEvent);
	}

	public void removeCurationEvent(CurationEvent curationEvent) {
		curationEventList.remove(curationEvent);
	}

}
