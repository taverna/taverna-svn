package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import org.emboss.jemboss.JembossJarUtil;
import org.emboss.jemboss.gui.startup.ProgList;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBroker;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.FlowBrokerFactory;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaFlowReceipt;
import uk.ac.soton.itinnovation.taverna.enactor.broker.TavernaStringifiedWorkflowSubmission;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.InvalidFlowBrokerRequestException;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.broker.WorkflowCommandException;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;

// IO Imports
import java.io.*;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scufl.ScuflModel;
import uk.ac.mrc.hgmp.taverna.retsina.ProgramSelectionPanel;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraph;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphPanel;
import java.lang.Exception;
import java.lang.InterruptedException;
import java.lang.Object;
import java.lang.SecurityManager;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;



/**
*
* GUI front editor for a ScuflModel
*
*/
public class Retsina extends JPanel  
                          implements ScuflUIComponent
{
    
  private ScuflGraphPanel graphPanel;
  private ScuflModel scuflModel;

  public Retsina() 
  {
    super(new BorderLayout());
    String wossname = null;

    try
    {
      JembossJarUtil jwoss = new JembossJarUtil("resources/wossname.jar");
      wossname = new String((byte[])jwoss.getElement("wossname.out"));
    }
    catch(Exception ex)
    {
      System.out.println("Failed to read wossname for menu construction");
    }
    
    JMenuBar progMenuBar = new JMenuBar();
    ProgList progs = new ProgList(wossname,progMenuBar);
    graphPanel = new ScuflGraphPanel(null,progs);
    JPanel westPanel = new ProgramSelectionPanel(wossname,graphPanel,
                                                 progs,progMenuBar);

//  setJMenuBar(createMenuBar(graphPanel));
    // Put the components in the content pane
    add(graphPanel,BorderLayout.CENTER);
    add(westPanel, BorderLayout.WEST);
  } 

  public ScuflGraph getScuflGraph()
  {
    return graphPanel.getCurrentJGraph();
  }

  /**
   *
   * Remove existing processors and children and
   * create a new ScuflModel.
   * @param String xscufl
   *
   */
  public void newWorkFlow()
  {
    ScuflGraph graph = getScuflGraph();
    // clear graph of old workflow
    Object[] cells = graph.getRoots();
    for(int i=0;i<cells.length;i++)
    {
      if(cells[i] instanceof ScuflGraphCell)
      {
        ScuflGraphCell cell = (ScuflGraphCell)cells[i];
        cell.removeAllChildren();
        graph.destroyProcessor(cell.getScuflProcessor());
      }
    }
                                                                                                              
    graph.getModel().remove(cells);
    graph.clearScuflModel();
  }

  /**
   *
   * Load in a new XScufl workflow.
   * @param String xscufl
   *
   */
  public void loadXScufl(String xscufl)
  {
    newWorkFlow();
    
    // load in new workflow
    getScuflGraph().loadXScufl(xscufl);
  }




  // ScuflUIComponent
  /**
   * Directs the implementing component to bind to the
   * specified ScuflModel instance, refresh its internal
   * state from the model and commence listening to events,
   * maintaining its state as these events dictate.
   */
  public void attachToModel(ScuflModel scuflModel)
  {
    this.scuflModel = scuflModel;
    graphPanel.attachToModel(scuflModel);
  }
                                                                                                       
  /**
   * Directs the implementing component to detach from the
   * model, set its internal state to some suitable blank
   * (i.e. blank image, no text in a text field etc) and
   * desist from listening to model events.
   */
  public void detachFromModel(){}
                                                                                                       
  /**
   * Get the preferred name of this component, for titles
   * in windows etc.
   */
  public String getName(){ return "Retsina"; }

}

