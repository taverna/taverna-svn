package net.sf.taverna.portal.utils;

import net.sf.taverna.portal.wireit.exception.WireItRunException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Wrapper class to hold a URI and a Delimiter.
 * <p>
 * These are used for Depth 1 port where data comes from an external source.
 * <p>
 * See Constructor some delimiters wich are changed from escaped characters.
 * @author Christian
 */
public class DelimiterURI {
 
    /** 
     * Uri to the data for this port.
     */
    private URI uri;
    /**
     * Delimiter in for this data.
     */
    private String delimiter;
    
    /**
     * Constructs a wrapper, converting escaped values to the actual value.
     * <p>
     * The following if by themselves are assumed to be escape characters and replaced.
     * "\n", "\t" and these two wrapped in doulde quotes.
     * <p>
     * Quotes are also stripped of.
     * <p>
     * This behaviour may actually be redundant if Taverna does it as well.
     * 
     * @param uriSt A String assumed to be a uri
     * @param delimiterSt a String representing the delimiter. Soe escape characters are removed.
     * @throws URISyntaxException
     * @throws WireItRunException 
     */
    public DelimiterURI(String uriSt, String delimiterSt) throws URISyntaxException, WireItRunException{
        this.uri = new URI(uriSt);
        if (delimiterSt.length() == 1){
            delimiter = delimiterSt;
        } else if (delimiterSt.equals("\\n")){
            delimiter = "\n";
        } else if (delimiterSt.equals("\\t")){
            delimiter = "\t";
        } else if (delimiterSt.equals("\"\\n\"")){
            delimiter = "\n";
        } else if (delimiterSt.equals("\"\\t\"")){
            delimiter = "\t";
        } else if (delimiterSt.startsWith("\"") && delimiterSt.endsWith("\"")) {
            delimiter = delimiterSt.substring(1, delimiterSt.length()-1);
        } else {
            delimiter = delimiterSt;
        }
    }
    
    public URI getURI(){
        return uri;
    }
    
    public String getDelimiter(){
        return delimiter;
    }
}
