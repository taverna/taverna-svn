/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflworkers.biomart.*;
import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import java.util.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * JPanel subclass for an Attribute Page
 * @author Tom Oinn
 */
public class AttributePageEditor extends JPanel {

    private Query query;
    private AttributePage page;

    public AttributePageEditor(Query query, AttributePage page) {
	super(new BorderLayout());
	//UIManager.put("CheckBox.background",  Color.WHITE);
	this.query = query;
	this.page = page;
	setOpaque(false);
	String title = page.getDescription();
	if (title == null) {
	    title = page.getDisplayName();
	}
	add(new ShadedLabel(title, ShadedLabel.TAVERNA_GREEN, true),
	    BorderLayout.NORTH);
	add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.WEST);
	add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.SOUTH);
	add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.EAST);
	JTabbedPane groups = new JTabbedPane();
	List groupList = page.getAttributeGroups();
	for (Iterator i = groupList.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof AttributeGroup) {
		AttributeGroup ag = (AttributeGroup)o;
		groups.add(ag.getDisplayName(), new AttributeGroupEditor(query, ag));
	    }
	    else {
		System.out.println(o);
	    }
	}
	add(groups, BorderLayout.CENTER);
    }
    protected void paintComponent(Graphics g) {
	final int width = getWidth();
	final int height = getHeight();
	Graphics2D g2d = (Graphics2D)g;
	Paint oldPaint = g2d.getPaint();
	g2d.setPaint(new GradientPaint(0, 0, ShadedLabel.TAVERNA_GREEN, width, 0, ShadedLabel.halfShade(ShadedLabel.TAVERNA_GREEN)));
	g2d.fillRect(0, 0, width, height);
	g2d.setPaint(oldPaint);
	super.paintComponent(g);
    }

}
class AttributeGroupEditor extends JPanel {

    AttributeGroupEditor(Query query, AttributeGroup group) {
	super(new BorderLayout());
	setOpaque(false);
	String title = group.getDescription();
	if (title == null) {
	    title = group.getDisplayName();
	}
	add(new ShadedLabel(group.getDescription(), ShadedLabel.TAVERNA_BLUE, true), BorderLayout.NORTH);
	add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.WEST);
	AttributeCollection[] collections = group.getAttributeCollections();
	JPanel cp = new JPanel();
	cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
	for (int i = 0; i < collections.length; i++) {
	    cp.add(new AttributeCollectionEditor(query, collections[i]));
	}
	cp.add(Box.createVerticalGlue());
	JScrollPane sp = new JScrollPane(cp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	cp.setBackground(Color.WHITE);
	sp.setBackground(Color.WHITE);
	sp.setPreferredSize(new Dimension(0,0));
	add(sp, BorderLayout.CENTER);
    }
    protected void paintComponent(Graphics g) {
	final int width = getWidth();
	final int height = getHeight();
	Graphics2D g2d = (Graphics2D)g;
	Paint oldPaint = g2d.getPaint();
	g2d.setPaint(new GradientPaint(0, 0, ShadedLabel.TAVERNA_BLUE, width, 0, ShadedLabel.halfShade(ShadedLabel.TAVERNA_BLUE)));
	g2d.fillRect(0, 0, width, height);
	g2d.setPaint(oldPaint);
	super.paintComponent(g);
    }
}

class AttributeCollectionEditor extends JPanel {
    
    AttributeCollectionEditor(Query query, AttributeCollection collection) {
	super(new BorderLayout());
	Attribute[] queryAttributes = query.getAttributes();
	setBackground(Color.WHITE);
	String title = collection.getDescription();
	if (title == null) {
	    title = collection.getDisplayName();
	}
	add(new ShadedLabel("</b>"+title+"<b>", ShadedLabel.TAVERNA_ORANGE, true),
	    BorderLayout.NORTH);
	
	JPanel attributePanel = new JPanel();
	attributePanel.setBackground(Color.WHITE);
	List attributes = collection.getAttributeDescriptions();
	int numberOfAttributes = attributes.size();
	int rows = numberOfAttributes / 3;
	if (rows * 3 < numberOfAttributes) {
	    rows++;
	}
	attributePanel.setLayout(new GridLayout(rows,2));
	for (Iterator i = attributes.iterator(); i.hasNext();) {
	    AttributeDescription desc = (AttributeDescription)i.next();
	    JCheckBox cb = new JCheckBox(desc.getDisplayName());
	    cb.setContentAreaFilled(false);
	    //cb.setOpaque(true);
	    //cb.setBackground(Color.WHITE);
	    cb.setMaximumSize(new Dimension(200,20));
	    String description = desc.getDescription();
	    if (description != null) {
		cb.setToolTipText(description);
	    }
	    if (queryAttributes.length > 0) {
		// Determine whether the query contains this attribute checked
		String myID = desc.getField()+desc.getKey()+desc.getTableConstraint();
		for (int j = 0; j < queryAttributes.length; j++) {
		    if (queryAttributes[j] instanceof FieldAttribute) {
			FieldAttribute f = (FieldAttribute)queryAttributes[j];
			String itsID = f.getField()+f.getKey()+f.getTableConstraint();
			//System.out.println("  "+itsID);
			if (myID.equals(itsID)) {
			    cb.setSelected(true);
			}
		    }
		}

	    }
	    attributePanel.add(cb);	    
	}
	// Fix up any blank space caused by only having one or two
	// items to show
	while (numberOfAttributes < 3) {
	    numberOfAttributes++;
	    attributePanel.add(Box.createHorizontalGlue());
	}
	attributePanel.setMaximumSize(new Dimension(5000,rows*20));
	add(attributePanel,
	    BorderLayout.CENTER);
	setMaximumSize(new Dimension(5000,rows*20+25));
    }

}
