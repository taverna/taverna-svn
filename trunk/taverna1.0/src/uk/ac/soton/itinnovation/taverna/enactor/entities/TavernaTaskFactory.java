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
//      Created Date        :   2003/04/08
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2004-02-04 11:21:21 $
//                              $Revision: 1.18 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.UnsupportedTavernaProcessorException;
import java.lang.String;



/**
 * Creates the correct concrete task for a given WSDL identifier
 * Future versions should load a task based on the workflow for a particular service.
 */
public class TavernaTaskFactory {

    private static Logger logger = Logger.getLogger(TavernaTaskFactory.class);
    
    /**
     * Retrieve a concrete Task instance. In the newer version (4.2.04) this
     * actually always returns the ProcessorTask itself, the burden of handling
     * the plugin framework has been shifted off to the ProcessorTask to allow
     * rescheduling during a task invocation in the case of alternate processors.
     */
    public static ProcessorTask getConcreteTavernaTask(String id,Processor processor,LogLevel l,String userID,String userCtx) throws UnsupportedTavernaProcessorException {
	return new ProcessorTask(id, processor, l, userID, userCtx);
    }
    
    /**
     * Retrieves a concrete instance of a taverna task 
     *
     * @param String identifier for the required application
     
     public static ProcessorTask getConcreteTavernaTask(String id,Processor processor,LogLevel l,String userID,String userCtx) throws UnsupportedTavernaProcessorException {
     ProcessorTask pTask = null;
     String taskClassName = ProcessorHelper.getTaskClassName(processor);
     if (taskClassName == null) {
     logger.error("Don't know how to deal with processor with name '" + processor.getName() + "'");
     throw new UnsupportedTavernaProcessorException("Don't know how to deal with processor with name '" + processor.getName() + "'");
     }
     try {
     Class processorDefn = null;
     Class[] argsClass = new Class[] {String.class,Processor.class,LogLevel.class,String.class,String.class};
     Object[] args = new Object[] {id,processor,l,userID,userCtx};
     Constructor argsConstructor;
     processorDefn = Class.forName(taskClassName);
     argsConstructor = processorDefn.getConstructor(argsClass);
     pTask = (ProcessorTask) argsConstructor.newInstance(args);
     } catch (InstantiationException e) {
     logger.error("Can't instantiate task for processor with identifier '" + processor.getName(),e);
     throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with identifier '" + processor.getName() + "'");
     } catch (IllegalAccessException e) {
     logger.error("Not allowed to instantiate processor with identifier '" + processor.getName(),e);
     throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with identifier '" + processor.getName() + "'");
     } catch (IllegalArgumentException e) {
     logger.error("Inappropriate arguments pass to custom task for processor with identifier '" + processor.getName()+ "'",e);
     throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with identifier '" + processor.getName() + "'");
     } catch (InvocationTargetException e) {
     logger.error("Unable to invoke constructor of custom task for processor with identifier '" + processor.getName() + "'",e);
     throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with identifier '" + processor.getName() + "'");
     } catch (Exception e) {
     logger.error("Don't know how to deal with processor with identifier '" + processor.getName() + "'",e);
     throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with identifier '" + processor.getName() + "'");
     }
     if(pTask==null) {
     logger.error("Don't know how to deal with processor with identifier '" + processor.getName() + "'");
     throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with identifier '" + processor.getName() + "'");
     }
     return pTask;
     }
    */
    
}
