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
package net.sf.taverna.t2.activities.localworker;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.HostInstitution;

public class LocalworkerActivity extends BeanshellActivity{
	
	@Override
	public LocalworkerActivityConfigurationBean getConfiguration() {
		super.getConfiguration();
		LocalworkerActivityConfigurationBean result = null;
		if (configurationBean == null) {
			return result;
		}

		if (configurationBean instanceof LocalworkerActivityConfigurationBean) {
			result = (LocalworkerActivityConfigurationBean) configurationBean;
		} else {
			result = new LocalworkerActivityConfigurationBean();
			result.setScript(configurationBean.getScript());
			result.setDependencies(configurationBean.getDependencies());
			result.setLocalworkerName(null);
			configurationBean = result;
		}
		return result;
	}

	/**
	 * Check if the activity has been made into a Beanshell
	 * @return
	 */
	public boolean isAltered() {
		for (AnnotationChain chain : getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				Object detail = assertion.getDetail();
				if (detail instanceof HostInstitution) {
					return true;
				}
			}
		}
		return false;		
	}

}
