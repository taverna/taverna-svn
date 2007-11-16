/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 * 
 */
public final class ScavengerDecoder implements
		PropertyDecoder<Scavenger, ProcessorFactory> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder#canDecode(java.lang.Class,
	 *      java.lang.Class)
	 */
	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(ProcessorFactory.class) && Scavenger.class
				.isAssignableFrom(sourceClass));
	}

	public Set<ProcessorFactory> decode(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			Scavenger encodedObject) {
		Set<ProcessorFactory> result = new HashSet<ProcessorFactory>();
		result.addAll(decodeObject(targetSet, encodedObject.getUserObject()));
		for (DefaultMutableTreeNode leaf = encodedObject.getFirstLeaf(); leaf != null; leaf = leaf
				.getNextLeaf()) {
			result.addAll(decodeObject(targetSet, leaf.getUserObject()));
		}
		return result;
	}

	private Set<ProcessorFactory> decodeObject(PropertiedObjectSet<ProcessorFactory> targetSet,
			Object userObject) {
		Set<ProcessorFactory> result = new HashSet<ProcessorFactory>();
		if ((userObject != null) && (userObject instanceof ProcessorFactory)) {
			PropertyDecoder decoder = PropertyDecoderRegistry.getDecoder(
					userObject.getClass(), ProcessorFactory.class);
			if (decoder != null) {
				result.addAll(decoder.decode(targetSet, userObject));
			} else {
				throw new NullPointerException ("No decoder found for " + userObject.getClass().getName());
			}			
		}
		return result;
	}
}
