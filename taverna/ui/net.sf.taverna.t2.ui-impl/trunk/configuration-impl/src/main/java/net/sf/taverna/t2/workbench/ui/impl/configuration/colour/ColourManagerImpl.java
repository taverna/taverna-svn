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
package net.sf.taverna.t2.workbench.ui.impl.configuration.colour;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;

/**
 * A factory class that determines the colour that a Colourable UI component
 * should be displayed as, according to a schema configured by the user.
 *
 * @author Stuart Owen
 * @author Ian Dunlop
 * @see Colourable
 *
 */
public class ColourManagerImpl extends AbstractConfigurable implements ColourManager {

	private Map<String, String> defaultPropertyMap;
	private Map<Object,Color> cachedColours;

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getCategory()
	 */
	@Override
	public String getCategory() {
		return "colour";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getDefaultPropertyMap()
	 */
	@Override
	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap==null) initialiseDefaults();
		return defaultPropertyMap;
	}

	@Override
	public String getDisplayName() {
		return "Colour Management";
	}

	@Override
	public String getFilePrefix() {
		return "ColourManagement";
	}

	/**
	 * Unique identifier for this ColourManager
	 */
	@Override
	public String getUUID() {
		return "a2148420-5967-11dd-ae16-0800200c9a66";
	}

	public ColourManagerImpl() {
		initialiseDefaults();
	}

	private void initialiseDefaults() {
		defaultPropertyMap=new HashMap<String, String>();
		cachedColours=new HashMap<Object, Color>();
		defaultPropertyMap.put("net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity", "#98fb98");//palegreen
		defaultPropertyMap.put("net.sf.taverna.t2.activities.beanshell.BeanshellActivity", "#deb887");//burlywood2
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomart.BiomartActivity", "#d1eeee");//lightcyan2
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomoby.BiomobyActivity", "#ffb90f");//darkgoldenrod1
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomoby.BiomobyObjectActivity", "#ffd700");//gold
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomoby.MobyParseDatatypeActivity", "#ffffff");//white
		defaultPropertyMap.put("net.sf.taverna.t2.activities.dataflow.DataflowActivity", "#ffc0cb");//pink
		defaultPropertyMap.put("net.sf.taverna.t2.activities.rshell.RshellActivity", "#648faa");//slightly lighter than steelblue4
		defaultPropertyMap.put("net.sf.taverna.t2.activities.soaplab.SoaplabActivity", "#fafad2");//lightgoldenrodyellow
		defaultPropertyMap.put("net.sf.taverna.t2.activities.stringconstant.StringConstantActivity", "#b0c4de");//lightsteelblue
		defaultPropertyMap.put("net.sf.taverna.t2.activities.wsdl.WSDLActivity", "#a2cd5a");//darkolivegreen3
		defaultPropertyMap.put("net.sf.taverna.t2.activities.localworker.LocalworkerActivity", "#d15fee"); //mediumorchid2
		defaultPropertyMap.put("net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLInputSplitterActivity", "#ab92ea"); //light purple non standard
		defaultPropertyMap.put("net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLOutputSplitterActivity", "#ab92ea"); //light purple non-standard
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.Merge", "#77aadd");
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.Processor", "#a1c69d"); // ShadedLabel.Green
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.ProcessorPort", "#8070ff"); // purplish
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.DataflowPort", "#eece8f"); // ShadedLabel.Orange
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.processor.activity.NonExecutableActivity", "#777777");

	}

	@Override
	public Color getPreferredColour(String itemKey) {
		Color colour = cachedColours.get(itemKey);
		if (colour == null) {
			String colourString=(String)getProperty(itemKey);
			colour = colourString==null ? Color.WHITE : Color.decode(colourString);
			cachedColours.put(itemKey,colour);
		}
		return colour;
	}

	@Override
	public void setPreferredColour(String itemKey, Color colour) {
		cachedColours.put(itemKey,colour);
	}

	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		if (cachedColours==null) {
			cachedColours=new HashMap<Object, Color>();
		}
		else {
			cachedColours.clear();
		}
	}

}
