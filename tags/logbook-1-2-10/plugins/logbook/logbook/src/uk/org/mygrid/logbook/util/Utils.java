package uk.org.mygrid.logbook.util;

import java.text.ParseException;
import java.util.Date;

import uk.org.mygrid.provenance.util.ProvenanceOntologyUtil;

public class Utils {

	public static final String INPUT_DIVIDER = "_in_";
	public static final String OUTPUT_DIVIDER = "_out_";

	static public String inputLocalName(String name) {
	    return localName(name, INPUT_DIVIDER);
	}

	static public String outputLocalName(String name) {
	    return localName(name, OUTPUT_DIVIDER);
	}

	static private String localName(String name, String separator) {
	    if (name.split("#").length > 1) {
	        name = name.split("#")[1];
	    } else {
	        name = "name";
	    }
	    String[] split = name.split(separator);
	    if (split.length == 2)
	        return split[1];
	    else
	        return name;
	}
	
	static public Date parseDateLiteral(String unparsedDateLiteral) throws ParseException {
		String unparsedDate = unparsedDateLiteral.substring(1, unparsedDateLiteral.lastIndexOf("\""));
		Date parsedDate = ProvenanceOntologyUtil.parseDateTime(unparsedDate);
		return parsedDate;
	}

}
