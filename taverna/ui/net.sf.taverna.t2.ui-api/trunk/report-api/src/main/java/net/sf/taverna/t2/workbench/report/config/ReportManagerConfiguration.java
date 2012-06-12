/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workbench.report.config;

import uk.org.taverna.configuration.Configurable;

/**
 *
 *
 * @author David Withers
 */
public interface ReportManagerConfiguration extends Configurable {

	public static final String TIMEOUT = "TIMEOUT";
	public static final String ON_EDIT = "ON_EDIT";
	public static final String ON_OPEN = "ON_OPEN";
	public static final String BEFORE_RUN = "BEFORE_RUN";
	public static final String NO_CHECK = "NoCheck";
	public static final String QUICK_CHECK = "QuickCheck";
	public static final String FULL_CHECK = "FullCheck";
	public static final String NONE = "Do not care";
	public static final String ERRORS_OR_WARNINGS = "Errors or warnings";
	public static final String ERRORS = "Errors";
	public static final String QUERY_BEFORE_RUN = "QUERY_BEFORE_RUN";
	public static final int DEFAULT_REPORT_EXPIRATION = 0;
	public static final String REPORT_EXPIRATION = "REPORT_EXPIRATION";

	public void applySettings();

}