package org.embl.ebi.escience.scuflui.results;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.embl.ebi.escience.baclava.DataThing;

public class SaveAsExcelTest extends TestCase {
	
	DataThing makeSimple() {
		return makeSimple(null);
	}
	
	DataThing makeSimple(String extra) {
		List<List<String>> values = new ArrayList<List<String>>();
		values.add(Arrays.asList("1", "2", "3"));
		values.add(Arrays.asList("4", "5", "6"));			
		if (extra != null) {
			values.add(Arrays.asList(extra));			
		}
		return new DataThing(values);
	}
	
	DataThing makeEmptyList() {
		List<List<String>> values = new ArrayList<List<String>>();
		values.add(new ArrayList<String>()); // empty!
		values.add(Arrays.asList("4", "5", "6"));		
		return new DataThing(values);
	}
		
	
	DataThing makeDeepList() {
		/*
		 * { {
		 *      {1 2 3}
		 *      {4 5 6}
		 *   }
		 *   {
		 *      {7 8 9}
		 *   }
		 * }
		 */
		List<List<List<String>>> deep = new ArrayList<List<List<String>>>();
		List<List<String>> values = new ArrayList<List<String>>();
		deep.add(values);
		values.add(Arrays.asList("1", "2", "3")); 					
		values.add(Arrays.asList("4", "5", "6"));			
		values = new ArrayList<List<String>>();
		deep.add(values);
		values.add(Arrays.asList("7", "8", "9"));		
		return new DataThing(deep);				
	}
	
	DataThing makeFlat() {
		List<String> l = Arrays.asList("1", "2", "3");
		return new DataThing(l);
	}

	DataThing makeSingle() {		
		return new DataThing("1");
	}

	@SuppressWarnings("unchecked")
	public void testMakeSimple() {
		DataThing simple = makeSimple();
		List<List<String>> l = (List<List<String>>) simple.getDataObject();
		// should be:
		// {
		//    { 1 2 3 }
		//    { 4 5 6 }
		// }
		assertEquals("1", l.get(0).get(0));
		assertEquals("2", l.get(0).get(1));
		assertEquals("3", l.get(0).get(2));
		assertEquals("4", l.get(1).get(0));
		assertEquals("5", l.get(1).get(1));
		assertEquals("6", l.get(1).get(2));	
	}
	
	@SuppressWarnings("unchecked")
	public void testMakeFlat() {
		DataThing simple = makeFlat();
		List<String> l = (List<String>) simple.getDataObject();
		// should be:
		//    { 1 2 3 }
		assertEquals("1", l.get(0));
		assertEquals("2", l.get(1));
		assertEquals("3", l.get(2));
	}
	
	public void testMakeSingle() {
		DataThing simple = makeSingle();
		String l = (String) simple.getDataObject();
		// should be:
		//    1
		assertEquals("1", l);		
	}
	
	@SuppressWarnings("unchecked")
	public void testMakeEmptyList() {
		DataThing empty = makeEmptyList();
		List<List<String>> l = (List<List<String>>) empty.getDataObject();
		// should be:
		// {
		//    { }
		//    { 4 5 6 }
		// }
		assertTrue(l.get(0).isEmpty());
		assertFalse(l.get(1).isEmpty());		
		assertEquals("4", l.get(1).get(0));
		assertEquals("5", l.get(1).get(1));
		assertEquals("6", l.get(1).get(2));	
	}
	
	@SuppressWarnings("unchecked")
	public void testMakeDeepList() {
		DataThing deep = makeDeepList();
		List<List<List<String>>> l = (List<List<List<String>>>) deep.getDataObject();
		/*
		 * {                 
		 *   {
		 *      {1 2 3}
		 *      {4 5 6}
		 *   }
		 *   {
		 *      {7 8 9}  
		 *   }
		 * }
		 */
		assertEquals("1", l.get(0).get(0).get(0));
		assertEquals("2", l.get(0).get(0).get(1));
		assertEquals("3", l.get(0).get(0).get(2));
		assertEquals("4", l.get(0).get(1).get(0));
		assertEquals("5", l.get(0).get(1).get(1));
		assertEquals("6", l.get(0).get(1).get(2));
		assertEquals("7", l.get(1).get(0).get(0));
		assertEquals("8", l.get(1).get(0).get(1));
		assertEquals("9", l.get(1).get(0).get(2));				
	}
	
	public void testSimpleGenerate() throws NoSuchElementException, IntrospectionException {
		Map<String, DataThing> map = new HashMap<String, DataThing>();						
		map.put("simple", makeSimple());
		SaveExcelAction action = new SaveExcelAction(map, null);
		action.generateSheet();
		HSSFCell cell = action.sheet.getRow(0).getCell((short)0);		
		// Is action.getCell OK?
		assertSame(cell, action.getCell(0, 0));
		// And so should our getCell be
		HSSFSheet sheet = action.sheet;
		assertSame(cell.getStringCellValue(), getCell(sheet, 0, 0));
		// should be:
		// simple
		//        1     2     3
		//        4     5     6
		assertEquals("simple", getCell(sheet, 0,0));		
		assertEquals("1", getCell(sheet, 1,0));
		assertEquals("2", getCell(sheet, 1,1));
		assertEquals("3", getCell(sheet, 1,2));
		assertEquals("4", getCell(sheet, 2,0));
		assertEquals("5", getCell(sheet, 2,1));
		assertEquals("6", getCell(sheet, 2,2));
				
	}
	
