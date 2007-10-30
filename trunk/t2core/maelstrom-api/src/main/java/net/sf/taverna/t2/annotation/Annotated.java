package net.sf.taverna.t2.annotation;

import java.util.Set;

import net.sf.taverna.t2.workflowmodel.Edit;

/**
 * Denotes that the object carries workflow object level annotation. Rather than
 * defining specific annotation types for each workflow entity we work on the
 * basis that multiple annotations of different types may apply, so free text
 * description is one example, semantic annotation of the internal function of a
 * processor might be another.
 * <p>
 * Where annotations are conceptually editable such as free text descriptions
 * the editing framework should internally remove the original annotation and
 * add the replacement rather than modifying the previous annotation in place.
 * 
 * @author Tom Oinn
 * 
 */
public interface Annotated<TargetType> {

	/**
	 * Each annotated object contains a bag of metadata object instances
	 * 
	 * @return set of metadata objects that apply to the annotated object
	 */
	Set<? extends WorkflowAnnotation> getAnnotations();

	/**
	 * Add new workflow object metadata to this annotated entity
	 * 
	 * @param <TargetType>
	 *            the type of the object being annotated
	 * @param newAnnotation
	 *            metadata object to add to the annotated object
	 * @return edit object to perform and undo the metadata assignment
	 */
	public Edit<? extends TargetType> getAddAnnotationEdit(
			WorkflowAnnotation newAnnotation);

	/**
	 * Remove an annotation object from the this annotated entity
	 * 
	 * @param <TargetType>
	 *            type of the workflow object from which the annotation is
	 *            removed
	 * @param annotationToRemove
	 *            metadata object to remove
	 * @param objectToAnnotate
	 *            object from which the metadata is removed
	 * @return edit object to perform and undo the metadata removal
	 */
	public Edit<? extends TargetType> getRemoveAnnotationEdit(
			WorkflowAnnotation annotationToRemove);

	/**
	 * Replace an annotation on this object with a new annotation object. In all
	 * probability this is implemented as a compound edit using the remove and
	 * add annotation edits but from a user's point of view we want to have this
	 * as a single editing operation within the UI.
	 * 
	 * @param <TargetType>
	 *            type of the workflow object under modification
	 * @param oldAnnotation
	 *            annotation to replace
	 * @param newAnnotation
	 *            new version of the annotation
	 * @param objectToAnnotate
	 *            workflow object being annotated
	 * @return edit object to perform and undo the metadata modification
	 */
	public Edit<? extends TargetType> getReplaceAnnotationEdit(
			WorkflowAnnotation oldAnnotation, WorkflowAnnotation newAnnotation);

}
