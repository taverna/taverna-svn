package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.configuration.Configurable;

public class QueryFactoryRegistry extends SPIRegistry<QueryFactory> {
	
	private static QueryFactoryRegistry instance = new QueryFactoryRegistry();
	
	

	public static QueryFactoryRegistry getInstance() {
		return instance;
	}
	
	private QueryFactoryRegistry() {
		super(QueryFactory.class);
	}
	
	
	
	@Override
	public List<QueryFactory> getInstances() {
		List<QueryFactory> instances = super.getInstances();
		for (QueryFactory fac : instances) {
			if (fac instanceof ActivityQueryFactory) {
				((ActivityQueryFactory)fac).setConfigurable(getConfigurable());
			}
		}
		return instances;
	}

	public List<Query<?>> getQueries() {
		List<QueryFactory> instances = getInstances();
		List<Query<?>> result = new ArrayList<Query<?>>();
		for (QueryFactory fac : instances) {
			result.addAll(fac.getQueries());
		}
		return result;
	}
	
	protected Configurable getConfigurable() {
		//FIXME: update to use an ActivityPaletteConfiguration
		return new Configurable() {

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
				Map<String,Object> map = new HashMap<String, Object>();
				List<String> wsdlList = new ArrayList<String>();
				wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/KEGG.wsdl");
				wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/bind.wsdl");
				wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/whatizit.wsdl");
				wsdlList.add("http://www.mygrid.org.uk/taverna-tests/testwsdls/GUIDGenerator.wsdl");
				List<String> soaplabList = new ArrayList<String>();
				soaplabList.add("http://www.ebi.ac.uk/soaplab/services/");
				map.put("taverna.defaultwsdl", wsdlList);
				map.put("taverna.defaultsoaplab", soaplabList);
				List<String> biomart = new ArrayList<String>();
				biomart.add("http://www.biomart.org/biomart");
				map.put("taverna.defaultmartregistry",biomart);
				return map;
			}

			public String getUUID() {
				// TODO Auto-generated method stub
				return null;
			}

			public void restoreDefaults() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
}
