package net.sf.taverna.service.backend.executor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.restlet.data.Reference;

/**
 * Execute one or more jobs as a separate process. This means that if everything
 * else fails, this process can be killed.
 * 
 * @author Stian Soiland
 */
public class RestfulExecutionProcess {

	// Command line parameters

	private static final String PASSWORD = "password";

	private static final String USERNAME = "username";

	private static final String BASE = "base";

	/**
	 * System exit codes
	 * 
	 * @author Stian Soiland
	 */
	public enum Exit {
		NORMAL, CONF_CANT_READ, CONF_INVALID, CMD_PARSE, MISSING_JOB, MISSING_BASE, MISSING_USER, MISSING_PW, INTERRUPTED;

		public void exit() {
			System.exit(ordinal());
		}
	}

	private static final String PROGRAM_NAME = "restfulexecution";

	private static final String CONF_NAME = PROGRAM_NAME + ".conf";

	private static final String CONFIG = "config";

	private static final String HELP = "help";

	private static Logger logger =
		Logger.getLogger(RestfulExecutionProcess.class);

	File config = new File(MyGridConfiguration.getUserDir("conf"), CONF_NAME);

	private Properties properties;

	private Reference base;

	private String username;

	private String password;
	
	private URL tavernaHomeURL;

	private List<RestfulExecutionThread> threads =
		new ArrayList<RestfulExecutionThread>();

	public static void main(String[] args) {

		try {
			new RestfulExecutionProcess().exec(args);
		} catch (InterruptedException e) {
			logger.warn("Unexpected interruption", e);
			Exit.INTERRUPTED.exit();
		}
		Exit.NORMAL.exit();
	}

	public void exec(String[] args) throws InterruptedException {
		Options options = makeOptions();
		CommandLineParser parser = new GnuParser();
		CommandLine line;
		try {
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Command line parsing failed.  Reason: "
				+ exp.getMessage());
			Exit.CMD_PARSE.exit();
			return;
		}

		if (line.hasOption(HELP)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(PROGRAM_NAME + " <job-uri> ..\n"
				+ "Execute job described by the job URI at the "
				+ "configured taverna service.", options);
			Exit.NORMAL.exit();
		}
		if (line.hasOption(CONFIG)) {
			config = new File(line.getOptionValue(CONFIG));
		}

		loadProperties();

		if (line.hasOption(BASE)) {
			base = new Reference(line.getOptionValue(BASE));
		}
		if (line.hasOption(USERNAME)) {
			username = line.getOptionValue(USERNAME);
		}
		if (line.hasOption(PASSWORD)) {
			password = line.getOptionValue(PASSWORD);
		}

		if (line.getArgs().length < 1) {
			System.err.println("At least one job URI must be given.");
			Exit.MISSING_JOB.exit();
		}
		if (base == null) {
			System.err.println("Base URI must be given in config file or as parameter");
			Exit.MISSING_BASE.exit();
		}
		if (username == null) {
			System.err.println("Username must be given in config file or as parameter");
			Exit.MISSING_USER.exit();
		}
		if (password == null) {
			System.err.println("Password must be given in config file or as parameter");
			Exit.MISSING_PW.exit();
		}

		startThreads(line.getArgs());
		joinThreads();
	}

	public void loadProperties() {
		properties = new Properties();
		if (!config.exists()) {
			logger.info("Did not exist: " + config);
			return;
		}
		try {
			properties.load(config.toURI().toURL().openStream());
		} catch (MalformedURLException e) {
			logger.error("Invalid configuration filename " + config, e);
			Exit.CONF_INVALID.exit();
		} catch (IOException e) {
			logger.warn("Could not read configuration " + config, e);
			Exit.CONF_CANT_READ.exit();
		}

		base = new Reference((String) properties.get(BASE));
		username = (String) properties.get(USERNAME);
		password = (String) properties.get(PASSWORD);
	}

	public void startThreads(String[] uris) {
		
		for (String jobURI : uris) {
			RestfulExecutionThread thread =
				new RestfulExecutionThread(jobURI, base.toString(), username,
					password);
			threads.add(thread);
			thread.setDaemon(true);
			thread.start();
		}
	}

	public void joinThreads() throws InterruptedException {
		// TODO: Monitor the threads as they are running
		for (RestfulExecutionThread thread : threads.toArray(new RestfulExecutionThread[0])) {
			// TODO: Timeout instead of waiting forever
			thread.join();
			if (!thread.isAlive()) {
				threads.remove(thread);
			}
		}
	}

	@SuppressWarnings("static-access")
	public Options makeOptions() {
		Options options = new Options();

		Option helpOption = new Option(HELP, "print this message");
		options.addOption(helpOption);

		Option configOption =
			OptionBuilder.hasArg().withArgName("file").withDescription(
				"configuration file, by default: " + config).create(CONFIG);
		options.addOption(configOption);

		Option baseOption =
			OptionBuilder.hasArg().withArgName("uri").withDescription(
				"base URI for taverna service").create(BASE);
		options.addOption(baseOption);

		Option userOption =
			OptionBuilder.hasArg().withArgName("user").withDescription(
				"worker's username for taverna service").create(USERNAME);
		options.addOption(userOption);

		Option pwOption =
			OptionBuilder.hasArg().withArgName("pw").withDescription(
				"worker's password for taverna service").create(PASSWORD);
		options.addOption(pwOption);
		return options;
	}
}
