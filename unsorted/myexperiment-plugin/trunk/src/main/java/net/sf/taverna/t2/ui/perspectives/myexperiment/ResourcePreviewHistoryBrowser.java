package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;

import org.apache.log4j.Logger;

public class ResourcePreviewHistoryBrowser extends JPanel implements ActionListener
{
  // CONSTANTS
  private static final double SIDEBAR_BALANCE = 0.4;
  
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;
  
  // COMPONENTS
  private JSplitPane spMainSplitPane;
  private JPanel jpSidebar;
  
  
  public ResourcePreviewHistoryBrowser(MainComponent component, MyExperimentClient client, Logger logger)
  {
    super();
    
    // set main variables to ensure access to myExperiment, logger and the parent component
    this.pluginMainComponent = component;
    this.myExperimentClient = client;
    this.logger = logger;
    
    
    this.initialiseUI();
    this.initialiseData();
    
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          spMainSplitPane.setDividerLocation(SIDEBAR_BALANCE);
          spMainSplitPane.setOneTouchExpandable(true);
          spMainSplitPane.setDoubleBuffered(true);
        }
    });
  }
  
  
  private void initialiseUI()
  {
    // create sidebar for listing of previewed resources
    this.jpSidebar = new JPanel();
    this.jpSidebar.setLayout(new BoxLayout(jpSidebar, BoxLayout.Y_AXIS));
    this.jpSidebar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Previosly Previewed Items"),
        BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    
    // wrap sidebar into a scroll pane
    JScrollPane spSidebar = new JScrollPane(this.jpSidebar);
    spSidebar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    spSidebar.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);
    
    // create preview panel
    
    
    // PUT EVERYTHING TOGETHER
    this.spMainSplitPane = new JSplitPane();
    this.spMainSplitPane.setLeftComponent(spSidebar);
    //this.spMainSplitPane.setRightComponent(this.jpTagSearchResults);
    
    this.setLayout(new BorderLayout());
    this.add(this.spMainSplitPane, BorderLayout.CENTER);
  }
  
  
  private void initialiseData()
  {
    this.populateSidebar();
  }
  
  
  /**
   * This helper can be called externally to refresh the preview history.
   * Is used inside ResourcePreviewBrowser every time a new item is previewed.
   * Also useful, when an option to 'clear preview history' is used in the Preferences window.
   */
  public void refreshSidebar()
  {
    this.jpSidebar.removeAll();
    populateSidebar();
  }
  
  
  /**
   * Retrieves history data from preview browser and populates the sidebar.
   */
  private void populateSidebar()
  {
    List<Resource> lResourceHistory = this.pluginMainComponent.getPreviewBrowser().getPreviewHistory();
    
    if (lResourceHistory.size() > 0)
    {
      for (int i = lResourceHistory.size() - 1; i >= 0; i--)
      {
        Resource r = lResourceHistory.get(i);
        this.jpSidebar.add(new JClickableLabel(r.getTitle(), 
                                               "preview:" + r.getItemType() + ":" + r.getURI(),
                                               this,
                                               new ImageIcon(MyExperimentPerspective.getLocalIconURL(r.getItemType())),
                                               SwingConstants.LEFT,
                                               r.getItemTypeName() + ": " + r.getTitle()
        ));
      }
    }
    else {
      JLabel lNoPreviewsYet = new JLabel("No items were previewed yet");
      lNoPreviewsYet.setFont(lNoPreviewsYet.getFont().deriveFont(Font.ITALIC));
      lNoPreviewsYet.setForeground(Color.GRAY);
      lNoPreviewsYet.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      this.jpSidebar.add(lNoPreviewsYet);
    }
    
    // make sure that the component is updated after population
    this.jpSidebar.revalidate();
    this.jpSidebar.repaint();
  }


  public void actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub
    
  }
  
}
