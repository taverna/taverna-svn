/*
 * Copyright (C) 2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.admin;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.ogf.usage.JobUsageRecord;
import org.taverna.server.master.common.Uri;
import org.taverna.server.master.common.VersionedElement;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The administration interface for Taverna Server.
 * 
 * @author Donal Fellows
 */
@Description("Administration interface for Taverna Server.")
public interface Admin {
	/**
	 * Get a description of the adminstration interface.
	 * 
	 * @param ui
	 *            What URI was used to access this resource?
	 * @return The description document.
	 */
	@GET
	@Path("/")
	@Produces({ "application/xml", "application/json" })
	@NonNull
	AdminDescription getDescription(@Context UriInfo ui);

	/**
	 * Get whether to allow new workflow runs to be created.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("allowNew")
	@Produces("text/plain")
	@Description("Whether to allow new workflow runs to be created.")
	boolean getAllowNew();

	/**
	 * Set whether to allow new workflow runs to be created.
	 * 
	 * @param newValue
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("allowNew")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Whether to allow new workflow runs to be created.")
	boolean setAllowNew(boolean newValue);

	/**
	 * Get whether to log the workflows submitted.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("logWorkflows")
	@Produces("text/plain")
	@Description("Whether to log the workflows submitted.")
	boolean getLogWorkflows();

	/**
	 * Set whether to log the workflows submitted.
	 * 
	 * @param logWorkflows
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("logWorkflows")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Whether to log the workflows submitted.")
	boolean setLogWorkflows(boolean logWorkflows);

	/**
	 * Get whether to log the user-directed faults.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("logFaults")
	@Produces("text/plain")
	@Description("Whether to log the user-directed faults.")
	boolean getLogFaults();

	/**
	 * Set whether to log the user-directed faults.
	 * 
	 * @param logFaults
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("logFaults")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Whether to log the user-directed faults.")
	boolean setLogFaults(boolean logFaults);

	/**
	 * Get what file to dump usage records to.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("usageRecordDumpFile")
	@Produces("text/plain")
	@Description("What file to dump usage records to.")
	@NonNull
	String getURFile();

	/**
	 * Set what file to dump usage records to.
	 * 
	 * @param urFile
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("usageRecordDumpFile")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What file to dump usage records to.")
	@NonNull
	String setURFile(@NonNull String urFile);

	/**
	 * The property for the number of times the service methods have been
	 * invoked.
	 * 
	 * @return The property value (read-only).
	 */
	@GET
	@Path("invokationCount")
	@Produces("text/plain")
	@Description("How many times have the service methods been invoked.")
	int invokeCount();

	/**
	 * The property for the number of runs that are currently in existence.
	 * 
	 * @return The property value (read-only).
	 */
	@GET
	@Path("runCount")
	@Produces("text/plain")
	@Description("How many runs are currently in existence.")
	int runCount();

	/**
	 * Get the location of the RMI registry.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("registryHost")
	@Produces("text/plain")
	@Description("Where is the RMI registry?")
	@NonNull
	String getRegistryHost();

	/**
	 * Set the location of the RMI registry.
	 * 
	 * @param registryHost
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("registryHost")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Where is the RMI registry?")
	@NonNull
	String setRegistryHost(@NonNull String registryHost);

	/**
	 * Get the port of the RMI registry.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("registryPort")
	@Produces("text/plain")
	@Description("On what port is the RMI registry?")
	int getRegistryPort();

	/**
	 * Set the port of the RMI registry.
	 * 
	 * @param registryPort
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("registryPort")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("On what port is the RMI registry?")
	int setRegistryPort(int registryPort);

	/**
	 * Get the maximum number of simultaneous runs.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("runLimit")
	@Produces("text/plain")
	@Description("What is the maximum number of simultaneous runs?")
	int getRunLimit();

	/**
	 * Set the maximum number of simultaneous runs.
	 * 
	 * @param runLimit
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("runLimit")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What is the maximum number of simultaneous runs?")
	int setRunLimit(int runLimit);

	/**
	 * Get the default lifetime of workflow runs.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("defaultLifetime")
	@Produces("text/plain")
	@Description("What is the default lifetime of workflow runs, in seconds?")
	int getDefaultLifetime();

	/**
	 * Set the default lifetime of workflow runs.
	 * 
	 * @param defaultLifetime
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("defaultLifetime")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What is the default lifetime of workflow runs, in seconds?")
	int setDefaultLifetime(int defaultLifetime);

	/**
	 * The property for the list of IDs of current runs.
	 * 
	 * @return The property value (read-only).
	 */
	@GET
	@Path("currentRuns")
	@Produces({ "application/xml", "application/json" })
	@Description("List the IDs of all current runs.")
	StringList currentRuns();

