package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * A class to show modal dialog boxes with previews of resources.
 * 
 * @author Sergejs Aleksejevs
 */
public class ResourcePreviewBrowser extends JFrame implements ActionListener, HyperlinkListener, ComponentListener
{
  // CONSTANTS
  protected static final int PREFERRED_WIDTH = 750;
  protected static final int PREFERRED_HEIGHT = 600;
  protected static final int PREFERRED_SCROLL = 10;
  protected static final int PREVIEW_HISTORY_LENGTH = 50;
  
  // navigation data
  private int iCurrentHistoryIdx;             // index within the current history
  private ArrayList<String> alCurrentHistory; // current history - e.g. if one opens Page1, then Page2; goes back and opens Page3 - current preview would hold only [Page1, Page3]
  private ArrayList<Resource> alFullHistory;    // all resources that were previewed since application started (will be used by ResourcePreviewHistoryBrowser)
  
  // components for accessing application's main elements
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;
  
  // holder of the data about currently previewed item
  private ResourcePreviewContent rpcContent;
  
  // components of the preview window
  private JPanel jpMain;
  private JPanel jpStatusBar;
  private JLabel lSpinnerIcon;
  private JButton bBack;
  private JButton bForward;
  private JButton bRefresh;
  private JButton bOpenInMyExp;
  private JButton bDownload;
  private JButton bOpenInTaverna;
  private JButton bAddComment;
  private JButton bAddRemoveFavourite;
  private JScrollPane spContentScroller;
  
