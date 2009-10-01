package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Tag;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.SearchEngine.QuerySearchInstance;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

public class HistoryBrowserTabContentPanel extends JPanel implements ActionListener
{
  // CONSTANTS
  public static final int DOWNLOADED_ITEMS_HISTORY_LENGTH = 50;
  public static final int OPENED_ITEMS_HISTORY_LENGTH = 50;
  public static final int COMMENTED_ON_ITEMS_HISTORY_LENGTH = 50;
  
  public static final int PREVIEWED_ITEMS_HISTORY = 0;
  public static final int DOWNLOADED_ITEMS_HISTORY = 1;
  public static final int OPENED_ITEMS_HISTORY = 2;
  public static final int COMMENTED_ON_ITEMS_HISTORY = 3;
  
  
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;
  
  // STORAGE
  private ArrayList<Resource> lDownloadedItems;
  private ArrayList<Resource> lOpenedItems;
  private ArrayList<Resource> lCommentedOnItems;
  
  // COMPONENTS
  private JPanel jpPreviewHistory;
  private JPanel jpSearchHistory;
  private JPanel jpTagSearchHistory;
  private JPanel jpDownloadedItemsHistory;
  private JPanel jpOpenedItemsHistory;
  private JPanel jpCommentedOnHistory;
  
  
  @SuppressWarnings("unchecked")
  public HistoryBrowserTabContentPanel(MainComponent component, MyExperimentClient client, Logger logger)
  {
    super();
    
    // set main variables to ensure access to myExperiment, logger and the parent component
    this.pluginMainComponent = component;
    this.myExperimentClient = client;
    this.logger = logger;
    
    
    // initialise downloaded items history
    String strDownloadedItemsHistory = (String)myExperimentClient.getSettings().get(MyExperimentClient.INI_DOWNLOADED_ITEMS_HISTORY);
    if (strDownloadedItemsHistory != null) {
      Object oDownloadedItemsHistory = Base64.decodeToObject(strDownloadedItemsHistory);
      this.lDownloadedItems = (ArrayList<Resource>)oDownloadedItemsHistory;
    }
    else {
      this.lDownloadedItems = new ArrayList<Resource>();
    }
    
    // initialise opened items history
    String strOpenedItemsHistory = (String)myExperimentClient.getSettings().get(MyExperimentClient.INI_OPENED_ITEMS_HISTORY);
    if (strOpenedItemsHistory != null) {
      Object oOpenedItemsHistory = Base64.decodeToObject(strOpenedItemsHistory);
      this.lOpenedItems = (ArrayList<Resource>)oOpenedItemsHistory;
    }
    else {
      this.lOpenedItems = new ArrayList<Resource>();
    }
    
    // initialise history of the items that were commented on
    String strCommentedItemsHistory = (String)myExperimentClient.getSettings().get(MyExperimentClient.INI_COMMENTED_ITEMS_HISTORY);
    if (strCommentedItemsHistory != null) {
      Object oCommentedItemsHistory = Base64.decodeToObject(strCommentedItemsHistory);
      this.lCommentedOnItems = (ArrayList<Resource>)oCommentedItemsHistory;
    }
    else {
      this.lCommentedOnItems = new ArrayList<Resource>();
    }
    
    
    this.initialiseUI();
    this.refreshAllData();
  }
  
