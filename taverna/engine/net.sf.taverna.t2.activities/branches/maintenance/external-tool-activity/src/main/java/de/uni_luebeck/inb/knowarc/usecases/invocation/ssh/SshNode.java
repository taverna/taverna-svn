/* Part of the KnowARC Janitor Use-case processor for taverna
 *  written 2007-2010 by Hajo Nils Krabbenhoeft and Steffen Moeller
 *  University of Luebeck, Institute for Neuro- and Bioinformatics
 *  University of Luebeck, Institute for Dermatolgy
 *
 *  This package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This package is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this package; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

public class SshNode {
	private String host = "127.0.0.1";
	private int port = 22;
	private String directory = "/tmp/";
	
	private SshUrl url;

	/**
	 * @param directory the directory to set
	 */
	public void setDirectory(String directory) {
		if (!directory.endsWith("/")) {
			directory = directory + "/";
		}
		this.directory = directory;
	}

	/**
	 * @return the directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	private SshUrl getUrl() {
		if (url == null) {
			url = new SshUrl(this);
		}
		return url;
	}
	
	public int hashCode() {
		return getUrl().hashCode();	
	}

	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof SshNode)) {
			return false;
		}
		return (this.hashCode() == obj.hashCode());
	}
	
}
