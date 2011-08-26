////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2003/4/9
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-04-17 15:21:49 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.monitor;

import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskReportHandler;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.eventservice.TaskStateMessage;

import java.lang.InterruptedException;



/**
 * Singleton monitor that encapsulates both a queue and task report handler.
 */
public class TavernaTaskMonitor {

    private Channel channel;
    private TaskReportHandler handler;
	private static TavernaTaskMonitor instance;

    private TavernaTaskMonitor() {
        //set up the linked list and the TaskReportHandler
		channel = new LinkedQueue();
        handler = new TaskReportHandler(channel);
    }

    /**
     * Get an instance of a TavernerMonitor.
     *
     * @return Monitor instance.
     */
    public static TavernaTaskMonitor getInstance() {
        if (instance == null)
            instance = new TavernaTaskMonitor();
        return instance;
    }

    /**
     * Checks to see if the monitor is empty.
     *
     * @return boolean true if empty false otherwise.
     */
    public boolean isEmpty() {
        if (channel.peek() == null)
            return true;
        else
            return false;
    }

    /**
     * Take an event from the monitor.
     *
     * @return TaskEventReport
     */
    public TaskStateMessage take() throws InterruptedException {
        return (TaskStateMessage) channel.take();
    }

    /**
     * Deposit a report for a task.
     *
     * @param TaskEventReport report.
     */
    public void put(TaskStateMessage report) {
        try {
            channel.put(report);
            handler.signal();
        } catch (InterruptedException ex) {
            //try once more
            try {
                channel.put(report);
                handler.signal();
            } catch (InterruptedException ex1) {}
        }
    }   
}
