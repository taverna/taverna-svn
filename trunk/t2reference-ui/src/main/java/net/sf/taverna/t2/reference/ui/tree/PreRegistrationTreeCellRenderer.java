package net.sf.taverna.t2.reference.ui.tree;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * A cell renderer for the pre-registration tree model, with appropriate
 * rendering for inline strings, web URLs and files. The renderer doesn't
 * attempt to show the contents (other than in the case of inline strings), but
 * does show the URL and File paths for those types along with sensible icons
 * stolen from Eclipse.
 * 
 * @author Tom Oinn
 * 
 */
public class PreRegistrationTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 5284952103994689024L;
	private ImageIcon textIcon = new ImageIcon(getClass().getResource(
			"/icons/wordassist_co.gif"));
	private ImageIcon fileIcon = new ImageIcon(getClass().getResource(
			"/icons/topic.gif"));
	private ImageIcon urlIcon = new ImageIcon(getClass().getResource(
			"/icons/web.gif"));
	private ImageIcon binaryIcon = new ImageIcon(getClass().getResource(
			"/icons/genericregister_obj.gif"));

	@Override
	public synchronized Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		if (value instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode) value)
					.getUserObject();
			if (userObject == null) {
				setText("List");
			}
			if (tree.getModel().getRoot() == value) {
				setText(userObject.toString());
			} else {
				if (userObject != null) {
					// Handle rendering of string, file, url, byte[] here
					if (userObject instanceof String) {
						setIcon(textIcon);
						String string = (String) userObject;
						if (string.length() < 50) {
							setText(string);
						} else {
							setText(string.substring(0, 50) + "...");
						}
					} else if (userObject instanceof byte[]) {
						byte[] bytes = (byte[]) userObject;
						setIcon(binaryIcon);
						setText("byte[] " + getHumanReadableSize(bytes.length));
					} else if (userObject instanceof File) {
						setIcon(fileIcon);
						try {
							setText(((File) userObject).getCanonicalPath());
						} catch (IOException e) {
							setText(userObject.toString());
						}
					} else if (userObject instanceof URL) {
						setIcon(urlIcon);
						setText(((URL) userObject).toExternalForm());
					}
				} else {
					if (expanded) {
						// setIcon(expandedIcon);
					} else {
						// setIcon(unexpandedIcon);
					}
				}
			}
		}
		return this;
	}

	private static String getHumanReadableSize(int size) {
		if (size < 10000) {
			return size + " bytes";
		} else if (size < 2000000) {
			return (int) (size / 1000) + " kB";
		} else if (size < 2000000000) {
			return (int) (size / (1000000)) + " mB";
		} else {
			return (int) (size / (1000000000)) + " gB";
		}
	}

}
