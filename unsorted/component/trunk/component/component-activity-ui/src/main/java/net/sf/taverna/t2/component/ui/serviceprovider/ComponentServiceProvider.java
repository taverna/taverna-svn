package net.sf.taverna.t2.component.ui.serviceprovider;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Pack;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.PackItem;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.User;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;

public class ComponentServiceProvider extends
	AbstractConfigurableServiceProvider<ComponentServiceProviderConfig> implements
	CustomizedConfigurePanelProvider<ComponentServiceProviderConfig> {
	
	private static final String T2FLOW = ".t2flow";

	private static final URI providerId = URI
		.create("http://example.com/2011/service-provider/component");
	
	private static Logger logger = Logger.getLogger(ComponentServiceProvider.class);
	
	MyExperimentClient myExperimentClient = new MyExperimentClient(logger);
	
	
	public ComponentServiceProvider() {
		super(new ComponentServiceProviderConfig());
	}

	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		ComponentServiceProviderConfig config = getConfiguration();

		if (config.getSource() != null) {
				Document packDoc;
				try {
					packDoc = myExperimentClient.getResource(Resource.PACK, config.getFamilySource().toString(), Resource.REQUEST_ALL_DATA);
				List<ServiceDescription> results = new ArrayList<ServiceDescription>();
				Pack p = Pack.buildFromXML(packDoc, myExperimentClient, logger);
				for (PackItem pi : p.getItems()) {
					ComponentServiceDesc service = new ComponentServiceDesc();
					service.setFamilyName(p.getTitle());
					Resource memberResource = pi.getItem();
					if ((memberResource != null) && (memberResource.getItemType() == Resource.WORKFLOW)){
						Document workflowDoc = myExperimentClient.getResource(Resource.WORKFLOW, memberResource.getURI(), Resource.REQUEST_ALL_DATA);
						Workflow w = Workflow.buildFromXML(workflowDoc, logger);
						if (!w.getContentType().equals(Workflow.MIME_TYPE_TAVERNA_2)) {
							continue;
						}
						Workflow w2 = myExperimentClient.fetchWorkflowBinary(memberResource.getURI());
						service.setName(w.getTitle());
						service.setDataflowString(new String(w2.getContent(), "utf-8"));
//						service.setUrl(memberResource.getURI());
						results.add(service);
					}
				}
				callBack.partialResults(results);
			
				
				// No more results will be coming
				callBack.finished();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e);
					callBack.fail("Unable to read component family", e);
				}
		} else {
			try {
				File familyDir = new File(config.getFamilySource().toURI());
				List<ServiceDescription> results = new ArrayList<ServiceDescription>();
				for (File workflow : familyDir.listFiles((FilenameFilter) new SuffixFileFilter(T2FLOW))) {
					String workflowString = FileUtils.readFileToString(workflow, "utf-8");
					ComponentServiceDesc service = new ComponentServiceDesc();
					service.setFamilyName(familyDir.getName());
					service.setName(StringUtils.remove(workflow.getName(), T2FLOW));
					service.setDataflowString(workflowString);
					results.add(service);
				}
				callBack.partialResults(results);
				
				// No more results will be coming
				callBack.finished();
			} catch (URISyntaxException e) {
				logger.error(e);
				callBack.fail("Unable to read local component family", e);
			} catch (IOException e) {
				logger.error(e);
				callBack.fail("Unable to read local component family", e);
			}
		}
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ComponentServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "Component service";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.asList(new Object[] {getConfiguration().getSource(), getConfiguration().getFamilySource()});
	}

	@Override
	public void createCustomizedConfigurePanel(
			CustomizedConfigureCallBack<ComponentServiceProviderConfig> callBack) {
		
		ComponentFamilyChooserPanel panel = new ComponentFamilyChooserPanel(false);
				
		int result = JOptionPane.showConfirmDialog(null, panel, "Component family import", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			
			ComponentServiceProviderConfig newConfig = panel.getConfig();
			if (newConfig != null) {
				callBack.newProviderConfiguration(newConfig);
			}
		}
		return;

	}

}
