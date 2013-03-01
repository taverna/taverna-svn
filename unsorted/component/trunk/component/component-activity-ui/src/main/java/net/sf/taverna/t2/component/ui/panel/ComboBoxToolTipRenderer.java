/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.Component;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * @author alanrw
 *
 */
public class ComboBoxToolTipRenderer extends BasicComboBoxRenderer {
	
	private final Map<Object, String> toolTipMap;
	private JComboBox box;
	
	public ComboBoxToolTipRenderer(JComboBox box, Map<Object, String> toolTipMap) {
		this.toolTipMap = toolTipMap;
		this.box =box;
	}
	
	@Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
		JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);
            if (-1 < index) {
                String toolTip = toolTipMap.get(list.getSelectedValue());
				box.setToolTipText(toolTip);
            }
        return comp;
    }
}
