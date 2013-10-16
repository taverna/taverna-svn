/**
 * 
 */
package net.sf.taverna.t2.component.ui.panel;

import static javax.swing.BorderFactory.createEtchedBorder;
import static javax.swing.BorderFactory.createTitledBorder;

import java.awt.BorderLayout;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import net.sf.taverna.t2.component.api.Profile;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

/**
 * @author alanrw
 * 
 */
@SuppressWarnings("serial")
public class PrefixPanel extends JPanel implements
		Observer<ProfileChoiceMessage> {
	private DefaultTableModel prefixModel = new DefaultTableModel(10, 2) {
		@Override
		public boolean isCellEditable(int row, int column) {
			// all cells false
			return false;
		};
	};

	private JTable prefixTable = new JTable(prefixModel);

	public PrefixPanel() {
		super();
		this.setLayout(new BorderLayout());
		prefixModel.setColumnIdentifiers(new String[] { "Prefix", "URL" });
		this.add(new JScrollPane(prefixTable), BorderLayout.CENTER);
		this.setBorder(createTitledBorder(createEtchedBorder(), "Prefixes",
				TitledBorder.CENTER, TitledBorder.TOP));
	}

	@Override
	public void notify(Observable<ProfileChoiceMessage> sender,
			ProfileChoiceMessage message) throws Exception {
		Profile newProfile = message.getChosenProfile();
		prefixModel.setRowCount(0);
		if (newProfile != null)
			for (Entry<String, String> entry : newProfile.getPrefixMap()
					.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (!value.endsWith("#"))
					value += "#";
				prefixModel.addRow(new String[] { key, value });
			}
		this.validate();
	}

	public TreeMap<String, String> getPrefixMap() {
		TreeMap<String, String> result = new TreeMap<String, String>();
		for (int i = 0; i < prefixModel.getRowCount(); i++)
			result.put((String) prefixModel.getValueAt(i, 0),
					(String) prefixModel.getValueAt(i, 1));
		return result;
	}
}
