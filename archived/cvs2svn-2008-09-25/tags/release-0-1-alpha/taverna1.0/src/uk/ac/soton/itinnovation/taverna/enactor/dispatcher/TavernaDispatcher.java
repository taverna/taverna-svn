///////////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this software belongs to the IT Innovation Centre of 2 Venture Road,
// Chilworth Science Park, Southampton SO16 7NP, UK. This software may not be used,
// sold, licensed, transferred, copied or reproduced in whole or in part in any manner
// or form or in or on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise without the prior
// written consent of the copyright owner.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2002/4/8
//      Created for Project :   MYGRID
//      Dependencies        :   
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-04-17 15:21:47 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.dispatcher;

import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.dispatcher.Dispatcher; // ambiguous with: org.apache.log4j.Dispatcher 
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.Task;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.taskstate.TaskState;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TavernaTask;
import uk.ac.soton.itinnovation.taverna.enactor.monitor.TavernaTaskMonitor;

import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Runnable;
import java.lang.System;
import java.lang.Thread;



/**
 * This is a dispatcher suitable for use with Taverna tasks.
 */
public class TavernaDispatcher extends Dispatcher {

  private static Logger logger = Logger.getLogger(TavernaDispatcher.class);

  private TavernaTask task;
  private Thread thisThread;

  public TavernaDispatcher(){
  }

  public void start() {
    thisThread = new Thread(new Runnable(){
      public void run(){
        dispatchTask();
      }
    });
    thisThread.start();
  }


  /**
   * Dispatches the passed task according to the dispatch rules for Taverna.
   *
   * @param Task to dispatch.
   */
  public void dispatch(Task task) {
    if(!(task instanceof TavernaTask)){
      throw new IllegalArgumentException("Cannot dispatch task is not an TavernaTask");
    }

    this.task = (TavernaTask)task;
    start();
  }

  private void dispatchTask() {
    logger.info("Dispatching task with ID: " + task.getID());
    TavernaTaskMonitor monitor = TavernaTaskMonitor.getInstance();
    TaskStateMessage taskState = null;

    //make final check not cancelled
    if (task.getCurrentState() != TaskState.CANCELLED) {
      logger.debug("Dispatching task with ID: " + task.getID() + " at " + System.currentTimeMillis());
      try {
        monitor.put(new TaskStateMessage(task.getParentFlow().getID(),
            task.getID(),
            TaskStateMessage.RUNNING,
            " running task " + task.getID()));

        taskState = task.doTask();

        monitor.put(taskState);
      }
      catch (Exception ex) {
        logger.error("Dispatch Failure for task with ID: " + task.getID(), ex);
        monitor.put(new TaskStateMessage(task.getParentFlow().getID(), task.getID(), TaskStateMessage.FAILED, "Unexplained dispatch failure for a task within the workflow."));
      }
    }
  }
}
