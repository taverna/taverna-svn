package org.taverna.server.master.interfaces;

import org.taverna.server.master.exceptions.FilesystemAccessException;

/**
 * An entry in a {@link Directory} representing a file or sub-directory.
 * 
 * @author Donal Fellows
 * @see Directory
 * @see File
 */
public interface DirectoryEntry {
	/**
	 * @return The "local" name of the entry. This will never be "<tt>..</tt>"
	 *         or contain the character "<tt>/</tt>".
	 */
	public String getName();

	/**
	 * @return The "full" name of the entry. This is computed relative to the
	 *         workflow run's working directory. It may contain the "<tt>/</tt>"
	 *         character.
	 */
	public String getFullName();

	/**
	 * Destroy this directory entry, deleting the file or sub-directory. The
	 * workflow run's working directory can never be manually destroyed.
	 * 
	 * @throws FilesystemAccessException
	 *             If the destroy fails for some reason.
	 */
	public void destroy() throws FilesystemAccessException;
	// TODO: Permissions
}
