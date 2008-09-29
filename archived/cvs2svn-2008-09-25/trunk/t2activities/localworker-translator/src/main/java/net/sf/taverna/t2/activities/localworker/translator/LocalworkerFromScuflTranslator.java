/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.localworker.translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.ActivityXMLSerializer;

import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

/**
 * Convenience class to convert from T1 localworkers to serialised T2
 * localworkers. Reads a scufl file with all the T1 localworkers then creates
 * the T2 equivalent from each one and writes it to disk
 * 
 * @author Ian Dunlop
 * 
 */
public class LocalworkerFromScuflTranslator {

	private ScuflModel model;
	private LocalworkerTranslator translator;
	private ActivityXMLSerializer serializer;

	public LocalworkerFromScuflTranslator() {
		System.setProperty("raven.eclipse", "true");
		Repository myRepository = LocalRepository.getRepository(new File(
				"/Users/Ian/.m2/repository/"));
		try {
			setUpRavenRepository();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		translator = new LocalworkerTranslator();
		serializer = new ActivityXMLSerializer();
	}

	protected static void setUpRavenRepository() throws IOException {
		File tmpDir = File.createTempFile("taverna", "raven");
		tmpDir.delete();
		tmpDir.mkdir();
		Repository tempRepository = LocalRepository.getRepository(tmpDir);
		TavernaSPIRegistry.setRepository(tempRepository);
	}

	public void translate(InputStream stream, String outputDirectory) {
		model = new ScuflModel();
		try {
			XScuflParser.populate(stream, model, null);
		} catch (UnknownProcessorException e) {
			System.out.println("Cannot read 1" + e.getMessage());

		} catch (UnknownPortException e) {
			System.out.println("Cannot read 2" + e.getMessage());
		} catch (ProcessorCreationException e) {
			System.out.println("Cannot read 3 " + e.getMessage());
		} catch (DataConstraintCreationException e) {
			System.out.println("Cannot read 4" + e.getMessage());
		} catch (DuplicateProcessorNameException e) {
			System.out.println("Cannot read 5" + e.getMessage());
		} catch (MalformedNameException e) {
			System.out.println("Cannot read 6" + e.getMessage());
		} catch (ConcurrencyConstraintCreationException e) {
			System.out.println("Cannot read 7" + e.getMessage());
		} catch (DuplicateConcurrencyConstraintNameException e) {
			System.out.println("Cannot read 8" + e.getMessage());

		} catch (XScuflFormatException e) {
			System.out.println("Cannot read 9" + e.getMessage());
		}

		org.embl.ebi.escience.scufl.Processor[] processors = model
				.getProcessors();

		for (org.embl.ebi.escience.scufl.Processor processor : processors) {
			try {
				String workerClassName = translator
						.getWorkerClassName(processor);
				// String name = processor.getName();
				System.out.println("processor: " + workerClassName);
				BeanshellActivityConfigurationBean bean = translator
						.createConfigType(processor);
				BeanshellActivity activity = new BeanshellActivity();
				activity.configure(bean);
				Element activityToXML = serializer.activityToXML(activity);
				XMLOutputter outputter = new XMLOutputter();
				FileOutputStream out = new FileOutputStream(new File(
						outputDirectory + workerClassName));
				outputter.output(activityToXML, out);
			} catch (ActivityTranslationException e) {
				System.out.println("oops " + e.toString());
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ActivityConfigurationException e) {
				// TODO Auto-generated catch block
				System.out.println("oops1 " + e.toString());
				e.printStackTrace();
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				System.out.println("oops2 " + e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("oops3 " + e.toString());
				e.printStackTrace();
			}
		}

		// dataflow = WorkflowModelTranslator
		// .doTranslation(model);
		// } catch (WorkflowTranslationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// for (Processor processor:dataflow.getProcessors()) {
		// for (Activity activity:processor.getActivityList()) {
		//				
		// }
		// }

	}

	public static void main(String[] args) {
		LocalworkerFromScuflTranslator trans = new LocalworkerFromScuflTranslator();
		FileInputStream in = null;
		try {
			in = new FileInputStream(new File(
					"/Users/Ian/scratch/localworkers.xml"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		trans.translate(in, "/Users/Ian/scratch/workers");
	}

}