	/**
	 * Get the Java binary to be used for execution of subprocesses.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("javaBinary")
	@Produces("text/plain")
	@Description("Which Java binary should be used for execution of subprocesses?")
	@NonNull
	String getJavaBinary();

	/**
	 * Set the Java binary to be used for execution of subprocesses.
	 * 
	 * @param javaBinary
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("javaBinary")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("Which Java binary should be used for execution of subprocesses?")
	@NonNull
	String setJavaBinary(@NonNull String javaBinary);

	/**
	 * Get the extra arguments to be supplied to Java subprocesses.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("extraArguments")
	@Produces("text/plain")
	@Description("What extra arguments should be supplied to Java subprocesses?")
	@NonNull
	StringList getExtraArguments();

	/**
	 * Set the extra arguments to be supplied to Java subprocesses.
	 * 
	 * @param extraArguments
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("extraArguments")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What extra arguments should be supplied to Java subprocesses?")
	@NonNull
	StringList setExtraArguments(@NonNull StringList extraArguments);

	/**
	 * Get the full pathname of the worker JAR file.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("serverWorkerJar")
	@Produces("text/plain")
	@Description("What is the full pathname of the server's per-user worker executable JAR file?")
	@NonNull
	String getServerWorkerJar();

	/**
	 * Set the full pathname of the worker JAR file.
	 * 
	 * @param serverWorkerJar
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("serverWorkerJar")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What is the full pathname of the server's per-user worker executable JAR file?")
	@NonNull
	String setServerWorkerJar(@NonNull String serverWorkerJar);

	/**
	 * Get the full pathname of the executeWorkflow.sh file.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("executeWorkflowScript")
	@Produces("text/plain")
	@Description("What is the full pathname of the core Taverna executeWorkflow script?")
	@NonNull
	String getExecuteWorkflowScript();

	/**
	 * Set the full pathname of the executeWorkflow.sh file.
	 * 
	 * @param executeWorkflowScript
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("executeWorkflowScript")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What is the full pathname of the core Taverna executeWorkflow script?")
	@NonNull
	String setExecuteWorkflowScript(@NonNull String executeWorkflowScript);

	/**
	 * Get the total duration of time to wait for the start of the
	 * forker process.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("registrationWaitSeconds")
	@Produces("text/plain")
	@Description("How long in total should the core wait for registration of the \"forker\" process, in seconds.")
	int getRegistrationWaitSeconds();

	/**
	 * Set the total duration of time to wait for the start of the
	 * forker process.
	 * 
	 * @param registrationWaitSeconds
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("registrationWaitSeconds")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("How long in total should the core wait for registration of the \"forker\" process, in seconds.")
	int setRegistrationWaitSeconds(int registrationWaitSeconds);

	/**
	 * Get the interval between checks for registration of the
	 * forker process.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("registrationPollMillis")
	@Produces("text/plain")
	@Description("What is the interval between checks for registration of the \"forker\" process, in milliseconds.")
	int getRegistrationPollMillis();

	/**
	 * Set the interval between checks for registration of the
	 * forker process.
	 * 
	 * @param registrationPollMillis
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("registrationPollMillis")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What is the interval between checks for registration of the \"forker\" process, in milliseconds.")
	int setRegistrationPollMillis(int registrationPollMillis);

	/**
	 * Get the full pathname of the file containing the
	 * impersonation credentials for the forker process.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("runasPasswordFile")
	@Produces("text/plain")
	@Description("What is the full pathname of the file containing the password used for impersonating other users? (On Unix, this is the password for the deployment user to use \"sudo\".)")
	@NonNull
	String getRunasPasswordFile();

	/**
	 * Set the full pathname of the file containing the
	 * impersonation credentials for the forker process.
	 * 
	 * @param runasPasswordFile
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("runasPasswordFile")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What is the full pathname of the file containing the password used for impersonating other users? (On Unix, this is the password for the deployment user to use \"sudo\".)")
	@NonNull
	String setRunasPasswordFile(@NonNull String runasPasswordFile);

	/**
	 * Get the full pathname of the forker's JAR.
	 * 
	 * @return The current setting.
	 */
	@GET
	@Path("serverForkerJar")
	@Produces("text/plain")
	@Description("What is the full pathname of the server's special authorized \"forker\" executable JAR file?")
	@NonNull
	String getServerForkerJar();

