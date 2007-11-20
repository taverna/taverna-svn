/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

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

	public DecodeRunIdentification<ProcessorFactory> decode(
			PropertiedObjectSet<ProcessorFactory> targetSet,
			Scavenger encodedObject) {
		DecodeRunIdentification<ProcessorFactory> ident = new DecodeRunIdentification<ProcessorFactory>();
		ident.setAffectedObjects(new HashSet<ProcessorFactory> ());
		ident.setPropertyKeyProfile(new HashSet<PropertyKey>());
		ident.setTimeOfRun(System.currentTimeMillis());
		decodeNode(targetSet, ident, encodedObject);

		return ident;
	}

	private void decodeNode(PropertiedObjectSet<ProcessorFactory> targetSet,
			DecodeRunIdentification<ProcessorFactory> ident,
			TreeNode node) {
		if (node instanceof DefaultMutableTreeNode) {
		Object userObject = ((DefaultMutableTreeNode)node).getUserObject();
		if ((userObject != null) && (userObject instanceof ProcessorFactory)) {
			PropertyDecoder decoder = PropertyDecoderRegistry.getDecoder(
					userObject.getClass(), ProcessorFactory.class);
			if (decoder != null) {
				DecodeRunIdentification<ProcessorFactory> subIdent =
					decoder.decode(targetSet, userObject);
				ident.getAffectedObjects().addAll(subIdent.getAffectedObjects());
				ident.getPropertyKeyProfile().addAll(subIdent.getPropertyKeyProfile());
			} else {
				throw new NullPointerException ("No decoder found for " + userObject.getClass().getName()); //$NON-NLS-1$
			}			
		}
		}
		int childCount = node.getChildCount();
		for (int i = 0; i < childCount; i++) {
			decodeNode(targetSet, ident, node.getChildAt(i));
		}
}

	}
