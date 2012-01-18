package idaservicetype.idaservicetype.ui.serviceprovider;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class IDAServiceProvider implements ServiceDescriptionProvider {
	
	private static final URI providerId = URI
		.create("http://example.com/2011/service-provider/idaservicetype");
	
	/**
	 * Do the actual search for services. Return using the callBack parameter.
	 */
	@SuppressWarnings("unchecked")
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		// Use callback.status() for long-running searches
		// callBack.status("Resolving example services");

		List<ServiceDescription> results = new ArrayList<ServiceDescription>();
		List<ServiceDescription> resultsTemplates = new ArrayList<ServiceDescription>();

		// FIXME: Implement the actual service search/lookup instead
		// of dummy for-loop
		
			System.out.println(" HERE HERE HERE " );

			IDAServiceDesc service = new IDAServiceDesc();
			// Populate the service description bean
			service.setExampleString("IDA Wizard");
			service.setExampleUri(URI.create(""));
			service.setIDATemplate(false);
			
			IDAServiceDesc service2 = new IDAServiceDesc();
			// Populate the service description bean
			service2.setExampleString("AttributeSelection");
			service2.setPreselectedTask("AttributeSelection");
			service2.setExampleUri(URI.create(""));
			service2.setIDATemplate(true);

			IDAServiceDesc service3 = new IDAServiceDesc();
			// Populate the service description bean
			service3.setExampleString("AttributeSelectionTask");
			service3.setPreselectedTask("AttributeSelectionTask");
			service3.setExampleUri(URI.create(""));
			service3.setIDATemplate(true);
			
			IDAServiceDesc service4 = new IDAServiceDesc();
			// Populate the service description bean
			service4.setExampleString("CleanMV");
			service4.setPreselectedTask("CleanMV");
			service4.setExampleUri(URI.create(""));
			service4.setIDATemplate(true);
			
			IDAServiceDesc service5 = new IDAServiceDesc();
			// Populate the service description bean
			service5.setExampleString("DataMining");
			service5.setPreselectedTask("DataMining");
			service5.setExampleUri(URI.create(""));
			service5.setIDATemplate(true);
			
			IDAServiceDesc service6 = new IDAServiceDesc();
			// Populate the service description bean
			service6.setExampleString("DiscretizeAll");
			service6.setPreselectedTask("DiscretizeAll");
			service6.setExampleUri(URI.create(""));
			service6.setIDATemplate(true);

			IDAServiceDesc service7 = new IDAServiceDesc();
			// Populate the service description bean
			service7.setExampleString("EvaluateAttributeSet");
			service7.setPreselectedTask("EvaluateAttributeSet");
			service7.setExampleUri(URI.create(""));
			service7.setIDATemplate(true);
			
			IDAServiceDesc service8 = new IDAServiceDesc();
			// Populate the service description bean
			service8.setExampleString("NormalizeScalar");
			service8.setPreselectedTask("NormalizeScalar");
			service8.setExampleUri(URI.create(""));
			service8.setIDATemplate(true);			
			
			IDAServiceDesc service9 = new IDAServiceDesc();
			// Populate the service description bean
			service9.setExampleString("OptionalMixedDataUnification");
			service9.setPreselectedTask("OptionalMixedDataUnification");
			service9.setExampleUri(URI.create(""));
			service9.setIDATemplate(true);				
			
			IDAServiceDesc service10 = new IDAServiceDesc();
			// Populate the service description bean
			service10.setExampleString("PredictTarget");
			service10.setPreselectedTask("PredictTarget");
			service10.setExampleUri(URI.create(""));
			service10.setIDATemplate(true);	
			
			IDAServiceDesc service11 = new IDAServiceDesc();
			// Populate the service description bean
			service11.setExampleString("Preprocessing");
			service11.setPreselectedTask("Preprocessing");
			service11.setExampleUri(URI.create(""));
			service11.setIDATemplate(true);	
			
			IDAServiceDesc service12 = new IDAServiceDesc();
			// Populate the service description bean
			service12.setExampleString("ReapplyAttributeSelection");
			service12.setPreselectedTask("ReapplyAttributeSelection");
			service12.setExampleUri(URI.create(""));
			service12.setIDATemplate(true);	
			
			IDAServiceDesc service13 = new IDAServiceDesc();
			// Populate the service description bean
			service13.setExampleString("ReapplyPreprocessing");
			service13.setPreselectedTask("ReapplyPreprocessing");
			service13.setExampleUri(URI.create(""));
			service13.setIDATemplate(true);	
			
			IDAServiceDesc service14 = new IDAServiceDesc();
			// Populate the service description bean
			service14.setExampleString("ValidationEvaluation");
			service14.setPreselectedTask("ValidationEvaluation");
			service14.setExampleUri(URI.create(""));
			service14.setIDATemplate(true);	
			
			IDAServiceDesc service15 = new IDAServiceDesc();
			// Populate the service description bean
			service15.setExampleString("ValidationTask");
			service15.setPreselectedTask("ValidationTask");
			service15.setExampleUri(URI.create(""));
			service15.setIDATemplate(true);	
			
			IDAServiceDesc service16 = new IDAServiceDesc();
			// Populate the service description bean
			service16.setExampleString("ValidationTraining");
			service16.setPreselectedTask("ValidationTraining");
			service16.setExampleUri(URI.create(""));
			service16.setIDATemplate(true);	
			
			// Optional: set description
			//service.setDescription("Service example number " + i);
			results.add(service);
			results.add(service2);
			results.add(service3);
			results.add(service4);
			results.add(service5);
			results.add(service6);
			results.add(service7);
			results.add(service8);
			results.add(service9);
			results.add(service10);
			results.add(service11);
			results.add(service12);
			results.add(service13);
			results.add(service14);
			results.add(service15);
			results.add(service16);
			
		// partialResults() can also be called several times from inside
		// for-loop if the full search takes a long time
		callBack.partialResults(results);

		// No more results will be coming
		callBack.finished();
	}

	/**
	 * Icon for service provider
	 */
	public Icon getIcon() {
		return ExampleServiceIcon.getIcon();
	}

	/**
	 * Name of service provider, appears in right click for 'Remove service
	 * provider'
	 */
	public String getName() {
		return "IDA_service_type";
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getId() {
		return providerId.toASCIIString();
	}

}
