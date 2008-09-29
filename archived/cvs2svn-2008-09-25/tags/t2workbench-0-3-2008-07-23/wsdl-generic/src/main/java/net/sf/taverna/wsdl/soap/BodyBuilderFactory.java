package net.sf.taverna.wsdl.soap;

import java.util.List;

import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

/**
 * Factory that creates an appropriate BodyBuilder according to the provided WSDLProcessors style and use.
 * @author Stuart Owen
 *
 */
@SuppressWarnings("unchecked")
public class BodyBuilderFactory {
	
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
		return new LiteralBodyBuilder(style,parser,operationName, inputDescriptors);
	}
}