	/**
	 * Set the full pathname of the forker's JAR.
	 * 
	 * @param serverForkerJar
	 *            What to set it to.
	 * @return The new setting.
	 */
	@PUT
	@Path("serverForkerJar")
	@Consumes("text/plain")
	@Produces("text/plain")
	@Description("What is the full pathname of the server's special authorized \"forker\" executable JAR file?")
	@NonNull
	String setServerForkerJar(@NonNull String serverForkerJar);

	/**
	 * The property for the length of time it took to start the forker.
	 * 
	 * @return The property value (read-only).
	 */
	@GET
	@Path("startupTime")
	@Produces("text/plain")
	@Description("How long did it take for the back-end \"forker\" to set itself up, in seconds.")
	int startupTime();

	/**
	 * The property for the last exit code of the forker process.
	 * 
	 * @return The property value (read-only).
	 */
	@GET
	@Path("lastExitCode")
	@Produces("text/plain")
	@Description("What was the last exit code of the \"forker\"? If null, no exit has ever been recorded.")
	Integer lastExitCode();

	/**
	 * The property for the mapping of usernames to factory process handles.
	 * 
	 * @return The property value (read-only).
	 */
	@GET
	@Path("factoryProcessMapping")
	@Produces({ "application/xml", "application/json" })
	@Description("What is the mapping of local usernames to factory process RMI IDs?")
	StringList factoryProcessMapping();

	/**
	 * The property for the list of usage records collected.
	 * 
	 * @return The property value (read-only).
	 */
	@GET
	@Path("usageRecords")
	@Produces("application/xml")
	@Description("What is the list of usage records that have been collected?")
	URList usageRecords();

	// -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

	/**
	 * The description of what properties are supported by the administration
	 * interface.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement(name = "description")
	@XmlType(name = "Description")
	public static class AdminDescription extends VersionedElement {
		public Uri allowNew;
		public Uri logWorkflows;
		public Uri logFaults;
		public Uri usageRecordDumpFile;
		public Uri invokationCount;
		public Uri runCount;
		public Uri registryHost;
		public Uri registryPort;
		public Uri runLimit;
		public Uri defaultLifetime;
		public Uri currentRuns;
		public Uri javaBinary;
		public Uri extraArguments;
		public Uri serverWorkerJar;
		public Uri serverForkerJar;
		public Uri executeWorkflowScript;
		public Uri registrationWaitSeconds;
		public Uri registrationPollMillis;
		public Uri runasPasswordFile;
		public Uri startupTime;
		public Uri lastExitCode;
		public Uri factoryProcessMapping;
		public Uri usageRecords;

		public AdminDescription() {
		}

		public AdminDescription(UriInfo ui) {
			allowNew = new Uri(ui, "allowNew");
			logWorkflows = new Uri(ui, "logWorkflows");
			logFaults = new Uri(ui, "logFaults");
			usageRecordDumpFile = new Uri(ui, "usageRecordDumpFile");
			invokationCount = new Uri(ui, "invokationCount");
			runCount = new Uri(ui, "runCount");
			registryHost = new Uri(ui, "registryHost");
			registryPort = new Uri(ui, "registryPort");
			runLimit = new Uri(ui, "runLimit");
			defaultLifetime = new Uri(ui, "defaultLifetime");
			currentRuns = new Uri(ui, "currentRuns");
			javaBinary = new Uri(ui, "javaBinary");
			extraArguments = new Uri(ui, "extraArguments");
			serverWorkerJar = new Uri(ui, "serverWorkerJar");
			serverForkerJar = new Uri(ui, "serverForkerJar");
			executeWorkflowScript = new Uri(ui, "executeWorkflowScript");
			registrationWaitSeconds = new Uri(ui, "registrationWaitSeconds");
			registrationPollMillis = new Uri(ui, "registrationPollMillis");
			runasPasswordFile = new Uri(ui, "runasPasswordFile");
			startupTime = new Uri(ui, "startupTime");
			lastExitCode = new Uri(ui, "lastExitCode");
			factoryProcessMapping = new Uri(ui, "factoryProcessMapping");
			usageRecords = new Uri(ui, "usageRecords");
		}
	}

	/**
	 * A list of strings, as XML.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement(name = "stringList")
	@XmlType(name = "StringList")
	public static class StringList {
		@XmlElement
		public List<String> string;
	}

	/**
	 * A list of usage records, as XML.
	 * 
	 * @author Donal Fellows
	 */
	@XmlRootElement(name = "usageRecordList")
	@XmlType(name = "UsageRecords")
	public static class URList {
		@XmlElement
		public List<JobUsageRecord> usageRecord;
	}
}
