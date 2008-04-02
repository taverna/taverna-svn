package uk.org.mygrid.logbook.ui.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import uk.org.mygrid.logbook.ui.LogBookUIRemoteModel;

public abstract class WorkflowNodeMemoryImpl implements WorkflowNode {

	private String lsid;

	private String title;

//	private String customTitle;

	private String author;

	private String description;

	private Date date;

	private String displayDate;

//	private static Logger logger = Logger
//			.getLogger(WorkflowNodeMemoryImpl.class);

	protected WorkflowNodeMemoryImpl() {
		// default
	}

	public WorkflowNodeMemoryImpl(String lsid, String title, String author,
			String description, String date) throws ParseException {
		this.lsid = lsid;
		this.title = title;
		this.author = author;
		this.description = description;
		this.date = LogBookUIRemoteModel.parseDate(date.substring(1, date.lastIndexOf("\"")));
	}

	public String getAuthor() {
		return author;
	}

	public String getCustomTitle() {
		// FIXME
		return getTitle();
	}

	public Date getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayDate() {
		if (displayDate == null) 
			displayDate = getDisplayDate(date);
		return displayDate;
	}

	public String getLsid() {
		return lsid;
	}
	
	public void setLsid(String lsid) {
		this.lsid = lsid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getRunID()
	 */
	public String getLsidSuffix() {
		if (lsid == null)
			return "";
		String[] split = lsid.split(":");
		if (split[3].equals("experimentinstance")
				|| split[3].equals("wfInstance"))
			return split[4];
		return "";
	}

	public String getTitle() {
		return title;
	}

	public static String getDisplayDate(Date aDate) {
//		Calendar c = Calendar.getInstance();
//		c.setTime(aDate);
//		String time = (new Time(aDate.getTime())).toString();
//		time = time.substring(0, 5);
//		return "<html><font color=#999999>" + c.get(Calendar.DAY_OF_MONTH)
//						+ "/" + (c.get(Calendar.MONTH) + 1) + "/"
//						+ c.get(Calendar.YEAR) + "</font>    " + time;
		return "<html><font color=#999999>" + DateFormat.getDateTimeInstance().format(aDate) + "</font>";
	}

	public int compareTo(WorkflowNode other) {
		if (this == other)
			return 0;
		// FIXME: not compatible with equals!
		if (this.date == null || other.getDate() == null)
			return 0;
		else
			return this.date.compareTo(other.getDate()) * -1;
	}

	@Override
	public String toString() {
		return lsid;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
