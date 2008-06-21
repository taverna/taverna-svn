package net.sf.taverna.t2referencetest;

import net.sf.taverna.t2.reference.ExternalReferenceTranslatorSPI;
import net.sf.taverna.t2.reference.ReferenceContext;

public class GreenToRed implements
		ExternalReferenceTranslatorSPI<GreenReference, RedReference> {

	public RedReference createReference(GreenReference ref,
			ReferenceContext context) {
		RedReference newReference = new RedReference();
		newReference.setContents(ref.getContents());
		// Insert a two second pause to simulate reference translation and to
		// test the behaviour of multiple concurrent translations
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			System.out
					.println("Translation thread was interrupted, probably something wrong.");
		}
		return newReference;
	}

	public Class<GreenReference> getSourceReferenceType() {
		return GreenReference.class;
	}

	public Class<RedReference> getTargetReferenceType() {
		return RedReference.class;
	}

	public float getTranslationCost() {
		return 0.4f;
	}

	public boolean isEnabled(ReferenceContext arg0) {
		return true;
	}

}
