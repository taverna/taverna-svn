/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenhšft, INB, University of Luebeck   
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

package net.sf.taverna.t2.activities.usecase;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.annotationbeans.MimeType;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;
import org.globus.ftp.exception.ClientException;

import de.uni_luebeck.inb.knowarc.gui.ProgressDisplayImpl;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInputUser;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseEnumeration;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvokation;

public class UseCaseActivity extends AbstractAsynchronousActivity<UseCaseActivityConfigurationBean> {
	private UseCaseActivityConfigurationBean configurationBean;
	private UseCaseDescription mydesc;

	private void addMimeTypes(Annotated<?> annotated, List<String> mimeTypes) {
		for (String mimeType : mimeTypes) {
			MimeType mimeTypeAnnotation = new MimeType();
			mimeTypeAnnotation.setText(mimeType);
			try {
				EditsRegistry.getEdits().getAddAnnotationChainEdit(annotated, mimeTypeAnnotation).doEdit();
			} catch (EditException e) {
				Logger.getLogger(UseCaseActivity.class).error(e);
			}
		}
	}

	private void addInputWithMime(String portName, int portDepth, Class<?> translatedElementClass, List<String> mimeTypes) {
		List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = Collections.emptyList();
		ActivityInputPort inputPort = EditsRegistry.getEdits().createActivityInputPort(portName, portDepth, true, handledReferenceSchemes,
				translatedElementClass);
		inputPorts.add(inputPort);
		addMimeTypes(inputPort, mimeTypes);
	}

	private void addOutputWithMime(String portName, int portDepth, List<String> mimeTypes) {
		OutputPort outputPort = EditsRegistry.getEdits().createActivityOutputPort(portName, portDepth, portDepth);
		outputPorts.add(outputPort);
		addMimeTypes(outputPort, mimeTypes);
	}

	@Override
	public void configure(UseCaseActivityConfigurationBean bean) throws ActivityConfigurationException {
		this.configurationBean = bean;

		try {
			List<UseCaseDescription> usecases = UseCaseEnumeration.enumerateXmlFile(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()),
					bean.getRepositoryUrl());
			for (UseCaseDescription usecase : usecases) {
				if (!usecase.usecaseid.equalsIgnoreCase(bean.getUsecaseid()))
					continue;
				mydesc = usecase;
				break;
			}

			for (Map.Entry<String, ScriptInput> cur : mydesc.inputs.entrySet()) {
				ScriptInputUser scriptInputUser = (ScriptInputUser) cur.getValue();
				addInputWithMime(cur.getKey(), scriptInputUser.list ? 1 : 0, cur.getValue().binary ? byte[].class : String.class, scriptInputUser.mime);

			}
			for (Map.Entry<String, ScriptOutput> cur : mydesc.outputs.entrySet()) {
				addOutputWithMime(cur.getKey(), 0, cur.getValue().mime);
			}

			addOutput("STDOUT", 0);
			addOutput("STDERR", 0);
		} catch (Exception e) {
			throw new ActivityConfigurationException("Couldn't create UseCase Activity", e);
		}
	}

	@Override
	public UseCaseActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data, final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();
				UseCaseInvokation invoke = null;
				try {
					invoke = UseCaseInvokation.createAppropriateInvokationFor(KnowARCConfigurationFactory.getConfiguration(), mydesc);
					for (String cur : invoke.getInputs()) {
						Object value = referenceService.renderIdentifier(data.get(cur), invoke.getType(cur), callback.getContext());
						invoke.setInput(cur, value);
					}
					Map<String, Object> downloads = invoke.Submit();
					Map<String, T2Reference> result = new HashMap<String, T2Reference>();
					for (Map.Entry<String, Object> cur : downloads.entrySet()) {
						Object value = cur.getValue();
						T2Reference reference = referenceService.register(value, 0, true, callback.getContext());
						result.put(cur.getKey(), reference);
					}
					callback.receiveResult(result, new int[0]);
				} catch (ServerException e) {
					callback.fail("Problem submitting job: ServerException: ", e);
				} catch (ReferenceServiceException e) {
					callback.fail("Problem with job input / output port: ", e);
				} catch (ClientException e) {
					callback.fail("Problem submitting job: ClientException: ", e);
				} catch (IOException e) {
					callback.fail("Problem submitting job: IOException: ", e);
				} catch (Exception e) {
					callback.fail(e.getMessage(), e);
				} finally {
					if (invoke != null)
						invoke.Cleanup();
				}
			}

		});

	}

}
