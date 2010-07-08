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
package net.sf.taverna.t2.activities.sadi.report;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.sadi.SADIActivityHealthChecker;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;

import org.apache.log4j.Logger;

/**
 * 
 * @author David Withers
 */
public class SADIActivityExplainer implements VisitExplainer {

	private static Logger logger = Logger.getLogger(SADIActivityExplainer.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.taverna.t2.workbench.report.explainer.VisitExplainer#canExplain
	 * (net.sf.taverna.t2.visit.VisitKind, int)
	 */
	public boolean canExplain(VisitKind vk, int resultId) {
		if (vk instanceof HealthCheck) {
			return resultId == SADIActivityHealthChecker.SADI_SERVICE_SLOW
					|| resultId == SADIActivityHealthChecker.SADI_SERVICE_DEAD;
		}
		return false;
	}

	public JComponent getExplanation(VisitReport vr) {
		VisitKind vk = vr.getKind();
		int resultId = vr.getResultId();

		if (vk instanceof HealthCheck) {
			if (resultId == SADIActivityHealthChecker.SADI_SERVICE_SLOW) {
				return slowServiceExplanation(vr);
			}
			if (resultId == SADIActivityHealthChecker.SADI_SERVICE_DEAD) {
				return deadServiceExplanation(vr);
			}
		}
		return null;
	}

	public JComponent getSolution(VisitReport vr) {
		VisitKind vk = vr.getKind();
		int resultId = vr.getResultId();
		if ((vk instanceof HealthCheck)) {
			if (resultId == SADIActivityHealthChecker.SADI_SERVICE_SLOW) {
				return slowServiceSolution(vr);
			}
			if (resultId == SADIActivityHealthChecker.SADI_SERVICE_DEAD) {
				return deadServiceSolution(vr);
			}
		}
		return null;
	}

	/**
	 * @param vr
	 * @return
	 */
	private JComponent slowServiceExplanation(VisitReport vr) {
		String service = vr.getProperty(SADIActivityHealthChecker.SADI_SERVICE_URI_PROPERTY)
				.toString();
		return createPanel(new Object[] { "The SADI Service Registry reported that the service at "
				+ service + " is running slowly." });
	}

	/**
	 * @param vr
	 * @return
	 */
	private JComponent deadServiceExplanation(VisitReport vr) {
		String service = vr.getProperty(SADIActivityHealthChecker.SADI_SERVICE_URI_PROPERTY)
				.toString();
		return createPanel(new Object[] { "The SADI Service Registry reported that the service at "
				+ service + " is dead." });
	}

	/**
	 * @param vr
	 * @return
	 */
	private JComponent slowServiceSolution(VisitReport vr) {
		return createPanel(new Object[] { "If the service runs too slowly please contact the service provider" });
	}

	/**
	 * @param vr
	 * @return
	 */
	private JComponent deadServiceSolution(VisitReport vr) {
		return createPanel(new Object[] { "Try the service later as it may be temporarily offline.  If the service remains offline, please contact the service provider" });
	}

	private static JPanel createPanel(Object[] components) {
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 1;
		gbc.weightx = 0.9;
		for (Object o : components) {
			if (o == null) {
				continue;
			}
			JComponent component = null;
			if (o instanceof String) {
				component = new ReadOnlyTextArea((String) o);
			} else if (o instanceof JComponent) {
				component = (JComponent) o;
			} else {
				logger.error("Unrecognized component " + o.getClass());
				continue;
			}
			gbc.gridy++;
			if (component instanceof JButton) {
				gbc.weightx = 0;
				gbc.gridwidth = 1;
				gbc.fill = GridBagConstraints.NONE;
			} else {
				gbc.weightx = 0.9;
				gbc.gridwidth = 2;
				gbc.fill = GridBagConstraints.HORIZONTAL;
			}
			result.add(component, gbc);
		}
		result.setBackground(SystemColor.text);
		return result;
	}
}
