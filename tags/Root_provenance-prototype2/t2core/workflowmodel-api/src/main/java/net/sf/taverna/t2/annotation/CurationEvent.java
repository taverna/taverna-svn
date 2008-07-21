package net.sf.taverna.t2.annotation;

/**
 * Represents a single act of curation, parameterized on a bean encapsulating
 * the necessary and sufficient information to describe the specifics of the
 * curation event.
 * 
 * @author Tom Oinn
 * 
 * @param <T>
 */
public interface CurationEvent<CurationType extends CurationEventBeanSPI> {

	public CurationType getDetail();

	/**
	 * The curation event type specifies whether this curation event is a
	 * validation, repudiation or neither of its target.
	 * 
	 * @return
	 */
	public CurationEventType getType();

	/**
	 * The curation event applies to a specific other event, either another
	 * curation event or an annotation assertion.
	 * 
	 * @return the event which this event is curating
	 */
	public Curateable getTarget();

}
