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
package net.sf.taverna.t2.activities.sadi.views;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Panel for displaying html in SADI views.
 * 
 * @author David Withers
 */
public class SADIHtmlPanel extends JEditorPane {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SADIActivityContextualView.class);

	private List<Table> tables = new ArrayList<Table>();

	/**
	 * Constructs a new SADIHtmlPanel.
	 */
	public SADIHtmlPanel() {
		setEditable(false);
		setContentType("text/html");
		if (Desktop.isDesktopSupported()) {
			addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent event) {
					if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						URL url = event.getURL();
						try {
							Desktop.getDesktop().browse(url.toURI());
						} catch (IOException e) {
							logger.warn("Failed to open " + url, e);
						} catch (URISyntaxException e) {
							logger.warn("Not a valid URI : " + url, e);
						}
					}
				}
			});
		}
	}

	/**
	 * Updates the contents of the html panel.
	 */
	public void update() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head>" + getStyle() + "</head><body>");
		for (Table table : tables) {
			sb.append(table);
		}
		sb.append("</body></html>");
		setText(sb.toString());
	}

	/**
	 * Creates a new html table and adds it to the panel.
	 * 
	 * @return a new html table
	 */
	public Table createTable() {
		Table table = new Table();
		tables.add(table);
		return table;
	}

	/**
	 * Removes all the html tables from the panel.
	 */
	public void clearTables() {
		tables.clear();
	}

	private String getStyle() {
		StringBuilder sb = new StringBuilder();
		sb.append("<style type='text/css'>");
		sb.append("table {width:100%} ");
		sb.append("td.first {width:0%; font-weight:bold; color:#4370b7} ");
		sb.append("td.last {width:100%} ");
		sb.append("tr.section {font-weight:bold} ");
		sb.append("tr.details {background-color:#eaf0f8} ");
		sb.append("tr.even {background-color:#e6efef} ");
		sb.append("tr.odd {background-color:#f4f8fc} ");
		sb.append("</style>");
		return sb.toString();
	}

	private String aTag(URL url) {
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"");
		sb.append(url);
		sb.append("\">");
		sb.append(url);
		sb.append("</a>");
		return sb.toString();
	}

	class Table {
		private List<String> rows = new ArrayList<String>();

		/**
		 * Adds a section to the table.
		 * 
		 * @param title the title of the section
		 */
		public void addSection(String title) {
			rows.add("<tr class=section><td colspan=2>" + title + "</td></tr>");
		}
		
		/**
		 * Adds a property to the table.
		 * 
		 * @param property the property
		 * @param value the value
		 * @param type
		 */
		public void addProperty(String property, String value, String type) {
			addProperty(property, value, null, type);
		}

		/**
		 * Adds a property to the table.
		 * 
		 * @param property the property
		 * @param value the value
		 * @param hover text that will appear when the mouse hovers over the cell
		 * @param type
		 */
		public void addProperty(String property, String value, String hover, String type) {
			StringBuilder sb = new StringBuilder();
			sb.append("<tr class=");
			sb.append(type);
			sb.append(">");
			sb.append("<td  class=first align=right>");
			sb.append(property);
			sb.append("</td><td class=last");
			if (hover != null) {
				sb.append(" title='");
				sb.append(StringEscapeUtils.escapeHtml(hover));
				sb.append("'");
			}
			sb.append(">");
			try {
				sb.append(aTag(new URL(value)));
			} catch (MalformedURLException e) {
				sb.append(value);
			}
			sb.append("</td></tr>");
			rows.add(sb.toString());
		}

		public String toString() {
			StringBuilder html = new StringBuilder();
			html.append("<table>");
			for (String row : rows) {
				html.append(row);
			}
			html.append("</table>");
			return html.toString();
		}
	}

}
