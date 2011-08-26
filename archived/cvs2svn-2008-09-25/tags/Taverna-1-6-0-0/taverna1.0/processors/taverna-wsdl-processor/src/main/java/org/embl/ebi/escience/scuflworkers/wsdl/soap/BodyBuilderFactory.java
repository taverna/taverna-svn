package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.parser.UnknownOperationException;

/**
 * Factory that creates an appropriate BodyBuilder according to the provided WSDLProcessors style and use.
 * @author Stuart Owen
 *
 */
public class BodyBuilderFactory {
	
	private static Logger logger = Logger.getLogger(BodyBuilderFactory.class);
	
	private static BodyBuilderFactory instance = new BodyBuilderFactory();
	
	public static BodyBuilderFactory instance() {
		return instance;
	}
	
	public BodyBuilder create(WSDLBasedProcessor processor) throws UnknownOperationException {
		String use = processor.getParser().getUse(processor.getOperationName());
		String style = processor.getParser().getStyle();
		if (use.equals("encoded")) {
			return new EncodedBodyBuilder(style, processor);
		}
		else if (use.equals("literal")) {
			return new LiteralBodyBuilder(style,processor);
		}
		logger.warn("WSDL 'use' is not recognised:"+style);
		logger.warn("Will default to assuming literal");
		return new LiteralBodyBuilder(style,processor);
	}
}
