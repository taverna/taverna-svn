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
import java.awt.Container;
import java.awt.event.*;
import javax.swing.event.*;
import java.text.*;

/**
 * JPanel subclass for a Filter page
 * @author Tom Oinn
 */
public class FilterPageEditor extends JPanel {
    
    Query query;
    FilterPage page;
    QueryListener queryListener;
    Map filterLocations = new HashMap();
    
    public FilterPageEditor(Query query, FilterPage page) {
	super(new BorderLayout());
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
	List filterGroups = page.getFilterGroups();
	for (Iterator i = filterGroups.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof FilterGroup) {
		// Generic filter
		FilterGroup fg = (FilterGroup)o;
		String groupName = fg.getDisplayName();
		groups.add(groupName, new FilterGroupEditor(query, fg));
	    }
	    else if (o instanceof DSFilterGroup) {
		System.out.println("Unable to set filter UI for "+o);
	    }
	}
	add(groups, BorderLayout.CENTER);

	queryListener = new QueryAdaptor() {
		//
	    };
	query.addQueryChangeListener(queryListener);
    }
    
    protected void finalize() throws Throwable {
	query.removeQueryChangeListener(queryListener);
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

    class FilterGroupEditor extends JPanel {
	
	public FilterGroupEditor(Query query, FilterGroup fg) {
	    super(new BorderLayout());
	    setOpaque(false);
	    String title = fg.getDescription();
	    if (title == null) {
		title = fg.getDisplayName();
	    }
	    add(new ShadedLabel(title, ShadedLabel.TAVERNA_BLUE, true),
		BorderLayout.NORTH);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.WEST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.EAST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.SOUTH);
	    JPanel filters = new JPanel();
	    filters.setLayout(new BoxLayout(filters, BoxLayout.PAGE_AXIS));
	    filters.setBackground(Color.WHITE);
	    FilterCollection[] collections = fg.getFilterCollections();
	    for (int i = 0; i < collections.length; i++) {
		filters.add(new FilterCollectionEditor(query, collections[i]));
	    }
	    JScrollPane sp = new JScrollPane(filters);
	    sp.setBackground(Color.WHITE);
	    sp.setPreferredSize(new Dimension(0,0));
	    filters.add(Box.createVerticalGlue());
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

    class FilterCollectionEditor extends JPanel {
	
	public FilterCollectionEditor(Query query, FilterCollection fc) {
	    super(new BorderLayout());
	    setBackground(Color.WHITE);
	    String title = fc.getDescription();
	    if (title == null) {
		title = fc.getDisplayName();
	    }
	    if (title == null) {
		title = "";
	    }
	    add(new ShadedLabel("</b>"+title+"<b>", ShadedLabel.TAVERNA_ORANGE, true),
		BorderLayout.NORTH);
	    JPanel filterPanel = new JPanel();
	    filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
	    filterPanel.setBackground(Color.WHITE);
	    // Get all the individual filters within this collection
	    List filterDescriptions = fc.getFilterDescriptions();
	    for (Iterator i = filterDescriptions.iterator(); i.hasNext();) {
		Object o = i.next();
		if (o instanceof FilterDescription) {
		    FilterDescription fd = (FilterDescription)o;
		    String filterType = fd.getType();
		    JComponent filterEditor = null;
		    String key = fd.getField()+fd.getKey()+fd.getTableConstraint();
		    if (filterType.equals("text")) {
			filterEditor = new TextFilterEditor(query, fd);
		    }
		    // Add more filter types here....
		    if (filterEditor != null) {
			filterPanel.add(filterEditor);
			filterLocations.put(key, filterEditor);
		    }
			
		}
	    }
	    int maxHeight = 0;
	    Component[] components = filterPanel.getComponents();
	    for (int i = 0; i < components.length; i++) {
		maxHeight += components[i].getMaximumSize().getHeight();
	    }
	    filterPanel.setMaximumSize(new Dimension(600,maxHeight));
	    setMaximumSize(new Dimension(600,maxHeight+20));
	    add(filterPanel, 
		BorderLayout.CENTER);
	}	
    }
    
    class TextFilterEditor extends JPanel {
	
	Query query;
	FilterDescription fd;

	public TextFilterEditor(Query theQuery, FilterDescription filterDescription) {
	    super(new GridLayout(1,3));
	    query = theQuery;
	    fd = filterDescription;
	    final String myID = fd.getField()+fd.getKey()+fd.getTableConstraint();
	    JLabel label = new JLabel(fd.getDisplayName());
	    label.setBackground(Color.WHITE);
	    final JTextArea field = new JTextArea();
	    field.setBackground(Color.WHITE);
	    field.setOpaque(true);
	    setBackground(Color.WHITE);
	    add(label);
	    add(field);
	    JButton clearButton = new JButton("Clear");
	    clearButton.setOpaque(false);
	    clearButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			field.setText("");
		    }
		});
	    add(clearButton);
	    setMaximumSize(new Dimension(600,25));
	    // Find if there's an equivalent filter in the query
	    Filter[] filters = query.getFilters();
	    for (int i = 0; i < filters.length; i++) {
		String theirKey = (filters[i].getField()+
				   filters[i].getKey()+
				   filters[i].getTableConstraint());
		if (theirKey.equals(myID)) {
		    field.setText(filters[i].getValue());
		}
	    }
	    // Register a listener to update the query object when the filter
	    // changes
	    field.getDocument().addDocumentListener(new DocumentListener() {
		    public void changedUpdate(DocumentEvent e) {
			update();
		    }
		    public void insertUpdate(DocumentEvent e) {
			update();
		    }
		    public void removeUpdate(DocumentEvent e) {
			update();
		    }
		    void update() {
			Filter[] filters = query.getFilters();
			Filter existingFilter = null;
			for (int i = 0; i < filters.length; i++) {
			    String theirKey = (filters[i].getField()+
					       filters[i].getKey()+
					       filters[i].getTableConstraint());
			    if (theirKey.equals(myID)) {
				existingFilter = filters[i];
			    }
			}
			if (field.getText().equals("") &&
			    existingFilter != null) {
			    // Remove the filter entirely
			    query.removeFilter(existingFilter);
			}
			else if (field.getText().equals("")==false) {
			    Filter newFilter = new BasicFilter(fd.getField(),
							       fd.getTableConstraint(),
							       fd.getKey(),
							       fd.getLegalQualifiers(),
							       field.getText(),
							       fd.getHandlerFromContext());
			    if (existingFilter == null) {
				// Create entirely new filter
				query.addFilter(newFilter);
			    }
			    else {
				// Update existing filter
				query.replaceFilter(existingFilter, newFilter);
			    }
			}
		    }
		});
	}
	
    }
}
