/**
 * 
 */
package net.sf.taverna.t2.drizzle.decoder;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("unchecked")
public class PropertyDecoderRegistry extends SPIRegistry<PropertyDecoder> {
	private static PropertyDecoderRegistry instance = null;

	/**
	 * Get (create if necessary) the {@link PropertyDecoderRegistry} singleton.
	 * 
	 * @return The {@link PropertyDecoderRegistry} instance.
	 */
	public static PropertyDecoderRegistry getInstance() {
		if (instance == null) {
			instance = new PropertyDecoderRegistry();
		}
		return instance;
	}

	/**
	 * Private constructor, use {@link #getInstance()}
	 * 
	 * @see #getInstance()
	 */
	private PropertyDecoderRegistry() {
		super(PropertyDecoder.class);
	}

	/**
	 * @param sourceClass
	 * @param targetClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<PropertyDecoder> getDecoders(
			Class sourceClass, Class targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		List<PropertyDecoder> decoders = new ArrayList<PropertyDecoder>();
		for (PropertyDecoder<?,?> decoder : getInstance().getInstances()) {
			if (decoder.canDecode(sourceClass, targetClass)) {
				decoders.add(decoder);
			}
		}
		return decoders;
	}
	
	/**
	 * @param sourceClass
	 * @param targetClass
	 * @return
	 */
	public static PropertyDecoder getDecoder(Class sourceClass, Class targetClass) {
		if (sourceClass == null) {
			throw new NullPointerException("sourceClass cannot be null"); //$NON-NLS-1$
		}
		if (targetClass == null) {
			throw new NullPointerException("targetClass cannot be null"); //$NON-NLS-1$
		}
		PropertyDecoder result = null;
		List<PropertyDecoder> decoders = getDecoders(sourceClass, targetClass);
		if (decoders.size() > 0) {
			result = decoders.get(0);
		}
		return result;
	}
}