  private void confirmHistoryDelete(final int id, String strBoxTitle) {
	if (JOptionPane.showConfirmDialog(null, "This will the " + strBoxTitle.toLowerCase() + " list.\nDo you want to proceed?", 
        "myExperiment Plugin - Confirmation Required", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { 
  	  switch (id) {
  		case 1:
          pluginMainComponent.getPreviewBrowser().clearPreviewHistory();
  		  break;
  		case 2:
    	  pluginMainComponent.getSearchTab().getSearchHistory().clear();
          pluginMainComponent.getSearchTab().updateSearchHistory();
          break;
  		case 3:
          pluginMainComponent.getTagBrowserTab().getTagSearchHistory().clear();
  		  break;
  		case 4: clearDownloadedItemsHistory(); break;
  		case 5: clearOpenedItemsHistory(); break;
  		case 6: clearCommentedOnItemsHistory(); break;
  	  }
  	  refreshAllData();
	}
  }
  
  private JPanel addSpecifiedPanel(final int id, final String strBoxTitle, JPanel jPanel) {
	JPanel jpTemp = new JPanel();
	jpTemp.setLayout(new BorderLayout());
	jpTemp.add(generateContentBox(strBoxTitle, jPanel), BorderLayout.CENTER);
	JButton bClear = new JButton("Clear " + strBoxTitle, WorkbenchIcons.configureIcon);
	bClear.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
		confirmHistoryDelete(id, strBoxTitle);
	  }
	});

