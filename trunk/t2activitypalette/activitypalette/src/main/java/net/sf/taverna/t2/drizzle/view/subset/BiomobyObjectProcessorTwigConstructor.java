/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.TwigConstructor;
import net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeObjectNodeImpl;

/**
 * @author alanrw
 *
 */
public final class BiomobyObjectProcessorTwigConstructor implements TwigConstructor<BiomobyObjectProcessor>{

	public boolean canHandle(Class<?> arg0) {
		return BiomobyObjectProcessor.class.isAssignableFrom(arg0.getClass());
	}

	public PropertiedTreeNode<BiomobyObjectProcessor> createTwig(BiomobyObjectProcessor arg0) {
		PropertiedTreeObjectNode<BiomobyObjectProcessor> result = new PropertiedTreeObjectNodeImpl<BiomobyObjectProcessor>();
		result.setObject(arg0);
		return result;
	}

}
