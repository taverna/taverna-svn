package net.sf.taverna.t2.workbench.configuration;

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

		public String getCategory() {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<String, Object> getDefaultPropertyMap() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<String, Object> getPropertyMap() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getUUID() {
			return "123";
		}

		public void restoreDefaults() {
			// TODO Auto-generated method stub
			
		}

		public Object getProperty(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public void setProperty(String key, Object value) {
			// TODO Auto-generated method stub
			
		}

		public void deleteProperty(String key) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
