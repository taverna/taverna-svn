package org.embl.ebi.escience.scuflworkers.wsdl.parser;

import org.embl.ebi.escience.scufl.ScuflException;

public class UnknownOperationException extends ScuflException 
{
	UnknownOperationException(String val)
	{
		super(val);
	}
}
