package uk.ac.man.cs.img.fetaClient.publisher;

import java.util.List;
import uk.ac.man.cs.img.fetaEngine.webservice.*;

public class FetaEnginePublishManager {
	
	private String engineLocation;
			
	public FetaEnginePublishManager(String fetaEngineURLStr) {

		engineLocation = fetaEngineURLStr;
		
	}
	
	public boolean publish(String descLocation) throws Exception {
		try {
			    FetaPublishResponseType response = null;

				FetaPortType port = new FetaLocator().getfeta(new java.net.URL(
						engineLocation));

				response = (FetaPublishResponseType) ((FetaPortTypeBindingStub) port)
						.publishDescription(descLocation);

				return true;

		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}


}
