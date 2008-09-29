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
package net.sf.taverna.t2.workflowmodel.impl;

import java.io.IOException;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.AbstractXMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.AbstractXMLSerializer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * An Edit that is responsible for configuring an Activity with a given
 * configuration bean.
 * 
 * @author Stuart Owen
 */
public class ConfigureActivityEdit extends AbstractActivityEdit {

	private static Logger logger = Logger
			.getLogger(ConfigureActivityEdit.class);

	private final Object configurationBean;

	private Element serializedBean;

	public ConfigureActivityEdit(Activity<?> activity, Object configurationBean) {
		super(activity);
		this.configurationBean = configurationBean;

	}

	class BeanSerialiser extends AbstractXMLSerializer {
		public Element beanAsElement(Object obj) throws JDOMException,
				IOException {
			return super.beanAsElement(obj);
		}
	}

	/**
	 * Deserialise the activity bean using its classloader if it has one,
	 * otherwise use the bean deserialisers
	 * 
	 */
	class BeanDeSerialiser extends AbstractXMLDeserializer {
		public Object createBean(Element configElement) {
			ClassLoader beanClassLoader = BeanDeSerialiser.class.getClassLoader();
			ClassLoader activityClassLoader = getActivity().getConfiguration()
					.getClass().getClassLoader();
			if (activityClassLoader != null) {
				return super.createBean(configElement, activityClassLoader);
			} else {
				return super.createBean(configElement, beanClassLoader);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doEditAction(AbstractActivity<?> activity)
			throws EditException {
		if (activity.getConfiguration() != null) {
			try {
				serializedBean = new BeanSerialiser().beanAsElement(activity
						.getConfiguration());
			} catch (Exception e) {
				logger.error("Error serializing configuration bean for: "
						+ activity);
				throw new EditException(
						"Error serializing configuration bean for: " + activity,
						e);
			}
		} else {
			serializedBean = null;
		}
		try {
			((Activity) activity).configure(configurationBean);
		} catch (ActivityConfigurationException e) {
			logger.error("Error configuring the activity:"
					+ activity.getClass().getSimpleName(), e);
			throw new EditException(e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void undoEditAction(AbstractActivity<?> activity) {
		try {
			if (serializedBean == null) {
				// FIXME: how to reconfigure an Activity that had a null config
				// bean??
			} else {
				Object bean = new BeanDeSerialiser().createBean(serializedBean);
				((Activity<Object>) activity).configure(bean);
			}
		} catch (ActivityConfigurationException e) {
			logger.error("There was an error reconfiguring " + activity
					+ " during an UNDO");
		}
	}

}
