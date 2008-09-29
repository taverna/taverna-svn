/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyDataType;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyRelationship;
import org.biomoby.shared.NoSuccessException;
import org.embl.ebi.escience.scufl.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * A processor that breaks up a Moby datatype into its component parts minus all
 * the moby wrappings.
 * 
 * @author Eddie Kawas
 */
public class MobyParseDatatypeProcessor extends Processor implements java.io.Serializable {

	private String datatypeName = "";

	private String registryEndpoint = "";

	private String workflowName = "";

	private String articleNameUsedByService = "";

	private Central central = null;

	private MobyDataType datatype = null;

	/**
	 * 
	 * @param model a scufl model
	 * @param workflowName the name of this processor
	 * @param datatypeName the name of the datatype that we are processing
	 * @param articleName the article name of the datatype
	 * @param registryEndpoint the endpoint where we can find information about the datatype
	 * @throws ProcessorCreationException
	 * @throws DuplicateProcessorNameException
	 */
	public MobyParseDatatypeProcessor(ScuflModel model, String workflowName, String datatypeName,
			String articleName, String registryEndpoint) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, workflowName);
		// set processor information
		this.datatypeName = datatypeName;
		this.workflowName = workflowName;
		this.articleNameUsedByService = articleName;
		this.registryEndpoint = registryEndpoint;

		if (this.isOffline()) {
			// throw new ProcessorCreationException(
			// "Parsing widget can only be created when you are online.");
		} else {
			try {
				init();
			} catch (DuplicatePortNameException e) {
				throw new ProcessorCreationException(
						"There was an error creating the parser. If the error was service specific please contact the service provider."
								+ e.getLocalizedMessage());
			} catch (PortCreationException e) {
				throw new ProcessorCreationException(
						"There was an error creating the parser. If the error was service specific please contact the service provider."
								+ e.getLocalizedMessage());
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @param workflowName
	 * @param datatype
	 * @param articleName
	 * @param registryEndpoint
	 * @throws ProcessorCreationException
	 * @throws DuplicateProcessorNameException
	 */
	public MobyParseDatatypeProcessor(ScuflModel model, String workflowName, MobyDataType datatype,
			String articleName, String registryEndpoint) throws ProcessorCreationException,
			DuplicateProcessorNameException {
		super(model, workflowName);
		// set processor information
		this.datatypeName = datatype.getName();
		this.datatype = datatype;
		this.workflowName = workflowName;
		this.articleNameUsedByService = articleName;
		this.registryEndpoint = registryEndpoint;

		if (this.isOffline()) {
			// throw new ProcessorCreationException(
			// "Parsing widget can only be created when you are online.");
		} else {
			try {
				init();
			} catch (DuplicatePortNameException e) {
				throw new ProcessorCreationException(
						"There was an error creating the parser. If the error was service specific please contact the service provider."
								+ e.getLocalizedMessage());
			} catch (PortCreationException e) {
				throw new ProcessorCreationException(
						"There was an error creating the parser. If the error was service specific please contact the service provider."
								+ e.getLocalizedMessage());
			}
		}
	}

	/*
	 * initializes this processor
	 */
	private void init() throws ProcessorCreationException, DuplicatePortNameException,
			PortCreationException {
		try {
			central = new CentralImpl(this.registryEndpoint);
		} catch (MobyException e) {
			throw new ProcessorCreationException("Couldn't create MobyCentral client for endpoint "
					+ this.registryEndpoint + System.getProperty("line.separator")
					+ e.getLocalizedMessage());
		}
		if (this.datatype == null) {
			try {
				this.datatype = central.getDataType(this.datatypeName);
			} catch (MobyException e) {
				throw new ProcessorCreationException(
						"There was a problem getting information from the MobyCentral registry at "
								+ this.registryEndpoint + System.getProperty("line.separator")
								+ e.getLocalizedMessage());
			} catch (NoSuccessException e) {
				throw new ProcessorCreationException(
						"There was no success in getting information from the MobyCentral registry at "
								+ this.registryEndpoint + System.getProperty("line.separator")
								+ e.getLocalizedMessage());
			}
		}

		setDescription("Processor to parse the datatype " + this.datatype.getName());
		
		ArrayList list = new ArrayList();
		if (isPrimitive(this.datatype.getName())) {
			list.add(this.articleNameUsedByService + "_" + this.datatype.getName());
		} else if (this.datatype.getName().equals("Object")) {
			// dont do anything because object has no value
		} else {
			processDatatype(this.datatype, central, this.articleNameUsedByService, list);
		}
		// add the input port called mobyData('datatypeName')
		Port inPort = new InputPort(this, "mobyData('" + this.datatype.getName() + "')");
		inPort.setSyntacticType("'text/xml'");
		this.addPort(inPort);
		// add the namespace/id ports to the processor
		Port ns = new OutputPort(this, "namespace");
		ns.setSyntacticType("'text/plain'");
		this.addPort(ns);
		Port id = new OutputPort(this, "id");
		id.setSyntacticType("'text/plain'");
		this.addPort(id);

		// list contains the output ports i have to create
		for (Iterator it = list.iterator(); it.hasNext();) {
			String portName = (String) it.next();
			if (portName.equals(this.articleNameUsedByService+"_id") || portName.equals(this.articleNameUsedByService+"_ns"))
				continue;
			Port outputPort = new OutputPort(this, portName);
			outputPort.setSyntacticType("'text/plain'");
			this.addPort(outputPort);

		}
	}

	/**
	 * Get the properties for this processor for display purposes
	 */
	public Properties getProperties() {
		Properties props = new Properties();
		return props;
	}

	private boolean isPrimitive(String name) {
		if (name.equals("Integer") || name.equals("String") || name.equals("Float")
				|| name.equals("DateTime") || name.equals("Boolean"))
			return true;
		return false;
	}

	private void processDatatype(MobyDataType dt, Central central, String currentName, List list)
			throws ProcessorCreationException {

		if (!dt.getParentName().equals("Object")) {
			flattenChildType(dt.getParentName(), central, currentName, list);
		} else {
			list
			.add(currentName + "_id");
			list
			.add(currentName + "_ns");
		}

		MobyRelationship[] relations = dt.getChildren();
		for (int i = 0; i < relations.length; i++) {
			MobyRelationship relation = relations[i];
			switch (relation.getRelationshipType()) {
			case CentralImpl.iHAS: {
				if (isPrimitive(relation.getDataTypeName())) {
					list
							.add(currentName + (currentName.equals("") ? "" : "_'")
									+ relation.getName() + (currentName.equals("") ? "" : "'"));
					list
					.add(currentName + (currentName.equals("") ? "" : "_'")
							+ relation.getName() + (currentName.equals("") ? "" : "'")+"_id");
					list
					.add(currentName + (currentName.equals("") ? "" : "_'")
							+ relation.getName() + (currentName.equals("") ? "" : "'")+"_ns");
				}
				else {
					flattenChildType(relation.getDataTypeName(), central, currentName
							+ (currentName.equals("") ? "" : "_'") + relation.getName() + (currentName.equals("") ? "" : "'"), list);
				}
			}
				break;
			case CentralImpl.iHASA: {
				if (isPrimitive(relation.getDataTypeName())) {
					list
							.add(currentName + (currentName.equals("") ? "" : "_'")
									+ relation.getName()+ (currentName.equals("") ? "" : "'"));
					list
					.add(currentName + (currentName.equals("") ? "" : "_'")
							+ relation.getName() + (currentName.equals("") ? "" : "'")+"_id");
					list
					.add(currentName + (currentName.equals("") ? "" : "_'")
							+ relation.getName() + (currentName.equals("") ? "" : "'")+"_ns");
				}
				else {

					flattenChildType(relation.getDataTypeName(), central, currentName
							+ (currentName.equals("") ? "" : "_'") + relation.getName() + (currentName.equals("") ? "" : "'"), list);
				}
			}
				break;
			default:
				break;
			}
		}

	}

	private void flattenChildType(String name, Central central, String current, List list)
			throws ProcessorCreationException {
		MobyDataType dt = null;
		try {
			dt = central.getDataType(name);
		} catch (MobyException e) {
			throw new ProcessorCreationException(
					"There was a problem getting information from the MobyCentral registry at "
							+ this.registryEndpoint + System.getProperty("line.separator")
							+ e.getLocalizedMessage());
		} catch (NoSuccessException e) {
			throw new ProcessorCreationException(
					"There was no success in getting information from the MobyCentral registry at "
							+ this.registryEndpoint + System.getProperty("line.separator")
							+ e.getLocalizedMessage());
		}
		processDatatype(dt, central, current, list);
	}

	public String getArticleNameUsedByService() {
		return articleNameUsedByService;
	}

	public Central getCentral() {
		return central;
	}

	public MobyDataType getDatatype() {
		return datatype;
	}

	public String getDatatypeName() {
		return datatypeName;
	}

	public String getRegistryEndpoint() {
		return registryEndpoint;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setArticleNameUsedByService(String articleNameUsedByService) {
		this.articleNameUsedByService = articleNameUsedByService;
	}

	public void setCentral(Central central) {
		this.central = central;
	}

	public void setDatatype(MobyDataType datatype) {
		this.datatype = datatype;
	}

	public void setDatatypeName(String datatypeName) {
		this.datatypeName = datatypeName;
	}

	public void setRegistryEndpoint(String registryEndpoint) {
		this.registryEndpoint = registryEndpoint;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
}
