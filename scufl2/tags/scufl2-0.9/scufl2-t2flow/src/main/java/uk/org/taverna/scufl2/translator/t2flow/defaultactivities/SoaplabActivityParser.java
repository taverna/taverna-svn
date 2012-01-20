package uk.org.taverna.scufl2.translator.t2flow.defaultactivities;

import java.math.BigInteger;
import java.net.URI;

import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.property.PropertyLiteral;
import uk.org.taverna.scufl2.api.property.PropertyResource;
import uk.org.taverna.scufl2.translator.t2flow.ParserState;
import uk.org.taverna.scufl2.translator.t2flow.T2FlowParser;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.ConfigBean;
import uk.org.taverna.scufl2.xml.t2flow.jaxb.SoaplabConfig;

public class SoaplabActivityParser extends AbstractActivityParser {
	private static URI activityRavenURI = T2FlowParser.ravenURI
			.resolve("net.sf.taverna.t2.activities/soaplab-activity/");

	private static String activityClassName = "net.sf.taverna.t2.activities.soaplab.SoaplabActivity";

	public static URI scufl2Uri = URI.create("http://ns.taverna.org.uk/2010/activity/soaplab");

	@Override
	public boolean canHandlePlugin(URI activityURI) {
		String activityUriStr = activityURI.toASCIIString();
		return activityUriStr.startsWith(activityRavenURI.toASCIIString())
				&& activityUriStr.endsWith(activityClassName);
	}

	@Override
	public URI mapT2flowRavenIdToScufl2URI(URI t2flowActivity) {
		return scufl2Uri;
	}

	@Override
	public Configuration parseConfiguration(T2FlowParser t2FlowParser, ConfigBean configBean, ParserState parserState)
			throws ReaderException {
		SoaplabConfig soaplabConfig = unmarshallConfig(t2FlowParser, configBean, "xstream",
				SoaplabConfig.class);

		Configuration configuration = new Configuration();
		configuration.setParent(parserState.getCurrentProfile());

		PropertyResource configResource = configuration.getPropertyResource();
		configResource.setTypeURI(scufl2Uri.resolve("#Config"));

		String endpoint = soaplabConfig.getEndpoint();
		if (endpoint == null || endpoint.equals("")) {
			throw new ReaderException("Soablab config has no endpoint set");
		}
		configResource.addPropertyAsString(scufl2Uri.resolve("#endpoint"), endpoint);

		double pollingBackoff = soaplabConfig.getPollingBackoff();
		configResource.addProperty(scufl2Uri.resolve("#pollingBackoff"), new PropertyLiteral(
				pollingBackoff));

		BigInteger pollingInterval = soaplabConfig.getPollingInterval();
		if (pollingInterval != null) {
			configResource.addProperty(scufl2Uri.resolve("#pollingInterval"), new PropertyLiteral(
					pollingInterval.intValue()));
		}

		BigInteger pollingIntervalMax = soaplabConfig.getPollingIntervalMax();
		if (pollingIntervalMax != null) {
			configResource.addProperty(scufl2Uri.resolve("#pollingIntervalMax"),
					new PropertyLiteral(pollingIntervalMax.intValue()));
		}

		return configuration;
	}

}
