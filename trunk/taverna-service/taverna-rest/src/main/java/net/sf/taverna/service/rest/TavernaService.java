package net.sf.taverna.service.rest;

import net.sf.taverna.service.queue.DefaultQueueMonitor;
import net.sf.taverna.service.rest.utils.UserUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class TavernaService {

	private static final int DEFAULT_PORT = 8976;

	private static final String ADMIN = "makeadmin";

	private static final String PORT = "port";

	private static final String HELP = "help";

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
		if (line.hasOption(ADMIN)) {
			String adminUsername = "admin";
			String password = UserUtils.resetPassword(adminUsername);
			UserUtils.makeAdmin(adminUsername);
			System.out.println("Admin user '" + adminUsername + "' with password: " + password);
			System.exit(0);
		}
		int port = DEFAULT_PORT;
		if (line.hasOption(PORT)) {
			port = (Integer) line.getOptionObject(PORT);
		}
		DefaultQueueMonitor queueMonitor = new DefaultQueueMonitor();
		queueMonitor.start();
		new RestApplication().startServer(port);
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
				"Create/recreate an admin account 'admin'. The new admin password "
					+ "is returned, the server is not started.").create(ADMIN);
		options.addOption(adminOption);
		return options;
	}
}
