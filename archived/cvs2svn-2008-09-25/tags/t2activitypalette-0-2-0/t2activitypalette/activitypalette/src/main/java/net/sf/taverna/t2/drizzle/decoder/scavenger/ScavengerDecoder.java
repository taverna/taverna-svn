/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder.scavenger;

import java.util.HashSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.drizzle.decoder.PropertyDecoder;
import net.sf.taverna.t2.drizzle.decoder.PropertyDecoderRegistry;
import net.sf.taverna.t2.drizzle.decoder.processorfactory.DefaultProcessorFactoryDecoder;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.query.DecodeRunIdentification;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 * 
 */
public final class ScavengerDecoder implements
		PropertyDecoder<Scavenger, ProcessorFactoryAdapter> {

	/**
	 * @see net.sf.taverna.t2.drizzle.decoder.PropertyDecoder#canDecode(java.lang.Class, java.lang.Class)
	 */
	public boolean canDecode(Class<?> sourceClass, Class<?> targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		return (targetClass.isAssignableFrom(ProcessorFactoryAdapter.class) && Scavenger.class
				.isAssignableFrom(sourceClass));
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.decoder.PropertyDecoder#decode(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet, java.lang.Object)
	 */
	public DecodeRunIdentification<ProcessorFactoryAdapter> decode(
			PropertiedObjectSet<ProcessorFactoryAdapter> targetSet,
			Scavenger encodedObject) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (encodedObject == null) {
			throw new NullPointerException("encodedObject cannot be null"); //$NON-NLS-1$
		}
		DecodeRunIdentification<ProcessorFactoryAdapter> ident = new DecodeRunIdentification<ProcessorFactoryAdapter>();
		ident.setAffectedObjects(new HashSet<ProcessorFactoryAdapter> ());
		ident.setPropertyKeyProfile(new HashSet<PropertyKey>());
		ident.setTimeOfRun(System.currentTimeMillis());
		decodeNode(targetSet, ident, encodedObject);

		return ident;
	}

	@SuppressWarnings("unchecked")
	private void decodeNode(PropertiedObjectSet<ProcessorFactoryAdapter> targetSet,
			DecodeRunIdentification<ProcessorFactoryAdapter> ident,
			TreeNode node) {
		if (targetSet == null) {
			throw new NullPointerException("targetSet cannot be null"); //$NON-NLS-1$
		}
		if (ident == null) {
			throw new NullPointerException("ident cannot be null"); //$NON-NLS-1$
		}
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		if (node instanceof DefaultMutableTreeNode) {
		Object userObject = ((DefaultMutableTreeNode)node).getUserObject();
		if ((userObject != null) && (userObject instanceof ProcessorFactory)) {
			PropertyDecoder decoder = PropertyDecoderRegistry.getDecoder(
					userObject.getClass(), ProcessorFactoryAdapter.class);
			if (decoder == null) {
				decoder = DefaultProcessorFactoryDecoder.getInstance(userObject.getClass());
			}
			if (decoder != null) {
				DecodeRunIdentification<ProcessorFactoryAdapter> subIdent =
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
