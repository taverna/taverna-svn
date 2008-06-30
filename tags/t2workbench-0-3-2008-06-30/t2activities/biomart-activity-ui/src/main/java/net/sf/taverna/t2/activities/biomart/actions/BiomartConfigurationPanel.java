package net.sf.taverna.t2.activities.biomart.actions;

import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartService;
import org.biomart.martservice.MartServiceException;
import org.biomart.martservice.MartServiceXMLHandler;
import org.biomart.martservice.config.QueryConfigController;
import org.biomart.martservice.config.ui.MartServiceQueryConfigUIFactory;
import org.biomart.martservice.config.ui.QueryConfigUIFactory;
import org.jdom.Element;

public class BiomartConfigurationPanel extends JPanel {
	
	private static final long serialVersionUID = 1884045346293327621L;
	
	private BiomartActivityConfigurationBean bean;
	private JButton okButton;
	private JButton cancelButton;

	private MartQuery biomartQuery;

	public BiomartConfigurationPanel(BiomartActivityConfigurationBean bean) {
		this.bean = bean;
		initialise();
	}

	private void initialise() {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		okButton=new JButton("OK");
		cancelButton=new JButton("Cancel");
		
		Element configurationElement = bean.getQuery();
		biomartQuery = MartServiceXMLHandler.elementToMartQuery(configurationElement, null);
		MartService service = biomartQuery.getMartService();
		
		//FIXME: need to set the cache directory for biomart, read from the configuration.
		//service.setCacheDirectory(new File(MyGridConfiguratio.getUserDir("taverna-biomart-processor"), "cache"));
		
		QueryConfigController controller = new QueryConfigController(biomartQuery);
		try {
			QueryConfigUIFactory queryConfigUIFactory = new MartServiceQueryConfigUIFactory(
					service, controller, biomartQuery.getMartDataset());
			add(queryConfigUIFactory.getDatasetConfigUI());
			add(Box.createGlue());
			add(buttonBar());
		} catch (MartServiceException e) {
			add(new JLabel("Error reading configuration properties"));
			add(new JLabel(e.getMessage()));
			add(Box.createGlue());
		}
		
	}
	
	private JPanel buttonBar() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		panel.add(cancelButton);
		panel.add(okButton);
		return panel;
	}

	public Element getQuery() {
		return MartServiceXMLHandler.martQueryToElement(biomartQuery, null);
	}

	public void setOkAction(Action okAction) {
		okButton.setAction(okAction);
	}

	public void setCancelAction(Action cancelAction) {
		cancelButton.setAction(cancelAction);
	}
}
