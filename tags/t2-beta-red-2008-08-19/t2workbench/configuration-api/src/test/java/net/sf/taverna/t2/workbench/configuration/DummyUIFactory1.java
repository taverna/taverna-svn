package net.sf.taverna.t2.workbench.configuration;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

public class DummyUIFactory1 implements ConfigurationUIFactory {

	public boolean canHandle(String uuid) {
		return getConfigurable().getUUID().equals(uuid);
	}

	public Configurable getConfigurable() {
		return new DummyConfigurable1();
	}

	public JPanel getConfigurationPanel() {
		return new JPanel();
	}
	
	static class DummyConfigurable1 implements Configurable {

		public void deleteProperty(String key) {
			// TODO Auto-generated method stub
			
		}

		public String getCategory() {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<String, String> getDefaultPropertyMap() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getProperty(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<String, String> getPropertyMap() {
			// TODO Auto-generated method stub
			return null;
		}

		public List<String> getPropertyStringList(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getUUID() {
			return "123";
		}

		public void restoreDefaults() {
			// TODO Auto-generated method stub
			
		}

		public void setProperty(String key, String value) {
			// TODO Auto-generated method stub
			
		}

		public void setPropertyStringList(String key, List<String> value) {
			// TODO Auto-generated method stub
			
		}

		
		
	}

}
