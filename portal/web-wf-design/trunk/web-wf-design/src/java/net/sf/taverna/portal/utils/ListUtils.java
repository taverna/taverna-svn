package net.sf.taverna.portal.utils;

import java.io.File;
import java.util.ArrayList;
import net.sf.taverna.portal.commandline.TavernaException;
import net.sf.taverna.portal.baclava.DataThingBasedBaclava;

/**
 * Utilities for converting Lists into a Single String Array.
 * <p>
 * Used to convert Baclava Lists and list of Lists into A String for WireIt's TextArea.
 * <p>
 * This is a prototype so may be missing Object types. Please extend as required.
 * @author Christian
 */
public class ListUtils {
    
    /**
     * Recurcive method to flatten Lists into a Single Array
     * @param list A possibly multiple level list.
     * @return List flattened and all objects as String
     */
    public static ArrayList<String> flattenList(ArrayList list){
        ArrayList<String> result = new ArrayList<String>();
        for (Object object:list){
            if (object instanceof ArrayList){
                result.addAll(flattenList((ArrayList)object));
            } else {
                result.add(object.toString());
            }
        }
        return result;
    }
    
    /**
     * Method to convert any object into an Array Of Strings.
     * <p>
     * Singletons are converted to an Array of length 1.
     * Lists are flattened.
     * @param object
     * @return 
     */
    public static String[] toStringArray(Object object){
        String[] result;
        if (object instanceof ArrayList){
            result = new String[0];
            ArrayList<String> list = flattenList((ArrayList)object);
            result = list.toArray(result);
        } else {
            result = new String[1];
            result[0] = object.toString();
        }
        return result;
    }
    
    /**
     * Testing method.
     * 
     * @param args
     * @throws TavernaException 
     */
    public static void main(String[] args) throws TavernaException {
        File output = new File("D:\\Programs\\Tomcat7\\webapps\\WireIt\\Outputs\\2011_10_26_12_50_27\\BaclavaOutput.xml");
        //File output = new File("D:\\Programs\\Tomcat7\\webapps\\WireIt\\Outputs\\2011_11_01_17_24_03\\BaclavaOutput.xml");
        DataThingBasedBaclava baclava;
        baclava = new DataThingBasedBaclava(output);
        //Object value = baclava.getValue("Result");
        Object value = baclava.getValue("Foo");
        System.out.println(value);
        String[] array = toStringArray(value);
        for (String single:array){
            System.out.println(single);
        }
    }
}
