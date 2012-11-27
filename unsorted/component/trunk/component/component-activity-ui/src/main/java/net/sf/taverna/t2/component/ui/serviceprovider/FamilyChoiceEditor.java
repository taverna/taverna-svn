/**
 * 
 */
package net.sf.taverna.t2.component.ui.serviceprovider;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;

/**
 * @author alanrw
 *
 */
public class FamilyChoiceEditor implements ComboBoxEditor {
	
	private static Logger logger = Logger.getLogger(FamilyChoiceEditor.class);

	
	JTextField textField = new JTextField(30);
	Object originalItem;
	private ComponentRegistry registry;

	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxEditor#addActionListener(java.awt.event.ActionListener)
	 */
	@Override
	public void addActionListener(ActionListener l) {
		textField.addActionListener(l);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxEditor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		return textField;
	}

	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxEditor#getItem()
	 */
	@Override
	public Object getItem() {
		if ((originalItem == null) || !ComponentFamilyChooserPanel.convertValueToString(originalItem).equals(textField.getText())) {
			try {
				return registry.createComponentFamily(textField.getText(), null);
			} catch (ComponentRegistryException e) {
				logger.error(e);
				return null;
			}
		} else {
			return originalItem;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxEditor#removeActionListener(java.awt.event.ActionListener)
	 */
	@Override
	public void removeActionListener(ActionListener l) {
		textField.removeActionListener(l);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxEditor#selectAll()
	 */
	@Override
	public void selectAll() {
		throw new UnsupportedOperationException(
        "Not supported yet. in select All");
	}

	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxEditor#setItem(java.lang.Object)
	 */
	@Override
	public void setItem(Object anObject) {
		originalItem = anObject;
		textField.setText(ComponentFamilyChooserPanel.convertValueToString(originalItem));
	}

	public void setRegistry(ComponentRegistry registry) {
		this.registry = registry;
	}

}
