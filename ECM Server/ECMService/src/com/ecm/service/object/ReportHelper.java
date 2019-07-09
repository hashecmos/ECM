package com.ecm.service.object;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ecm.db.list.ECMAdministrationList;
import com.ecm.db.list.ECMDocSignList;
import com.ecm.db.list.ECMWorkitemList;
import com.ecm.db.transport.TMgmtReport;
import com.ecm.db.transport.TReportFilter;
import com.ecm.db.util.ECMLogger;
import com.ecm.db.util.ExcelHelper;
import com.ecm.filenet.list.FNDocumentSearch;
import com.ecm.filenet.list.FNObjectStoreList;
import com.ecm.filenet.model.FNDocument;
import com.ecm.filenet.model.FNObjectStore;
import com.ecm.filenet.transport.TFNDocument;
import com.ecm.filenet.transport.TFNProperty;

public class ReportHelper {

	private static ECMLogger logger = ECMLogger.getInstance(IntegrationHelper.class);

	public static ReportHelper getInstance() {
		return new ReportHelper();
	}

	private ReportHelper() {
	}

	public void exportDocumentsToExcel(HttpServletRequest req,
			HttpServletResponse resp, ArrayList<TFNDocument> docList,
			String reportName) throws Exception {
		try {
			ExcelHelper.createExcelFileForDocuments(req, resp, reportName, getExcelInputs(docList));
		} catch (Exception e) {
			logger.logException(e);
			throw e;
		}
	}

	public ArrayList<TMgmtReport> getOrgAllReportCount(TReportFilter trf)
			throws Exception {
		try {

			ArrayList<TMgmtReport> tmrList = new ArrayList<TMgmtReport>();

			TMgmtReport tmrReport = new TMgmtReport();
			tmrReport.category = "Received Workflows";
			tmrReport.details = ECMWorkitemList.getInstance()
					.getOrgWorkitemCount(trf.orgCode, trf.userType,
							trf.fromDate, trf.toDate, trf.EmpNo);
			tmrList.add(tmrReport);

			tmrReport = new TMgmtReport();
			tmrReport.category = "Sent Workflows";
			tmrReport.details = ECMWorkitemList.getInstance()
					.getOrgSentitemCount(trf.orgCode, trf.userType,
							trf.fromDate, trf.toDate, trf.EmpNo);
			tmrList.add(tmrReport);

			tmrReport = new TMgmtReport();
			tmrReport.category = "Created Documents";
			tmrReport.details = ECMAdministrationList.getInstance().getAllSubOrgUnits(trf.orgCode, trf.EmpNo);
			FNObjectStore os = FNObjectStoreList.getInstance().getApplicationObjectStore("ECM", "ECMOS");
			tmrReport.details = FNDocumentSearch.getInstance(os).getOrgDocumentCounts(tmrReport.details, trf);
			tmrList.add(tmrReport);

			tmrReport = new TMgmtReport();
			tmrReport.category = "eSign Documents";
			tmrReport.details = ECMDocSignList.getInstance().getOrgESignCount(
					trf.orgCode, trf.userType, trf.fromDate, trf.toDate,
					trf.EmpNo);
			tmrList.add(tmrReport);
			
			return tmrList;
		} catch (Exception e) {
			logger.logException(e);
			throw e;
		}
	}

	private ArrayList<Object> getHeaderRow() {
		ArrayList<Object> row = new ArrayList<Object>();
		row.add("Sl.No");
		row.add("Document Title");
		row.add("ECM No");
		row.add("Document Class");
		row.add("Added On");
		row.add("Created By");
		row.add("Modified On");
		row.add("Modified By");
		// row.add("Content Size");
		// row.add("Version ID");
		row.add("Format");
		// row.add("Document Date");
		return row;
	}

	private ArrayList<ArrayList<Object>> getExcelInputs(
			ArrayList<TFNDocument> searchRes) {
		ArrayList<ArrayList<Object>> xlInput = new ArrayList<ArrayList<Object>>();
		if (searchRes == null)
			return xlInput;
		xlInput.add(getHeaderRow());

		int rowCount = 0;
		for (TFNDocument doc : searchRes) {
			ArrayList<Object> row = new ArrayList<Object>();
			row.add(rowCount + 1);
			row.add(doc.fileName);
			row.add(getPropertyValue(doc, "ECMNo"));

			if (doc.docclass == null || doc.docclass.equalsIgnoreCase(""))
				doc.docclass = getDocumentClassName(doc.id);

			row.add(doc.docclass);
			row.add(doc.addOn);
			row.add(doc.creator);
			row.add(doc.modOn);
			row.add(doc.modifier);
			// row.add(doc.size);
			// row.add(doc.vsid);
			row.add(doc.format.replaceAll("application/", "").replaceAll(
					"image/", ""));

			// row.add(getProperty(doc, "DocumentDate"));
			xlInput.add(row);
			rowCount++;
		}
		return xlInput;
	}

	private String getPropertyValue(TFNDocument doc, String propName) {
		try {
			for (TFNProperty prop : doc.props) {
				if ((prop == null) || (prop.name == null))
					continue;
				if (prop.name.trim().equalsIgnoreCase(propName)) {
					return prop.mvalues.get(0);
				}
			}
		} catch (Exception e) {
			logger.logException(e);
		}
		return "";
	}

	private String getDocumentClassName(String docid) {
		String result = "";
		try {
			FNObjectStore os;
			os = FNObjectStoreList.getInstance().getApplicationObjectStore(
					"ECM", "ECMOS");

			FNDocument fnd = FNDocument.getInstance(os);
			fnd.setId(docid);
			fnd.loadCurrentVersionOnly();
			return fnd.getDocClass();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