	public void testFlatList() throws NoSuchElementException, IntrospectionException {
		// Test with {1 2 3}
		// to:
		// flat
		// 1
		// 2
		// 3
		Map<String, DataThing> map = new HashMap<String, DataThing>();						
		map.put("flat", makeFlat());
		SaveExcelAction action = new SaveExcelAction(map, null);
		action.generateSheet();
		HSSFSheet sheet = action.sheet;
		assertEquals("flat", getCell(sheet, 0,0));		
		assertEquals("1", getCell(sheet, 1,0));
		assertEquals("2", getCell(sheet, 2,0));
		assertEquals("3", getCell(sheet, 3,0));
		assertNull(getCell(sheet, 1,1));
		assertNull(getCell(sheet, 1,2));
		assertNull(getCell(sheet, 3,1));	
	}
	
	public void testSingle() throws NoSuchElementException, IntrospectionException {
		// Test with 1
		// to:
		// single
		// 1		
		Map<String, DataThing> map = new HashMap<String, DataThing>();						
		map.put("single", makeSingle());
		SaveExcelAction action = new SaveExcelAction(map, null);
		action.generateSheet();
		HSSFSheet sheet = action.sheet;
		assertEquals("single", getCell(sheet, 0,0));		
		assertEquals("1", getCell(sheet, 1,0));		
		assertNull(getCell(sheet, 1,1));
		assertNull(getCell(sheet, 2,0));		
	}
	
	public void testSimpleSave() throws NoSuchElementException, IntrospectionException, IOException {
		Map<String, DataThing> map = new HashMap<String, DataThing>();						
		map.put("simple", makeSimple());
		SaveExcelAction action = new SaveExcelAction(map, null);
		action.generateSheet();
		File tempFile = File.createTempFile("tavernatest", ".xls");
		action.saveSheet(tempFile);
		// Let's open it again
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(tempFile));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        assertEquals("simple", sheet.getRow(0).getCell((short) 0).getStringCellValue());
        assertEquals("6", sheet.getRow(2).getCell((short) 2).getStringCellValue());				
	}
	
	String getCell(HSSFSheet sheet, int row, int col) {
		try {	
			return sheet.getRow(row).getCell((short) col).getStringCellValue();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public void testMultipleSheet() throws NoSuchElementException, IntrospectionException, IOException  {
		Map<String, DataThing> map = new HashMap<String, DataThing>();
		map.put("simple", makeSimple());
		map.put("some", makeSimple("Something"));		
		SaveExcelAction action = new SaveExcelAction(map, null);
		action.generateSheet();
		File tempFile = File.createTempFile("tavernatest", ".xls");
		action.saveSheet(tempFile);
		// Let's open it again
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(tempFile));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        
        int simpleCol, someCol;
        // We saved from a Map, so we don't know if simple or some is first.
        if (getCell(sheet, 0, 0).equals("simple")) {
        	simpleCol = 0;  someCol = 4;
        } else {
        	simpleCol = 4;  someCol = 0;
        }                
        assertEquals("simple", getCell(sheet, 0, simpleCol)); 
        assertEquals("some", getCell(sheet, 0, someCol));        
        assertEquals("1", getCell(sheet, 1, simpleCol));
        assertEquals("1", getCell(sheet, 1, someCol));
        assertEquals("6", getCell(sheet, 2, simpleCol+2));
        assertEquals("6", getCell(sheet, 2, someCol+2));
        assertNull(getCell(sheet, 3, simpleCol));                
        assertEquals("Something", getCell(sheet, 3, someCol));	
	}
	
	public void testEmptyLists() throws NoSuchElementException, IntrospectionException {
		Map<String, DataThing> map = new HashMap<String, DataThing>();						
		map.put("empty", makeEmptyList());
		SaveExcelAction action = new SaveExcelAction(map, null);
		action.generateSheet();
		HSSFSheet sheet = action.sheet;
		// should be:
		// empty
		// 
		//        4     5     6
		assertEquals("empty", getCell(sheet, 0,0));
		assertNull(getCell(sheet, 1,0));
		assertNull(getCell(sheet, 1,1));
		assertNull(getCell(sheet, 1,2));		
		assertEquals("4", getCell(sheet, 2,0));
		assertEquals("5", getCell(sheet, 2,1));
		assertEquals("6", getCell(sheet, 2,2));		
	}
	
	public void testDeepLists() throws NoSuchElementException, IntrospectionException {		
		Map<String, DataThing> map = new HashMap<String, DataThing>();						
		map.put("deep", makeDeepList());
		SaveExcelAction action = new SaveExcelAction(map, null);
		action.generateSheet();
		HSSFSheet sheet = action.sheet;		
		// should be:
		// deep
		//        1     2     3
		//        4     5     6		
		//        7     8     9		
		assertEquals("deep", getCell(sheet, 0,0));		
		assertEquals("1", getCell(sheet, 1,0));
		assertEquals("2", getCell(sheet, 1,1));
		assertEquals("3", getCell(sheet, 1,2));
		assertEquals("4", getCell(sheet, 2,0));
		assertEquals("5", getCell(sheet, 2,1));
		assertEquals("6", getCell(sheet, 2,2));
		assertEquals("7", getCell(sheet, 3,0));
		assertEquals("8", getCell(sheet, 3,1));
		assertEquals("9", getCell(sheet, 3,2));
	}
	
	
}
