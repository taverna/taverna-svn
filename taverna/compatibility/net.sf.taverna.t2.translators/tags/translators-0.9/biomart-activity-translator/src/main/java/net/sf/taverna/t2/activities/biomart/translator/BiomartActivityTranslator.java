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
package net.sf.taverna.t2.activities.biomart.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;

import org.embl.ebi.escience.scufl.Processor;
import org.jdom.Element;
import org.jdom.Namespace;


/**
 * An ActivityTranslator specifically for translating Taverna 1 Biomart Processors to a Taverna 2 Biomart Activity
 * 
 * @see ActivityTranslator
 * @author David Withers
 */
public class BiomartActivityTranslator extends AbstractActivityTranslator<Element> {

	@Override
	protected BiomartActivity createUnconfiguredActivity() {
		return new BiomartActivity();
	}

	@Override
	protected Element createConfigType(
			Processor processor) throws ActivityTranslationException {
		return getQueryElement(processor);
	}
	
	private Element getQueryElement(Processor processor) throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getQueryElement", Namespace.class);
			return (Element) method.invoke(processor, (Object) null);
		} catch (SecurityException e) {
			throw new ActivityTranslationException("The was a Security exception whilst trying to invoke getQueryElement through introspection",e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException("The service does not have the method getQueryElement, and therefore does not conform to being a Biomart service",e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException("The method getQueryElement on the Biomart service had unexpected arguments",e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException("Unable to access the method getQueryElement on the Biomart service",e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException("An error occurred invoking the method getQueryElement on the Biomart service",e);
		}
	}
	
	public boolean canHandle(Processor processor) {
		return processor != null && processor.getClass().getName().equals("org.embl.ebi.escience.scuflworkers.biomart.BiomartProcessor");
	}

}
