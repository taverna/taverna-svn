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
package net.sf.taverna.t2.cloudone.p2p.http;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 * Host for the {@link CloudOneApplication} in stand alone mode (for testing
 * purposes)
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class CloudOneServer {

	private static final int DEFAULT_PORT = 7380;

	private static final String DEFAULT_HOST = "localhost";

	private static final String PORT = "port";

	private static final String HELP = "help";

	private static final String HOST = "host";

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CloudOneServer.class);

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
			formatter.printHelp("cloudoneserver \n"
					+ "Run the CloudOne REST server.", options);
			System.exit(0);
		}

		int port = DEFAULT_PORT;
		if (line.hasOption(PORT)) {
			port = (Integer) line.getOptionObject(PORT);
		}

		String host = DEFAULT_HOST;
		if (line.hasOption(HOST)) {
			host = line.getOptionValue(HOST);
		}

		CloudOneApplication application = new CloudOneApplication(host, port);
		application.startServer();
	}

	@SuppressWarnings("static-access")
	private static Options makeOptions() {
		Options options = new Options();

		Option helpOption = new Option(HELP, "print this message");
		options.addOption(helpOption);

		Option hostOption = OptionBuilder.hasArg().withArgName(HOST)
				.withDescription(
						"hostname for server, default is " + DEFAULT_HOST)
				.create(HOST);
		options.addOption(hostOption);

		Option portOption = OptionBuilder.hasArg().withArgName(PORT)
				.withDescription(
						"port to listen to, default is " + DEFAULT_PORT)
				.withType(DEFAULT_PORT).create(PORT);
		options.addOption(portOption);

		return options;
	}
}