	jpTemp.add(bClear, BorderLayout.SOUTH);
	return jpTemp;
  }
  
  private void initialiseUI()
  {
    // create helper text
    ShadedLabel lHelper = new ShadedLabel("All history sections are local to myExperiment plugin usage." +
        " Detailed history of your actions on myExperiment is available in your profile on myExperiment.", ShadedLabel.BLUE);
    
    // create all individual content holder panels
    this.jpPreviewHistory = new JPanel();
    this.jpTagSearchHistory = new JPanel();
    this.jpSearchHistory = new JPanel();
    this.jpDownloadedItemsHistory = new JPanel();
    this.jpOpenedItemsHistory = new JPanel();
    this.jpCommentedOnHistory = new JPanel();
    
    // create standard boxes for each content holder panels
    JPanel jpPreviewHistoryBox = addSpecifiedPanel(1, "Preview History", jpPreviewHistory);
    JPanel jpSearchHistoryBox = addSpecifiedPanel(2, "Search History", jpSearchHistory);
    JPanel jpTagSearchHistoryBox = addSpecifiedPanel(3, "Tag Search History", jpTagSearchHistory);
    JPanel jpDownloadedItemsHistoryBox = addSpecifiedPanel(4, "Downloaded Items", jpDownloadedItemsHistory);
    JPanel jpOpenedItemsHistoryBox = addSpecifiedPanel(5, "Opened Items", jpOpenedItemsHistory);
    JPanel jpCommentedOnHistoryBox = addSpecifiedPanel(6, "Commented On", jpCommentedOnHistory);

    // PUT BOXES TOGETHER
    JPanel jpMain = new JPanel();
    jpMain.setLayout(new GridLayout(2, 3));
    
    jpMain.add(jpPreviewHistoryBox);
    jpMain.add(jpSearchHistoryBox);
    jpMain.add(jpTagSearchHistoryBox);
    jpMain.add(jpDownloadedItemsHistoryBox);
    jpMain.add(jpOpenedItemsHistoryBox);
    jpMain.add(jpCommentedOnHistoryBox);
    
    
    // PUT EVERYTHING TOGETHER
    this.setLayout(new BorderLayout());
    this.add(lHelper, BorderLayout.NORTH);
    this.add(jpMain, BorderLayout.CENTER);
  }
  
  
  public ArrayList<Resource> getDownloadedItemsHistoryList()
  {
    return (this.lDownloadedItems);
  }
  
  public void clearDownloadedItemsHistory()
  {
    this.lDownloadedItems.clear();
  }
  
  public ArrayList<Resource> getOpenedItemsHistoryList()
  {
    return (this.lOpenedItems);
  }
  
  public void clearOpenedItemsHistory()
  {
    this.lOpenedItems.clear();
  }
  
  public ArrayList<Resource> getCommentedOnItemsHistoryList()
  {
    return (this.lCommentedOnItems);
  }
  
  public void clearCommentedOnItemsHistory()
  {
    this.lCommentedOnItems.clear();
  }
  
  
  /**
   * Used to refresh all boxes at a time (for example at launch time).
   */
  private void refreshAllData()
  {
    this.refreshHistoryBox(PREVIEWED_ITEMS_HISTORY);
    this.refreshHistoryBox(DOWNLOADED_ITEMS_HISTORY);
    this.refreshHistoryBox(OPENED_ITEMS_HISTORY);
    this.refreshHistoryBox(COMMENTED_ON_ITEMS_HISTORY);
    this.refreshSearchHistory();
    this.refreshTagSearchHistory();
  }
  
  
  /**
   * This helper can be called externally to refresh the following history boxes:
   * previewed items history, downloaded items history, opened items history and
   * the history of items that were commented on.
   * 
   * Is used inside ResourcePreviewBrowser and MainComponent every time a relevant action occurs.
   * Also useful, when an option to 'clear preview history' is used in the Preferences window for.
   * a particular history type.
   */
  public void refreshHistoryBox(int historyType)
  {
    switch (historyType) {
      case PREVIEWED_ITEMS_HISTORY:
        this.jpPreviewHistory.removeAll();
        populateHistoryBox(this.pluginMainComponent.getPreviewBrowser().getPreviewHistory(),
                           this.jpPreviewHistory,
                           "No items were previewed yet");
        break;
      case DOWNLOADED_ITEMS_HISTORY:
        this.jpDownloadedItemsHistory.removeAll();
        populateHistoryBox(this.lDownloadedItems, this.jpDownloadedItemsHistory, "No items were downloaded yet");
        break;
      case OPENED_ITEMS_HISTORY:
        this.jpOpenedItemsHistory.removeAll();
        populateHistoryBox(this.lOpenedItems, this.jpOpenedItemsHistory, "No items were opened yet");
        break;
      case COMMENTED_ON_ITEMS_HISTORY:
        this.jpCommentedOnHistory.removeAll();
        populateHistoryBox(this.lCommentedOnItems, this.jpCommentedOnHistory, "You didn't comment on any items yet");
        break;
    }
  }
  
  
  /**
   * Retrieves history data from a relevant list and populates the specified panel with it.
   * All listed items will be resources that can be opened by Preview Browser.
   */
  private void populateHistoryBox(List<Resource> lHistory, JPanel jpPanelToPopulate, String strLabelIfNoItems)
  {
    if (lHistory.size() > 0)
    {
      for (int i = lHistory.size() - 1; i >= 0; i--)
      {
        Resource r = lHistory.get(i);
        JClickableLabel lResource = Util.generateClickableLabelFor(r, this.pluginMainComponent.getPreviewBrowser());
        jpPanelToPopulate.add(lResource);
      }
    }
    else {
      jpPanelToPopulate.add(Util.generateNoneTextLabel(strLabelIfNoItems));
    }
    
    // make sure that the component is updated after population
    jpPanelToPopulate.revalidate();
    jpPanelToPopulate.repaint();
  }
  
  
  /**
   * This helper can be called externally to refresh the search history.
   * Is used inside SearchTabContentPanel every time a new item is added to search history.
   */
  public void refreshSearchHistory()
  {
    this.jpSearchHistory.removeAll();
    populateSearchHistory();
  }
  
  
  /**
   * Retrieves search history data from SearchTabContentPanel and populates the relevant panel.
   */
  private void populateSearchHistory()
  {
    List<QuerySearchInstance> lSearchHistory = this.pluginMainComponent.getSearchTab().getSearchHistory();
    
    // prepare layout
    this.jpSearchHistory.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    
    if (lSearchHistory.size() > 0)
    {
      for (int i = lSearchHistory.size() - 1; i >= 0; i--)
      {
        QuerySearchInstance qsiCurrent = lSearchHistory.get(i);
        JClickableLabel jclCurrentEntryLabel = new JClickableLabel(qsiCurrent.getSearchQuery(), SearchTabContentPanel.SEARCH_FROM_HISTORY + ":" + i,
            this, WorkbenchIcons.findIcon, SwingUtilities.LEFT, qsiCurrent.toString());
        JLabel jlCurrentEntrySettings = new JLabel(qsiCurrent.detailsAsString());
        jlCurrentEntrySettings.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 0));
        
        JPanel jpCurrentSearchHistoryEntry = new JPanel();
        jpCurrentSearchHistoryEntry.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        jpCurrentSearchHistoryEntry.add(jclCurrentEntryLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        jpCurrentSearchHistoryEntry.add(jlCurrentEntrySettings, c);
        
        c.gridy = lSearchHistory.size() - 1 - i;
        if (i == 0) c.weighty = 1.0;
        this.jpSearchHistory.add(jpCurrentSearchHistoryEntry, c);
      }
    }
    else {
      c.weightx = 1.0;
      c.weighty = 1.0;
      this.jpSearchHistory.add(Util.generateNoneTextLabel(SearchResultsPanel.NO_SEARCHES_STATUS), c);
    }
    
    // make sure that the component is updated after population
    this.jpSearchHistory.revalidate();
    this.jpSearchHistory.repaint();
  }
  
  
  /**
   * This helper can be called externally to refresh the tag search history.
   * Is used inside TagBrowserTabContentPanel every time a new tag is searched for.
   */
  public void refreshTagSearchHistory()
  {
    this.jpTagSearchHistory.removeAll();
    populateTagSearchHistory();
  }
  
  
  /**
   * Retrieves tag search history data from Tag Browser and populates the relevant panel.
   */
  private void populateTagSearchHistory()
  {
    List<Tag> lTagSearchHistory = this.pluginMainComponent.getTagBrowserTab().getTagSearchHistory();
    
    if (lTagSearchHistory.size() > 0)
    {
      for (int i = lTagSearchHistory.size() - 1; i >= 0; i--)
      {
        Tag t = lTagSearchHistory.get(i);
        JClickableLabel lTag = new JClickableLabel(t.getTagName(),
                                                   "tag:" + t.getTagName(),
                                                   this,
                                                   new ImageIcon(MyExperimentPerspective.getLocalIconURL(Resource.TAG)),  // HACK: after deserialization t.getItemType() return "Unknown" type
                                                   SwingConstants.LEFT,
                                                   "Tag: " + t.getTagName()
                                                   );
        this.jpTagSearchHistory.add(lTag);
      }
    }
    else {
      this.jpTagSearchHistory.add(Util.generateNoneTextLabel("No searches by tags have been made yet"));
    }
    
    // make sure that the component is updated after population
    this.jpTagSearchHistory.revalidate();
    this.jpTagSearchHistory.repaint();
  }
  
  
  /**
   * @param strBoxTitle Title of the content box.
   * @param jpContentPanel JPanel which will be populated with history listing.
   * @return Prepared JPanel with a border, title and jpContentPanel wrapped into a scroll pane.
   */
  private static JPanel generateContentBox(String strBoxTitle, JPanel jpContentPanel)
  {
    // set layout for the content panel
    jpContentPanel.setLayout(new BoxLayout(jpContentPanel, BoxLayout.Y_AXIS));
    
    // wrap content panel into a standard scroll pane
    JScrollPane spContent = new JScrollPane(jpContentPanel);
    spContent.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    spContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    spContent.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);
    
    // create the actual box stub with a border which will contain the scroll pane
    JPanel jpBox = new JPanel();
    jpBox.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(2, 2, 2, 2),
        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " " + strBoxTitle + " ")));
    
    jpBox.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    jpBox.add(spContent, c);
    
    return (jpBox);
  }
  
  
  // *** Callback for ActionListener interface ***
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() instanceof JClickableLabel)
    {
      if (e.getActionCommand().startsWith(SearchTabContentPanel.SEARCH_FROM_HISTORY)) {
        // open search tab and start the chosen search
        this.pluginMainComponent.getSearchTab().actionPerformed(e);
        this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getSearchTab());
      }
      else if (e.getActionCommand().startsWith("tag:")) {
        // open tag browser tab and start the chosen tag search
        this.pluginMainComponent.getTagBrowserTab().actionPerformed(e);
        this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getTagBrowserTab());
      }
    }
    
  }
  
}
