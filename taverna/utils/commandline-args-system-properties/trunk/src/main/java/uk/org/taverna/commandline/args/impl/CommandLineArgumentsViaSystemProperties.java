/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester
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
package uk.org.taverna.commandline.args.impl;

import java.util.ArrayList;
import java.util.List;

import uk.org.taverna.commandline.args.CommandLineArguments;

/**
 * Command line argument service implementation that uses specally named
 * system properties to pass the command line arguments into Taverna.
 *
 * System property "taverna.commandline.args" holds the total number of
 * command line arguments to expect.
 *
 * System property taverna.commandline.arg.<n> hold the <n>th command
 * line argument. Some of these are options names others hold option
 * parameters.
 *
 * @author Alex Nenadic
 *
 */
public class CommandLineArgumentsViaSystemProperties implements CommandLineArguments{

	// System property holding the number of command line arguments to expect
	public static final String TAVERNA_COMMANDLINE_ARGS_NUMBER_SYSTEM_PROPERTY = "taverna.commandline.args";

	// System property prefix indicating the command line argument.
	// Properties are in format taverna.commandline.arg.<n> where <n> indicates the index
	// of the property holding the <n>th command line parameter.
	public static final String TAVERNA_COMMANDLINE_ARG_SYSTEM_PROPERTY = "taverna.commandline.arg";

	public static final char SPACE_REPLACEMENT_CHARACTER = '\013';

	@Override
	public String[] getCommandLineArguments() {
		List<String> arguments = new ArrayList<String>();
		Integer argumentCount = Integer.getInteger(TAVERNA_COMMANDLINE_ARGS_NUMBER_SYSTEM_PROPERTY);
		if (argumentCount != null) {
			for (int i = 0; i < argumentCount; i++) {
				String arg = System.getProperty(TAVERNA_COMMANDLINE_ARG_SYSTEM_PROPERTY + "." + i);
				if (arg != null) {
					arguments.add(arg.replace(SPACE_REPLACEMENT_CHARACTER, ' '));
				}
			}
		}
		return arguments.toArray(new String[arguments.size()]);
	}

}
