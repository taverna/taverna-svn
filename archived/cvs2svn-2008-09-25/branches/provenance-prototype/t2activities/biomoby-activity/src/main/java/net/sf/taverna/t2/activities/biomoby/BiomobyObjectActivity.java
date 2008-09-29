package net.sf.taverna.t2.activities.biomoby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;
import org.biomoby.client.CentralImpl;
import org.biomoby.client.taverna.plugin.XMLUtilities;
import org.biomoby.registry.meta.Registry;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyDataType;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyNamespace;
import org.biomoby.shared.MobyRelationship;
import org.biomoby.shared.NoSuccessException;
import org.biomoby.shared.data.MobyDataBoolean;
import org.biomoby.shared.data.MobyDataComposite;
import org.biomoby.shared.data.MobyDataDateTime;
import org.biomoby.shared.data.MobyDataFloat;
import org.biomoby.shared.data.MobyDataInstance;
import org.biomoby.shared.data.MobyDataInt;
import org.biomoby.shared.data.MobyDataString;
import org.jdom.Element;

/**
 * An Activity providing Biomoby Object functionality.
 * 
 * Copied from org.biomoby.client.taverna.plugin.BiomobyObjectProcessor and
 * org.biomoby.client.taverna.plugin.BiomobyObjectTask and converted to a Taverna 2
 * Activity.
 * 
 * @author Edward Kawas
 * @author David Withers
 */
public class BiomobyObjectActivity extends AbstractAsynchronousActivity<BiomobyObjectActivityConfigurationBean> {

    private static Logger logger = Logger.getLogger(BiomobyActivity.class);

    private BiomobyObjectActivityConfigurationBean configurationBean;

    private Central worker = null;

    private MobyDataType mobyObject = null;

    @Override
	public void configure(BiomobyObjectActivityConfigurationBean configurationBean) throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		init();
		generatePorts();
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				DataFacade dataFacade = new DataFacade(callback.getContext()
						.getDataManager());

				Map<String, EntityIdentifier> outputMap = new HashMap<String, EntityIdentifier>();

				// initialize the namespace and id fields
				String namespace = "";
				String id = "";
				String article = "";
				boolean isPrimitiveType = data.containsKey("value");
				String objectName = configurationBean.getServiceName();
				// would like to use the MobyObjectClass to generate a skeleton

				String registryEndpoint = worker.getRegistryEndpoint();
				Registry mRegistry = new Registry(registryEndpoint,registryEndpoint,"http://domain.com/MOBY/Central");

