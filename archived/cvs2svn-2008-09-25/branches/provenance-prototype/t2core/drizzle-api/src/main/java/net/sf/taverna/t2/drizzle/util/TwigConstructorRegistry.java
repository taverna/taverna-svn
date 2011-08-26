/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("unchecked")
public class TwigConstructorRegistry extends SPIRegistry<TwigConstructor> {
	private static TwigConstructorRegistry instance = null;

	public static TwigConstructorRegistry getInstance() {
		if (instance == null) {
			instance = new TwigConstructorRegistry();
		}
		return instance;
	}

	/**
	 * Private constructor, use {@link #getInstance()}
	 * 
	 * @see #getInstance()
	 */
	private TwigConstructorRegistry() {
		super(TwigConstructor.class);
	}

	/**
	 * Find {@link PropertyDecoder}s that can decode encodedObject
	 * 
	 * @param encodedObject
	 *            Object to decode
	 * @return A list of {@link PropertyDecoder}s
	 */
	@SuppressWarnings("unchecked")
	public static <Source> List<TwigConstructor<Source>> getConstructors(
			Class<Source> sourceClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		List<TwigConstructor<Source>> constructors = new ArrayList<TwigConstructor<Source>>();
		for (TwigConstructor<?> constructor : getInstance().getInstances()) {
			if (constructor.canHandle(sourceClass)) {
				constructors.add((TwigConstructor<Source>)constructor);
			}
		}
		return constructors;
	}
	
	public static <Source> TwigConstructor<Source> getTwigConstructor(Class<Source> sourceClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		TwigConstructor<Source> result = null;
		List<TwigConstructor<Source>> constructors = getConstructors(sourceClass);
		if (constructors.size() > 0) {
			result = constructors.get(0);
		}
		return result;
	}
}
