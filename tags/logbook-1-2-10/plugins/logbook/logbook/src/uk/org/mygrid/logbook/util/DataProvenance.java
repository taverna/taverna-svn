package uk.org.mygrid.logbook.util;

import java.text.DateFormat;
import java.util.Date;

public class DataProvenance {

	private String dataLSID;

	private String workflowRun;

	private Date date;

	public DataProvenance(String dataLSID, String workflowRun, Date date) {
		super();
		this.dataLSID = dataLSID;
		this.workflowRun = workflowRun;
		this.date = date;
	}

	public String getDataLSID() {
		return dataLSID;
	}

	public Date getDate() {
		return date;
	}

	public String getWorkflowRun() {
		return workflowRun;
	}

	@Override
	public String toString() {
		return DateFormat.getDateTimeInstance().format(date);
	}

}
