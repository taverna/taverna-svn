package net.sf.taverna.t2.platform.taverna;

import java.io.InputStream;
import java.net.URL;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

/**
 * Provides facilities to instantiate Taverna 2 workflow definition objects from
 * a variety of input sources, each of which provides the XML serialization of
 * the workflow.
 * 
 * @author Tom Oinn
 * 
 */
public interface WorkflowParser {

	/**
	 * Load a dataflow from the serialized form located at the specified URL
	 * 
	 * @param sourceURL
	 *            a URL to an XML dataflow serialization
	 * @return a Dataflow instance constructed from the XML form
	 */
	Dataflow createDataflow(URL sourceURL) throws DeserializationException,
			EditException;

	/**
	 * Load a dataflow from a stream of XML.
	 * 
	 * @param is
	 *            the stream to use
	 * @return a configured dataflow instance
	 * @throws DeserializationException
	 * @throws EditException
	 */
	Dataflow createDataflow(InputStream is) throws DeserializationException,
			EditException;

	/**
	 * Load a dataflow from a serialized XML form loaded as a classpath resource
	 * 
	 * @param classPathResource
	 *            name of a resource relative to the classpath, this should
	 *            point to the xml file containing the serialized workflow
	 * @return a dataflow instance
	 * @throws DeserializationException
	 * @throws EditException
	 */
	Dataflow createDataflow(String classPathResource)
			throws DeserializationException, EditException;

}
