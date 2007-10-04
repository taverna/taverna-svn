/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.inferno;

import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import uk.ac.rdg.resc.jstyx.client.CStyxFile;
import uk.ac.rdg.resc.jstyx.client.StyxConnection;

/**
 * Collects the various services within a specified SGS container
 * 
 * @author Tom Oinn
 */
public class InfernoScavenger extends Scavenger {

	/**
	 * Create a new inferno scavenger with the specified host and port
	 */
	public InfernoScavenger(String host, int port)
			throws ScavengerCreationException {
		super("Styx @ " + host + ":" + port);
		try {
			StyxConnection session = new StyxConnection(host, port);
			// session.connect();
			CStyxFile directory = new CStyxFile(session, "/");
			if (directory.isDirectory() == false) {
				throw new ScavengerCreationException(
						"Root isn't a directory, bad!");
			}
			CStyxFile[] services = directory.getChildren();
			for (int i = 0; i < services.length; i++) {
				CStyxFile serviceNode = services[i];
				String serviceName = serviceNode.getName();
				InfernoProcessorFactory ipf = new InfernoProcessorFactory(host,
						port, serviceName);
				add(new DefaultMutableTreeNode(ipf));
			}
			session.close();
		} catch (ScavengerCreationException ex) {
			throw ex;
		} catch (Exception ex) {
			ScavengerCreationException sce = new ScavengerCreationException(
					"Unable to create inferno scavenger : " + ex.getMessage());
			sce.initCause(ex);
			throw sce;
		}
	}

}
