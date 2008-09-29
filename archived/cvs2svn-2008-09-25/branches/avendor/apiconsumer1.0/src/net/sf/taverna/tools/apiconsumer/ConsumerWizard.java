/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.tools.apiconsumer;

import com.sun.javadoc.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.*;
import java.util.prefs.*;
import org.jdom.*;
import org.jdom.output.*;

/**
 * The top level UI component for the consumer wizard
 * @author Tom Oinn
 * @version $Id: ConsumerWizard.java,v 1.1.1.1 2005-03-01 16:57:12 mereden Exp $
 */
public class ConsumerWizard extends JFrame {

    boolean running = true;
    APIDescription description = new APIDescription();
    JFileChooser fc;
    
    public ConsumerWizard(ClassDoc[] classes) {
	super("API Consumer");
	new SplashScreen(8000);
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	catch (Exception ex) {
	    //
	}
	fc = new JFileChooser();
	getContentPane().setLayout(new BorderLayout());

	final ClassTree tree = new ClassTree(new ClassTreeModel(classes), description);
	final ClassSummaryPane summary = new ClassSummaryPane();
	final JTabbedPane tabs = new JTabbedPane();
	

	JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					 new JScrollPane(tree),
					 tabs);
	tabs.addTab("Description",new JScrollPane(summary));
	tabs.addTab("Methods", new JPanel());
	tabs.setEnabledAt(1, false);
	pane.setDividerLocation(-1);
	getContentPane().add(pane, BorderLayout.CENTER);	

	tree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    DefaultMutableTreeNode node = 
			(DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		    if (node == null) {
			return;
		    }
		    Object nodeObject = node.getUserObject();
		    if (nodeObject instanceof String) {
			summary.setText("");
			tabs.setEnabledAt(1, false);
			tabs.setSelectedIndex(0);
		    }
		    if (nodeObject instanceof ClassDoc) {
			summary.setText(HTMLClassSummary.getSummary((ClassDoc)nodeObject));
			tabs.setComponentAt(1, new JScrollPane(new MethodSelectionPanel(ConsumerWizard.this.description, ((ClassDoc)nodeObject), tree)));
			tabs.setEnabledAt(1, true);
		    }
		}
	    });


	setBounds(100,100,1000,500);
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    running = false;
		}
	    });
	setJMenuBar(createMenuBar());
	show();
    }

    public boolean isRunning() {
	return this.running;
    }

    private JMenuBar createMenuBar() {
	JMenuBar bar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	JMenuItem saveXMLItem = new JMenuItem("Save as XML");
	fileMenu.add(saveXMLItem);
	saveXMLItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    Preferences prefs = Preferences.userNodeForPackage(ConsumerWizard.class);
		    String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		    fc.resetChoosableFileFilters();
		    int returnVal = fc.showSaveDialog(ConsumerWizard.this);
		    try {
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    prefs.put("currentDir", fc.getCurrentDirectory().toString());
			    File file = fc.getSelectedFile();
			    PrintWriter out = new PrintWriter(new FileWriter(file));
			    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			    Document doc = new Document(ConsumerWizard.this.description.asXML());
			    out.println(xo.outputString(doc));
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			JOptionPane.showMessageDialog(ConsumerWizard.this,
						      "Problem saving API : \n"+ex.getMessage(),
						      "Error!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	bar.add(fileMenu);
	return bar;
    }

}
