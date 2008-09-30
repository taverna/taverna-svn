package net.sf.taverna.service;

import net.sf.taverna.service.queue.DefaultQueueMonitor;
import net.sf.taverna.service.rest.RestApplication;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.rest.utils.UserUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;

public class TavernaService {

	private static final int DEFAULT_PORT = 8976;

	private static final String MAKE_ADMIN = "makeadmin";

	private static final String START_DB = "startdb";

	private static final String STOP_DB = "stopdb";

	private static final String PORT = "port";
	
	private static final String VELOCITY = "velocity";

	private static final String HELP = "help";

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TavernaService.class);

	public static void main(String[] args) {
		Options options = makeOptions();
		CommandLineParser parser = new GnuParser();
		CommandLine line;
		try {
			// parse the command line arguments
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			System.exit(1);
			return;
		}

		if (line.hasOption(HELP)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(
				"tavernaservice \n"
					+ "Run the Taverna job service. Clients (such as Taverna) "
					+ "and workers (who execute workflows) will communicate with this service "
					+ "using a RESTful HTTP API.", options);
			System.exit(0);
		}

		if (line.hasOption(MAKE_ADMIN)) {
			String adminUsername = "admin";
			String password = line.getOptionValue(MAKE_ADMIN);
			if (password == null || password.equals("")) {
				password = UserUtils.resetPassword(adminUsername);
				System.out.println("Set password " + password + " for " + adminUsername);
			} else {
				UserUtils.resetPassword(adminUsername, password);
				System.out.println("Set password for " + adminUsername);
			}
			
			UserUtils.makeAdmin(adminUsername);
			System.exit(0);
		}

		if (line.hasOption(START_DB)) {
			NetworkServerControl.main(new String[] { "start", "-p", "1337" });
			System.exit(0);
		}

		if (line.hasOption(STOP_DB)) {
			NetworkServerControl.main(new String[] { "shutdown", "-p", "1337" });
			System.exit(0);
		}

		int port = DEFAULT_PORT;
		if (line.hasOption(PORT)) {
			port = (Integer) line.getOptionObject(PORT);
		}
		
		if (line.hasOption(VELOCITY)) {
			VelocityRepresentation.setResourcePath(line.getOptionValue(VELOCITY));
		} else {
			System.err.println("Option -" + VELOCITY + " is required");
			System.exit(1);
		}
		
		RestApplication application = new RestApplication();
		
		URIFactory uriFactory =
			URIFactory.getInstance();
		DefaultQueueMonitor queueMonitor = new DefaultQueueMonitor(uriFactory);
		queueMonitor.start();
		
		application.startServer(port);
	}

	@SuppressWarnings("static-access")
	private static Options makeOptions() {
		Options options = new Options();

		Option helpOption = new Option(HELP, "print this message");
		options.addOption(helpOption);

		Option portOption =
			OptionBuilder.hasArg().withArgName(PORT).withDescription(
				"port to listen to, default is " + DEFAULT_PORT).withType(
				DEFAULT_PORT).create(PORT);
		options.addOption(portOption);

		Option adminOption =
			OptionBuilder.withDescription(
				"Create/recreate an admin account 'admin'. If password is not "
					+ "specified, a random password is created and returned. "
					+ "The server is not started.").hasOptionalArg().withArgName(
				"password").create(MAKE_ADMIN);
		options.addOption(adminOption);

		Option startDb =
			OptionBuilder.withDescription(
				"Start the Apache Derby server. The Taverna server is not started").create(
				START_DB);
		options.addOption(startDb);

		Option stopDb =
			OptionBuilder.withDescription(
				"Stop the Apache Derby server. The Taverna server is not started").create(
				STOP_DB);
		options.addOption(stopDb);
		
		Option velocity =
			OptionBuilder.withDescription("Path to velocity templates").hasArg().withArgName(
				VELOCITY).create(VELOCITY);
		options.addOption(velocity);
		
		return options;
	}
}
