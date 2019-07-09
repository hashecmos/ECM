package com.ecm.db.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.util.IOUtils;

import com.ecm.db.list.ECMRoleList;
import com.ecm.db.list.ECMUserList;
import com.ecm.db.model.ECMRole;
import com.ecm.db.model.ECMUser;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.transport.TWorkitemQuery;

public class ExcelHelper {
	public static void createExcelFile(HttpServletRequest req, HttpServletResponse resp,
			String sheetName, ArrayList<ArrayList<Object>> bookData,
			TWorkitemQuery twi, Boolean isSent) throws Exception {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet(sheetName);
			Map<String, CellStyle> styles = createStyles(workbook);
			InputStream inputStream = new FileInputStream(req.getSession().getServletContext().getRealPath("images") + "\\koc.png");
			byte[] bytes = IOUtils.toByteArray(inputStream);
			int pictureIdx = workbook.addPicture(bytes,
					Workbook.PICTURE_TYPE_PNG);
			inputStream.close();
			CellStyle cellStyle = workbook.createCellStyle();
			CreationHelper helper = workbook.getCreationHelper();
			Drawing drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(1);
			anchor.setRow1(0);
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			Cell cell = sheet.createRow(0).createCell(1);
			pict.resize(0.99, 1.0);
			sheet.setPrintGridlines(false);
			sheet.setDisplayGridlines(false);
			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			sheet.setFitToPage(true);
			sheet.setHorizontallyCenter(true);
			sheet.setColumnWidth(0, 6 * 256);
			sheet.setColumnWidth(1, 6 * 256);
			sheet.setColumnWidth(2, 11 * 256);
			sheet.setColumnWidth(3, 14 * 256);
			sheet.setColumnWidth(4, 14 * 256);
			sheet.setColumnWidth(5, 14 * 256);
			sheet.setColumnWidth(6, 14 * 256);
			sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0, 9999),
					IgnoredErrorType.NUMBER_STORED_AS_TEXT);
			Row titleRow = sheet.createRow(0);
			titleRow.setHeightInPoints(47);
			for (int i = 0; i <= 15; i++) {
				titleRow.createCell(i).setCellStyle(styles.get("title"));
			}
			Cell titleCell = titleRow.getCell(2);
			if (isSent) {
				titleCell.setCellValue("DIMS Sent Report");
			} else {
				titleCell.setCellValue("DIMS Inbox Report");
			}
			sheet.addMergedRegion(CellRangeAddress.valueOf("$C$1:$H$1"));
			Row row = sheet.createRow(1);
			cell = row.createCell(1);
			row = sheet.createRow(2);
			cell = row.createCell(2);
			cell.setCellValue("Subject-");
			cell = row.createCell(3);
			cell.setCellValue(twi.subject);
			cell.setCellStyle(styles.get("item_left"));
			cell = row.createCell(5);
			if (isSent) {
				cell.setCellValue("Recipient Name:-");
				cell = row.createCell(6);
				cell.setCellValue(splitandCheckTypes(twi.recipientName));
			} else {
				cell.setCellValue("Sender Name:-");
				cell = row.createCell(6);
				cell.setCellValue(splitandCheckTypes(twi.senderName));
			}
			cell.setCellStyle(styles.get("item_left"));
			row = sheet.createRow(3);
			cell = row.createCell(2);
			cell.setCellValue("Status:-");
			cell = row.createCell(3);
			cell.setCellValue(twi.status);
			cell.setCellStyle(styles.get("item_left"));
			cell = row.createCell(5);
			cell.setCellValue("Type:");
			cell = row.createCell(6);
			cell.setCellValue(twi.type);
			cell.setCellStyle(styles.get("item_left"));
			row = sheet.createRow(4);
			cell = row.createCell(2);
			if (isSent) {
				cell.setCellValue("Sent Date:-");
			} else {
				cell.setCellValue("Received Date:-");
			}
			cell = row.createCell(3);
			if (twi.receivedDate != null) {
				cell.setCellValue(twi.receivedDate.replace(";", " - "));
			}
			cell.setCellStyle(styles.get("item_left"));
			cell = row.createCell(5);
			cell.setCellValue("Priority:-");
			cell = row.createCell(6);
			if (twi.priority == 2) {
				cell.setCellValue("Normal");
			} else if (twi.priority == 3) {
				cell.setCellValue("Heigh");
			} else if (twi.priority == 1) {
				cell.setCellValue("Low");
			}
			cell.setCellStyle(styles.get("item_left"));
			if (!isSent) {
				row = sheet.createRow(5);
				cell = row.createCell(2);
				cell.setCellValue("Deadline:");
				cell = row.createCell(3);
				cell.setCellValue(twi.deadline);
				if (twi.deadline != null) {
					cell.setCellValue(twi.deadline.replace(";", " - "));
				}
				cell.setCellStyle(styles.get("item_left"));
				cell = row.createCell(5);
				cell.setCellValue("Instructions:-");
				cell = row.createCell(6);
				cell.setCellValue(twi.instructions);
				cell.setCellStyle(styles.get("item_left"));
			}
			sheet.createFreezePane(0, 8);
			int rowCount = 7;
			int cellCount = 0;
			for (ArrayList<Object> aBook : bookData) {
				row = sheet.createRow(rowCount++);
				// sheet.autoSizeColumn(rowCount);
				if (row == null)
					continue;
				int columnCount = 0;
				for (Object field : aBook) {
					if (field == null)
						field = "";
					cell = row.createCell(columnCount++);
					cellStyle = sheet.getWorkbook().createCellStyle();
					sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0,
							9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
					Font font = sheet.getWorkbook().createFont();
					font.setBold(true);
					font.setFontHeightInPoints((short) 9);
					font.setFontName("Trebuchet MS");
					cellStyle.setFont(font);
					cellStyle.setAlignment(HorizontalAlignment.LEFT);
					cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
					cellStyle
							.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
									.getIndex());
					cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					XSSFCellStyle my_style = workbook.createCellStyle();
					my_style.setBorderBottom(XSSFCellStyle.BORDER_DOTTED);
					//sheet.autoSizeColumn(columnCount);
					if (cellCount == 0);
					{
						cell.setCellStyle(cellStyle);
						sheet.autoSizeColumn(cellCount);
					
					}
					if (cellCount >= 1) {
						cell.setCellStyle(my_style);
					}
					if (cell == null)
						continue;
					if (field instanceof String) {
						cell.setCellValue((String) field);
					} else if (field instanceof Integer) {
						cell.setCellValue((Integer) field);
					} else if (field instanceof Date) {
						cell.setCellValue((Date) field);
					} else
						cell.setCellValue((String) field.toString());
				}
				cellCount++;
			}
			String docTitle = "Inbox-"
					+ DBUtil.formatDateForFileName(new Date());
			workbook.write(resp.getOutputStream());
			workbook.close();
			resp.setContentType("application/vnd.ms-excel");
			resp.setHeader("Content-Disposition", "attachment; filename="
					+ docTitle + ".xlsx");
		} catch (Exception e) {
			throw e;
		}
	}

	
	//ROLE:1;10000@USER:1007
	private static String splitandCheckTypes(String fullString) throws Exception {
		String retString = "";
		if ((fullString != null) && (fullString.length() > 0)) {
			String[] types = fullString.trim().split("@");
			String roleTypes = types[0];
			retString = splitandCheck(roleTypes);
			if (types.length > 1) {
				String userTypes = types[1];
				retString = retString + ","
						+ splitandCheck(userTypes);
			}
		}
		return retString;
	}

	//ROLE:1;10000
	//USER:1007

	private static String splitandCheck(String fullString) throws Exception {
		String retString = "";
		if ((fullString != null) && (fullString.length() > 0)) {
			String[] props = fullString.trim().split(":");
			String type = props[0];
			String ids = "";
			if (props.length > 1)
				ids = props[1];
			String[] idlist = ids.split(";");
			String inClause = "";
			for (String id : idlist) {
				if (id.trim().length() > 0) {
					if (inClause.length() > 0)
						inClause += ",";
					if(type.equalsIgnoreCase("USER"))
						inClause += ECMUserList.getInstance().getUserFullName(DBUtil.stringToLong(id.trim()));
					else{
						ECMRole eRole = new ECMRole();
						eRole.setId(DBUtil.stringToLong(id.trim()));
						eRole.load();
						inClause += eRole.getName();	
					}
				}
			}
			if (inClause.length() > 0) {
				retString = inClause + " ";
			}
		}
		return retString;
	}

	public static void createExcelFileForReports(HttpServletRequest req, HttpServletResponse resp,
			String sheetName, ArrayList<ArrayList<Object>> bookData,
			TReportFilter twq, String isSent) throws Exception {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet(sheetName);
			Map<String, CellStyle> styles = createStyles(workbook);
			InputStream inputStream = new FileInputStream(req.getSession().getServletContext().getRealPath("images") + "\\koc.png");
			byte[] bytes = IOUtils.toByteArray(inputStream);
			int pictureIdx = workbook.addPicture(bytes,
					Workbook.PICTURE_TYPE_PNG);
			inputStream.close();
			CellStyle cellStyle = workbook.createCellStyle();
			CreationHelper helper = workbook.getCreationHelper();
			Drawing drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(1);
			anchor.setRow1(0);
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			pict.resize(0.99, 1.0);
			sheet.setPrintGridlines(false);
			sheet.setDisplayGridlines(false);
			PrintSetup printSetup = sheet.getPrintSetup();
			printSetup.setLandscape(true);
			sheet.setFitToPage(true);
			sheet.setHorizontallyCenter(true);
			sheet.setColumnWidth(0, 6 * 256);
			sheet.setColumnWidth(1, 6 * 256);
			sheet.setColumnWidth(2, 11 * 256);
			sheet.setColumnWidth(3, 14 * 256);
			sheet.setColumnWidth(4, 14 * 256);
			sheet.setColumnWidth(5, 14 * 256);
			sheet.setColumnWidth(6, 14 * 256);
			Row titleRow = sheet.createRow(0);
			titleRow.setHeightInPoints(34);
			for (int i = 0; i <= 13; i++) {
				titleRow.createCell(i).setCellStyle(styles.get("title"));
			}
			Cell titleCell = titleRow.getCell(2);
			if (isSent.equals("ExportOrgwC")) {
				titleCell.setCellValue("Sent  WorkItem Count Report");
			}
			if (isSent.equals("exportRII")) {
				titleCell.setCellValue("Sent Workflow Items Report");
			}
			if (isSent.equals("exportOrgDocCounts")) {
				titleCell.setCellValue("Document Count Report");
			}
			if (isSent.equals("exportOrgESignsReport")) {
				titleCell.setCellValue("ESent  Document Count Report");
			} else {
				titleCell.setCellValue("Inbox Workflow Items Report");
			}
			sheet.addMergedRegion(CellRangeAddress.valueOf("$C$1:$H$1"));
			Row row = sheet.createRow(1);
			Cell cell = row.createCell(1);
			row = sheet.createRow(2);
			cell = row.createCell(2);
			cell.setCellValue("Status:-");
			cell = row.createCell(3);
			cell.setCellValue(twq.status);
			cell.setCellStyle(styles.get("item_left"));
			cell = row.createCell(5);
			cell.setCellValue("OrgCode:-");
			cell = row.createCell(6);
			cell.setCellValue(twq.orgCode);
			cell.setCellStyle(styles.get("item_left"));
			row = sheet.createRow(4);
			cell = row.createCell(2);
			cell.setCellValue("To Date:-");
			cell = row.createCell(3);
			cell.setCellValue(twq.toDate);
			cell.setCellStyle(styles.get("item_left"));
			cell = row.createCell(5);
			cell.setCellValue("From Date:-");
			cell = row.createCell(6);
			cell.setCellValue(twq.fromDate);
			cell.setCellStyle(styles.get("item_left"));
			sheet.createFreezePane(0, 7);
			int rowCount = 6;
			int cellCount = 0;
			for (ArrayList<Object> aBook : bookData) {
				row = sheet.createRow(rowCount++);
				sheet.autoSizeColumn(rowCount);
				if (row == null)
					continue;
				int columnCount = 0;
				for (Object field : aBook) {
					if (field == null) {
						field = "";
						System.out.print(field);
					}
					cell = row.createCell(columnCount++);
					XSSFCellStyle my_style1 = workbook.createCellStyle();
					my_style1 = sheet.getWorkbook().createCellStyle();
					Font font = sheet.getWorkbook().createFont();
					font.setBold(true);
					font.setFontHeightInPoints((short) 9);
					font.setFontName("Trebuchet MS");
					my_style1.setFont(font);
					my_style1.setAlignment(HorizontalAlignment.CENTER);
					my_style1.setVerticalAlignment(VerticalAlignment.CENTER);
					my_style1
							.setFillForegroundColor(IndexedColors.GREY_25_PERCENT
									.getIndex());
					my_style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					XSSFCellStyle my_style2 = workbook.createCellStyle();
					my_style2.setBorderBottom(XSSFCellStyle.BORDER_DOTTED);
					sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0,
							9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
					sheet.autoSizeColumn(columnCount);
					if (cellCount == 0)
						;
					{
						cell.setCellStyle(my_style1);
					}
					if (cellCount >= 1) {
						cell.setCellStyle(my_style2);
					}
					if (cell == null)
						continue;
					if (field instanceof String) {
						cell.setCellValue((String) field);
					} else if (field instanceof Integer) {
						cell.setCellValue((Integer) field);
					} else if (field instanceof Date) {
						cell.setCellValue((Date) field);
					} else
						cell.setCellValue((String) field.toString());
				}
				cellCount++;
			}
			String docTitle = "Inbox-"
					+ DBUtil.formatDateForFileName(new Date());
			workbook.write(resp.getOutputStream());
			workbook.close();
			resp.setContentType("application/vnd.ms-excel");
			resp.setHeader("Content-Disposition", "attachment; filename="
					+ docTitle + ".xlsx");
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void createExcelFileForDocuments(HttpServletRequest req, HttpServletResponse resp,
            String sheetName, ArrayList<ArrayList<Object>> bookData) throws Exception {
      try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("ECM_Document_Reports");
            Map<String, CellStyle> styles = createStyles(workbook);
            InputStream inputStream = new FileInputStream(req.getSession().getServletContext().getRealPath("images") + "\\koc.png");
            byte[] bytes = IOUtils.toByteArray(inputStream);
            int pictureIdx = workbook.addPicture(bytes,
                        Workbook.PICTURE_TYPE_PNG);
            inputStream.close();
            CellStyle cellStyle = workbook.createCellStyle();
            CreationHelper helper = workbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            Cell cell = sheet.createRow(0).createCell(1);
            pict.resize(0.99, 1.0);
            sheet.setPrintGridlines(false);
            sheet.setDisplayGridlines(false);
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setLandscape(true);
            sheet.setFitToPage(true);
            sheet.setHorizontallyCenter(true);
            sheet.setColumnWidth(0, 10 * 256);
            sheet.setColumnWidth(1, 6 * 256);
            sheet.setColumnWidth(2, 11 * 256);
            sheet.setColumnWidth(3, 14 * 256);
            sheet.setColumnWidth(4, 14 * 256);
            sheet.setColumnWidth(5, 14 * 256);
            sheet.setColumnWidth(6, 14 * 256);
            sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0, 9999),
                        IgnoredErrorType.NUMBER_STORED_AS_TEXT);
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(47);
            for (int i = 0; i <= 12; i++) {
                  titleRow.createCell(i).setCellStyle(styles.get("title"));
            }
            Cell titleCell = titleRow.getCell(1);
            titleCell.setCellValue(sheetName);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$B$1:$H$1"));
            
            //AKV Start
            Row userRow = sheet.createRow(1);
            cellStyle = sheet.getWorkbook().createCellStyle();
            sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0,
                        9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
            Font font1 = sheet.getWorkbook().createFont();
            font1.setBold(false);
            font1.setItalic(true);
            font1.setFontHeightInPoints((short) 9);
            font1.setFontName("Trebuchet MS");
            cellStyle.setFont(font1);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            for (int i = 0; i <= 12; i++) {
            	userRow.createCell(i).setCellStyle(cellStyle);  
            }
            Cell usrCell = userRow.getCell(1);
            usrCell.setCellStyle(cellStyle);
            String empName = RequestHelper.getLoggedInEmpName(req, "");
            usrCell.setCellValue("Created by: " + empName + "| Created On: " + DBUtil.getTodayDateTime());
            
            sheet.autoSizeColumn(1);
            sheet.addMergedRegion(CellRangeAddress.valueOf("$B$2:$H$2"));
            
            Row row = sheet.createRow(2);
          //AKV End
            
            int rowCount = 3;
            int cellCount = 0;
            for (ArrayList<Object> aBook : bookData) {
                  row = sheet.createRow(rowCount++);
                  // sheet.autoSizeColumn(rowCount);
                  if (row == null)
                        continue;
                  int columnCount = 0;
                  for (Object field : aBook) {
                        if (field == null)
                              field = "";
                        cell = row.createCell(columnCount++);
                        cellStyle = sheet.getWorkbook().createCellStyle();
                        sheet.addIgnoredErrors(new CellRangeAddress(0, 9999, 0, 9999), IgnoredErrorType.NUMBER_STORED_AS_TEXT);
                        Font font = sheet.getWorkbook().createFont();
                        font.setBold(true);
                        font.setFontHeightInPoints((short) 9);
                        font.setFontName("Trebuchet MS");
                        cellStyle.setFont(font);
                        cellStyle.setAlignment(HorizontalAlignment.LEFT);
                        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                        cellStyle
                                    .setFillForegroundColor(IndexedColors.GREY_25_PERCENT
                                                .getIndex());
                        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        XSSFCellStyle my_style = workbook.createCellStyle();
                        my_style.setBorderBottom(XSSFCellStyle.BORDER_DOTTED);
                        //sheet.autoSizeColumn(columnCount);
                        if (cellCount == 0);
                        {
                              cell.setCellStyle(cellStyle);
                              sheet.autoSizeColumn(cellCount);
                        }
                        if (cellCount >= 1) {
                              cell.setCellStyle(my_style);
                        }
                        if (cell == null)
                              continue;
                        if (field instanceof String) {
                              cell.setCellValue((String) field);
                        } else if (field instanceof Integer) {
                              cell.setCellValue((Integer) field);
                        } else if (field instanceof Date) {
                              cell.setCellValue((Date) field);
                        } else
                              cell.setCellValue((String) field.toString());
                  }
                  cellCount++;
            }
            String docTitle = "ECM-Documents-Report"
                        + DBUtil.formatDateForFileName(new Date());
            workbook.write(resp.getOutputStream());
            workbook.close();
            resp.setContentType("application/vnd.ms-excel");
            resp.setHeader("Content-Disposition", "attachment; filename="
                        + docTitle + ".xlsx");
      } catch (Exception e) {
            throw e;
      }
}


	public static Map<String, CellStyle> createStyles(Workbook workbook) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		CellStyle style;
		Font titleFont = workbook.createFont();
		titleFont.setFontHeightInPoints((short) 14);
		titleFont.setFontName("Trebuchet MS");
		style = workbook.createCellStyle();
		style.setFont(titleFont);
		style.setBorderBottom(BorderStyle.THICK);
		style.setBottomBorderColor(IndexedColors.BLUE.getIndex());
		styles.put("title", style);
		Font itemFont = workbook.createFont();
		itemFont.setFontHeightInPoints((short) 9);
		itemFont.setFontName("Trebuchet MS");
		style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(itemFont);
		style.setBorderBottom(BorderStyle.THIN);
		
        style.setWrapText(true); //Set wordwrap
            
		style.setBottomBorderColor(IndexedColors.BLUE.getIndex());
		styles.put("item_left", style);
		Font monthFont = workbook.createFont();
		monthFont.setFontHeightInPoints((short) 14);
		monthFont.setColor(IndexedColors.WHITE.getIndex());
		monthFont.setBold(true);
		style = workbook.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(monthFont);
		styles.put("header", style);
		styles.put("item_right", style);
		return styles;
	}
}
