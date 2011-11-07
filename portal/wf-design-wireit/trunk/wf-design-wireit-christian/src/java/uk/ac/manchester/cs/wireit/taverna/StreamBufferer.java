package uk.ac.manchester.cs.wireit.taverna;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class to capture a stream and send anything on that Stream to a String Builder
 * @author Christian
 */
public class StreamBufferer extends Thread {

    private InputStream is;
    private String type;
    private StringBuilder builder;
    public static String NEW_LINE = System.getProperty("line.separator");

    /**
     * Constructs a wrapper around a Stream.
     * 
     * Each line received on the stream is appended to the builder.
     * 
     * @param is Stream from which data should be captured
     * @param type Tag to add in front of each line captured from the Stream
     * @param builder Builder to append each line captured too.
     */
    public StreamBufferer(InputStream is, String type, StringBuilder builder){
        this.is = is;
        this.type = type;
        this.builder = builder;
    }
    
    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null){
                synchronized(builder){
                    builder.append(type);
                    builder.append(">");
                    builder.append(line);
                    builder.append(NEW_LINE);
                }
                //System.out.println(type + ">" + line);    
            } 
        } catch (IOException ioe) {
            StackTraceElement[] stackTraceElements = ioe.getStackTrace();
            for (StackTraceElement stackTraceElement :stackTraceElements){
                builder.append("StreamBuffer>");
                builder.append(stackTraceElement);
                builder.append(NEW_LINE);
            }
            //ioe.printStackTrace();  
        }
    }
}

