/**
 * 
 */
package net.sf.taverna.t2.component.annotation;

import static net.sf.taverna.t2.component.annotation.SemanticAnnotationUtils.getDisplayName;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author alanrw
 * 
 */
public class NodeListCellRenderer implements ListCellRenderer {
	private static DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		return defaultRenderer.getListCellRendererComponent(list,
				getDisplayName((RDFNode) value), index, isSelected,
				cellHasFocus);
	}

}
