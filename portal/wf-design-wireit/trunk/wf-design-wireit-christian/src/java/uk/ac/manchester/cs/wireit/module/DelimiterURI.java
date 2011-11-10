/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit.module;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Christian
 */
public class DelimiterURI {
 
    private URI uri;
    private char delimiter;
    
    public DelimiterURI(String uriSt, String delimiterSt) throws URISyntaxException, WireItRunException{
        this.uri = new URI(uriSt);
        if (delimiterSt.length() == 1){
            delimiter = delimiterSt.charAt(0);
        } else if (delimiterSt.equals("\\n")){
            delimiter = '\n';
        } else if (delimiterSt.equals("\\t")){
            delimiter = '\t';
        } else if (delimiterSt.equals("\"\\n\"")){
            delimiter = '\n';
        } else if (delimiterSt.equals("\"\\t\"")){
            delimiter = '\t';
        } else if ((delimiterSt.length() == 3)&& (delimiterSt.charAt(0) == '\"') && (delimiterSt.charAt(2) == '\"')) {
            delimiter = delimiterSt.charAt(1);
        } else {
            throw new WireItRunException ("Unable to convert delimiter " + delimiterSt + " to a character" );
        }
    }
    
    public URI getURI(){
        return uri;
    }
    
    public char getDelimiter(){
        return delimiter;
    }
}
