/**
 * 
 */
package net.sf.taverna.t2.activities.apiconsumer.servicedescriptions;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;

/**
 * @author alanrw
 *
 */
public class ApiConsumerServiceProvider  extends
AbstractConfigurableServiceProvider<ApiConsumerServiceProviderConfig>
implements
CustomizedConfigurePanelProvider<ApiConsumerServiceProviderConfig>{

	private static Logger logger = Logger.getLogger(ApiConsumerServiceProvider.class);
	
	public ApiConsumerServiceProvider() {
		super(new ApiConsumerServiceProviderConfig());
	}

	public void createCustomizedConfigurePanel(
			net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider.CustomizedConfigureCallBack<ApiConsumerServiceProviderConfig> callBack) {
		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fc.setDialogTitle("Select API consumer definition file");
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			File file = fc.getSelectedFile();
			callBack.newProviderConfiguration(new ApiConsumerServiceProviderConfig(file.getAbsolutePath()));
			JOptionPane
					.showMessageDialog(
							null,
							"Make sure you also copy the API jar,\nand any jars it depends on, to\n" +  ApiConsumerActivity.libDir + "\n\n" +
							"and configure any API consumer services\nto select those jars",
							"Information message",
							JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callback) {
		ApiConsumerServiceProviderConfig config = getConfiguration();
		logger.info("About to parse API Consumer definition: "+ config.getAbsolutePath());

		try {
			File apiconsumerDefinitionFile = new File(config.getAbsolutePath());
			
			List<ApiConsumerServiceDescription> descriptions = new ArrayList<ApiConsumerServiceDescription>();

			// Load the XML document into a JDOM Document
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new FileInputStream(apiconsumerDefinitionFile));
			Element root = doc.getRootElement();
			String apiConsumerName = root.getAttributeValue("name");
			Element apiConsumerDescriptionElement = root.getChild("Description");
			String apiConsumerDescription = apiConsumerDescriptionElement.getValue();

			// Iterate over the classes...
			Element classesElement = root.getChild("Classes");
			List<?> classesList = classesElement.getChildren("Class");
			for (Iterator<?> i = classesList.iterator(); i.hasNext();) {
				Element classElement = (Element) i.next();
				String className = classElement.getAttributeValue("name");
				// Iterate over methods
				Element methodsElement = classElement.getChild("Methods");
				List<?> methodsList = methodsElement.getChildren("Method");
				for (Iterator<?> j = methodsList.iterator(); j.hasNext();) {
					Element methodElement = (Element) j.next();

					String methodName = methodElement.getAttributeValue("name");
					String methodType = methodElement.getAttributeValue("type");
					boolean methodStatic = methodElement.getAttributeValue(
							"static", "false").equals("true");
					boolean methodConstructor = methodElement
							.getAttributeValue("constructor", "false").equals(
									"true");
					int dimension = Integer.parseInt(methodElement
							.getAttributeValue("dimension"));
					String description = methodElement.getChild("Description")
							.getTextTrim();
					List<?> paramList = methodElement.getChildren("Parameter");
					String[] pNames = new String[paramList.size()];
					String[] pTypes = new String[paramList.size()];
					int[] pDimensions = new int[paramList.size()];
					int count = 0;
					for (Iterator<?> k = paramList.iterator(); k.hasNext();) {
						Element parameterElement = (Element) k.next();
						pNames[count] = parameterElement
								.getAttributeValue("name");
						pTypes[count] = parameterElement
								.getAttributeValue("type");
						pDimensions[count] = Integer.parseInt(parameterElement
								.getAttributeValue("dimension"));
						count++;
					}
					
					ApiConsumerServiceDescription serviceDescription = new ApiConsumerServiceDescription();
					serviceDescription.setApiConsumerName(apiConsumerName);
					serviceDescription.setApiConsumerDescription(apiConsumerDescription);
					serviceDescription.setDescription(description);
					serviceDescription.setClassName(className);
					serviceDescription.setMethodName(methodName);
					serviceDescription.setParameterNames(pNames);
					serviceDescription.setParameterTypes(pTypes);
					serviceDescription.setParameterDimensions(pDimensions);
					serviceDescription.setReturnType(methodType);
					serviceDescription.setReturnDimension(dimension);
					serviceDescription.setConstructor(methodConstructor);
					serviceDescription.setStatic(methodStatic);
					descriptions.add(serviceDescription);
				}
			}
			callback.partialResults(descriptions);
		} catch (Exception ex) {
			callback.fail("Unable to add new API Consumer activity", ex);
		}
		logger.info("Finished parsing API Consumer definition file : " + config.getAbsolutePath());
	}

	public Icon getIcon() {
		return ApiConsumerActivityIcon.getApiConsumerIcon();
	}

	public String getName() {
		return ApiConsumerServiceDescription.API_CONSUMER_SERVICE;
	}

	@Override
	public String toString() {
		if (getConfiguration() != null) {
		return ApiConsumerServiceDescription.API_CONSUMER_SERVICE + " " + getConfiguration().getAbsolutePath();
		}
		return ApiConsumerServiceDescription.API_CONSUMER_SERVICE;
	}

	@Override
	protected List<String> getIdentifyingData() {
		List<String> result;
		result = Arrays.asList(getConfiguration().getAbsolutePath());
		return result;
	}

}
