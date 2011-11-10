/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit.module;

import java.io.File;
import java.util.ArrayList;
import uk.ac.manchester.cs.wireit.taverna.TavernaException;
import uk.ac.manchester.cs.wireit.taverna.baclava.DataThingBasedBaclava;

/**
 *
 * @author Christian
 */
public class ListUtils {
    
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
    
    public static void main(String[] args) throws TavernaException {
        File output = new File("D:\\Programs\\Tomcat7\\webapps\\WireIt\\Output\\2011_10_26_12_50_27\\BaclavaOutput.xml");
        //File output = new File("D:\\Programs\\Tomcat7\\webapps\\WireIt\\Output\\2011_11_01_17_24_03\\BaclavaOutput.xml");
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