  // icons
  private ImageIcon iconOpenInMyExp = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("open_in_my_experiment_icon"));
  private ImageIcon iconAddFavourite = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("add_favourite_icon"));
  private ImageIcon iconDeleteFavourite = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("delete_favourite_icon"));
  private ImageIcon iconAddComment = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("add_comment_icon"));
  private ImageIcon iconSpinner = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner"));
  private ImageIcon iconSpinnerStopped = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner_stopped"));
  
  
  public ResourcePreviewBrowser(MainComponent component, MyExperimentClient client, Logger logger)
  {
    super();
    
    // set main variables to ensure access to myExperiment, logger and the parent component
    this.pluginMainComponent = component;
    this.myExperimentClient = client;
    this.logger = logger;
    
    // initialise previewed items history
    String strPreviewedItemsHistory = (String)myExperimentClient.getSettings().get(MyExperimentClient.INI_PREVIEWED_ITEMS_HISTORY);
    if (strPreviewedItemsHistory != null) {
      Object oPreviewedItemsHistory = Base64.decodeToObject(strPreviewedItemsHistory);
      this.alFullHistory = (ArrayList<Resource>)oPreviewedItemsHistory;
    }
    else {
      this.alFullHistory = new ArrayList<Resource>();
    }
    
    // no navigation history at loading
    this.iCurrentHistoryIdx = -1;
    this.alCurrentHistory = new ArrayList<String>();
    
    // set options of the preview dialog box
    this.setIconImage(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("myexp_icon")).getImage());
    this.addComponentListener(this);
    
    this.initialiseUI();
  }
  
  
  /**
   * Accessor method for getting a full history of previewed resources as a list.
   */
  public ArrayList<Resource> getPreviewHistory()
  {
    return (this.alFullHistory);
  }
  
  
  /**
   * As opposed to getPreviewHistory() which returns full history of previewed resources,
   * this helper method only retrieves the current history stack.
   * 
   * Example: if a user was to view the following items - A -> B -> C
   *                                                           B <- C
   *                                                           B -> D,
   * the full history would be [A,C,B,D];
   * current history stack would be [A,B,D] - note how item C was "forgotten" (this works the same way as all web browsers do)
   */
  public List<String> getCurrentPreviewHistory()
  {
    return (this.alCurrentHistory);
  }
  
  
  /**
   * Deletes both 'current history' (the latest preview history stack) and the
   * 'full preview history'. Also, resets the index in the current history,
   * so that the preview browser would not allow using Back-Forward buttons until
   * some new previews are opened.
   */
  public void clearPreviewHistory()
  {
    this.iCurrentHistoryIdx = -1;
    this.alCurrentHistory.clear();
    this.alFullHistory.clear();
  }
  
  
  /**
   * This method is a launcher for the real worker method ('createPreview()')
   * that does all the job.
   * 
   * The purpose of having this method is to manage history. This method is to
   * be called every time when a "new" preview is requested. This will add a new
   * link to the CurrentHistory stack.
   * 
   * Clicks on "Back" and "Forward" buttons will only need to advance the counter
   * of the current position in the CurrentHistory. Therefore, these will directly
   * call 'createPreview()'.
   */
  public void preview(String action)
  {
    // *** History Update ***
    // if this is not the "newest" page in current history, remove all newer ones
    // (that is if the user went "back" and opened some new link from on of the older pages)
    while(alCurrentHistory.size() > iCurrentHistoryIdx + 1)
    {
      alCurrentHistory.remove(alCurrentHistory.size() - 1);
    }
    
    boolean bPreviewNotTheSameAsTheLastOne = true;
    if(alCurrentHistory.size() > 0)
    {
      // will add new page to the history only if it's not the same as the last one!
      if(action.equals(alCurrentHistory.get(alCurrentHistory.size() - 1))) {
        bPreviewNotTheSameAsTheLastOne = false;
      }
      
      // this is not the first page in the history, enable "Back" button (if only this isn't the same page as was the first one);
      // (this, however, is the last page in the history now - so disable "Forward" button)
      bBack.setEnabled(bPreviewNotTheSameAsTheLastOne || alCurrentHistory.size() > 1);
      bForward.setEnabled(false);
    }
    else if (alCurrentHistory.size() == 0) {
      // this is the first preview after application has loaded or since the
      // preview history was cleared - disable both Back and Forward buttons
      bBack.setEnabled(false);
      bForward.setEnabled(false);
    }
    
    // add current preview URI to the history
    if(bPreviewNotTheSameAsTheLastOne)
    {
      iCurrentHistoryIdx++;
      alCurrentHistory.add(action);
    }
    
    // *** Launch Preview ***
    createPreview(action);
  }
  
  private void createPreview(String action)
  {
    // JUST FOR TESTING THE CURRENT_HISTORY OPERATION
    //javax.swing.JOptionPane.showMessageDialog(null, "History idx: " + this.iCurrentHistoryIdx + "\n" + alCurrentHistory.toString());
    
    // show that loading is in progress
    this.setTitle("Loading preview...");
    this.lSpinnerIcon.setIcon(this.iconSpinner);
    
    // disable all action buttons while loading is in progress
    bOpenInMyExp.setEnabled(false);
    bDownload.setEnabled(false);
    bOpenInTaverna.setEnabled(false);
    bAddRemoveFavourite.setEnabled(false);
    bAddComment.setEnabled(false);
    
    
    // Make call to myExperiment API in a different thread
    // (then use SwingUtilities.invokeLater to update the UI when ready).
    final String strAction = action;
    final EventListener self = this;
    
    new Thread("Load myExperiment resource preview content") {
      public void run() {
        logger.debug("Starting to fetch the preview content data");

        try {
          // *** Fetch Data and Create Preview Content ***
          rpcContent = pluginMainComponent.getPreviewFactory().createPreview(strAction, self);
          
          // as all the details about the previewed resource are now known, can store this into full preview history
          // (before that make sure that if the this item was viewed before, it's removed and re-added at the "top" of the list)
          // (also make sure that the history size doesn't exceed the pre-set value)
          alFullHistory.remove(rpcContent.getResource());
          alFullHistory.add(rpcContent.getResource());
          if (alFullHistory.size() > PREVIEW_HISTORY_LENGTH) alFullHistory.remove(0);
          pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.PREVIEWED_ITEMS_HISTORY);
          
          
          // *** Update the Preview Dialog Box when everything is ready ***
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // 'stop' loading action in the status bar and window title
              setTitle(Resource.getResourceTypeName(rpcContent.getResourceType()) + ": " + rpcContent.getResourceTitle());
              lSpinnerIcon.setIcon(iconSpinnerStopped);
              
              // update the state of action buttons in the button bar
              updateButtonBarState(rpcContent);
              
              // wrap received content into a ScrollPane
              spContentScroller = new JScrollPane(rpcContent.getContent());
              spContentScroller.setBorder(BorderFactory.createEmptyBorder());
              spContentScroller.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);
              
              // remove everything from the preview and re-add all components
              // (NB! Removing only CENTER component didn't work properly)
              jpMain.removeAll();
              jpMain.add(jpStatusBar, BorderLayout.NORTH);
              jpMain.add(spContentScroller, BorderLayout.CENTER);
              validate();
              repaint();
            }
          });
        }
        catch (Exception ex) {
          logger.error("Exception on attempt to login to myExperiment:\n", ex);
        }
      }
    }.start();
        
    
    // show the dialog box
    this.setVisible(true);
  }
  
  
  private void initialiseUI()
  {
    // create the STATUS BAR of the preview window
    JPanel jpNavigationButtons = new JPanel();
    bBack = new JButton(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("back_icon")));
    bBack.setToolTipText("Back");
    bBack.addActionListener(this);
    bBack.setEnabled(false);
    jpNavigationButtons.add(bBack);
    
    bForward = new JButton(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("forward_icon")));
    bForward.setToolTipText("Forward");
    bForward.addActionListener(this);
    bForward.setEnabled(false);
    jpNavigationButtons.add(bForward);
    
    JPanel jpStatusRefresh = new JPanel();
    bRefresh = new JButton(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("refresh_icon")));
    bRefresh.setToolTipText("Refresh");
    bRefresh.addActionListener(this);
    jpStatusRefresh.add(bRefresh);
    
    lSpinnerIcon = new JLabel(this.iconSpinner);
    jpStatusRefresh.add(lSpinnerIcon);
    
    // ACTION BUTTONS
    // 'open in myExperiment' button is the only one that is always available,
    // still will be set available during loading of the preview for consistency of the UI
    bOpenInMyExp = new JButton(iconOpenInMyExp);
    bOpenInMyExp.setEnabled(false);
    bOpenInMyExp.addActionListener(this);
    
    bDownload = new JButton(WorkbenchIcons.saveIcon);
    bDownload.setEnabled(false);
    bDownload.addActionListener(this);
    
    bOpenInTaverna = new JButton(WorkbenchIcons.openIcon);
    bOpenInTaverna.setEnabled(false);
    bOpenInTaverna.addActionListener(this);
    
    bAddRemoveFavourite = new JButton(iconAddFavourite);
    bAddRemoveFavourite.setEnabled(false);
    bAddRemoveFavourite.addActionListener(this);
    
    bAddComment = new JButton(iconAddComment);
    bAddComment.setEnabled(false);
    bAddComment.addActionListener(this);
    
    // put all action buttons into a button bar
    JPanel jpActionButtons = new JPanel();
    jpActionButtons.add(bOpenInMyExp);
    jpActionButtons.add(bDownload);
    jpActionButtons.add(bOpenInTaverna);
    jpActionButtons.add(bAddRemoveFavourite);
    jpActionButtons.add(bAddComment);
    
    
    jpStatusBar = new JPanel();
    jpStatusBar.setLayout(new BorderLayout());
    jpStatusBar.add(jpNavigationButtons, BorderLayout.WEST);
    jpStatusBar.add(jpActionButtons, BorderLayout.CENTER);
    jpStatusBar.add(jpStatusRefresh, BorderLayout.EAST);
    
    
    // put everything together
    jpMain = new JPanel();
    jpMain.setOpaque(true);
    jpMain.setLayout(new BorderLayout());
    jpMain.add(jpStatusBar, BorderLayout.NORTH);
    
    
    // add all content into the main dialog
    this.getContentPane().add(jpMain);
  }
  
  
  private void updateButtonBarState(ResourcePreviewContent content)
  {
    // get the visible type name of the resource
    Resource r = this.rpcContent.getResource();
    String strResourceType = Resource.getResourceTypeName(r.getItemType()).toLowerCase();
    
    // "Open in myExperiment" is always available for every item type
    this.bOpenInMyExp.setEnabled(true);
    this.bOpenInMyExp.setToolTipText("Open this " + strResourceType + " in myExperiment");
    
    // "Download" - only for selected types and based on current user's permissions
    // (these conditions are checked within the action)
    this.bDownload.setAction(pluginMainComponent.new DownloadResourceAction(r, false));
    
    // "Open in Taverna" - only for Taverna workflows and when download is allowed for current user
    // (these checks are carried out inside the action)
    this.bOpenInTaverna.setAction(pluginMainComponent.new LoadResourceInTavernaAction(r, false));
    
    
    // "Add to Favourites" - for all types, but only for logged in users
    String strTooltip = "It is currently not possible to add " + strResourceType + "s to favourites";
    boolean bFavouritingAvailable = false;
    if (r.isFavouritable()) {
      if (myExperimentClient.isLoggedIn()) {
        if (r.isFavouritedBy(myExperimentClient.getCurrentUser())) {
          strTooltip = "Remove this " + strResourceType + " from your favourites";
          this.bAddRemoveFavourite.setIcon(iconDeleteFavourite);
        }
        else {
          strTooltip = "Add this " + strResourceType + " to your favourites";
          this.bAddRemoveFavourite.setIcon(iconAddFavourite);
        }
        bFavouritingAvailable = true;
      }
      else {
        // TODO should be changed to display login box first, then favouriting option
        strTooltip = "Only logged in users can add items to favourites";
      }
    }
    this.bAddRemoveFavourite.setToolTipText(strTooltip);
    this.bAddRemoveFavourite.setEnabled(bFavouritingAvailable);
    
    
    // "Add Comment" - for all types besides users and only for logged in users
    strTooltip = "It is currently not possible to comment on " + strResourceType + "s";
    boolean bCommentingAvailable = false;
    if (r.isCommentableOn()) {
      if (myExperimentClient.isLoggedIn()) {
        strTooltip = "Add a comment on this " + strResourceType;
        bCommentingAvailable = true;
      }
      else {
        // TODO should be changed to display login box first, then commenting option
        strTooltip = "Only logged in users can make comments";
      }
    }
    this.bAddComment.setToolTipText(strTooltip);
    this.bAddComment.setEnabled(bCommentingAvailable);
  }
  
  
  public void hyperlinkUpdate(HyperlinkEvent e)
  {
    if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
    {
      String strAction = e.getDescription().toString();
      
      if (strAction.startsWith("preview:")) {
        this.preview(strAction);
      }
      else {
        try {
          BrowserLauncher launcher = new BrowserLauncher();
          launcher.openURLinBrowser(strAction);
        }
        catch (Exception ex) {
          logger.error("Failed while trying to open the URL in a standard browser; URL was: " + strAction
              + "\nException was: " + ex);
        }
      }
    }
  }
  
  
  public void actionPerformed(ActionEvent e)
  {
    if(e.getSource().equals(this.bBack))
    {
      // "Back" button clicked
      
      // update position in the history
      iCurrentHistoryIdx--;
      
      // enable or disable "back"/"forward" buttons as appropriate
      bBack.setEnabled(iCurrentHistoryIdx > 0);
      bForward.setEnabled(iCurrentHistoryIdx < alCurrentHistory.size() - 1);
      
      // open requested preview from the history
      this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
    }
    else if(e.getSource().equals(this.bForward))
    {
      // "Forward" button clicked
      
      // update position in the history
      iCurrentHistoryIdx++;
      
      // enable or disable "back"/"forward" buttons as appropriate
      bBack.setEnabled(iCurrentHistoryIdx > 0);
      bForward.setEnabled(iCurrentHistoryIdx < alCurrentHistory.size() - 1);
      
      // open requested preview from the history
      this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
    }
    else if(e.getSource().equals(this.bRefresh))
    {
      // "Refresh" button clicked
      
      // simply reload the same preview
      this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
    }
    else if(e.getSource().equals(this.bOpenInMyExp))
    {
      // "Open in myExperiment" button clicked
      try {
        BrowserLauncher launcher = new BrowserLauncher();
        launcher.openURLinBrowser(this.rpcContent.getResourceURL());
      }
      catch (Exception ex) {
        logger.error("Failed while trying to open the URL in a standard browser; URL was: " + this.rpcContent.getResourceURL()
            + "\nException was: " + ex);
      }
    }
    else if(e.getSource().equals(this.bAddComment))
    {
      // "Add Comment" button was clicked
      String strComment = null;
      AddCommentDialog commentDialog = new AddCommentDialog(this, this.rpcContent.getResource(), pluginMainComponent, myExperimentClient, logger);
      if ((strComment = commentDialog.launchAddCommentDialogAndPostCommentIfRequired()) != null) {
        // comment was added because return value is not null;
        // a good option now would be to reload only the comments tab, but
        // for now we refresh the whole of the preview
        this.actionPerformed(new ActionEvent(this.bRefresh, 0, ""));
        
        // update history of the items that were commented on, making sure that:
        // - there's only one occurrence of this item in the history;
        // - if this item was in the history before, it is moved to the 'top' now;
        // - predefined history size is not exceeded 
        this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().remove(this.rpcContent.getResource());
        this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().add(this.rpcContent.getResource());
        if (this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().size() > 
            HistoryBrowserTabContentPanel.COMMENTED_ON_ITEMS_HISTORY)
        {
          this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().remove(0);
        }
        
        // now update the history of the items that were commented on in 'History' tab
        if (this.pluginMainComponent.getHistoryBrowser() != null) {
          this.pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.COMMENTED_ON_ITEMS_HISTORY);
        }
      }
    }
    else if (e.getSource().equals(this.bAddRemoveFavourite))
    {
      boolean bItemIsFavourited = this.rpcContent.getResource().isFavouritedBy(this.myExperimentClient.getCurrentUser());
      
      AddRemoveFavouriteDialog favouriteDialog = new AddRemoveFavouriteDialog(this, !bItemIsFavourited, this.rpcContent.getResource(), pluginMainComponent, myExperimentClient, logger);
      int iFavouritingStatus = favouriteDialog.launchAddRemoveFavouriteDialogAndPerformNecessaryActionIfRequired();
      
      // if the operation wasn't cancelled, update status of the "add/remove favourite"
      // button and the list of favourites in the user profile
      if (iFavouritingStatus != AddRemoveFavouriteDialog.OPERATION_CANCELLED)
      {
        this.updateButtonBarState(this.rpcContent);
        this.pluginMainComponent.getMyStuffTab().getSidebar().repopulateFavouritesBox();
        this.pluginMainComponent.getMyStuffTab().getSidebar().revalidate();
      }
    }
    else if(e.getSource() instanceof JClickableLabel)
    {
      // clicked somewhere on a JClickableLabel; if that's a 'preview' request -
      // launch preview
      if(e.getActionCommand().startsWith("preview:"))
      {
        this.preview(e.getActionCommand());
      }
      else if (e.getActionCommand().startsWith("tag:"))
      {
        // pass this event onto the Tag Browser tab
        this.pluginMainComponent.getTagBrowserTab().actionPerformed(e);
        this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getTagBrowserTab());
      }
      else
      {
        // show the link otherwise
        try {
          BrowserLauncher launcher = new BrowserLauncher();
          launcher.openURLinBrowser(e.getActionCommand());
        }
        catch (Exception ex) {
          logger.error("Failed while trying to open the URL in a standard browser; URL was: " + e.getActionCommand()
              + "\nException was: " + ex);
        }
      }
    }
    else if (e.getSource() instanceof TagCloudPanel && e.getActionCommand().startsWith("tag:"))
    {
      // close the window and pass this event onto the Tag Browser tab
      this.setVisible(false);
      this.pluginMainComponent.getTagBrowserTab().actionPerformed(e);
      this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getTagBrowserTab());
    }
  }
  
  
  // *** Callbacks for ComponentListener interface ***
  
  public void componentShown(ComponentEvent e)
  {
    // every time the preview browser window is shown, it will start loading a preview -
    // this state is set in the preview() method; (so this won't have to be done here)
    
    // remove everything from the preview and re-add only the status bar
    // (this is done so that newly opened preview window won't show the old preview)
    jpMain.removeAll();
    jpMain.add(jpStatusBar, BorderLayout.NORTH);
    repaint();
    
    // set the size of the dialog box
    // (NB! Size needs to be set before the position!)
    this.setSize(ResourcePreviewBrowser.PREFERRED_WIDTH, ResourcePreviewBrowser.PREFERRED_HEIGHT);
    
    // make sure that the dialog box appears centered horizontally relatively to the main
    // component; also, pad by 30px vertically from the top of the main component
    int iMainComponentCenterX = (int)Math.round(this.pluginMainComponent.getLocationOnScreen().getX() + (this.pluginMainComponent.getWidth() / 2));
    int iPosX = iMainComponentCenterX - (this.getWidth() / 2);
    int iPosY = ((int)this.pluginMainComponent.getLocationOnScreen().getY()) + 30;
    this.setLocation(iPosX, iPosY);
  }
  
  public void componentHidden(ComponentEvent e)
  {
    // do nothing
  }
  
  public void componentResized(ComponentEvent e)
  {
    // do nothing
  }
  
  public void componentMoved(ComponentEvent e)
  {
    // do nothing
  }
  
  
  
}
