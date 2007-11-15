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
public final class ScavengerDecoder implements PropertyDecoder<Scavenger, ProcessorFactory> {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.activityregistry.PropertyDecoder#canDecode(java.lang.Class, java.lang.Class)
	 */
	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		return (targetClass.isAssignableFrom(ProcessorFactory.class) &&
				Scavenger.class.isAssignableFrom(sourceClass));
	}

	public Set<ProcessorFactory> decode(PropertiedObjectSet<ProcessorFactory> targetSet, Scavenger encodedObject) {
		Set<ProcessorFactory> result = new HashSet<ProcessorFactory>();
		for (DefaultMutableTreeNode leaf = encodedObject.getFirstLeaf();
			 leaf != null;
			 leaf = leaf.getNextLeaf()) {
			Object userObject = leaf.getUserObject();
			if (userObject instanceof ProcessorFactory) {
			PropertyDecoder decoder = PropertyDecoderRegistry.getDecoder(userObject.getClass(), ProcessorFactory.class);
			if (decoder != null) {
				result.addAll (decoder.decode(targetSet, userObject));
			}
			}
		}
		return result;
	}

}
