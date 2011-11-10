package uk.ac.manchester.cs.wireit.taverna;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Single class to hold the input whatever the format for a Taverna input.
 * 
 * @author Christian
 */
public class TavernaInput {

    private String name;
    private int depth;
    private String value;
    private String[] values;
    private String uri;
    private String delimiter;
    
    /**
     * Constructs an Input with a name and a depth ready to store the input values.
     * 
     * @param name Name used in the workflow
     * @param depth Depth as defined by the workflow
     * @throws TavernaException Thrown if the name is null or Emoty.
     *     Thrown if the depth is not 0 or 1.
     */
    public TavernaInput(String name, int depth) throws TavernaException {
        if (name == null) {
            throw new NullPointerException("Input name may not be null.");
        }
        if (name.isEmpty()) {
            throw new TavernaException("Input name may not be empty.");
        }
        if (depth < 0 || depth > 1){
            throw new TavernaException("Only Input Depths allowed are zero or one. Use inputDoc for deeper lists.");
        }
        this.name = name;
        this.depth = depth;
    }
   
    private void resetInputs(){
        delimiter = null;
        value = null;
        values = null;
        uri = null;
    }
    
    /**
     * Sets the value to be used for this input to a string.
     * 
     * Clears any previously stored values even if an Exception if thrown.
     * The value may not be null but may be an empty String.
     * <p>
     * Taverna allows single values for inputs of depth one so this method does too.
     * 
     * @param value New value to be assigned to this input port
     * @throws TavernaException Value may not be null.
     */
    public void setStringInput(String value) throws TavernaException {
        resetInputs();
        if (value == null){
            throw new NullPointerException ("null value is not allowed");
        }
        if (value.contains("\n")){
            throw new TavernaException("Command line tool can not handle String values with a carriage return in them");
        }
        this.value = value;
    }
    
    /**
     * Sets the values to be used for this input to a array.
     * 
     * Clears any previously stored values even if an Exception if thrown.
     * <p>
     * Requires 
     * <ul>
     *   <li> The input to have a depth of 1
     *   <li> The array not to be null
     *   <li> The array not to be empty
     *   <li> None of the values may be null, but they may be empty.
     * <ul>
     * <p>
     * This class will attempt to find a suitable delimiter that is not in any of the values.
     * 
     * @param values array of values to be used.
     * @throws TavernaException Any of the above requires has not been met.
     */
    public void setStringsInput(String[] values) throws TavernaException {
        resetInputs();
        if (depth < 1){
            throw new TavernaException ("Input " + name + " only has a depth of " + depth);
        }
        if (values == null){
            throw new NullPointerException ("null values is not allowed");
        }
        if (values.length == 0){
            throw new TavernaException ("Empty values not allowed");
        }
        for (int i = 0; i< values.length; i++){
            if (values[i] == null){
                throw new TavernaException ("None of the values may be null. ");
            }
        }
        this.values = values;
    }

    /**
     * Sets a uri to be used as the source of the input values and sets a delimiter.
     * 
     * Clears any previously stored values even if an Exception if thrown.
     * <p>
     * Only allowed for inputs with a depth of one.
     * <p>
     * The uri is not validated beyond checking it is not null or empty.
     * 
     * @param uri String to be passed to Taverna as a uri
     * @param delimiterCh Charater to be passed to Taverna as the delimiter
     * @throws TavernaException Thrown if the uri is null or empty.
     *     Also thrown if the depth is not 1.
     */
    public void setListURIInput(String uri, char delimiterCh) throws TavernaException {
        if (depth != 1){
            throw new TavernaException ("Delimiters are only allowed with depth 1");
        }
        setSingleURIInput(uri);
        this.delimiter = "" + delimiterCh;
    }

