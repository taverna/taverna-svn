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
package net.sf.taverna.t2.activities.stringconstant.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;

/**
 * An ActivityTranslator specifically for translating a Taverna 1 StringConstant Processor to an equivalent Taverna 2 StringConstant Activity
 * 
 * @see ActivityTranslator
 * @author Stuart Owen
 */
public class StringConstantActivityTranslator extends AbstractActivityTranslator<StringConstantConfigurationBean> {

	@Override
	protected StringConstantConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		StringConstantConfigurationBean bean = new StringConstantConfigurationBean();
		bean.setValue(determineValue(processor));
		return bean;
	}

	@Override
	protected Activity<StringConstantConfigurationBean> createUnconfiguredActivity() {
		return new StringConstantActivity();
	}
	
	private String determineValue(Processor processor) throws ActivityTranslationException {
		try {
			Method m=processor.getClass().getMethod("getStringValue");
			return (String)m.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getStringValue through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The processor does not have the method getStringValue, an therefore does not conform to being a StringConstant processor",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getStringValue on the StringConstant processor had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getStringValue on the StringConstant processor",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getStringValue on the StringConstant processor",e);
		}
	}
	
	public boolean canHandle(Processor processor) {
		return processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessor");
	}

}
