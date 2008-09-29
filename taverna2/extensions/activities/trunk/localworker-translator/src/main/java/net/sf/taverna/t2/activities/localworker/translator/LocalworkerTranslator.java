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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivityConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.scufl.Processor;

/**
 * An ActivityTranslator specifically for translating Taverna 1 LocalService
 * Processors to a Taverna 2 Beanshell Activity
 * 
 * @see ActivityTranslator
 * @author David Withers
 */
public class LocalworkerTranslator extends
		AbstractActivityTranslator<BeanshellActivityConfigurationBean> {
	
	private static Map<String, String> localWorkerToScript = new HashMap<String, String>();
	
	private static Map<String, List<String>> localWorkerToDependecies = new HashMap<String, List<String>>();
	
	static {
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.ByteArrayToString", "ByteArrayToString");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.DecodeBase64", "DecodeBase64");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.EchoList", "EchoList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.EmitLotsOfStrings", "EmitLotsOfStrings");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.EncodeBase64", "EncodeBase64");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.ExtractImageLinks", "ExtractImageLinks");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FailIfFalse", "FailIfFalse");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FailIfTrue", "FailIfTrue");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FilterStringList", "FilterStringList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.FlattenList", "FlattenList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.PadNumber", "PadNumber");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.RegularExpressionStringList", "RegularExpressionStringList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.SendEmail", "SendEmail");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.SliceList", "SliceList");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.SplitByRegex", "SplitByRegex");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringConcat", "StringConcat");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringListMerge", "StringListMerge");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringSetDifference", "StringSetDifference");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringSetIntersection", "StringSetIntersection");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringSetUnion", "StringSetUnion");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.StringStripDuplicates", "StringStripDuplicates");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.TestAlwaysFailingProcessor", "TestAlwaysFailingProcessor");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.WebImageFetcher", "WebImageFetcher");
		localWorkerToScript.put("org.embl.ebi.escience.scuflworkers.java.WebPageFetcher", "WebPageFetcher");
		
		localWorkerToDependecies.put("org.embl.ebi.escience.scuflworkers.java.DecodeBase64", Collections.singletonList("commons-codec:commons-codec:1.3"));
		localWorkerToDependecies.put("org.embl.ebi.escience.scuflworkers.java.EncodeBase64", Collections.singletonList("commons-codec:commons-codec:1.3"));
		List<String> dependencies = new ArrayList<String>();
		dependencies.add("javax.mail:mail:1.4");
		dependencies.add("javax.activation:activation:1.1");
		localWorkerToDependecies.put("org.embl.ebi.escience.scuflworkers.java.SendEmail", dependencies);
		
		//xml:XPathText
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.xml.XPathTextWorker", "XPathTextWorker");
		dependencies=new ArrayList<String>();
		dependencies.add("dom4j:dom4j:1.6");
		localWorkerToDependecies.put("net.sourceforge.taverna.scuflworkers.xml.XPathTextWorker", dependencies);
		
		//biojava
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.biojava.GenBankParserWorker", "GenBankParserWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.biojava.ReverseCompWorker", "ReverseCompWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.biojava.SwissProtParserWorker", "SwissProtParserWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.biojava.TranscribeWorker", "TranscribeWorker");

		localWorkerToDependecies.put("net.sourceforge.taverna.scuflworkers.biojava.GenBankParserWorker", Collections.singletonList("uk.org.mygrid.resources:biojava:1.4pre1"));
		localWorkerToDependecies.put("net.sourceforge.taverna.scuflworkers.biojava.ReverseCompWorker", Collections.singletonList("uk.org.mygrid.resources:biojava:1.4pre1"));
		localWorkerToDependecies.put("net.sourceforge.taverna.scuflworkers.biojava.SwissProtParserWorker", Collections.singletonList("uk.org.mygrid.resources:biojava:1.4pre1"));
		localWorkerToDependecies.put("net.sourceforge.taverna.scuflworkers.biojava.TranscribeWorker", Collections.singletonList("uk.org.mygrid.resources:biojava:1.4pre1"));
		
		//io
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.TextFileReader", "TextFileReader");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.TextFileWriter", "TextFileWriter");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.LocalCommand", "LocalCommand");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.FileListByExtTask", "FileListByExtTask");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.FileListByRegexTask", "FileListByRegexTask");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.DataRangeTask", "DataRangeTask");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.ConcatenateFileListWorker", "ConcatenateFileListWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.io.EnvVariableWorker", "EnvVariableWorker");

		//ui
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.ui.AskWorker", "AskWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.ui.SelectWorker", "SelectWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.ui.ChooseWorker", "ChooseWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.ui.TellWorker", "TellWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.ui.WarnWorker", "WarnWorker");
		localWorkerToScript.put("net.sourceforge.taverna.scuflworkers.ui.SelectFileWorker", "SelectFileWorker");
	}

	@Override
	protected LocalworkerActivity createUnconfiguredActivity() {
		return new LocalworkerActivity();
	}

	@Override
	protected LocalworkerActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		LocalworkerActivityConfigurationBean bean = new LocalworkerActivityConfigurationBean();
		populateConfigurationBeanPortDetails(processor, bean);
		String workerClassName = getWorkerClassName(processor);
		bean.setScript(getScript(workerClassName));
		if (localWorkerToDependecies.containsKey(workerClassName)) {
			bean.setDependencies(localWorkerToDependecies.get(workerClassName));
		}
		return bean;
	}

	public boolean canHandle(Processor processor) {
		boolean result = false;
		if (processor != null) {
			if (processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor")) {
				try {
					String localworkerClassName = getWorkerClassName(processor);
					result = localWorkerToScript.containsKey(localworkerClassName);
				} catch (ActivityTranslationException e) {
					//return false
				}
			}
		}
		return result;
	}
/**
 * Get the beanshell script for a T1 style localworker
 * 
 * @param workerClassName
 * @return
 * @throws ActivityTranslationException
 */
	public String getScript(String workerClassName) throws ActivityTranslationException {
		String scriptName = localWorkerToScript.get(workerClassName);
		if (scriptName == null) {
			throw new ActivityTranslationException("Unable to find the script for:"+workerClassName); 
		}
		return readScript(scriptName);
	}
	
	private String readScript(String script) throws ActivityTranslationException {
		try {
			return IOUtils.toString(LocalworkerTranslator.class.getResourceAsStream("/" + script));
		} catch (IOException e) {
			throw new ActivityTranslationException("Error reading script resource for " + script, e);
		}
	}

	public String getWorkerClassName(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getWorkerClassName");
			return (String) method.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getWorkerClassName through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getWorkerClassName, an therefore does not conform to being a LocalService processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getWorkerClassName on the LocalService processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getWorkerClassName on the LocalService processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getWorkerClassName on the LocalService processor",e);
		}
	}

	public static Map<String, String> getLocalWorkerToScript() {
		return localWorkerToScript;
	}

}
