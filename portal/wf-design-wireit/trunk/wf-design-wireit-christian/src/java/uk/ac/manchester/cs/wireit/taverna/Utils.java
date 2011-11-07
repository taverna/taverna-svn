package uk.ac.manchester.cs.wireit.taverna;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Support class that holds static method shared by several classes
 * 
 * @author Christian
 */
public class Utils {
    
    /**
     * Checks to make sure a directory exists.
     * @param dir Directory to check.
     * @NullPointerException If the directory is null
     * @FileNotFoundException If the directory does nnot exists
     * @throws IOException If the directory is not a directory or can not be read.
     */
    public static void checkDirectory(File dir) throws IOException{
        if (dir == null){
            throw new NullPointerException ("Illegal attempt to use null directory");            
        }
        if (!dir.exists()){
            throw new FileNotFoundException (dir.getAbsolutePath() + " does not exist.");
        }
        if (!dir.isDirectory()){
            throw new IOException (dir.getAbsolutePath() + " is not a directory.");
        }
        if (!dir.canRead()){
            throw new IOException (dir.getAbsolutePath() + " can not be read.");
        }       
    }

    /**
     * Check to see that a file appear exectuable.
     * @param file File to be tested.
     * @throws NullPointerException If the file is null.
     * @throws FileNotFoundException If the file does not exists or is not a file.
     * @throws IOException If the file can not be read or executed.
     */
    public static void checkFileExecutable(File file) throws IOException{
        checkFile(file);
        if (!file.canExecute()){
             throw new IOException (file.getAbsolutePath() + " can not be executed.");
        }        
    }
    
    /**
     * Check to see that a file appears readable.
     * @param file File to be tested.
     * @throws NullPointerException If the file is null.
     * @throws FileNotFoundException If the file does not exists or is not a file.
     * @throws IOException If the file can not be read.
     */
    public static void checkFile(File file) throws IOException{
        if (file == null){
            throw new NullPointerException ("Illegal attempt to use null file");            
        }
        if (!file.isFile()){
             throw new FileNotFoundException (file.getAbsolutePath() + " is not a file.");
        }
        if (!file.canRead()){
             throw new IOException (file.getAbsolutePath() + " can not be read.");
        }
    }

    private static String twoCharacter(int value){
        if (value < 10) {
            return "0" + value;
        } else {
            return "" + value;
        }
    }
    
    /**
     * Creates a sub directory whose name is based on the current date and time.
     * 
     * Format is year month day hour minute second so that alphabetical order is the same as date order.
     * @param parent Directory to create this directory in.
     * @return new created directory
     * @NullPointerException If the directory is null
     * @FileNotFoundException If the directory does nnot exists
     * @throws IOException If the parent is not a directory or can not be read.
     *     Also thrown if the new directory is not a directory becasue either it could not be created 
     * or is already a file.
     */
    public static File createCalendarBasedDirectory(File parent) throws IOException{
        checkDirectory(parent);
        GregorianCalendar calendar = new GregorianCalendar();
        String name = calendar.get(Calendar.YEAR) + "_" + twoCharacter(calendar.get(Calendar.MONTH) + 1) + "_" + 
                twoCharacter(calendar.get(Calendar.DAY_OF_MONTH)) + "_" + 
                twoCharacter(calendar.get(Calendar.HOUR_OF_DAY)) + "_" + 
                twoCharacter(calendar.get(Calendar.MINUTE)) + "_" + twoCharacter(calendar.get(Calendar.SECOND));
        File directory = new File (parent, name);
        directory.mkdir();
        if (!directory.isDirectory()) {
            throw new IOException("Not able to create Directory " + directory.getAbsolutePath() + " directory.");
        }
       
        return directory;
    }
    
    /** 
     * Dettermines if the system this is run on is Windows or not.
     * 
     * Works by checking of the os.name property begins with "win" ignoring case.
     * 
     * @return True if or only if this is Windows.
     */
    public static boolean isWindows(){
		String os = System.getProperty("os.name").toLowerCase();
		//windows
	    return (os.indexOf( "win" ) >= 0); 
	}


}
/*
 *    /*    Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat();
        System.out.println(formatter.format(now));
        Calendar calendar = new GregorianCalendar();
        System.out.println("ERA: " + calendar.get(Calendar.ERA));
 System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 System.out.println("DATE: " + calendar.get(Calendar.DATE));
 System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 System.out.println("DAY_OF_WEEK_IN_MONTH: "
                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 System.out.println("ZONE_OFFSET: "
                    + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000)));
 System.out.println("DST_OFFSET: "
                    + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000)));

 System.out.println("Current Time, with hour reset to 3");
 calendar.clear(Calendar.HOUR_OF_DAY); // so doesn't override
 calendar.set(Calendar.HOUR, 3);
 System.out.println("ERA: " + calendar.get(Calendar.ERA));
 System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 System.out.println("DATE: " + calendar.get(Calendar.DATE));
 System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 System.out.println("DAY_OF_WEEK_IN_MONTH: "
                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 System.out.println("ZONE_OFFSET: "
        + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000))); // in hours
 System.out.println("DST_OFFSET: "
        + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000))); // in hours

 */