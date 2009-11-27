/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sequencefile;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ValueCarryingExternalReference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;

/**
 * A a Taverna 2 Activity that reads sequence files and outputs a stream of
 * sequences.
 * 
 * @author David Withers
 * @author Eddie Kawas
 */
@SuppressWarnings("deprecation")
public class SequenceFileActivity extends
		AbstractAsynchronousActivity<SequenceFileActivityConfigurationBean> {

	private static final String INPUT_PORT_NAME = "fileurl";

	private static final String OUTPUT_PORT_NAME = "sequences";

	private static Logger logger = Logger.getLogger(SequenceFileActivity.class);

	private SequenceFileActivityConfigurationBean configurationBean;

	private FileFormat fileFormat;

	private SequenceType sequenceType;

	public SequenceFileActivity() {
	}

	@Override
	public void configure(SequenceFileActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		fileFormat = configurationBean.getFileFormat();
		logger.debug("Setting file format to " + fileFormat);
		sequenceType = configurationBean.getSequenceType();
		logger.debug("Setting sequence type to " + sequenceType);
		configurePorts();
	}

	@Override
	public SequenceFileActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();

				try {
					// resolve inputs
					T2Reference inputReference = data.get(INPUT_PORT_NAME);

					InputStream inputStream = getInputStream(context, referenceService,
							inputReference);
					if (inputStream == null) {
						logger.warn("Input is not a file reference or a file name");
						callback.fail("Input is not a file reference or a file name");
						return;
					}

					// run the activity
					BufferedReader inputReader = new BufferedReader(new InputStreamReader(
							inputStream));
					SequenceIterator sequenceIterator = (SequenceIterator) SeqIOTools
							.fileToBiojava(fileFormat.name(), sequenceType.name(), inputReader);

					List<T2Reference> outputList = new ArrayList<T2Reference>();
					//
					for (long index = 0; sequenceIterator.hasNext(); index++) {
						String sequence = sequenceIterator.nextSequence().seqString();
						T2Reference data = referenceService.register(sequence, 0, true, context);
						outputList.add((int) index, data);
						outputData.put(OUTPUT_PORT_NAME, data);
						callback.receiveResult(outputData, new int[] { (int) index });
					}

					// register outputs
					T2Reference outputReference = referenceService.register(outputList, 1, true,
							context);
					outputData.put(OUTPUT_PORT_NAME, outputReference);

					// send result to the callback
					callback.receiveResult(outputData, new int[0]);
				} catch (ReferenceServiceException e) {
					logger.warn("ReferenceService error while executing activity", e);
					callback.fail("ReferenceService error while executing activity", e);
				} catch (IOException e) {
					logger.warn("Error reading sequence file", e);
					callback.fail("Error reading sequence file", e);
				} catch (BioException e) {
					logger.warn("Error reading sequence file", e);
					callback.fail("Error reading sequence file", e);
				}
			}

		});
	}

	private void configurePorts() {
		removeInputs();
		addInput(INPUT_PORT_NAME, 0, false, null, null);

		removeOutputs();
		addOutput(OUTPUT_PORT_NAME, 1, 0);
	}

	private InputStream getInputStream(InvocationContext context,
			ReferenceService referenceService, T2Reference inputRef) throws IOException {
		InputStream inputStream = null;

		Identified identified = referenceService.resolveIdentifier(inputRef, null, context);
		if (identified instanceof ReferenceSet) {
			ReferenceSet referenceSet = (ReferenceSet) identified;
			Set<ExternalReferenceSPI> externalReferences = referenceSet.getExternalReferences();
			for (ExternalReferenceSPI externalReference : externalReferences) {
				if (externalReference instanceof ValueCarryingExternalReference<?>) {
					ValueCarryingExternalReference<?> vcer = (ValueCarryingExternalReference<?>) externalReference;
					if (String.class.isAssignableFrom(vcer.getValueType())) {
						String input = (String) vcer.getValue();
						try {
							URL url = new URL(input);
							inputStream = url.openStream();
							logger.debug("Sequence file url is '" + input + "'");
						} catch (MalformedURLException e) {
							logger.debug("Sequence file name is '" + input + "'");
							inputStream = new FileInputStream(input);
						}
					}
					break;
				} else {
					inputStream = externalReference.openStream(context);
					break;
				}
			}
		}
		return inputStream;
	}

}
