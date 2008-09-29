package net.sourceforge.taverna.publish;

import java.awt.BorderLayout;

import javax.swing.JDialog;

import org.jdesktop.swing.form.JForm;

/**
 * This class creates a dialog that allows the user to publish a workflow to a
 * shared file server, or to a webdav server.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class PublishDialog extends JDialog {

	public PublishDialog() {
		this.setLayout(new BorderLayout());
		JForm form = new JForm();
		/*
		 * form.add(""); try { form.bind(customer, "firstName"); // binds to
		 * "firstName" property form.bind(customer, "lastName"); // binds to
		 * "lastName" property form.bind(customer, "address"); // binds to
		 * "address" property (nested bean) form.bind(cart, "items"); // binds
		 * to "items" property } catch (BindException e) { }
		 */

	}

}
