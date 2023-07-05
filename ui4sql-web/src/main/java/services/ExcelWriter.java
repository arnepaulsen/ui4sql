/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

import db.*;

import java.util.Hashtable;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.Collections;
import java.util.Iterator;

import java.sql.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
// import jxl.*;
import org.apache.poi.hssf.usermodel.*;

/*
 *	@name:
 *			ExcelWriter
 *
 * @function: 
 * 
 * 			Create an Excel workbook from a ResultSet, and return the file name
 * 
 * 			- All columns in the RS must be type cast as a type String
 *
 *  @author:
 *		Arne Paulsen
 *
 * @Notes:	
 * 
 * 			This program uses the Jakarta POI project to write to Excel.
 * 
 * 			See http://jakarta.apache.org/poi/index.html
 * 
 *  		POI has several APIs to write Excel.  This program uses the HSSFWorkbook object, which is a simple model 
 *  		with methods to manipulate most of the spreadsheet sub-objects, including sheets, rows, columns, fonts, etc.  
 *  
 * 
 *			HSSF stands for "Horrible Spread Sheet Format" 
 * 
 * 
 * 
 * @parms:   
 * 			
 * @processing:
 *
 * 	
 *
 * @History:
 * 		Version 1.0 10/9/07 First Try
 * 
 * 6/5/10 turn of debut
 * 9/23/10 toggle debug
 * 
 */

public class ExcelWriter {

	static boolean debug = false;

	public int rsStartColumn = 1;

	public int rsEndColumn = 99999;

	/*
	 * Crate the workbook from HashTable (WorkPlugin)
	 * 
	 */

	public String makeWorkbook(String filePath, String templateName,
			String fileNamePrefix, Hashtable ht, String[] headers,
			short[] header_colors, String sheetName) {

		HSSFWorkbook wb;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyhhmmaa");

		String fileName = fileNamePrefix + "_" + dateFormat.format(new Date())
				+ ".xls"; // make a file name
		try {
			wb = new HSSFWorkbook(); // create workbook
			addSheet(wb, sheetName, ht, headers, header_colors); // add sheet
			FileOutputStream out = new FileOutputStream(filePath + fileName);
			wb.write(out);
		} catch (java.io.IOException ex) {

			debug("IO Exception writing file " + ex.toString());
			return fileName;
		}
		return fileName;
	}

	/*
	 * Add a worksheet to the workbook using a ResultSet
	 */
	private int addSheet(HSSFWorkbook wb, String sheetName, Hashtable ht,
			String[] sheet_headers, short[] header_colors) {

		HSSFSheet s = wb.createSheet();

		wb.setSheetName(0, sheetName);

		short rownum = 1; // at least we would print the headers

		try {

			putStringHeaders(wb, s, sheet_headers, header_colors); // put the
			// headers

			SortedSet set = Collections.synchronizedSortedSet(new TreeSet(ht
					.keySet()));

			Object rowId;
			Iterator it = set.iterator();
			DbField[] fields;

			while (it.hasNext()) {
				//debug("... it.netx()");
				rowId = (Object) it.next();
				fields = (DbField[]) ht.get(rowId);

				short tokennum = 0;

				HSSFRow r = s.createRow(rownum);

				for (int x = 2; x < fields.length; x++) {
					HSSFCell c = r.createCell(tokennum);
					c.setCellValue(fields[x].getText());
					tokennum++;
				}

				rownum++;
			}

		} catch (Exception e) {
			debug("addSheet.... " + e.toString());
		}

		return rownum - 1; // take off one for the header record
	}

	/*
	 * Crate the workbook from the input text files.
	 * 
	 */

	public String makeWorkbook2(String filePath, String templateName,
			String fileNamePrefix, ResultSet rs, String[] headers,
			short[] header_colors, String sheetName) {

		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"MMM_dd_yyyy_hhmmss_aa");

