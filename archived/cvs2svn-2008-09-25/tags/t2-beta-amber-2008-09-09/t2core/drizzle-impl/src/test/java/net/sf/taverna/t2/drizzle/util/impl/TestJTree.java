/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeModel;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.drizzle.util.StringKey;
import net.sf.taverna.t2.drizzle.util.StringValue;
import net.sf.taverna.t2.utility.TreeModelAdapter;

import org.junit.Ignore;

/**
 * @author alanrw
 * 
 */
@Ignore("Not a test case")
public final class TestJTree extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1244291627173869253L;

	PropertiedObjectSet<StringObject> testSet = new PropertiedObjectSetImpl<StringObject>();

	StringObject service1 = new StringObject("blast service"); //$NON-NLS-1$

	StringObject service2 = new StringObject("emma service"); //$NON-NLS-1$

	private StringObject service3 = new StringObject("fetchPdb service"); //$NON-NLS-1$

	private StringObject service4 = new StringObject("renderPdb service"); //$NON-NLS-1$

	PropertyKey domainKey = new StringKey("domain"); //$NON-NLS-1$

	PropertyKey nameKey = new StringKey("name"); //$NON-NLS-1$

	PropertyKey providerKey = new StringKey("provider"); //$NON-NLS-1$

	PropertyKey typeKey = new StringKey("type"); //$NON-NLS-1$

	PropertyValue ebiValue = new StringValue("ebi"); //$NON-NLS-1$

	private PropertyValue manchesterValue = new StringValue("manchester"); //$NON-NLS-1$

	private PropertyValue soaplabValue = new StringValue("soaplab"); //$NON-NLS-1$

	PropertyValue wsdlValue = new StringValue("wsdl"); //$NON-NLS-1$

	PropertyValue geneticsValue = new StringValue("genetics"); //$NON-NLS-1$

	private PropertyValue structureValue = new StringValue("structure"); //$NON-NLS-1$

	PropertyValue blastValue = new StringValue("blast"); //$NON-NLS-1$

	private PropertyValue emmaValue = new StringValue("emma"); //$NON-NLS-1$

	private PropertyValue fetchPdbValue = new StringValue("fetchPdb"); //$NON-NLS-1$

	private PropertyValue renderPdbValue = new StringValue("renderPdb"); //$NON-NLS-1$

	JTree currentTree;

	private PropertiedGraphView<StringObject> graphView = null;

	TreeModel model = null;

	List<PropertyKeySetting> fullKeySettings = null;

	private void initializeSet() {
		this.graphView = new PropertiedGraphViewImpl<StringObject>();

		this.testSet
				.setProperty(this.service1, this.providerKey, this.ebiValue);
		this.testSet.setProperty(this.service1, this.typeKey, this.wsdlValue);
		this.testSet.setProperty(this.service1, this.domainKey,
				this.geneticsValue);
		this.testSet.setProperty(this.service1, this.nameKey, this.blastValue);

		this.testSet
				.setProperty(this.service2, this.providerKey, this.ebiValue);
		this.testSet
				.setProperty(this.service2, this.typeKey, this.soaplabValue);
		this.testSet.setProperty(this.service2, this.domainKey,
				this.geneticsValue);
		this.testSet.setProperty(this.service2, this.nameKey, this.emmaValue);

		this.testSet
				.setProperty(this.service3, this.providerKey, this.ebiValue);
		this.testSet
				.setProperty(this.service3, this.typeKey, this.soaplabValue);
		this.testSet.setProperty(this.service3, this.domainKey,
				this.structureValue);
		this.testSet.setProperty(this.service3, this.nameKey,
				this.fetchPdbValue);

		this.testSet.setProperty(this.service4, this.providerKey,
				this.manchesterValue);
		this.testSet.setProperty(this.service4, this.typeKey, this.wsdlValue);
		this.testSet.setProperty(this.service4, this.domainKey,
				this.structureValue);
		this.testSet.setProperty(this.service4, this.nameKey,
				this.renderPdbValue);

		this.graphView.setPropertiedObjectSet(this.testSet);

	}

	private List<PropertyKeySetting> initializeSettings() {
		List<PropertyKeySetting> keySettings = new ArrayList<PropertyKeySetting>();

		PropertyKeySetting typeSetting = new PropertyKeySettingImpl();
		typeSetting.setPropertyKey(this.typeKey);
		keySettings.add(typeSetting);
		PropertyKeySetting edgeSetting = new PropertyKeySettingImpl();
		keySettings.add(edgeSetting);
		PropertyKeySetting domainSetting = new PropertyKeySettingImpl();
		domainSetting.setPropertyKey(this.domainKey);
		keySettings.add(domainSetting);
		PropertyKeySetting providerSetting = new PropertyKeySettingImpl();
		providerSetting.setPropertyKey(this.providerKey);
		keySettings.add(providerSetting);
		PropertyKeySetting nameSetting = new PropertyKeySettingImpl();
		nameSetting.setPropertyKey(this.nameKey);
		keySettings.add(nameSetting);
		return keySettings;
	}

	/**
	 * Populate the PropertiedObjectSet and create a TreeModel over it
	 * 
	 * @return
	 */
	public TreeModel createTree(final List<PropertyKeySetting> keySettings) {
		PropertiedTreeModel<StringObject> testImpl = new PropertiedTreeModelImpl<StringObject>();
		testImpl.setPropertyKeySettings(keySettings);
		testImpl.setPropertiedGraphView(this.graphView);

		TreeModel untypedView = TreeModelAdapter.untypedView(testImpl);
		return untypedView;
	}

	void expandAll(final JTree expansionTree) {
		for (int i = 0; i <= expansionTree.getRowCount(); i++) {
			expansionTree.expandRow(i);
		}
	}

	/**
	 * Construct a TestJTree that takes a PropertiedObjectSet populated with
	 * example data and shows it as a JTree. The effects of altering the
	 * PropertiedObjectSet are shown to be mirrored in the JTree.
	 */
	public TestJTree() {
		initializeSet();
		this.fullKeySettings = initializeSettings();

		this.model = createTree(this.fullKeySettings);
		Container content = getContentPane();
		this.currentTree = new JTree(this.model);
//		this.currentTree.setCellRenderer(new TableTreeCellRenderer());
		this.currentTree.setRowHeight(0);
		this.currentTree.setRootVisible(false);
		content.setLayout(new BorderLayout());
		content.add(this.currentTree, BorderLayout.CENTER);
		setSize(275, 300);
		expandAll(this.currentTree);
		setVisible(true);
		Vector<String> keyNames = new Vector<String>();
		int edgeIndex = 0;
		for (PropertyKeySetting pks : this.fullKeySettings) {
			PropertyKey pk = pks.getPropertyKey();
			if (pk != null) {
				keyNames.add(pk.toString());
			} else {
				edgeIndex = keyNames.size();
				keyNames.add(""); //$NON-NLS-1$
			}
		}
		DefaultTableModel tableModel = new DefaultTableModel(keyNames, 0);
		final JTable table = new JTable(tableModel);
		final TableColumnModel columnModel = table.getColumnModel();
		TableColumn edgeColumn = columnModel.getColumn(edgeIndex);
		DefaultTableCellRenderer blackRenderer = new DefaultTableCellRenderer();
		blackRenderer.setBackground(Color.BLACK);
		edgeColumn.setHeaderRenderer(blackRenderer);
		JTableHeader tableHeader = table.getTableHeader();
//		tableHeader.addMouseListener(new MouseAdapter() {
//			int fromIndex = -1;
//
//			@Override
//			public void mousePressed(MouseEvent me) {
//				this.fromIndex = columnModel.getColumnIndexAtX(me.getX());
//			}
//
//			@Override
//			public void mouseExited(MouseEvent me) {
//	//			this.fromIndex = -1;
//			}
//
//			@Override
//			public void mouseReleased(MouseEvent me) {
//				if (this.fromIndex != -1) {
//					int toIndex = columnModel.getColumnIndexAtX(me.getX());
//					if ((this.fromIndex != toIndex) &&(toIndex != -1)) {
//						PropertyKeySetting movedKey = TestJTree.this.fullKeySettings
//								.get(this.fromIndex);
//						TestJTree.this.fullKeySettings.remove(this.fromIndex);
//						TestJTree.this.fullKeySettings.add(toIndex, movedKey);
//						TestJTree.this.model = createTree(TestJTree.this.fullKeySettings);
//						TestJTree.this.currentTree
//								.setModel(TestJTree.this.model);
//						expandAll(TestJTree.this.currentTree);
//					}
//					this.fromIndex = -1;
//				}
//			}
//		});
		columnModel.addColumnModelListener(new TableColumnModelListener() {

			public void columnAdded(TableColumnModelEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnMarginChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnMoved(TableColumnModelEvent arg0) {
				int fromIndex = arg0.getFromIndex();
				int toIndex = arg0.getToIndex();
				if ((fromIndex != toIndex) && (table.getSelectedColumn() == -1)) {
					PropertyKeySetting movedKey = TestJTree.this.fullKeySettings
							.get(fromIndex);
					TestJTree.this.fullKeySettings.remove(fromIndex);
					TestJTree.this.fullKeySettings.add(toIndex, movedKey);
					TestJTree.this.model = createTree(TestJTree.this.fullKeySettings);
					TestJTree.this.currentTree.setModel(TestJTree.this.model);
					expandAll(TestJTree.this.currentTree);
				}
			}

			public void columnRemoved(TableColumnModelEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void columnSelectionChanged(ListSelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

		});
		content.add(tableHeader, BorderLayout.NORTH);

		validate();
	}

	/**
	 * Show the test JTree.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new TestJTree();
	}
}
