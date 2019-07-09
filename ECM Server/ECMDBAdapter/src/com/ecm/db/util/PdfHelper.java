package com.ecm.db.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.ecm.db.list.ECMUserList;
import com.ecm.db.model.ECMRole;
import com.ecm.db.model.ECMUser;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.transport.TWorkitemQuery;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Header;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Section;
import com.itextpdf.text.TextElementArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfHelper {

	public static void createPdfFile(HttpServletResponse resp, HttpServletRequest req, String sheetName,
			ArrayList<ArrayList<Object>> bookData  ,TReportFilter twq, boolean isSent) throws Exception {
		OutputStream os = resp.getOutputStream();
		

		try {
			Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD, new BaseColor(255, 255, 255));
			Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.NORMAL, new BaseColor(0, 0, 0));
			/*Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
			Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);*/
			Paragraph paragraph = new Paragraph();
			Document doc = null;
			float left = 26;
	        float right = 26;
	        float top = 30;
	        float bottom = 150;
			if (bookData.get(0).size() > 5) {
				
				Rectangle envelope = new Rectangle(1200f, 850f);
				 doc = new Document(envelope, left, right,bottom,top);
				
			} else {
				Rectangle envelope1 = new Rectangle(1200f, 850f);
				doc = new Document(envelope1, left, right,bottom,top);
				//doc = new Document(PageSize.A4);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(doc, baos);
			HeaderFooterPageEvent event = new HeaderFooterPageEvent(sheetName, req);
			writer.setPageEvent(event);
			doc.open();
			/*doc.add(spaceTable(8));
	        doc.add(topSpaceTable(4));*/
	        float[] threeColumnWidth = { 1, 1};
	        PdfPTable Btable = new PdfPTable(2);
	        Btable.setWidthPercentage(90);
	        Btable.setWidths(threeColumnWidth);
	        Btable.setSpacingAfter(40);
	        Btable.addCell(leftBorderColumn(" Organization Code: ", twq.orgCode));
	       // Btable.addCell(noBorderColumn("Status: ",twq.status ));
	        Btable.addCell(rightBorderColumn("Status: ",twq.status));
	        doc.add(Btable);
	        Btable.deleteLastRow();
	        Btable.addCell(leftBorderColumn("From: ",twq.fromDate ));
	       // Btable.addCell(noBorderColumn("To: ", twq.toDate));
	        Btable.addCell(rightBorderColumn("To: ", twq.toDate));
	        doc.add(Btable);
			/* PdfPTable Dtable = new PdfPTable(1);
		        Dtable.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        Dtable.setWidthPercentage(30);
		        Font white = new Font();
		        white.setColor(BaseColor.WHITE);
		        PdfPCell cellD = new PdfPCell(new Phrase(" Date" , white));
		        cellD.setBackgroundColor(BaseColor.BLACK);
		        cellD.setBorderColor(BaseColor.GRAY);
		        cellD.setBorderWidth(2f);
		        Dtable.addCell(cellD);
		        PdfPCell cellTwo = new PdfPCell(new Phrase("10/01/2015"));
		        cellTwo.setBorderWidth(2f);
		        Dtable.addCell(cellTwo);
		        doc.add(Dtable);
		        Dtable.setTotalWidth(90);
		        PdfContentByte canvas = writer.getDirectContent();
		        Dtable.writeSelectedRows(0, -1, doc.right() - 90, doc.top(), canvas);
		        Paragraph paragraph2 = new Paragraph();

                paragraph2.setSpacingAfter(25);
                paragraph2.setSpacingBefore(25);
                paragraph2.setAlignment(Element.ALIGN_CENTER);
                paragraph2.setIndentationLeft(50);
                paragraph2.setIndentationRight(50);
                Chunk chunk =new Chunk("This is Table"); 
                paragraph2.add(chunk);
                doc.add(paragraph2);*/
			    PdfPTable table = new PdfPTable(bookData.get(0).size());
			      if (bookData.get(0).size() > 5) {
			   // table.setTotalWidth((PageSize.A4.rotate().getWidth() - (PageSize.A4.getWidth() / 4)));
			    	  float[] columnWidths = new float[]{20f, 60f, 30f, 60f,60f, 60f, 30f, 30f,30f};
			            table.setWidths(columnWidths);
			            table.setWidthPercentage(100);
			} else
			      {
			       table.setTotalWidth((PageSize.A4.getWidth() - (PageSize.A4.getWidth() / 4)));
			      }
			int index = 0;
            for (ArrayList<Object> aBook : bookData) {
				for (int i = 0; i < aBook.size(); i++) {
					if (index == 0) {
						
						
						PdfPCell tableCell = insertCell(table, aBook.get(i).toString(), Element.ALIGN_LEFT,1, boldFont, true);
						table.addCell(tableCell);
					} else {
						String text = "";
						if (aBook.get(i) != null) {
							text = aBook.get(i).toString();
						}
						PdfPCell tableCell = insertCell(table, text, Element.ALIGN_LEFT, 1, normalFont, false);
						table.addCell(tableCell);
					}
				}
				index++;
			}
			String docTitle = sheetName + "-" + DBUtil.formatDateForFileName(new Date());
			paragraph.add(table);
			doc.add(paragraph);
			
			doc.close();
			resp.setContentType("application/pdf");
			resp.setHeader("Content-Disposition", "attachment; filename=" + docTitle + ".pdf");

			baos.writeTo(os);
			os.flush();
			os.close();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void createWorkitemPdfFile(HttpServletResponse resp, HttpServletRequest req, String sheetName,
			ArrayList<ArrayList<Object>> bookData  ,TWorkitemQuery twq, boolean isSent) throws Exception {
		OutputStream os = resp.getOutputStream();
		

		try {
			Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD, new BaseColor(255, 255, 255));
			Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.NORMAL, new BaseColor(0, 0, 0));
			/*Font chapterFont = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLDITALIC);
			Font paragraphFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);*/
			Paragraph paragraph = new Paragraph();
			Document doc = null;
			float left = 26;
	        float right = 26;
	        float top = 30;
	        float bottom = 150;
			if (bookData.get(0).size() > 5) {
				
				Rectangle envelope = new Rectangle(1200f, 850f);
				 doc = new Document(envelope, left, right,bottom,top);
				
			} else {
				Rectangle envelope1 = new Rectangle(1200f, 850f);
				doc = new Document(envelope1, left, right,bottom,top);
				//doc = new Document(PageSize.A4);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(doc, baos);
			HeaderFooterPageEvent event = new HeaderFooterPageEvent(sheetName, req);
			writer.setPageEvent(event);
			doc.open();
			/*doc.add(spaceTable(8));
	        doc.add(topSpaceTable(4));*/
	        float[] threeColumnWidth = { 1, 1};
	        PdfPTable Btable = new PdfPTable(2);
	        Btable.setWidthPercentage(90);
	        Btable.setWidths(threeColumnWidth);
	        Btable.setSpacingAfter(40);
	        Btable.addCell(leftBorderColumn("Subject: ",twq.subject ));
	        if (isSent) {
	        	Btable.addCell(rightBorderColumn("Recipient Name: ", splitandCheckTypes(twq.recipientName)));
			} else {
				Btable.addCell(rightBorderColumn("Sender Name: ", splitandCheckTypes(twq.senderName)));
			}
		    doc.add(Btable);
		    Btable.deleteLastRow();
		    Btable.addCell(leftBorderColumn("Status: ",twq.status ));
			Btable.addCell(rightBorderColumn("Type: ", twq.type));
			doc.add(Btable);
			Btable.deleteLastRow();
			if (isSent) {
				Btable.addCell(leftBorderColumn("Sent Date: ",twq.receivedDate ));
			}
			else{
				Btable.addCell(leftBorderColumn("Received Date: ",twq.receivedDate ));
			}
		    String priority = ""; 
		    if(twq.priority >=0 && twq.priority == 1){
		    	priority = "Low";
		    } else if(twq.priority >=0 && twq.priority == 2){
		    	priority = "Normal";
		    } else if(twq.priority >=0 && twq.priority == 3){
		    	priority = "High";
		    }
			Btable.addCell(rightBorderColumn("Priority: ", priority));
			doc.add(Btable);
			Btable.deleteLastRow();
		    if (!isSent) {
				Btable.addCell(leftBorderColumn("Deadline: ",twq.deadline ));
				Btable.addCell(rightBorderColumn("Instructions: ", twq.instructions));
				doc.add(Btable);
				Btable.deleteLastRow();
		    }
			/* PdfPTable Dtable = new PdfPTable(1);
		        Dtable.setHorizontalAlignment(Element.ALIGN_RIGHT);
		        Dtable.setWidthPercentage(30);
		        Font white = new Font();
		        white.setColor(BaseColor.WHITE);
		        PdfPCell cellD = new PdfPCell(new Phrase(" Date" , white));
		        cellD.setBackgroundColor(BaseColor.BLACK);
		        cellD.setBorderColor(BaseColor.GRAY);
		        cellD.setBorderWidth(2f);
		        Dtable.addCell(cellD);
		        PdfPCell cellTwo = new PdfPCell(new Phrase("10/01/2015"));
		        cellTwo.setBorderWidth(2f);
		        Dtable.addCell(cellTwo);
		        doc.add(Dtable);
		        Dtable.setTotalWidth(90);
		        PdfContentByte canvas = writer.getDirectContent();
		        Dtable.writeSelectedRows(0, -1, doc.right() - 90, doc.top(), canvas);
		        Paragraph paragraph2 = new Paragraph();

                paragraph2.setSpacingAfter(25);
                paragraph2.setSpacingBefore(25);
                paragraph2.setAlignment(Element.ALIGN_CENTER);
                paragraph2.setIndentationLeft(50);
                paragraph2.setIndentationRight(50);
                Chunk chunk =new Chunk("This is Table"); 
                paragraph2.add(chunk);
                doc.add(paragraph2);*/
			    PdfPTable table = new PdfPTable(bookData.get(0).size());
			      if (bookData.get(0).size() > 5) {
			   // table.setTotalWidth((PageSize.A4.rotate().getWidth() - (PageSize.A4.getWidth() / 4)));
			    	  float[] columnWidths = new float[]{20f, 60f, 30f, 60f,60f, 60f, 30f, 30f,30f};
			            //table.setWidths(columnWidths);
			            table.setWidthPercentage(100);
			} else
			      {
			       table.setTotalWidth((PageSize.A4.getWidth() - (PageSize.A4.getWidth() / 4)));
			      }
			int index = 0;
            for (ArrayList<Object> aBook : bookData) {
				for (int i = 0; i < aBook.size(); i++) {
					if (index == 0) {
						
						
						PdfPCell tableCell = insertCell(table, aBook.get(i).toString(), Element.ALIGN_LEFT,1, boldFont, true);
						table.addCell(tableCell);
					} else {
						String text = "";
						if (aBook.get(i) != null) {
							text = aBook.get(i).toString();
						}
						PdfPCell tableCell = insertCell(table, text, Element.ALIGN_LEFT, 1, normalFont, false);
						table.addCell(tableCell);
					}
				}
				index++;
			}
			String docTitle = sheetName + "-" + DBUtil.formatDateForFileName(new Date());
			paragraph.add(table);
			doc.add(paragraph);
			
			doc.close();
			resp.setContentType("application/pdf");
			resp.setHeader("Content-Disposition", "attachment; filename=" + docTitle + ".pdf");

			baos.writeTo(os);
			os.flush();
			os.close();
		} catch (Exception e) {
			throw e;
		}
	}
	
	private static PdfPCell leftBorderColumn(String boldContent, String normalContent) {

	        Paragraph resultField = new Paragraph(boldContent, new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD));
	        resultField.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.NORMAL));
	        resultField.add(normalContent);
	        PdfPCell cell = new PdfPCell(resultField);
	        cell.setBorder(Rectangle.NO_BORDER);
	        /*cell.setBorder(Rectangle.LEFT);*/
	        cell.setPaddingLeft(10);
	        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
	        cell.setVerticalAlignment(Element.ALIGN_CENTER);
	        return cell;
	    }

	private static PdfPCell rightBorderColumn(String boldContent, String normalContent) {

	        Paragraph resultField = new Paragraph(boldContent, new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD));
	        resultField.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.NORMAL));
	        resultField.add(normalContent);
	        PdfPCell cell = new PdfPCell(resultField);
	        cell.setBorder(Rectangle.NO_BORDER);
	        /*cell.setBorder(Rectangle.RIGHT);*/
	        cell.setPaddingLeft(10);
	        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	        cell.setVerticalAlignment(Element.ALIGN_CENTER);
	        return cell;
	    }

	private static PdfPCell noBorderColumn(String boldContent, String normalContent) {

	        Paragraph resultField = new Paragraph(boldContent, new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD));
	        resultField.setFont(new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.NORMAL));
	        resultField.add(normalContent);
	        PdfPCell cell = new PdfPCell(resultField);
	        cell.setBorder(Rectangle.NO_BORDER);
	        cell.setPaddingLeft(10);
	        cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
	        cell.setVerticalAlignment(Element.ALIGN_CENTER);
	        return cell;
	    }
	
	private static PdfPCell insertCell(PdfPTable table, String text, int align,int colspan, Font font, boolean isbackGround) throws Exception,Exception 
	{

		if (text == null || text.trim().equalsIgnoreCase("NULL")) {
			text = "";
		}

		PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
	     cell.setHorizontalAlignment(align);
		 cell.setVerticalAlignment(Element.ALIGN_BASELINE);
	     cell.setNoWrap(isbackGround);
		 cell.setColspan(colspan);
		 cell.setBorderColorLeft(BaseColor.RED);
		 cell.setBorderColorRight(BaseColor.RED);
		 cell.setBorderColorBottom(BaseColor.RED);
	     cell.setPadding(10f);
	     
		if (isbackGround) {
			cell.setBackgroundColor(new BaseColor(0, 81, 141));
		}

		if (text.trim().equalsIgnoreCase(""))
			cell.setMinimumHeight(10f);
		return cell;
	}
	
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
	
	 public static ByteArrayOutputStream convertWordToPdf(InputStream is){
		 
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 
		 try{
/*		  //create file inputstream object to read data from file 
		  //FileInputStream fs=new FileInputStream(src);
		  //create document object to wrap the file inputstream object
		  XWPFDocument doc=new XWPFDocument(is); 
		  //72 units=1 inch
		  Document pdfdoc=new Document(PageSize.A4,72,72,72,72);
		  //create a pdf writer object to write text to mypdf.pdf file
		  //PdfWriter pwriter=PdfWriter.getInstance(pdfdoc, new FileOutputStream(desc));
		  PdfWriter pwriter=PdfWriter.getInstance(pdfdoc, baos);
		  //specify the vertical space between the lines of text
		  pwriter.setInitialLeading(20);
		  //get all paragraphs from word docx
		  List plist=doc.getParagraphs();
		  
		  //open pdf document for writing
		  pdfdoc.open();
		  for (int i = 0; i < plist.size(); i++) {
		   //read through the list of paragraphs
		   XWPFParagraph pa = (XWPFParagraph) plist.get(i);
		   //get all run objects from each paragraph
		      List runs = pa.getRuns();
		      //read through the run objects
		      for (int j = 0; j < runs.size(); j++) {       
		       XWPFRun run=(XWPFRun) runs.get(j);
		       //get pictures from the run and add them to the pdf document
		       List piclist=run.getEmbeddedPictures();
		       //traverse through the list and write each image to a file
		       Iterator iterator=piclist.iterator();
		       while(iterator.hasNext()){
		          XWPFPicture pic=(XWPFPicture) iterator.next();
		          XWPFPictureData picdata=pic.getPictureData();
		          byte[] bytepic=picdata.getData(); 
		          Image imag=Image.getInstance(bytepic);
		         pdfdoc.add(imag);
		             
		       }
		       //get color code
		       int color=getCode(run.getColor());
		       //construct font object
		       Font f=null;
		       if(run.isBold() && run.isItalic())
		        f=FontFactory.getFont(FontFactory.TIMES_ROMAN,run.getFontSize(),Font.BOLDITALIC, new BaseColor(color));
		          else if(run.isBold())
		        f=FontFactory.getFont(FontFactory.TIMES_ROMAN,run.getFontSize(),Font.BOLD, new BaseColor(color));
		       else if(run.isItalic())
		        f=FontFactory.getFont(FontFactory.TIMES_ROMAN,run.getFontSize(),Font.ITALIC, new BaseColor(color));
		       else if(run.isStrike())
		        f=FontFactory.getFont(FontFactory.TIMES_ROMAN,run.getFontSize(),Font.STRIKETHRU, new BaseColor(color));
		       else
		        f=FontFactory.getFont(FontFactory.TIMES_ROMAN,run.getFontSize(),Font.NORMAL, new BaseColor(color));
		       //construct unicode string
		       String text=run.getText(-1);
		       byte[] bs;
		       if (text!=null){
		        bs=text.getBytes();
		        String str=new String(bs,"UTF-8");
		        //add string to the pdf document
		        Chunk chObj1=new Chunk(str,f);
		        pdfdoc.add(chObj1);
		       }      
		       
		      }
		      //output new line
		      pdfdoc.add(new Chunk(Chunk.NEWLINE));
		      }
		     //close pdf document  
		  	 pdfdoc.close();*/
	
		  }catch(Exception e){
			  e.printStackTrace();
		  }
		  return baos;
	 }
	
	 public static int getCode(String code){
		  int colorCode;
		  if(code!=null)
		   colorCode=Long.decode("0x"+code).intValue();
		  else
		   colorCode=Long.decode("0x000000").intValue();
		  return colorCode;
	}
}
