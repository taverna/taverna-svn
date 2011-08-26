package net.sourceforge.taverna.publish;

import java.util.prefs.Preferences;

import javax.swing.JTextField;

import org.jdesktop.swing.data.MetaData;
import org.jdesktop.swing.form.DefaultFormFactory;
import org.jdesktop.swing.form.JForm;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class RepositoryForm extends JForm {

	/**
	 * This constructor is used to create a new Repository.
	 * 
	 */
	public RepositoryForm() {
		init();
	}

	public RepositoryForm(Preferences prefsNode) {
		init();
	}

	public void init() {
		DefaultFormFactory formFactory = new DefaultFormFactory();
		JTextField nameFld = new JTextField();
		JTextField urlFld = new JTextField();
		MetaData repMeta = new MetaData("name", String.class, "Repository Name");
		MetaData urlMeta = new MetaData("url", String.class, "Repository URL");
		formFactory.addComponent(this, nameFld, repMeta);
		formFactory.addComponent(this, urlFld, urlMeta);

	}

}
