/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.BorderLayout;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

/**
 * @author alanrw
 *
 */
public class PrefixPanel extends JPanel implements Observer<ProfileChoiceMessage>{
	
	private DefaultTableModel prefixModel = new DefaultTableModel(10,2) {
		@Override
	    public boolean isCellEditable(int row, int column) {
	       //all cells false
	       return false;
	    };
	};
	
	private JTable prefixTable = new JTable(prefixModel);
	
	public PrefixPanel() {
		super();
		this.setLayout(new BorderLayout());
		prefixModel.setColumnIdentifiers(new String[] {"Prefix", "URL"});
		this.add (new JScrollPane(prefixTable), BorderLayout.CENTER);
		this.setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
                "Prefixes",
                TitledBorder.CENTER,
                TitledBorder.TOP));
	}

	@Override
	public void notify(Observable<ProfileChoiceMessage> sender,
			ProfileChoiceMessage message) throws Exception {
		ComponentProfile newProfile = message.getChosenProfile();
		prefixModel.setRowCount(0);
		if (newProfile != null) {
			for (Entry<String, String> entry: newProfile.getPrefixMap().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (!value.endsWith("#")) {
					value += "#";
				}
				prefixModel.addRow(new String[] {key, value});
			}
		}
		this.validate();
	}
	
	public TreeMap<String, String> getPrefixMap() {
		TreeMap<String, String> result = new TreeMap<String, String>();
		for (int i = 0; i < prefixModel.getRowCount(); i++) {
			result.put((String) prefixModel.getValueAt(i, 0), (String) prefixModel.getValueAt(i, 1));
		}
		return result;
	}

}
