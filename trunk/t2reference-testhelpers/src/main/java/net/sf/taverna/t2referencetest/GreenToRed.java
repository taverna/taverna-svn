package net.sf.taverna.t2referencetest;

import net.sf.taverna.t2.reference.ExternalReferenceConstructionCallback;
import net.sf.taverna.t2.reference.ExternalReferenceConstructionException;
import net.sf.taverna.t2.reference.ExternalReferenceTranslatorSPI;
import net.sf.taverna.t2.reference.ReferenceContext;

public class GreenToRed implements
		ExternalReferenceTranslatorSPI<GreenReference, RedReference> {

	public RedReference createReference(GreenReference ref,
			ReferenceContext context) {
		RedReference newReference = new RedReference();
		newReference.setContents(ref.getContents());
		return newReference;
	}

	public void createReferenceAsynch(final GreenReference ref,
			final ReferenceContext context,
			final ExternalReferenceConstructionCallback callback) {
		Runnable r = new Runnable() {
			public void run() {
				try {
					callback.ExternalReferenceCreated(createReference(ref,
							context));
				} catch (ExternalReferenceConstructionException ex) {
					callback.ExternalReferenceConstructionFailed(ex);
				}
			}
		};
		new Thread(r).start();
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