				if (isPrimitiveType) {
					try {

						EntityIdentifier inputId;
						try {
							inputId = data.get("namespace");
							namespace = (String) dataFacade.resolve(inputId, String.class);
						} catch (Exception e) {
						}

						try {
							inputId = data.get("id");
							id = (String) dataFacade.resolve(inputId, String.class);
						} catch (Exception e) {
						}

						try {
							inputId = data.get("article name");
							article = (String) dataFacade.resolve(inputId, String.class);
						} catch (Exception e) {
						}

						inputId = data.get("value");
						String value = (String) dataFacade.resolve(inputId, String.class);

						if (objectName.equals("String")) {
							if (value == null) {
								value = "";
							}
							MobyDataString d = new MobyDataString(value,mRegistry);
							d.setId(id);
							MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
							if (mNamespace != null)
								d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
							d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
							outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
									"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
						} else if (objectName.equals("Float")) {
							if (value == null || value.trim().equals("")) {
								MobyDataComposite d = new MobyDataComposite(
										MobyDataType.getDataType("Float", mRegistry));
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							} else {
								MobyDataFloat d = new MobyDataFloat(value,mRegistry);
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							}
						} else if (objectName.equals("Integer")) {

							try {
								int val = 0;
								val = Integer.parseInt(value);
								MobyDataInt d = new MobyDataInt(val,mRegistry);
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							} catch (Exception e) {
								MobyDataComposite d = new MobyDataComposite(
										MobyDataType.getDataType("Integer", mRegistry));
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							}
						}else if (objectName.equals("Boolean")) {
							if (value == null || value.trim().equals("")) {
								MobyDataComposite d = new MobyDataComposite(MobyDataType.getDataType("Boolean", mRegistry));
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							} else {
								MobyDataBoolean d = new MobyDataBoolean(value,mRegistry);
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							}
						} else if (objectName.equals("DateTime")) {
							if (value == null || value.trim().equals("")) {
								MobyDataComposite d = new MobyDataComposite(MobyDataType.getDataType("DateTime", mRegistry));
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							} else {
								MobyDataDateTime d = new MobyDataDateTime("", value,mRegistry);
								d.setId(id);
								MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
								if (mNamespace != null)
									d.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
								d.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);
								outputMap.put("mobyData", dataFacade.register(XMLUtilities.createMobyDataElementWrapper(
										"<Simple articleName=\""+article+"\">" + d.toXML() +"</Simple>" ), 0));
							}
						}
						callback.receiveResult(outputMap, new int[0]);						
					} catch (Exception ex) {
						// details of other exceptions will appear only in a log
						ex.printStackTrace();
						logger.error("Error creating biomoby object for biomoby", ex);
						callback.fail("Activity failed due to problem creating biomoby object (see details in log)", ex);
					}
				} else {
					// Situation where simples are feeding into this non primitive type
					try {
						EntityIdentifier inputId;
						try {
							inputId = data.get("namespace");
							namespace = (String) dataFacade.resolve(inputId, String.class);
						} catch (Exception e) {
						}

						try {
							inputId = data.get("id");
							id = (String) dataFacade.resolve(inputId, String.class);
						} catch (Exception e) {
						}

						try {
							inputId = data.get("article name");
							article = (String) dataFacade.resolve(inputId, String.class);
						} catch (Exception e) {
						}

						//Element mobyObjectElement = mo.createObject(objectName,namespace, id, article);

						MobyDataComposite composite = new MobyDataComposite(MobyDataType.getDataType(objectName, mRegistry));
						composite.setId(id);
						MobyNamespace mNamespace = MobyNamespace.getNamespace(namespace,mRegistry);
						if (mNamespace != null)
							composite.setPrimaryNamespace(MobyNamespace.getNamespace(namespace,mRegistry));
						composite.setXmlMode(MobyDataInstance.SERVICE_XML_MODE);

						// Element mobyObjectElement = XMLUtilities.getDOMDocument(XMLUtilities.createMobyDataElementWrapper( "<Simple articleName=\""+article+"\">" + composite.toXML() +"</Simple>" )).detachRootElement();
						Element mobyObjectElement = XMLUtilities.getDOMDocument(( composite.toXML() )).detachRootElement();

						// using the inputs, iterate through and fill in data
						for (ActivityInputPort inputPort : getInputPorts()) {
							String portName = inputPort.getName();
							if (!(portName.equalsIgnoreCase("namespace")
									|| portName.equalsIgnoreCase("id") || portName
									.equalsIgnoreCase("article name"))) {
								String type = portName.substring(0, portName
										.indexOf("("));
								String articleName = portName.substring(
										type.length() + 1, portName.length() - 1);
								inputId = null;
								try {
									inputId = data.get(portName);
								} catch (Exception e) {

								}
								if (inputId != null) {
									String value = (String) dataFacade.resolve(inputId, inputPort.getTranslatedElementClass());
									Element valueElement = (XMLUtilities.getDOMDocument(value)).getRootElement();
									if (valueElement.getChild("mobyContent",XMLUtilities.MOBY_NS) != null) {
										valueElement = valueElement.getChild("mobyContent",XMLUtilities.MOBY_NS);
									} else {
										valueElement = valueElement.getChild("mobyContent");
									}
									if (valueElement.getChild("mobyData",XMLUtilities.MOBY_NS) != null ) {
										valueElement = valueElement.getChild("mobyData",XMLUtilities.MOBY_NS);
									} else {
										valueElement = valueElement.getChild("mobyData");
									}
									if (valueElement.getChild("Simple",XMLUtilities.MOBY_NS) != null ) {
										valueElement = valueElement.getChild("Simple",XMLUtilities.MOBY_NS);
									} else {
										valueElement = valueElement.getChild("Simple");
									}
									if (valueElement.getChild(type,XMLUtilities.MOBY_NS) != null ) {
										valueElement = valueElement.getChild(type,XMLUtilities.MOBY_NS);
									} else {
										valueElement = valueElement.getChild(type);
									}
									// Element _child = mo.getObject(mobyObjectElement,type, articleName);

									valueElement.removeAttribute("articleName");
									valueElement.removeAttribute("articleName", XMLUtilities.MOBY_NS);
									valueElement.setAttribute("articleName", articleName, XMLUtilities.MOBY_NS);
//									Element _newChild = mo.createObject(type, _ns, _id,
//									articleName);
//									_newChild.setText(valueElement
//									.getText());
//									_newChild.addContent(_child.cloneContent());
									// import and replace the node
									// mobyObjectElement.removeContent(_child);
									mobyObjectElement.addContent(valueElement.detach());
								}
							}
						}
						Element simple = new Element("Simple", XMLUtilities.MOBY_NS);
						simple.setAttribute("articleName", article, XMLUtilities.MOBY_NS);
						simple.addContent(mobyObjectElement);

						org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());
						String mobyDataString = outputter.outputString(XMLUtilities.createMobyDataElementWrapper(simple));
						outputMap.put("mobyData", dataFacade.register(mobyDataString, 0));

						callback.receiveResult(outputMap, new int[0]);
					} catch (Exception ex) {
						// details of other exceptions will appear only in a log
						ex.printStackTrace();
						logger.error("Error creating biomoby object for biomoby", ex);
						callback.fail("Activity failed due to problem creating biomoby object (see details in log)", ex);
					}
				}
			}
		});

	}

	@Override
	public BiomobyObjectActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

    private void init() throws ActivityConfigurationException {
        // Find the service endpoint (by calling Moby registry)
        try {
            if (mobyObject == null) {
                worker = new CentralImpl(configurationBean.getMobyEndpoint());
                mobyObject = worker.getDataType(configurationBean.getServiceName());
            }

        } catch (Exception e) {
            if (e instanceof ActivityConfigurationException) {
                throw (ActivityConfigurationException) e;
            }
            throw new ActivityConfigurationException(formatError(e.toString()));
        }
    }

    private void generatePorts() {
    	//inputs
		addInput("namespace", 0, true,
				new ArrayList<Class<? extends ReferenceScheme<?>>>(),
				String.class);

		addInput("id", 0, true,
				new ArrayList<Class<? extends ReferenceScheme<?>>>(),
				String.class);

		addInput("article name", 0, true,
				new ArrayList<Class<? extends ReferenceScheme<?>>>(),
				String.class);

    	try {
    		String serviceName = configurationBean.getServiceName();
    		MobyDataType datatype = worker.getDataType(serviceName);
    		MobyRelationship[] relations = datatype.getChildren();
    		processRelationships(relations);
    		String parent = "Object";
    		try {
    			parent = datatype.getParentNames()[0];
    			if (parent.indexOf(":") > 0) {
    				parent = parent.substring(parent.lastIndexOf(":") + 1);
    			}
    		} catch (ArrayIndexOutOfBoundsException e) {
    			// parent is then by default object
    		}
    		if (parent.equalsIgnoreCase("String")
    				|| parent.equalsIgnoreCase("Integer")
    				|| parent.equalsIgnoreCase("float")
    				|| parent.equalsIgnoreCase("DateTime")
    				|| parent.equalsIgnoreCase("Boolean")
    				|| serviceName.equalsIgnoreCase("String")
    				|| serviceName.equalsIgnoreCase("Boolean")
    				|| serviceName.equalsIgnoreCase("Integer")
    				|| serviceName.equalsIgnoreCase("float")
    				|| serviceName.equalsIgnoreCase("DateTime")) {
    			addInput("value", 0, true,
    					new ArrayList<Class<? extends ReferenceScheme<?>>>(),
    					String.class);
    		} else {
    			if (!parent.equalsIgnoreCase("Object"))
    				extractParentContainerRelationships(parent);
    		}
    	} catch (MobyException e) {
    	} catch (NoSuccessException e) {
    	}

    	//outputs
		addOutput("mobyData", 0);
    }

    private void extractParentContainerRelationships(String string) {
        try {
            MobyDataType datatype = worker.getDataType(string);
            // need to propagate the isa up to Object to get all of the has/hasa
            MobyRelationship[] relations = datatype.getChildren();
            processRelationships(relations);
            String parent = "Object";
            try {
                parent = datatype.getParentNames()[0];
                if (parent.indexOf(":") > 0) {
                    parent = parent.substring(parent.lastIndexOf(":") + 1);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // parent is then by default object
            }
    		String serviceName = configurationBean.getServiceName();
            if (parent.equalsIgnoreCase("String")
                    || parent.equalsIgnoreCase("Integer")
                    || parent.equalsIgnoreCase("float")
                    || serviceName.equalsIgnoreCase("String")
                     || parent.equalsIgnoreCase("Boolean")
                    || serviceName.equalsIgnoreCase("Boolean")
                    || serviceName.equalsIgnoreCase("Integer")
                    || serviceName.equalsIgnoreCase("float")) {
        		addInput("value", 0, true,
        				new ArrayList<Class<? extends ReferenceScheme<?>>>(),
        				String.class);
            } else {
                if (!parent.equalsIgnoreCase("Object"))
                    extractParentContainerRelationships(parent);
            }
        } catch (MobyException e) {
        } catch (NoSuccessException e) {
        }
    }

    private void processRelationships(MobyRelationship[] relations) {
        for (int x = 0; x < relations.length; x++) {
            MobyRelationship relationship = relations[x];

            // strip urn:lsid:...
            String name = relationship.getDataTypeName();
            if (name.indexOf(":") > 0) {
                name = name.substring(name.lastIndexOf(":") + 1);
            }
            // port name == DataType(articleName)
            name = name + "(" + relationship.getName() + ")";
            switch (relationship.getRelationshipType()) {
            case (Central.iHAS): {
                // TODO - not really supported
        		addInput(name, 0, true,
        				new ArrayList<Class<? extends ReferenceScheme<?>>>(),
        				String.class);
                break;
            }
            case (Central.iHASA): {
        		addInput(name, 0, true,
        				new ArrayList<Class<? extends ReferenceScheme<?>>>(),
        				String.class);
                break;
            }
            default:
                break;
            }
        }
    }


	protected String formatError(String msg) {
		// Removed references to the authority, some errors
		// were causing it to be null which in turn threw
		// a NPE from here, breaking Taverna's error handlers
		return ("Problems with service '" + configurationBean.getServiceName()
				+ "' provided by authority '"
				+ configurationBean.getAuthorityName()
				+ "'\nfrom Moby registry at "
				+ configurationBean.getMobyEndpoint() + ":\n\n" + msg);
	}

	protected ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	protected OutputPort getOutputPort(String name) {
		for (OutputPort port : getOutputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

}
