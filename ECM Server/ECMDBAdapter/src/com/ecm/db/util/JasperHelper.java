package com.ecm.db.util;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import net.sf.jasperreports.engine.JasperCompileManager;
//import net.sf.jasperreports.engine.JasperFillManager;
//import net.sf.jasperreports.engine.JasperPrint;
//import net.sf.jasperreports.engine.JasperReport;
//import net.sf.jasperreports.engine.JasperRunManager;
//import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
//import net.sf.jasperreports.engine.design.JasperDesign;
//import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
//import net.sf.jasperreports.engine.xml.JRXmlLoader;
//import net.sf.jasperreports.export.SimpleExporterInput;
//import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
//import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;













import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import com.ecm.db.list.ECMUserList;
import com.ecm.db.model.ECMInboxItems;
import com.ecm.db.model.ECMOrgReport;
import com.ecm.db.model.ECMRole;
import com.ecm.db.model.ECMSentitems;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.transport.TWorkitemQuery;

public class JasperHelper {
	public static void generateSentitemJasperReport(HttpServletResponse resp,
			HttpServletRequest req, String reportTitle,
			ArrayList<ECMSentitems> bookData, TWorkitemQuery twq, boolean isSent) {
		String mainReportPath;
		String subReportPath;
		OutputStream outStream;
		JasperReport mainJasperReport;
		JasperReport subJasperReport;
		JasperDesign mainReportJasperDesign;
		JasperDesign subReportJasperDesign;
		JRBeanCollectionDataSource reportSource;
		JasperPrint printFile;
		try {
			String username = ECMUserList.getInstance().getLoginName(RequestHelper.getLoggedInEmployee(req, ""));
			if(twq.exportFilter) {
				mainReportPath = req.getServletContext().getRealPath("reports")
						+ "\\ECMSentMainReportFilter.jrxml";
			} else {
				mainReportPath = req.getServletContext().getRealPath("reports")
						+ "\\ECMSentMainReport.jrxml";
			}
			subReportPath = req.getServletContext().getRealPath("reports")
					+ "\\ECMSentSubReport.jrxml";
			
			mainReportJasperDesign = JRXmlLoader.load(mainReportPath);
			subReportJasperDesign = JRXmlLoader.load(subReportPath);
			
			mainJasperReport = JasperCompileManager
					.compileReport(mainReportJasperDesign);
			subJasperReport = JasperCompileManager
					.compileReport(subReportJasperDesign);

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", reportTitle);
			parameters.put("SUBREPORT_DIR", req.getServletContext().getRealPath("reports"));
			parameters.put("imgPath", req.getServletContext().getRealPath("reports"));
			parameters.put("trackSentitem", subJasperReport);
			parameters.put("generatedBy", username);
			parameters.put("reportCount", bookData.size());
			if(twq.exportFilter){
				parameters.put("FSubject", twq.subject);
				parameters.put("FStatus", twq.status);
				parameters.put("FType", twq.type);
				parameters.put("FPriority", getPriorityValue(twq.priority));
				parameters.put("FSender", splitandCheckTypes(twq.recipientName));
				parameters.put("FDueDate", twq.deadline);
				parameters.put("FReceivedDate", twq.receivedDate);
				parameters.put("FRemarks", twq.comments);
				parameters.put("FInstruction", twq.instructions);
				parameters.put("FKeywords", twq.keywords);
				parameters.put("FAction", twq.actions);
			}
			reportSource = new JRBeanCollectionDataSource(bookData);
		    if(twq.exportFormat.equalsIgnoreCase("xls")){
		    	printFile = JasperFillManager.fillReport(mainJasperReport,
							parameters, reportSource);
		    	outStream = resp.getOutputStream();
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setExporterInput(new SimpleExporterInput(printFile));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				//configuration.setOnePagePerSheet(false);
				configuration.setDetectCellType(true);
				//configuration.setCollapseRowSpan(false);
				//configuration.setIgnoreGraphics(false);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
				resp.setHeader("Content-Disposition", "attachment; filename=SentItemReport.xlsx");
				resp.setContentType("application/vnd.ms-excel; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
		    } else {
		    	byte[] byteStream;
				byteStream = JasperRunManager.runReportToPdf(mainJasperReport,
						parameters, reportSource);
				resp.setHeader("Content-Disposition", "attachment; filename=SentItemReport.pdf");
				resp.setContentType("application/pdf; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				outStream = resp.getOutputStream();
				outStream.write(byteStream, 0, byteStream.length);
		    }
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void generateInboxJasperReport(HttpServletResponse resp,
			HttpServletRequest req, String reportTitle,
			ArrayList<ECMInboxItems> bookData, TWorkitemQuery twq) {
		String mainReportPath;
		OutputStream outStream;
		JasperReport mainJasperReport;
		JasperDesign mainReportJasperDesign;
		JRBeanCollectionDataSource reportSource;
		JasperPrint printFile = null;
		Map<String, Object> parameters = null;
		try {
			String username = ECMUserList.getInstance().getLoginName(RequestHelper.getLoggedInEmployee(req, ""));
			if(twq.exportFilter) {
				mainReportPath = req.getServletContext().getRealPath("reports")
						+ "\\ECMInboxFilter.jrxml";
			} else {
				mainReportPath = req.getServletContext().getRealPath("reports")
						+ "\\ECMInboxType.jrxml";
			}
			mainReportJasperDesign = JRXmlLoader.load(mainReportPath);
			mainJasperReport = JasperCompileManager
					.compileReport(mainReportJasperDesign);
			parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", reportTitle);
			parameters.put("imgPath", req.getServletContext().getRealPath("reports"));
			parameters.put("generatedBy", username);
			parameters.put("reportCount", bookData.size());
			if(twq.exportFilter){
				parameters.put("FSubject", twq.subject);
				parameters.put("FStatus", twq.status);
				parameters.put("FType", twq.type);
				parameters.put("FPriority", getPriorityValue(twq.priority));
				parameters.put("FSender", splitandCheckTypes(twq.senderName));
				parameters.put("FDueDate", twq.deadline);
				parameters.put("FReceivedDate", twq.receivedDate);
				parameters.put("FRemarks", twq.comments);
				parameters.put("FInstruction", twq.instructions);
				parameters.put("FKeywords", twq.keywords);
				parameters.put("FAction", twq.actions);
			}
			reportSource = new JRBeanCollectionDataSource(bookData);
		    if(twq.exportFormat.equalsIgnoreCase("xls")){
		    	printFile = JasperFillManager.fillReport(mainJasperReport,parameters, reportSource);
		    	outStream = resp.getOutputStream();		
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setExporterInput(new SimpleExporterInput(printFile));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				//configuration.setOnePagePerSheet(false);
				configuration.setDetectCellType(true);
				//configuration.setCollapseRowSpan(false);
				//configuration.setIgnoreGraphics(false);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
		        resp.setHeader("Content-Disposition", "attachment; filename=" + "InboxReport" + ".xlsx");
		        resp.setContentType("application/vnd.ms-excel; charset=UTF-8");
		        resp.setCharacterEncoding("UTF-8");      
		    } else {
		    	byte[] byteStream;
				byteStream = JasperRunManager.runReportToPdf(mainJasperReport,
						parameters, reportSource);
				resp.setHeader("Content-Disposition", "attachment; filename=InboxReport.pdf");
				resp.setContentType("application/pdf; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				outStream = resp.getOutputStream();
				outStream.write(byteStream, 0, byteStream.length);
		    }
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static String getPriorityValue(long priority) {
		String strValue = "";
		int iPriority = (int) priority;
		if (iPriority == 3)
			strValue = "High";
		else if (iPriority == 2)
			strValue = "Normal";
		else if (iPriority == 1)
			strValue = "Low";
		return strValue;
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
	
	
	public static void generateOrgJasperReportCount(HttpServletResponse resp,
			HttpServletRequest req, String reportTitle,
			ArrayList<ECMOrgReport> bookData, TReportFilter trf) {
		String mainReportPath;
		OutputStream outStream;
		JasperReport mainJasperReport;
		JasperDesign mainReportJasperDesign;
		JRBeanCollectionDataSource reportSource;
		JasperPrint printFile = null;
		Map<String, Object> parameters = null;
		try {
			if(reportTitle.equalsIgnoreCase("eSign Documents")){
				mainReportPath = req.getServletContext().getRealPath("reports")
						+ "\\ECMOrgESignReportCount.jrxml";
			} else {
				mainReportPath = req.getServletContext().getRealPath("reports")
						+ "\\ECMOrgReportCount.jrxml";	
			}
			mainReportJasperDesign = JRXmlLoader.load(mainReportPath);
			mainJasperReport = JasperCompileManager
					.compileReport(mainReportJasperDesign);
			parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", reportTitle);
			parameters.put("imgPath", req.getServletContext().getRealPath("reports"));
			parameters.put("generatedBy", trf.userName);
			parameters.put("reportCount", bookData.size());
			parameters.put("FOrgCode", trf.orgCode);
			parameters.put("FStatus", trf.status);
			parameters.put("FFromDate", trf.fromDate);
			parameters.put("FToDate", trf.toDate);
			
			reportSource = new JRBeanCollectionDataSource(bookData);
		    if(trf.exportType.equalsIgnoreCase("excel")){
		    	printFile = JasperFillManager.fillReport(mainJasperReport,parameters, reportSource);
		    	outStream = resp.getOutputStream();		
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setExporterInput(new SimpleExporterInput(printFile));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				configuration.setDetectCellType(true);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
		        resp.setHeader("Content-Disposition", "attachment; filename=" + "ECMOrgESignReport" + ".xlsx");
		        resp.setContentType("application/vnd.ms-excel; charset=UTF-8");
		        resp.setCharacterEncoding("UTF-8");      
		    } else {
		    	byte[] byteStream;
				byteStream = JasperRunManager.runReportToPdf(mainJasperReport,
						parameters, reportSource);
				resp.setHeader("Content-Disposition", "attachment; filename=ECMOrgESignReport.pdf");
				resp.setContentType("application/pdf; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				outStream = resp.getOutputStream();
				outStream.write(byteStream, 0, byteStream.length);
		    }
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void generateOrgInboxJasperReport(HttpServletResponse resp,
			HttpServletRequest req, String reportTitle,
			ArrayList<ECMInboxItems> bookData, TWorkitemQuery twq) {
		String mainReportPath;
		OutputStream outStream;
		JasperReport mainJasperReport;
		JasperDesign mainReportJasperDesign;
		JRBeanCollectionDataSource reportSource;
		JasperPrint printFile = null;
		Map<String, Object> parameters = null;
		try {
			String username = ECMUserList.getInstance().getLoginName(RequestHelper.getLoggedInEmployee(req, ""));
			mainReportPath = req.getServletContext().getRealPath("reports")
							+ "\\ECMOrgReportInboxFilter.jrxml";
			mainReportJasperDesign = JRXmlLoader.load(mainReportPath);
			mainJasperReport = JasperCompileManager
					.compileReport(mainReportJasperDesign);
			parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", reportTitle);
			parameters.put("imgPath", req.getServletContext().getRealPath("reports"));
			parameters.put("generatedBy", username);
			parameters.put("reportCount", bookData.size());
			parameters.put("FOrgCode", twq.orgCode);
			parameters.put("FStatus", twq.repStatus);
			parameters.put("FFromDate", twq.fromDate);
			parameters.put("FToDate", twq.toDate);
			reportSource = new JRBeanCollectionDataSource(bookData);
		    if(twq.exportFormat.equalsIgnoreCase("xls")){
		    	printFile = JasperFillManager.fillReport(mainJasperReport,parameters, reportSource);
		    	outStream = resp.getOutputStream();		
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setExporterInput(new SimpleExporterInput(printFile));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				configuration.setDetectCellType(true);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
		        resp.setHeader("Content-Disposition", "attachment; filename=" + "InboxReport" + ".xlsx");
		        resp.setContentType("application/vnd.ms-excel; charset=UTF-8");
		        resp.setCharacterEncoding("UTF-8");      
		    } else {
		    	byte[] byteStream;
				byteStream = JasperRunManager.runReportToPdf(mainJasperReport,
						parameters, reportSource);
				resp.setHeader("Content-Disposition", "attachment; filename=InboxReport.pdf");
				resp.setContentType("application/pdf; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				outStream = resp.getOutputStream();
				outStream.write(byteStream, 0, byteStream.length);
		    }
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void generateOrgSentJasperReport(HttpServletResponse resp,
			HttpServletRequest req, String reportTitle,
			ArrayList<ECMSentitems> bookData, TWorkitemQuery twq, boolean isSent) {
		String mainReportPath;
		String subReportPath;
		OutputStream outStream;
		JasperReport mainJasperReport;
		JasperReport subJasperReport;
		JasperDesign mainReportJasperDesign;
		JasperDesign subReportJasperDesign;
		JRBeanCollectionDataSource reportSource;
		JasperPrint printFile;
		try {
			String username = ECMUserList.getInstance().getLoginName(RequestHelper.getLoggedInEmployee(req, ""));
			mainReportPath = req.getServletContext().getRealPath("reports")
							+ "\\ECMOrgReportSentMainReportFilter.jrxml";
			subReportPath = req.getServletContext().getRealPath("reports")
							+ "\\ECMSentSubReport.jrxml";
			
			mainReportJasperDesign = JRXmlLoader.load(mainReportPath);
			subReportJasperDesign = JRXmlLoader.load(subReportPath);
			
			mainJasperReport = JasperCompileManager
					.compileReport(mainReportJasperDesign);
			subJasperReport = JasperCompileManager
					.compileReport(subReportJasperDesign);

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", reportTitle);
			parameters.put("SUBREPORT_DIR", req.getServletContext().getRealPath("reports"));
			parameters.put("imgPath", req.getServletContext().getRealPath("reports"));
			parameters.put("trackSentitem", subJasperReport);
			parameters.put("generatedBy", username);
			parameters.put("reportCount", bookData.size());
			parameters.put("FOrgCode", twq.orgCode);
			parameters.put("FStatus", twq.repStatus);
			parameters.put("FFromDate", twq.fromDate);
			parameters.put("FToDate", twq.toDate);
			reportSource = new JRBeanCollectionDataSource(bookData);
		    if(twq.exportFormat.equalsIgnoreCase("xls")){
		    	printFile = JasperFillManager.fillReport(mainJasperReport,
							parameters, reportSource);
		    	outStream = resp.getOutputStream();
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setExporterInput(new SimpleExporterInput(printFile));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				configuration.setDetectCellType(true);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
				resp.setHeader("Content-Disposition", "attachment; filename=SentItemReport.xlsx");
				resp.setContentType("application/vnd.ms-excel; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
		    } else {
		    	byte[] byteStream;
				byteStream = JasperRunManager.runReportToPdf(mainJasperReport,
						parameters, reportSource);
				resp.setHeader("Content-Disposition", "attachment; filename=SentItemReport.pdf");
				resp.setContentType("application/pdf; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				outStream = resp.getOutputStream();
				outStream.write(byteStream, 0, byteStream.length);
		    }
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void generateOrgAllJasperReportCount(HttpServletResponse resp,
			HttpServletRequest req, String reportTitle,
			ArrayList<ECMOrgReport> bookData, TReportFilter trf) {
		String mainReportPath;
		OutputStream outStream;
		JasperReport mainJasperReport;
		JasperDesign mainReportJasperDesign;
		JRBeanCollectionDataSource reportSource;
		JasperPrint printFile = null;
		Map<String, Object> parameters = null;
		try {
			mainReportPath = req.getServletContext().getRealPath("reports")
						+ "\\ECMAllReportCount.jrxml";	
			mainReportJasperDesign = JRXmlLoader.load(mainReportPath);
			mainJasperReport = JasperCompileManager
					.compileReport(mainReportJasperDesign);
			parameters = new HashMap<String, Object>();
			parameters.put("reportTitle", reportTitle);
			parameters.put("imgPath", req.getServletContext().getRealPath("reports"));
			parameters.put("generatedBy", trf.userName);
			parameters.put("reportCount", bookData.size());
			parameters.put("FOrgCode", trf.orgCode);
			parameters.put("FStatus", trf.status);
			parameters.put("FFromDate", trf.fromDate);
			parameters.put("FToDate", trf.toDate);
			
			reportSource = new JRBeanCollectionDataSource(bookData);
		    if(trf.exportType.equalsIgnoreCase("excel")){
		    	printFile = JasperFillManager.fillReport(mainJasperReport,parameters, reportSource);
		    	outStream = resp.getOutputStream();		
				JRXlsxExporter exporter = new JRXlsxExporter();
				exporter.setExporterInput(new SimpleExporterInput(printFile));
				exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
				SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
				configuration.setDetectCellType(true);
				exporter.setConfiguration(configuration);
				exporter.exportReport();
		        resp.setHeader("Content-Disposition", "attachment; filename=" + "ECMOrgESignReport" + ".xlsx");
		        resp.setContentType("application/vnd.ms-excel; charset=UTF-8");
		        resp.setCharacterEncoding("UTF-8");      
		    } else {
		    	byte[] byteStream;
				byteStream = JasperRunManager.runReportToPdf(mainJasperReport,
						parameters, reportSource);
				resp.setHeader("Content-Disposition", "attachment; filename=ECMOrgESignReport.pdf");
				resp.setContentType("application/pdf; charset=UTF-8");
				resp.setCharacterEncoding("UTF-8");
				outStream = resp.getOutputStream();
				outStream.write(byteStream, 0, byteStream.length);
		    }
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
