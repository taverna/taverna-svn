// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.SearchEngine;

/*
 * @author Jiten Bhagat
 */
public class TagBrowserTabContentPanel extends JPanel implements ActionListener //, ChangeListener, HyperlinkListener
{
  // CONSTANTS
  private static final double TAG_CLOUD_BALANCE = 0.35;
  private static final double TAG_SIDEBAR_BALANCE = 0.4;
  
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;
	
  
  // COMPONENTS
  private JSplitPane spMainSplitPane;
  private JSplitPane spTagCloudSplitPane;
  private TagCloudPanel jpMyTags;
  private TagCloudPanel jpAllTags;
  private SearchResultsPanel jpTagSearchResults;
  private JButton bLoginToSeeMyTags;
  private JPanel jpLoginToSeeMyTags;
  
  // last tag for which the search has been made
  private String strCurrentTagCommand;
  
  //Search components 
  private SearchEngine searchEngine;    // The search engine for executing keyword query searches 
  private Vector<Long> vCurrentSearchThreadID; // This will keep ID of the current search thread (there will only be one such thread)
  
	
	public TagBrowserTabContentPanel(MainComponent component, MyExperimentClient client, Logger logger)
	{
    super();
    
    // set main variables to ensure access to myExperiment, logger and the parent component
	  this.pluginMainComponent = component;
	  this.myExperimentClient = client;
	  this.logger = logger;
	  
	  // no tag searches have been done yet
	  this.strCurrentTagCommand = null;
		
		this.initialiseUI();
		
	  // initialise the search engine
    vCurrentSearchThreadID = new Vector<Long>(1);
    vCurrentSearchThreadID.add(null);  // this is just a placeholder, so that it's possible to update this value instead of adding new ones later
    this.searchEngine = new SearchEngine(vCurrentSearchThreadID, true, jpTagSearchResults, pluginMainComponent, myExperimentClient, logger);
		
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	spTagCloudSplitPane.setDividerLocation(TAG_CLOUD_BALANCE);
		      spMainSplitPane.setDividerLocation(TAG_SIDEBAR_BALANCE);
		    	spMainSplitPane.setOneTouchExpandable(true);
				  spMainSplitPane.setDoubleBuffered(true);
		    }
		});
	}
	
	
	private void initialiseUI()
	{
    // This panel will be used when the user is not logged in - i.e. when can't show "My Tags";
	  // log-in button will be shown instead
    this.bLoginToSeeMyTags = new JButton("Login to see your tags", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("login_icon")));
    this.bLoginToSeeMyTags.addActionListener(this);
    
    this.jpLoginToSeeMyTags = new JPanel();
    this.jpLoginToSeeMyTags.setLayout(new GridBagLayout());
    this.jpLoginToSeeMyTags.add(this.bLoginToSeeMyTags);
	  
	  
	  // Create the tag clouds
	  this.jpMyTags = new TagCloudPanel("My Tags", TagCloudPanel.TAGCLOUD_TYPE_USER, this, pluginMainComponent, myExperimentClient, logger);
	  this.jpAllTags = new TagCloudPanel("All Tags", TagCloudPanel.TAGCLOUD_TYPE_GENERAL, this, pluginMainComponent, myExperimentClient, logger);
	  
	  // add the two tag clouds to the left-hand side sidebar
	  this.spTagCloudSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    this.spTagCloudSplitPane.setTopComponent(this.myExperimentClient.isLoggedIn() ? jpMyTags : jpLoginToSeeMyTags);
    this.spTagCloudSplitPane.setBottomComponent(jpAllTags);
    
    // create panel for tag search results
    this.jpTagSearchResults = new SearchResultsPanel(this, pluginMainComponent, myExperimentClient, logger);
    
    this.spMainSplitPane = new JSplitPane();
    this.spMainSplitPane.setLeftComponent(spTagCloudSplitPane);
    this.spMainSplitPane.setRightComponent(this.jpTagSearchResults);
    
    this.setLayout(new BorderLayout());
    this.add(this.spMainSplitPane, BorderLayout.CENTER);
  }
	
	
	// this helper is called when the user logs in / out to swap the
	// view accordingly; 'refresh' on "My Tags" panel should be called
	// immediately after changing the view
	public void setMyTagsShown(boolean bShow)
	{
	  if (bShow) {
	    this.spTagCloudSplitPane.setTopComponent(this.jpMyTags);
	  }
	  else {
	    this.spTagCloudSplitPane.setTopComponent(this.jpLoginToSeeMyTags);
	  }
	  
	  // in either case apply element balance again
	  this.spTagCloudSplitPane.setDividerLocation(TAG_CLOUD_BALANCE);
	}
	
	
	public void refresh()
	{
	  if (this.myExperimentClient.isLoggedIn()) {
	    // "My Tags" are only accessible when the user has logged in
	    this.jpMyTags.refresh();
	  }
	  this.jpAllTags.refresh();
	}
	
	
	// re-executes the search for the most recent tag
	// (if tag searches have already been done before)
	public void rerunLastTagSearch()
	{
	  if (this.strCurrentTagCommand != null) {
	    this.actionPerformed(new ActionEvent(this.jpAllTags, 0, this.strCurrentTagCommand));
	  }
	}
	
	
	public TagCloudPanel getMyTagPanel()
	{
	  return (this.jpMyTags);
	}
	
	
	public TagCloudPanel getAllTagPanel()
  {
    return (this.jpAllTags);
  }
	
	
	public SearchResultsPanel getTagSearchResultPanel()
	{
	  return (this.jpTagSearchResults);
	}
	
	
	public void actionPerformed(ActionEvent e)
	{
	  if (e.getSource().equals(this.jpTagSearchResults.bRefresh))
	  {
      // disable clearing results and re-run last tag search
	    this.getTagSearchResultPanel().bClear.setEnabled(false);
	    this.rerunLastTagSearch();
    }
    else if (e.getSource().equals(this.jpTagSearchResults.bClear))
    {
      // clear last search and disable re-running it
      this.strCurrentTagCommand = null;
      vCurrentSearchThreadID.set(0, null);
      this.getTagSearchResultPanel().clear();
      this.getTagSearchResultPanel().setStatus(SearchResultsPanel.NO_SEARCHES_STATUS);
      this.getTagSearchResultPanel().bClear.setEnabled(false);
      this.getTagSearchResultPanel().bRefresh.setEnabled(false);
    }
    else if (e.getSource().equals(this.jpMyTags) || e.getSource().equals(this.jpAllTags)) {
      // one of the tags was clicked as a hyperlink
      if (e.getActionCommand().startsWith("tag:")) {
        // record the tag search command and run the search
        this.strCurrentTagCommand = e.getActionCommand();
        this.searchEngine.searchAndPopulateResults(strCurrentTagCommand);
      }
    }
    else if ((e.getSource() instanceof JClickableLabel || e.getSource() instanceof TagCloudPanel) 
             && e.getActionCommand().startsWith("tag:"))
    {
      // one of the tags was clicked as a JClickableLabel
      // (record the tag search command and run the search)
      this.strCurrentTagCommand = e.getActionCommand();
      this.searchEngine.searchAndPopulateResults(strCurrentTagCommand);
    }
    else if (e.getSource().equals(this.bLoginToSeeMyTags))
    {
      // set the return "link"
      this.pluginMainComponent.getMyStuffTab().cTabContentComponentToSwitchToAfterLogin = this;
      
      // switch to login tab
      this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getMyStuffTab());
    }
	}
	
	
	/*
	public void refresh() {
		this.refreshCloud();
		this.refreshResults();
	}*/
	
	
	/*
	public void refreshResults() {
		if (this.currentTagName != null && !this.currentTagName.equals("")) {
			this.resultsStatusLabel.setText("Searching for workflows with tag '" + this.currentTagName + "' from myExperiment...");
			
			// Make call to myExperiment API in a different thread
			// (then use SwingUtilities.invokeLater to update the UI when ready).
			new Thread("Perform tag search for TagsBrowserPanel") {
				public void run() {
					logger.debug("Performing tag search for Tags Browser tab");

					try {
						tagSearchResults = client.getTagResults(currentTagName);

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								repopulateResults();
							}
						});
					} catch (Exception ex) {
						logger.error("Failed to get tag results from myExperiment", ex);
					}
				}
			}.start();
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					clearResults();
				}
			});
		}
	}*/
  
	/*
	public void repopulate() {
		this.repopulateCloud();
		this.repopulateResults();
	}
	*/
	
	
	/*
	public void repopulateResults() {
		logger.debug("Repopulating tag results pane");

		this.resultsStatusLabel.setText(this.tagSearchResults.getWorkflows().size() + " workflows found for tag '" + this.currentTagName + "'");
		
		this.workflowsListPanel.setWorkflows(this.tagSearchResults.getWorkflows());
		
		this.resultsClearButton.setEnabled(true);
		this.resultsRefreshButton.setEnabled(true);
		
		this.revalidate();
	}*/
	
	/*
	public void clear() {
		this.clearCloud();
		this.clearResults();
	}*/
	
	/*
	public void clearCloud() {
		this.cloudStatusLabel.setText("");
		this.cloudTextPane.setDocument(new HTMLDocument());
		this.revalidate();
	}*/
	
	/*
	public void clearResults() {
		this.resultsStatusLabel.setText("");
		this.resultsClearButton.setEnabled(false);
		this.resultsRefreshButton.setEnabled(false);
		this.workflowsListPanel.clear();
		this.revalidate();
	}*/
	
}
