package net.sf.taverna.t2.platform.plugin;

/**
 * Identifier for a t2platform plugin
 * 
 * @author Tom Oinn
 */
public final class PluginIdentifier {

	private String groupId, pluginId, version;

	/**
	 * Create a new artifact identifier, two identifiers are considered equal if
	 * all fields on both artifact identifiers match.
	 * 
	 * @param groupId
	 *            the group ID for this plugin identifier
	 * @param pluginId
	 *            the artifact ID for this plugin identifier
	 * @param version
	 *            the version for this plugin identifier
	 * @throws RuntimeException
	 *             if any property is null
	 */
	public PluginIdentifier(String groupId, String pluginId, String version) {
		if (groupId == null) {
			throw new RuntimeException(
					"Cannot create a plug-in identifier with a null groupId");
		}
		if (pluginId == null) {
			throw new RuntimeException(
					"Cannot create a plug-in identifier with a null pluginId");
		}
		if (version == null) {
			throw new RuntimeException(
					"Cannot create a plug-inidentifier with a null version");
		}
		this.groupId = groupId;
		this.pluginId = pluginId;
		this.version = version;
	}

	/**
	 * Create a new artifact identifier from a single colon separated string of
	 * the form
	 * <code><b>plugin</b>:<em>groupId</em>:<em>artifactId</em>:<em>version</em></code>
	 * <p>
	 * The initial 'plugin:' is used to ensure that there is no confusion
	 * between this class and the similar artifact identifier, while both have a
	 * group, id and version they are not the same!
	 * 
	 * @throws RuntimeException
	 *             if the specification string is null, or does not match the
	 *             pattern above
	 */
	public PluginIdentifier(String compactSpecification) {
		if (compactSpecification == null) {
			throw new RuntimeException(
					"Cannot create an artifact identifier from a null compact specification");
		}
		String[] parts = compactSpecification.split(":");
		if (parts.length != 4) {
			throw new RuntimeException(
					"Cannot create an artifact identifier from invalid compact specification '"
							+ compactSpecification + "'");
		}
		if (parts[0].equals("plugin") == false) {
			throw new RuntimeException(
					"Not a plugin identifier, must start with 'plugin:'");
		}
		this.groupId = parts[1];
		this.pluginId = parts[2];
		this.version = parts[3];
	}

	/**
	 * @return the group ID part of this plug-in identifer
	 */
	public final String getGroupId() {
		return groupId;
	}

	/**
	 * @return the plug-in ID part of this plug-in identifier
	 */
	public final String getPluginId() {
		return pluginId;
	}

	/**
	 * @return the version part of this plug-in identifier. Versions are period
	 *         separated numeric strings, with the most significant part first
	 *         and least significant last. Versions are comparable through
	 *         lexicographic ordering from least significant to most, inserting
	 *         the version '0' in places where version lengths mismatch, i.e.
	 *         '8.1' is equal to '8.1.0' and less than '8.1.2'
	 */
	public final String getVersion() {
		return version;
	}

	/**
	 * Two identifiers are equal if both are instances of PluginIdentifier,
	 * neither are null and all of artifactId, pluginId and version fields match
	 * under string comparison including by case.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof PluginIdentifier) {
			PluginIdentifier ai = (PluginIdentifier) other;
			return (ai.getPluginId().equals(pluginId)
					&& ai.getGroupId().equals(groupId) && ai.getVersion()
					.equals(version));
		} else {
			return false;
		}
	}

	/**
	 * Compares two plug-in identifiers ignoring the version property
	 * 
	 * @return true if the other identifier is not null and matches the groupId
	 *         and pluginId properties of this plug-in identifier
	 */
	public boolean equalsIgnoreVersion(PluginIdentifier other) {
		if (other == null) {
			return false;
		}
		return (other.getGroupId().equals(groupId) && other.getPluginId()
				.equals(pluginId));
	}

	/**
	 * Returns the compact form of this artifact identifier as a single string
	 * of the form
	 * <code><b>plugin</b>:<em>groupId</em>:<em>pluginId</em>:<em>version</em></code>
	 */
	@Override
	public String toString() {
		return "plugin:" + groupId + ":" + pluginId + ":" + version;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
