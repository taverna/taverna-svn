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
	 * Find {@link PropertyDecoder}s that can decode encodedObject
	 * 
	 * @param encodedObject
	 *            Object to decode
	 * @return A list of {@link PropertyDecoder}s
	 */
	@SuppressWarnings("unchecked")
	public static <Source, Target> List<PropertyDecoder<Source,Target>> getDecoders(
			Class<Source> sourceClass, Class<Target> targetClass) {
		List<PropertyDecoder<Source,Target>> decoders = new ArrayList<PropertyDecoder<Source,Target>>();
		for (PropertyDecoder<?,?> decoder : getInstance().getInstances()) {
			if (decoder.canDecode(sourceClass, targetClass)) {
				decoders.add((PropertyDecoder<Source,Target>)decoder);
			}
		}
		return decoders;
	}
	
	public static <Source,Target> PropertyDecoder<Source,Target> getDecoder(Class<Source> sourceClass, Class<Target> targetClass) {
		PropertyDecoder<Source,Target> result = null;
		List<PropertyDecoder<Source,Target>> decoders = getDecoders(sourceClass, targetClass);
		if (decoders.size() > 0) {
			result = decoders.get(0);
		}
		return result;
	}
}