			String fileName = fileNamePrefix + "_"
					+ dateFormat.format(new Date()) + ".xls"; // make a file
			// name

			// wb = new HSSFWorkbook(); // create workbook

			HSSFWorkbook wb = new HSSFWorkbook();

			addSheet(wb, sheetName, rs, headers, header_colors); // add sheet

			FileOutputStream out = new FileOutputStream(filePath + fileName);

			wb.write(out);


			return fileName;

		} catch (java.io.IOException ex) {

			debug("IO Exception writing file " + ex.toString());
			return "bad file";
		}

	}

	/*
	 * Add a worksheet to the workbook using a ResultSet
	 */
	private int addSheet(HSSFWorkbook wb, String sheetName, ResultSet rs,
			String[] sheet_headers, short[] header_colors) {

		//debug("adding sheet");

		HSSFSheet s = wb.createSheet();

		// HSSFSheet s = wb.getSheetAt(1);

		wb.setSheetName(0, sheetName);

		short rownum = 1; // at least we would print the headers

		try {

			// put the headers
			ResultSetMetaData rsmd = rs.getMetaData();
			int columns = rsmd.getColumnCount();

			if (sheet_headers != null)
				putStringHeaders(wb, s, sheet_headers, header_colors);
			else
				putMetadataHeaders(wb, s, rsmd);

			// put each row
			while (rs.next()) {

				HSSFRow r = s.createRow(rownum);

				short tokennum = 0;

				for (int x = 1; x < sheet_headers.length + 1; x++) {
					HSSFCell c = r.createCell(tokennum);
					c.setCellValue(getStringFromColumn(rs, rsmd, x));
					tokennum++;
				}
				rownum++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rownum - 1; // take off one for the header record
	}

	public String appendWorkbook(String filePath, String templatePath,
			String fileNamePrefix, ResultSet rs, short startRow, int columns) {

		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"MMM_dd_yyyy_hhmmss_aa");

			String fileName = fileNamePrefix + "_"
					+ dateFormat.format(new Date()) + ".xls"; // make a file
			// name

			// wb = new HSSFWorkbook(); // create workbook

			String outfile = new String(filePath + fileName);

			HSSFWorkbook _hssfworkbook = null;

			_hssfworkbook = new HSSFWorkbook(new FileInputStream(templatePath));

			updateSheet(_hssfworkbook, rs, startRow, columns); // add sheet

			FileOutputStream out = new FileOutputStream(outfile);

			_hssfworkbook.write(out);

			return fileName;

		} catch (java.io.IOException ex) {

			debug("ExcelWriter:appendWorkbook - Exception appending workbook -  " + ex.toString());
			return "bad file";
		}

	}

	/*
	 * Add a worksheet to the workbook using a ResultSet
	 */
	@SuppressWarnings("deprecation")
	private int updateSheet(HSSFWorkbook wb, ResultSet rs, short startRow, int excelColumns) {

		HSSFSheet s = wb.getSheetAt(0);
		
		// HSSFSheet s = wb.getSheetAt(1);

		short rownum = startRow; // at least we would print the headers
		

		try {

			// put the headers
			ResultSetMetaData rsmd = rs.getMetaData();
			int columns = rsmd.getColumnCount();

			
			// put each row
			while (rs.next()) {

				HSSFRow r = s.createRow(rownum);

				short tokennum = 0;

				for (int x = 1; x < excelColumns + 1; x++) {
					HSSFCell c = r.createCell(tokennum);
					c.setCellValue(getStringFromColumn(rs, rsmd, x));
					tokennum++;
				}
				rownum++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rownum - 1; // take off one for the header record
	}

	private void putStringHeaders(HSSFWorkbook wb, HSSFSheet s,
			String[] sheet_headers, short[] colors) {
		HSSFFont fontBold = wb.createFont();
		fontBold.setFontHeightInPoints((short) 13);

		fontBold.setFontName("Arial");
		fontBold.setBoldweight((short) 14);

		// make it blue
		// fontBold.setColor( (short)0xb );

		// style.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);

		// style.setBottomBorderColor(fillColor2);
		// style.setFillForegroundColor(fillColor2);

		try {

			short tokennum = 0;
			HSSFRow r = s.createRow(0);

			// print headers from plugin getExcelHeaders()
			int columns = sheet_headers.length;

			for (int x = 0; x < columns; x++) {

				// debug("header " + x + " .. for " + sheet_headers[x]);

				HSSFCell c = r.createCell(tokennum);

				HSSFCellStyle style = wb.createCellStyle();
				style.setFont(fontBold);

				// style.setFillForegroundColor(HSSFCellStyle.);
				style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

				if (colors != null) {
					// debug("... setting style color ");
					style.setFillForegroundColor(colors[x]);
				}
				c.setCellStyle(style);
				// c.setCellType(HSSFCell.CELL_TYPE_STRING);
				c.setCellValue(sheet_headers[x]);

				tokennum++;
			}

		} catch (Exception e) {
		}

	}

	private void putMetadataHeaders(HSSFWorkbook wb, HSSFSheet s,
			ResultSetMetaData rsmd) {

		HSSFFont fontBold = wb.createFont();
		fontBold.setFontHeightInPoints((short) 14);
		// fontBold.setFontName("Courier New");
		fontBold.setBoldweight((short) 18);

		HSSFCellStyle style = wb.createCellStyle();
		style.setFont(fontBold);

		try {

			short tokennum = 0;
			HSSFRow r = s.createRow(1);

			int columns = rsmd.getColumnCount();

			for (int x = rsStartColumn; (x < columns + 1) && (x < rsEndColumn); x++) {

				HSSFCell c = r.createCell(tokennum);
				// c.setCellType(HSSFCell.CELL_TYPE_STRING);
				c.setCellValue(rsmd.getColumnName(x));
				c.setCellStyle(style);
				tokennum++;
			}

		} catch (Exception e) {
		}

	}

	// return a String for the specified column in the current row
	private String getStringFromColumn(ResultSet parmRs,
			ResultSetMetaData rsmd, int i) {

		int t = 0;
		
		debug ("Excel writer column : " + i);
		try {

			t = rsmd.getColumnType(i);

			//debug("excel write column type : " + t + " name " + rsmd.getColumnName(i) );
			
			// blob
			if (t == -4) {
				//debug("getting blob");

				Blob blob = parmRs.getBlob(i);
				byte[] b = blob.getBytes(1, (int) blob.length());
				String s = new String(b);
				// debug("the blob string is " + s);
				return s;
			}

			// string
			if (t == 12 || t == 1) {
				return parmRs.getString(i);
			}

			// date
			if (t == 91) {
				try {
					return parmRs.getDate(i).toString();
				} catch (SQLException e) {
					return "";
				} catch (Exception e) {
					return "";
				}
			}

			// timestamp
			if (t == 93) {
				try {
					return parmRs.getTimestamp(i).toString();
				} catch (SQLException e) {
					return "";
				} catch (Exception e) {
					return "";
				}
			}

			// integer
			if (t == 4 || t == -6 || t == 5 || t == -5 || t == 8) {
				int z = parmRs.getInt(i);
				return "" + z;
			}

			if (t == 7) {
				// java 1.5 Float f = parmRs.getFloat(i);
				Float f = new Float(parmRs.getFloat(i)); // java 1.4
				return f.toString();
			}

			// varchar -3, text = -1
			if (t == -3 || t == -1) {
				byte[] ba = parmRs.getBytes(i);
				String s = new String(ba);
				return s;
			}

		} catch (SQLException e) {
			debug("SQLException getting string : " + e.toString());
		} catch (Exception e) {
			debug("Exception getting string" + e.toString());
		}

		return "";
	}

	private void debug(String s) {
		if (debug)
			System.out.println("ExcelWriter : " + s);
	}

}
