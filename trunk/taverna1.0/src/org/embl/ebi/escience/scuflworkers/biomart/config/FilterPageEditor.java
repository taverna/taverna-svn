/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart.config;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflworkers.biomart.*;
import org.ensembl.mart.lib.*;
import org.ensembl.mart.lib.config.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
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
	List filterGroupEditorList = new ArrayList();
	for (Iterator i = filterGroups.iterator(); i.hasNext();) {
	    Object o = i.next();
	    if (o instanceof FilterGroup) {
		// Generic filter
		FilterGroup fg = (FilterGroup)o;
		String groupName = fg.getDisplayName();
		FilterGroupEditor fge = new FilterGroupEditor(query, fg);
		groups.add(groupName, fge);
		filterGroupEditorList.add(fge);
	    }
	    else if (o instanceof DSFilterGroup) {
		System.out.println("Unable to set filter UI for "+o);
	    }
	}
	add(groups, BorderLayout.CENTER);
	
	for (Iterator i = filterGroupEditorList.iterator(); i.hasNext();) {
	    ((FilterGroupEditor)i.next()).pushFilterOptions();
	}							  
	
	//queryListener = new QueryAdaptor() {
	//
	//    };
	//query.addQueryChangeListener(queryListener);
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
	
	Map internalNameToLeafEditor = new HashMap();
	List listFilterList = new ArrayList();

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
		FilterCollectionEditor fce = new FilterCollectionEditor(query, collections[i]);
		filters.add(fce);
		for (Iterator j = fce.internalNameToLeafEditor.values().iterator(); j.hasNext();) {
		    Object o = j.next();
		    if (o instanceof ListFilterEditor) {
			listFilterList.add(o);
		    }
		}
		internalNameToLeafEditor.putAll(fce.internalNameToLeafEditor);
	    }
	    JScrollPane sp = new JScrollPane(filters);
	    sp.setBackground(Color.WHITE);
	    sp.setPreferredSize(new Dimension(0,0));
	    filters.add(Box.createVerticalGlue());
	    add(sp, BorderLayout.CENTER);
	    
	}

	public void pushFilterOptions() {
	    for (Iterator i = listFilterList.iterator(); i.hasNext(); ) {
		ListFilterEditor lfe = (ListFilterEditor)i.next();
		lfe.pushFilterOptions();
	    }
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
	
	Map internalNameToLeafEditor = new HashMap();

	public FilterCollectionEditor(Query query, FilterCollection fc) {
	    super(new BorderLayout());
	    setBackground(Color.WHITE);
	    String title = fc.getDescription();
	    if (title == null) {
		title = fc.getDisplayName();
	    }
	    if (title == null) {
		title = "&nbsp;";
	    }
	    ShadedLabel sl = new ShadedLabel("</b>"+title+"<b>", ShadedLabel.TAVERNA_ORANGE, true);
	    sl.setMaximumSize(new Dimension(6000,20));
	    sl.setMinimumSize(new Dimension(60,20));
	    add(sl, BorderLayout.NORTH);
	    JPanel filterPanel = new JPanel();
	    filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.PAGE_AXIS));
	    filterPanel.setBackground(Color.WHITE);	
	    filterPanel.add(Box.createRigidArea(new Dimension(2,2)));

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
		    else if (filterType.equals("boolean") ||
			     filterType.equals("boolean_num") ||
			     filterType.equals("boolean_list")) {
			filterEditor = new BooleanFilterEditor(query, fd);
		    }
		    else if (filterType.equals("list") ||
			     filterType.equals("drop_down_basic_filter") ||
			     filterType.equals("text_entry_basic_filter")) {
			filterEditor = new ListFilterEditor(query, fd);
		    }
		    else if (filterType.equals("id_list")) {
			filterEditor = new IDListFilterEditor(query, fd);
		    }
		    else {
			filterEditor = new ToDoMessage(filterType);
		    }
		    // Add more filter types here....
		    if (filterEditor != null) {
			filterPanel.add(filterEditor);
			filterPanel.add(Box.createRigidArea(new Dimension(2,2)));
			filterLocations.put(key, filterEditor);
			internalNameToLeafEditor.put(fd.getInternalName(), filterEditor);
		    }
			
		}
	    }
	    int maxHeight = 2;
	    Component[] components = filterPanel.getComponents();
	    for (int i = 0; i < components.length; i++) {
		if (components[i] instanceof FilterEditor) {
		    maxHeight += components[i].getMaximumSize().getHeight();
		    maxHeight += 2;
		}
	    }
	    filterPanel.setMaximumSize(new Dimension(6000,maxHeight));
	    setMaximumSize(new Dimension(6000,maxHeight+sl.getHeight()));
	    add(filterPanel, 
		BorderLayout.CENTER);
	}	
    }

    abstract class FilterEditor extends JPanel {
	
	protected OptionHolder emptySelection = new OptionHolder(null);

	protected PushOptionsHandler[] pushOptionHandlers;

	class PushOptionsHandler {
	    
	    private PushAction optionPush;

	    public PushOptionsHandler(PushAction optionPush) {
		this.optionPush = optionPush;
	    }

	    private FilterEditor getTargetEditor() {
		FilterEditor fe = FilterEditor.this;
		Component currentComponent = fe;
		while (currentComponent instanceof FilterGroupEditor == false &&
		       currentComponent != null) {
		    currentComponent = currentComponent.getParent();
		}
		FilterGroupEditor fge = (FilterGroupEditor)currentComponent;
		//for (Iterator i = fge.internalNameToLeafEditor.keySet().iterator(); i.hasNext();) {
		//    String s = (String)i.next();
		//    System.out.println(s);
		//}
		return (FilterEditor)fge.internalNameToLeafEditor.get(optionPush.getRef());
	    }
	    
	    public void push() {
		System.out.println("Push to : "+optionPush.getRef());
		getTargetEditor().setOptions(optionPush.getOptions());
	    }

	    public void remove() {
		getTargetEditor().setOptions(null);
	    }
	    
	}
	
	class OptionHolder {
	    Option option;
	    public OptionHolder(Option o) {
		this.option = o;
	    }
	    public String toString() {
		if (option == null) {
		    return "No filter";
		}
		return option.getDisplayName();
	    }
	}

	protected FilterEditor(LayoutManager layout) {
	    super(layout);
	}

	protected FilterEditor() {
	    super();
	}
	
	protected void setOptions(Option[] options) {
	    //
	}

	protected void assignPushOptions(PushAction[] optionPushes) {
	    pushOptionHandlers = new PushOptionsHandler[optionPushes.length];
	    for (int i = 0; i < optionPushes.length; i++) {
		pushOptionHandlers[i] = 
		    new PushOptionsHandler(optionPushes[i]);
		pushOptionHandlers[i].push();
	    }
	}
	
	protected void unassignPushOptions() {
	    int n = (pushOptionHandlers == null) ? 0 : pushOptionHandlers.length;
	    for (int i = 0; i < n; i++) {
		pushOptionHandlers[i].remove();
	    }
	}

    }

    class IDListFilterEditor extends FilterEditor {

	String[] myIDs = new String[0];
	JComboBox list = new JComboBox();
	JTextArea idText = new JTextArea(10,10);
	Query query;
	FilterDescription fd;
	ActionListener listListener = null;
	JButton clearButton, updateButton;
	
	public void setOptions(Option[] options) {
	    if (list == null) {
		return;
	    }
	    list.removeActionListener(listListener);
	    list.removeAllItems();
	    list.addItem(emptySelection);
	    myIDs = new String[options.length];
	    for (int i = 0; i < options.length; i++) {
		Option o = options[i];
		list.addItem(new OptionHolder(o));
		myIDs[i] = (o.getFieldFromContext()+
			    o.getKeyFromContext()+
			    o.getTableConstraintFromContext());
	    }
	    IDListFilter f = getCurrentFilter();
	    if (f!=null) {
		list.setSelectedIndex(lastFilterIndex+1);
		idText.setEnabled(true);
		
		String[] ids = f.getIdentifiers();
		if (ids.length > 0) {
		    StringBuffer sb = new StringBuffer();
		    for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
			    sb.append("\n");
			}
			sb.append(ids[i]);
		    }
		    idText.setText(sb.toString());
		}
		else {
		    idText.setText("");
		}
	    }	
	    else {
		list.setSelectedIndex(0);
		idText.setText("");
		idText.setEnabled(false);
	    }
	    list.addActionListener(listListener);
	    list.validate();
	}

	int lastFilterIndex = -1;

	public IDListFilter getCurrentFilter() {
	    Filter[] currentFilters = query.getFilters();
	    for (int i = 0; i < currentFilters.length; i++) {
		String filterID = (currentFilters[i].getField()+
				   currentFilters[i].getKey()+
				   currentFilters[i].getTableConstraint());
		for (int j = 0; j < myIDs.length; j++) {
		    if (filterID.equals(myIDs[j]) &&
			currentFilters[i] instanceof IDListFilter) {
			lastFilterIndex = j;
			return (IDListFilter)currentFilters[i];
		    }
		}
	    }
	    return null;
	}
	
	public IDListFilterEditor(Query theQuery, FilterDescription filterDescription) {
	    super(new BorderLayout());
	    setBackground(Color.WHITE);
	    query = theQuery;
	    fd = filterDescription;
	    setOptions(fd.getOptions());
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.WEST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.EAST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.SOUTH);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.NORTH);
	    JPanel internalPanel = new JPanel(new BorderLayout());
	    internalPanel.setOpaque(false);
	    internalPanel.add(list, BorderLayout.NORTH);
	    JScrollPane sp = new JScrollPane(idText);
	    //sp.setPreferredSize(new Dimension(0,0));
	    internalPanel.add(sp, BorderLayout.CENTER);
	    //internalPanel.add(new JLabel(fd.getDisplayName()), BorderLayout.WEST);
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
	    updateButton = new JButton("Update IDs");
	    updateButton.setMaximumSize(new Dimension(300,25));
	    clearButton = new JButton("Clear");
	    clearButton.setMaximumSize(new Dimension(300,25));
	    buttonPanel.add(updateButton);
	    buttonPanel.add(clearButton);
	    buttonPanel.add(Box.createVerticalGlue());
	    buttonPanel.setOpaque(false);
	    internalPanel.add(buttonPanel, BorderLayout.EAST);
	    add(internalPanel, BorderLayout.CENTER);
	    setMaximumSize(new Dimension(6000,250));
	    
	    clearButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			idText.setText("");
			updateButton.setEnabled(false);
			list.setSelectedIndex(0);
		    }
		});

	    updateButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			IDListFilter currentFilter = getCurrentFilter();
			Option o = ((OptionHolder)list.getSelectedItem()).option;
			IDListFilter newFilter = new IDListFilter(o.getFieldFromContext(),
							    o.getTableConstraintFromContext(),
							    o.getKeyFromContext(),
							    idText.getText().split("(\\s+|\\s*,\\s*)"));
			if (currentFilter == null) {
			    query.addFilter(newFilter);
			}
			else {
			    query.replaceFilter(currentFilter, newFilter);
			}
			clearButton.setEnabled(true);
			String[] ids = newFilter.getIdentifiers();
			if (ids.length > 0) {
			    StringBuffer sb = new StringBuffer();
			    for (int i = 0; i < ids.length; i++) {
				if (i > 0) {
				    sb.append("\n");
				}
				sb.append(ids[i]);
			    }
			    idText.setText(sb.toString());
			}
		    }
		});

	    // Create the actionlistener for the checkbox
	    listListener = new ActionListener() {
		    OptionHolder lastSelectedOption = (OptionHolder)list.getSelectedItem();
		    public void actionPerformed(ActionEvent ae) {
			if (list.getSelectedItem() == lastSelectedOption) {
			    // Ignore, no change in selection
			    return;
			}		
			clearButton.setEnabled(false);
			if (list.getSelectedItem() == emptySelection) {
			    updateButton.setEnabled(false);
			    // Destroy the active filter if any, disable
			    // the text area
			    idText.setEnabled(false);
			    Filter currentFilter = getCurrentFilter();
			    if (currentFilter != null) {
				query.removeFilter(currentFilter);
			    }
			    return;
			}
			updateButton.setEnabled(true);
			if (idText.getText().equals("") == false) {
			    // is both an id selector and some id text
			    Filter currentFilter = getCurrentFilter();
			    Option o = ((OptionHolder)list.getSelectedItem()).option;
			    Filter newFilter = new IDListFilter(o.getFieldFromContext(),
								o.getTableConstraintFromContext(),
								o.getKeyFromContext(),
								idText.getText().split("(\\s+|\\s*,\\s*)"));
			    if (currentFilter == null) {
				query.addFilter(newFilter);
			    }
			    else {
				query.removeFilter(currentFilter);
				query.addFilter(newFilter);
				
				// query.replaceFilter(currentFilter, newFilter);
			    }
			    clearButton.setEnabled(true);
			}
			idText.setEnabled(true);
		    }
		};
	    list.addActionListener(listListener);
	}
	
    }

    class ListFilterEditor extends FilterEditor {

	String[] myIDs = new String[0];
	public JComboBox list = new JComboBox();
	Query query;
	FilterDescription fd;
	ActionListener listListener;
	
	public BasicFilter getCurrentFilter() {
	    Filter[] currentFilters = query.getFilters();
	    for (int i = 0; i < currentFilters.length; i++) {
		String filterID = (currentFilters[i].getField()+
				   currentFilters[i].getKey()+
				   currentFilters[i].getTableConstraint());
		for (int j = 0; j < myIDs.length; j++) {
		    if (filterID.equals(myIDs[j]) &&
			currentFilters[i] instanceof BasicFilter) {
			return (BasicFilter)currentFilters[i];
		    }
		}
	    }
	    return null;
	}

	public void setOptions(Option[] options) {
	    System.out.println("Setting options for "+fd.getDisplayName());
	    unassignPushOptions();
	    BasicFilter currentFilter = getCurrentFilter();
	    if (currentFilter != null) {
		query.removeFilter(currentFilter);
	    }
	    if (options != null) {
		myIDs = new String[options.length];
	    }
	    else {
		myIDs = new String[0];
	    }
	    list.removeActionListener(listListener);
	    list.removeAllItems();
	    list.addItem(emptySelection);
	    //list.setSelectedIndex(0);
	    for (int i = 0; i < myIDs.length; i++) {
		list.addItem(new OptionHolder(options[i]));
		// Note IDs different in this case
		myIDs[i] = (options[i].getFieldFromContext()+
			    options[i].getKeyFromContext()+
			    options[i].getTableConstraintFromContext());
	    }
	    if (myIDs.length == 0) {
		list.setEnabled(false);
		list.setSelectedIndex(0);
	    }
	    else {
		list.setEnabled(true);
		Filter[] currentFilters = query.getFilters();
		for (int i = 0; i < currentFilters.length; i++) {
		    String filterID = (currentFilters[i].getField()+
				       currentFilters[i].getKey()+
				       currentFilters[i].getTableConstraint());
		    for (int j = 0; j < myIDs.length; j++) {
			if (filterID.equals(myIDs[j]) &&
			    currentFilters[i] instanceof BasicFilter) {
			    Option op = ((OptionHolder)list.getItemAt(j+1)).option;
			    if (currentFilters[i].getValue().equals(op.getValue())) {
				list.setSelectedIndex(j+1);
				break;
			    }
			}
		    }
		}	
	    }
	    list.addActionListener(listListener);	    
	    list.validate();
	    System.out.println("Done setting options for "+fd.getDisplayName());
	}

	public void pushFilterOptions() {
	    OptionHolder oh = (OptionHolder)list.getSelectedItem();
	    if (oh != emptySelection && oh != null) {
		Option o = oh.option;
		assignPushOptions(o.getPushActions());
	    }
	}

	public ListFilterEditor(Query theQuery, FilterDescription filterDescription) {
	    super(new BorderLayout());
	    fd = filterDescription;
	    query = theQuery;
	    setOpaque(false);
	    Option[] options = filterDescription.getOptions();
	    myIDs = new String[options.length];
	    list.addItem(emptySelection);
	    list.setSelectedIndex(0);
	    for (int i = 0; i < options.length; i++) {
		list.addItem(new OptionHolder(options[i]));
		// Note IDs different in this case
		myIDs[i] = (options[i].getFieldFromContext()+
			    options[i].getKeyFromContext()+
			    options[i].getTableConstraintFromContext());
	    }
	    if (options.length == 0) {
		list.setEnabled(false);
	    }
	    
	    // Find an existing value if such exists
	    Filter[] currentFilters = query.getFilters();
	    for (int i = 0; i < currentFilters.length; i++) {
		String filterID = (currentFilters[i].getField()+
				   currentFilters[i].getKey()+
				   currentFilters[i].getTableConstraint());
		for (int j = 0; j < myIDs.length; j++) {
		    if (filterID.equals(myIDs[j]) &&
			currentFilters[i] instanceof BasicFilter) {
			Option op = ((OptionHolder)list.getItemAt(j+1)).option;
			if (currentFilters[i].getValue().equals(op.getValue())) {
			    list.setSelectedIndex(j+1);
			    break;
			}
		    }
		}
	    }
	    
	    // Create a listener for the list box
	    listListener = new ActionListener() {
		    OptionHolder lastSelected = (OptionHolder)list.getSelectedItem();
		    public void actionPerformed(ActionEvent ae) {
			OptionHolder oh = (OptionHolder)list.getSelectedItem();
			if (oh == lastSelected) {
			    return;
			}
			lastSelected = oh;
			Filter currentFilter = getCurrentFilter();
			if (currentFilter != null) {
			    query.removeFilter(currentFilter);
			}
			if (oh != emptySelection) {
			    Option o = oh.option;
			    System.out.println(o);
			    if (o.getValue() != null) {
				Filter newFilter = new BasicFilter(o.getFieldFromContext(),
								   o.getTableConstraintFromContext(),
								   o.getKeyFromContext(),
								   "=",
								   o.getValue(),
								   o.getHandlerFromContext());
				System.out.println(newFilter);
				query.addFilter(newFilter);
			    }
			    assignPushOptions(o.getPushActions());
			}
			
			else {
			    unassignPushOptions();
			}
		    }
		};
	    list.addActionListener(listListener);
	    	    
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.EAST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.WEST);
	    JPanel internalPanel = new JPanel();
	    internalPanel.setLayout(new BoxLayout(internalPanel, BoxLayout.LINE_AXIS));
	    internalPanel.setOpaque(false);
	    JLabel filterNameLabel = new JLabel(fd.getDisplayName());
	    //filterNameLabel.setMinimumSize(new Dimension(80,25));
	    //filterNameLabel.setPreferredSize(new Dimension(80,25));
	    internalPanel.add(filterNameLabel);
	    internalPanel.add(Box.createRigidArea(new Dimension(10,10)));
	    internalPanel.add(list);
	    internalPanel.add(Box.createHorizontalGlue());
	    add(internalPanel, BorderLayout.CENTER);
	    setMaximumSize(new Dimension(6000,25));
	}
	
	    

    }


    class ToDoMessage extends FilterEditor {
	public ToDoMessage(String message) {
	    super(new BorderLayout());
	    setOpaque(false);
	    setMaximumSize(new Dimension(6000,25));
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.WEST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.EAST);
	    add(new JLabel("<html><body>To do - <font color=\"red\">"+message+"</font></body></html>"), BorderLayout.CENTER);
	}
    }

    class BooleanFilterEditor extends FilterEditor {

	Query query;
	FilterDescription fd;
	
	private JRadioButton require = new JRadioButton("Require");
	private JRadioButton exclude = new JRadioButton("Exclude");
	private JRadioButton ignore  = new JRadioButton("Ignore");
	private JComboBox fieldSelect = null;

	private String excludeFilterType = null;
	private String requireFilterType = null;
	
	private ActionListener fieldSelectListener = null;

	String[] myIDs;

	public void setOptions(Option[] options) {
	    if (fieldSelect == null) {
		return;
	    }
	    fieldSelect.removeActionListener(fieldSelectListener);
	    fieldSelect.removeAllItems();
	    myIDs = new String[options.length];
	    for (int i = 0; i < options.length; i++) {
		Option o = options[i];
		fieldSelect.addItem(new OptionHolder(o));
		myIDs[i] = (options[i].getFieldFromContext()+
			    options[i].getKeyFromContext()+
			    options[i].getTableConstraintFromContext());
	    }
	    fieldSelect.addActionListener(fieldSelectListener);
	    fieldSelect.setSelectedIndex(0);	    
	}
	
	public BooleanFilter getCurrentFilter() {
	    Filter[] filters = query.getFilters();
	    for (int i = 0; i < filters.length; i++) {
		for (int j = 0; j < myIDs.length; j++) {
		    String filterID = filters[i].getField()+filters[i].getKey()+filters[i].getTableConstraint();
		    if (filterID.equals(myIDs[j]) && filters[i] instanceof BooleanFilter) {
			return (BooleanFilter)filters[i];
		    }
		}
	    }
	    return null;
	}

	public BooleanFilterEditor(Query theQuery, FilterDescription filterDescription) {
	    super(new BorderLayout());
	    setBackground(Color.WHITE);
	    query = theQuery;
	    fd = filterDescription;
	    //final String myID = fd.getField()+fd.getKey()+fd.getTableConstraint();
	    if ("boolean".equals(fd.getType())) {
		requireFilterType = BooleanFilter.isNotNULL;
		excludeFilterType = BooleanFilter.isNULL;
	    } else if ("boolean_num".equals(fd.getType())) {
		requireFilterType = BooleanFilter.isNotNULL_NUM;
		excludeFilterType = BooleanFilter.isNULL_NUM;
	    } else if ("boolean_list".equals(fd.getType())) {
		requireFilterType = BooleanFilter.isNotNULL;
		excludeFilterType = BooleanFilter.isNULL;
		fieldSelect = new JComboBox();
		Option[] options = fd.getOptions();
		myIDs = new String[options.length];
		for (int i = 0; i < options.length; i++) {
		    fieldSelect.addItem(new OptionHolder(options[i]));
		    myIDs[i] = (options[i].getFieldFromContext()+
				options[i].getKeyFromContext()+
				options[i].getTableConstraintFromContext());
		}
		// By default there is no selected index, this causes errors
		// so select the first in the list by default instead.
		fieldSelect.setSelectedIndex(0);
	    } else {
		System.out.println("Don't understand type "+fd.getType()+" in the current impl.");
		return;
	    }
	    
	    if (fieldSelect == null) {
		// Single option only
		myIDs = new String[1];
		myIDs[0] = fd.getField()+fd.getKey()+fd.getTableConstraint();
	    }
	    
	    // See if there's a filter with this key in the current query
	    Filter[] filters = query.getFilters();
	    ignore.setSelected(true);
	    for (int i = 0; i < filters.length; i++) {
		for (int j = 0; j < myIDs.length; j++) {
		    String filterID = filters[i].getField()+filters[i].getKey()+filters[i].getTableConstraint();
		    if (filterID.equals(myIDs[j]) && filters[i] instanceof BooleanFilter) {
			BooleanFilter bf = (BooleanFilter)filters[i];
			if (bf.getQualifier().equals(excludeFilterType)) {
			    exclude.setSelected(true);
			    ignore.setSelected(false);
			}
			else if (bf.getQualifier().equals(requireFilterType)) {
			    require.setSelected(true);
			    ignore.setSelected(false);
			}
			if (fieldSelect != null) {
			    fieldSelect.setSelectedIndex(j);
			}
			break;
		    }
		}
	    }
	    
	    // Register item listener for the field selector here...
	    if (fieldSelect != null) {
		fieldSelectListener = new ActionListener() {
			OptionHolder lastSelectedItem = null;
			public void actionPerformed(ActionEvent ae) {
			    OptionHolder oh = (OptionHolder)fieldSelect.getSelectedItem();
			    if (oh == lastSelectedItem) {
				return;
			    }
			    if (ignore.isSelected()) {
				return;
			    }
			    String filterType = requireFilterType;
			    if (exclude.isSelected()) {
				filterType = excludeFilterType;
			    }
			    BooleanFilter oldFilter = getCurrentFilter();
			    
			    QueryFilterSettings settings = fd;
			    if (fieldSelect != null) {
				settings = ((OptionHolder)fieldSelect.getSelectedItem()).option;
				//System.out.println(settings);
			    }
			    BooleanFilter newFilter = new BooleanFilter(settings.getFieldFromContext(),
									settings.getTableConstraintFromContext(),
									settings.getKeyFromContext(),
									filterType,
									settings.getHandlerFromContext());
			    if (oldFilter != null) {
				query.replaceFilter(oldFilter, newFilter);
			    }
			    else {
				query.addFilter(newFilter);
			    }
			    lastSelectedItem = oh;
			}
		    };
		fieldSelect.addActionListener(fieldSelectListener);
		// TODO - add item listener!
	    }
			

	    // Register listeners for the radio buttons
	    require.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			if (require.isSelected()) {
			    if (ignore.isSelected()) {
				ignore.setSelected(false);
			    }
			    if (exclude.isSelected()) {
				exclude.setSelected(false);
			    }
			    BooleanFilter oldFilter = getCurrentFilter();
			    if (oldFilter != null) {
				BooleanFilter newFilter = new BooleanFilter(oldFilter.getField(),
									    oldFilter.getTableConstraint(),
									    oldFilter.getKey(),
									    requireFilterType,
									    oldFilter.getHandler());
				query.replaceFilter(oldFilter, newFilter);
				return;
			    }
			    else {
				QueryFilterSettings settings = fd;
				if (fieldSelect != null) {
				    settings = ((OptionHolder)fieldSelect.getSelectedItem()).option;
				    //System.out.println(settings);
				}
				BooleanFilter newFilter = new BooleanFilter(settings.getFieldFromContext(),
									    settings.getTableConstraintFromContext(),
									    settings.getKeyFromContext(),
									    requireFilterType,
									    settings.getHandlerFromContext());
				query.addFilter(newFilter);
			    }
			}
			else {
			    if (ignore.isSelected() == false) {
				ignore.setSelected(true);
			    }
			}
		    }
		});
	    exclude.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			if (exclude.isSelected()) {
			    if (require.isSelected()) {
				require.setSelected(false);
			    }
			    if (ignore.isSelected()) {
				ignore.setSelected(false);
			    }
			    BooleanFilter oldFilter = getCurrentFilter();
			    if (oldFilter != null) {
				BooleanFilter newFilter = new BooleanFilter(oldFilter.getField(),
									    oldFilter.getTableConstraint(),
									    oldFilter.getKey(),
									    excludeFilterType,
									    oldFilter.getHandler());
				query.replaceFilter(oldFilter, newFilter);
				return;
			    }
			    else {
				// Didn't find a matching filter, create a new one
				QueryFilterSettings settings = fd;
				if (fieldSelect != null) {
				    settings = ((OptionHolder)fieldSelect.getSelectedItem()).option;
				}
				BooleanFilter newFilter = new BooleanFilter(settings.getFieldFromContext(),
									    settings.getTableConstraintFromContext(),
									    settings.getKeyFromContext(),
									    excludeFilterType,
									    settings.getHandlerFromContext());
				query.addFilter(newFilter);
			    }
			}
			else {
			    if (ignore.isSelected() == false) {
				ignore.setSelected(true);
			    }
			}
		    }
		});
	    ignore.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			if (ignore.isSelected()) {
			    if (require.isSelected()) {
				require.setSelected(false);
			    }
			    if (exclude.isSelected()) {
				exclude.setSelected(false);
			    }
			    BooleanFilter oldFilter = getCurrentFilter();
			    if (oldFilter != null) {
				query.removeFilter(oldFilter);
			    }
			}
			else if (exclude.isSelected() == false &&
				 require.isSelected() == false &&
				 ignore.isSelected() == false) {
			    ignore.setSelected(true);
			}
			      
		    }
		});

	    JPanel choicePanel = new JPanel(new GridLayout(1,2));
	    choicePanel.setOpaque(false);
	    JLabel label = new JLabel(fd.getDisplayName());
	    label.setBackground(Color.WHITE);
	    label.setOpaque(false);
	    require.setOpaque(false);
	    exclude.setOpaque(false);
	    ignore.setOpaque(false);
	    JPanel buttonPanel = new JPanel(new GridLayout(1,3));
	    buttonPanel.setOpaque(false);
	    if (fieldSelect == null) {
		choicePanel.add(label);
	    }
	    else {
		choicePanel.add(fieldSelect);
	    }
	    buttonPanel.add(require);
	    buttonPanel.add(exclude);
	    buttonPanel.add(ignore);
	    choicePanel.add(buttonPanel);
	    add(choicePanel, BorderLayout.CENTER);
	    add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.WEST);
	    add(Box.createRigidArea(new Dimension(10,10)),
	    BorderLayout.EAST);	    
	    	    
	    setMaximumSize(new Dimension(6000,25));
	}

    }
    
    class TextFilterEditor extends FilterEditor {
	
	Query query;
	FilterDescription fd;

	public TextFilterEditor(Query theQuery, FilterDescription filterDescription) {
	    super(new BorderLayout());
	    query = theQuery;
	    fd = filterDescription;
	    final String myID = fd.getField()+fd.getKey()+fd.getTableConstraint();
	    JLabel label = new JLabel(fd.getDisplayName());
	    label.setBackground(Color.WHITE);
	    final JTextField field = new JTextField();
	    //field.setBackground(ShadedLabel.TAVERNA_GREEN);
	    //field.setOpaque(true);
	    setBackground(Color.WHITE);
	    JPanel inputPanel = new JPanel(new GridLayout(1,3));
	    inputPanel.setOpaque(false);
	    inputPanel.add(label);
	    inputPanel.add(field);
	    JButton clearButton = new JButton("Clear");
	    clearButton.setOpaque(false);
	    clearButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			field.setText("");
		    }
		});
	    inputPanel.add(clearButton);
	    add(inputPanel, BorderLayout.CENTER);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.WEST);
	    add(Box.createRigidArea(new Dimension(10,10)),
		BorderLayout.EAST);
	    setMaximumSize(new Dimension(6000,20));
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
