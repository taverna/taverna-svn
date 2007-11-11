/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * @author alanrw
 *
 */
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
	 * Find {@link PropertyDecoder}s that can decode encodedObject
	 * 
	 * @param encodedObject
	 *            Object to decode
	 * @return A list of {@link PropertyDecoder}s
	 */
	public List<PropertyDecoder> getDecoders(
			Class sourceClass, Class targetClass) {
		List<PropertyDecoder> decoders = new ArrayList<PropertyDecoder>();
		for (PropertyDecoder decoder : getInstances()) {
			if (decoder.canDecode(sourceClass, targetClass)) {
				decoders.add(decoder);
			}
		}
		return decoders;
	}
}
