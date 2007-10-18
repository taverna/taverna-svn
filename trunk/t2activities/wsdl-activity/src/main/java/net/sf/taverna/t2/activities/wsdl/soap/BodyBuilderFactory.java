package net.sf.taverna.t2.activities.wsdl.soap;

import java.util.List;

import net.sf.taverna.t2.activities.wsdl.parser.TypeDescriptor;
import net.sf.taverna.t2.activities.wsdl.parser.UnknownOperationException;
import net.sf.taverna.t2.activities.wsdl.parser.WSDLParser;

import org.apache.log4j.Logger;

/**
 * Factory that creates an appropriate BodyBuilder according to the provided WSDLProcessors style and use.
 * @author Stuart Owen
 *
 */
@SuppressWarnings("unchecked")
public class BodyBuilderFactory {
	
	private static Logger logger = Logger.getLogger(BodyBuilderFactory.class);
	
	private static BodyBuilderFactory instance = new BodyBuilderFactory();
	
	public static BodyBuilderFactory instance() {
		return instance;
	}
	
	public BodyBuilder create(WSDLParser parser, String operationName, List<TypeDescriptor> inputDescriptors) throws UnknownOperationException {
		String use = parser.getUse(operationName);
		String style = parser.getStyle();
		if (use.equals("encoded")) {
			return new EncodedBodyBuilder(style, parser,operationName, inputDescriptors);
		}
		else if (use.equals("literal")) {
			return new LiteralBodyBuilder(style,parser,operationName, inputDescriptors);
		}
		logger.warn("WSDL 'use' is not recognised:"+style);
		logger.warn("Will default to assuming literal");
		return new LiteralBodyBuilder(style,parser,operationName, inputDescriptors);
	}
}
