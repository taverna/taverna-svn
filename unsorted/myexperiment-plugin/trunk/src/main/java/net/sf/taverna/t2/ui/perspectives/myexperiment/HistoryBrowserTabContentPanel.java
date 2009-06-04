package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
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
  
  
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;
  
  // COMPONENTS
  private JPanel jpPreviewHistory;
  private JPanel jpSearchHistory;
  private JPanel jpTagSearchHistory;
  private JPanel jpDownloadedItemsHistory;
  private JPanel jpOpenedItemsHistory;
  private JPanel jpCommentedOnHistory;
  
  
  public HistoryBrowserTabContentPanel(MainComponent component, MyExperimentClient client, Logger logger)
  {
    super();
    
    // set main variables to ensure access to myExperiment, logger and the parent component
    this.pluginMainComponent = component;
    this.myExperimentClient = client;
    this.logger = logger;
    
    this.initialiseUI();
    this.refreshAllData();
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
    JPanel jpPreviewHistoryBox = generateContentBox("Preview History", jpPreviewHistory);
    JPanel jpSearchHistoryBox = generateContentBox("Search History", jpSearchHistory);
    JPanel jpTagSearchHistoryBox = generateContentBox("Tag Search History", jpTagSearchHistory);
    JPanel jpDownloadedItemsHistoryBox = generateContentBox("Downloaded Items", jpDownloadedItemsHistory);
    JPanel jpOpenedItemsHistoryBox = generateContentBox("Opened Items", jpOpenedItemsHistory);
    JPanel jpCommentedOnHistoryBox = generateContentBox("Commented On", jpCommentedOnHistory);
    
    
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
  
  
  /**
   * Used to refresh all boxes at a time (for example at launch time).
   */
  private void refreshAllData()
  {
    this.refreshPreviewHistory();
    this.refreshSearchHistory();
    this.refreshTagSearchHistory();
  }
  
  
  /**
   * This helper can be called externally to refresh the preview history.
   * Is used inside ResourcePreviewBrowser every time a new item is previewed.
   * Also useful, when an option to 'clear preview history' is used in the Preferences window.
   */
  public void refreshPreviewHistory()
  {
    this.jpPreviewHistory.removeAll();
    populatePreviewHistory();
  }
  
  
  /**
   * Retrieves preview history data from Preview Browser and populates the relevant panel.
   */
  private void populatePreviewHistory()
  {
    List<Resource> lPreviewedItems = this.pluginMainComponent.getPreviewBrowser().getPreviewHistory();
    
    if (lPreviewedItems.size() > 0)
    {
      for (int i = lPreviewedItems.size() - 1; i >= 0; i--)
      {
        Resource r = lPreviewedItems.get(i);
        JClickableLabel lResource = Util.generateClickableLabelFor(r, this.pluginMainComponent.getPreviewBrowser());
        this.jpPreviewHistory.add(lResource);
      }
    }
    else {
      this.jpPreviewHistory.add(Util.generateNoneTextLabel("No items were previewed yet"));
    }
    
    // make sure that the component is updated after population
    this.jpPreviewHistory.revalidate();
    this.jpPreviewHistory.repaint();
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
    
    if (lSearchHistory.size() > 0)
    {
      this.jpSearchHistory.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.NORTHWEST;
      
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
      this.jpSearchHistory.add(Util.generateNoneTextLabel(SearchResultsPanel.NO_SEARCHES_STATUS));
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
                                                   new ImageIcon(MyExperimentPerspective.getLocalIconURL(t.getItemType())),
                                                   SwingConstants.LEFT,
                                                   t.getItemTypeName() + ": " + t.getTagName()
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
