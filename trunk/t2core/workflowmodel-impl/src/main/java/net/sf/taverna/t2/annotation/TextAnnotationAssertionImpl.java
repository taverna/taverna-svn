package net.sf.taverna.t2.annotation;

import java.util.Date;
import java.util.List;

import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;

public class TextAnnotationAssertionImpl implements AnnotationAssertion<FreeTextDescription> {
	
	private FreeTextDescription freeTextDescription;
	private AnnotationRole annotationRole;
	private Date date;
	private final List<Person> creators;
	private final AnnotationSourceSPI annotationSource;
	
	public TextAnnotationAssertionImpl(FreeTextDescription freeTextDescription, AnnotationRole annotationRole, List<Person> creators, AnnotationSourceSPI annotationSource) {
		this.freeTextDescription = freeTextDescription;
		this.annotationRole = annotationRole;
		this.creators = creators;
		this.annotationSource = annotationSource;
		date = new Date();
	}

	public FreeTextDescription getDetail() {
		// TODO Auto-generated method stub
		return freeTextDescription;
	}

	public AnnotationRole getRole() {
		// TODO Auto-generated method stub
		return annotationRole;
	}

	public Date getCreationDate() {
		// TODO Auto-generated method stub
		return date;
	}

	public List<? extends Person> getCreators() {
		// TODO Auto-generated method stub
		return creators;
	}

	public List<CurationEvent<?>> getCurationAssertions() {
		// TODO Auto-generated method stub
		return null;
	}

	public AnnotationSourceSPI getSource() {
		// TODO Auto-generated method stub
		return annotationSource;
	}

}
