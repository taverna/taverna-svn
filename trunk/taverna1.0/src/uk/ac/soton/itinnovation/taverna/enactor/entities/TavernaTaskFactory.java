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
//                              $Date: 2003-04-17 15:21:48 $
//                              $Revision: 1.3 $
//
///////////////////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.taverna.enactor.entities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.UnsupportedTavernaProcessorException;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalAccessException;
import java.lang.IllegalArgumentException;
import java.lang.InstantiationException;
import java.lang.Object;
import java.lang.String;



/**
 * Creates the correct concrete task for a given WSDL identifier
 * Future versions should load a task based on the workflow for a particular service.
 */
public class TavernaTaskFactory {

	private static Logger logger = Logger.getLogger(TavernaTaskFactory.class);

    /**
     * Retrieves a concrete instance of a taverna task 
	 *
     * @param String identifier for the required application
     */
    public static ProcessorTask getConcreteTavernaTask(String id,String taskID,Processor processor) throws UnsupportedTavernaProcessorException {
        ProcessorTask pTask = null;
		//obtain the class of this taskID, this could be shifted to configuration later
		String taskClassName = null;
		if(taskID.equals("http://industry.ebi.ac.uk/soap/soaplab/edit::seqret")) 
			taskClassName = "uk.ac.soton.itinnovation.taverna.enactorentities.SeqretTask";
		else if(taskID.equals("http://industry.ebi.ac.uk/soap/soaplab/nucleic_gene_finding::getorf"))
			taskClassName = "uk.ac.soton.itinnovation.taverna.enactorentities.GetOrfTask";
		else if(taskID.equals("http://industry.ebi.ac.uk/soap/soaplab/nucleic_translation::transeq"))
			taskClassName = "uk.ac.soton.itinnovation.taverna.enactorentities.TranSeqTask";
		else if(taskID.equals("http://industry.ebi.ac.uk/soap/soaplab/alignment_multiple::emma"))
			taskClassName = "uk.ac.soton.itinnovation.taverna.enactorentities.EmmaSeqTask";
		else if(taskID.equals("http://industry.ebi.ac.uk/soap/soaplab/nucleic_profiles::prophecy")) 
			taskClassName = "uk.ac.soton.itinnovation.taverna.enactorentities.ProphecyTask";
		else if(taskID.equals("http://industry.ebi.ac.uk/soap/soaplab/nucleic_profiles::prophet")) 
			taskClassName = "uk.ac.soton.itinnovation.taverna.enactorentities.ProphetTask";
		else {
			logger.error("Don't know how to deal with processor with soaplab wsdl '" + taskID + "'");
			throw new UnsupportedTavernaProcessorException("Don't know how to deal with processor with soaplab wsdl '" + taskID + "'");
		}
		try {
            
			Class processorDefn = null;
			Class[] argsClass = new Class[] {String.class,Processor.class};
			Object[] args = new Object[] {id,processor};
			Constructor argsConstructor;
			processorDefn = Class.forName(taskClassName);
			argsConstructor = processorDefn.getConstructor(argsClass);
			pTask = (ProcessorTask) argsConstructor.newInstance(args);
		} catch (InstantiationException e) {
			logger.error("Can't instantiate task for processor with soaplab wsdl '" + taskID + "'",e);
			throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with soaplab wsdl '" + taskID + "'");
		} catch (IllegalAccessException e) {
			logger.error("Not allowed to instantiate processor with soaplab wsdl '" + taskID + "'",e);
			throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with soaplab wsdl '" + taskID + "'");
		} catch (IllegalArgumentException e) {
			logger.error("Inappropriate arguments pass to customt task for processor with soaplab wsdl '" + taskID + "'",e);
			throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with soaplab wsdl '" + taskID + "'");
		} catch (InvocationTargetException e) {
			logger.error("Unable to invoke constructor of custom task for processor with soaplab wsdl '" + taskID + "'",e);
			throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with soaplab wsdl '" + taskID + "'");
		} catch (Exception e) {
            logger.error("Don't know how to deal with processor with soaplab wsdl '" + taskID + "'",e);
			throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with soaplab wsdl '" + taskID + "'");
        }
		if(pTask==null) {
			logger.error("Don't know how to deal with processor with soaplab wsdl '" + taskID + "'");
			throw new UnsupportedTavernaProcessorException("Couldn't load implementation for processor with soaplab wsdl '" + taskID + "'");
		}
		return pTask;
    }
}