    /**
     * Sets a uri to be used as the source of the input values.
     * 
     * Clears any previously stored values even if an Exception if thrown.
     * <p>
     * Allowed for inputs with a depth of zero and one. 
     * In both case the assumption is that there is a single value stored at this uri.
     * <p>
     * The uri is not validated beyond checking it is not null or empty.
     * 
     * @param uri String to be passed to Taverna as a uri
     * @throws TavernaException Thrown if the uri is null or empty.
     */
    public void setSingleURIInput(String uri) throws TavernaException {
        resetInputs();
        if (uri == null){
            throw new NullPointerException ("null URI is not allowed");
        }
        if (uri.isEmpty()){
            throw new TavernaException ("Empty URI is not allowed");
        }
        this.uri = uri;
    }

    
    /**
     * Sets a file to be used as the source of the input values and sets a delimiter.
     * 
     * Clears any previously stored values even if an Exception if thrown.
     * <p>
     * Only allowed for inputs with a depth of one.
     * <p>
     * The file is not validated beyond checking it exists and is readable.
     * 
     * @param file File to be passed to Taverna as a uri
     * @param delimiterCh Charater to be passed to Taverna as the delimiter
     * @throws TavernaException Thrown if the uri is null or empty.
     *     Also thrown if the depth is not 1.
     */
    public void setListFileInput(File file, char delimiterCh) throws TavernaException, IOException {
        resetInputs();
        Utils.checkFile(file);
        setListURIInput("file:"+file.getAbsolutePath(), delimiterCh);
    }
    
   /**
     * Sets a file to be used as the source of the input values .
     * 
     * Clears any previously stored values even if an Exception if thrown.
     * <p>
     * Allowed for inputs with a depth of zero and one. 
     * In both case the assumption is that there is a single value stored in this file.
     * <p>
     * The uri is not validated beyond checking it is not null or empty.
     * 
     * @param file File to be passed to Taverna as a uri
     * @throws TavernaException Thrown if the uri is null or empty.
     */
    public void setSingleFileInput(File file) throws TavernaException, IOException {
        resetInputs();        
        Utils.checkFile(file);
        setSingleURIInput("file:"+file.getAbsolutePath());
     }

    /**
     * Gets the name of this input.
     * 
     * Never null or empty as the constructor fails if it is.
     * 
     * @return the name
     */
    public String getName(){
        return name;
    }
    
    /**
     * Looks for a delimiter character that is not in any of the values.
     * <p>
     * There is no guarantee that over time this method will always return the same delimiter for the same values.
     * Although the current implementation does.
     * @param values List of values to be delimited
     * @return String to use as the delimiter.
     * @throws NullPointerException If the array or any of the fields in the array are null.
     * @throws TavernaException Thrown if all of the candidate delimiters are in the values.
     */
    public String findADelimiter(String[] values) throws TavernaException{
        String[] possibleDelimiters = {" ",",",";",":","/","\\",".","#","_","£","$","^","!","@"};
        for (int i = 0; i <  possibleDelimiters.length; i++){
            String possibleDelimiter = possibleDelimiters[i];
            boolean usable = true;
            for (String value:values){
                if (value.contains(possibleDelimiter)){
                    usable = false;
                }
            }
            if (usable){
                return possibleDelimiter;
            }
        }
        throw new TavernaException("Sorry values contain all characters tested as delimiters");
    }
    
    /**
     * Check to see if there is a value associated with this input.
     * 
     * @return True if there is a single value, array of files or a file/uri associated with this input.
     *      Otherwise false;
     */
    public boolean hasValue(){
         if (value != null){
             return true;
         }
         if (values != null){
             return true;
         }
         if (uri != null){
             return true;
         }
         return false;
    }
    
    /**
     * Returns the arguements associated with this input and its values.
     * <p>
     * For lists of values a delimiter is autogenerated so that the whole list is passed as a single parameter split 
     *    by this list.
     * 
     * @return The argurments required to express this input.
     * 
     * @throws TavernaException Thrown if this method is called before a value is added. 
     */
    public List<String> getInputArguements() throws TavernaException{
        ArrayList<String> list = new ArrayList<String>();
        if (value != null){
            list.add("-inputvalue");
            list.add(name);
            list.add("\"" + value + "\"");
        } else if (values != null){
            delimiter = findADelimiter(values);
            list.add("-inputdelimiter");
            list.add(name);
            list.add("\"" + delimiter + "\"");
            list.add("-inputvalue");
            list.add(name);
            String longValue =  "\"";
            for (int i = 0; i < values.length - 1; i++){
                longValue+= values[i] + delimiter;
            }
            longValue+= values[values.length - 1] + "\"";
            list.add(longValue);
        } else if (uri != null){
            list.add("-inputfile");
            list.add(name);
            list.add(uri);    
            if (delimiter != null){
                list.add("-inputdelimiter");
                list.add(name);
                list.add("\"" + delimiter + "\"");
            }
        } else {
            throw new TavernaException("Illegal call to getInputArguements before setting input");
        }
        return list;
    }
    
}
