package uk.ac.manchester.cs.elico.utilities.csvimporter;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JPanel;

import com.csvreader.CsvReader;

public class CSVImporter extends JPanel {
	// skeleton code
	public static void tester() {
		
		CsvReader csvReader = null;
		
		try {
			
			csvReader = new CsvReader("");
			csvReader.setDelimiter(',');
			
		} catch (FileNotFoundException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			
			csvReader.readHeaders();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			
			while (csvReader.readRecord()) {
				
				String productID = csvReader.get("FIELD");
				System.out.println("header :" + productID);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		tester();
	}

}
