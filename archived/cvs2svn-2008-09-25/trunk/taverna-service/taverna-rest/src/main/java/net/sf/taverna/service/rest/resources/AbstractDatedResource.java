package net.sf.taverna.service.rest.resources;

import java.util.Calendar;
import java.util.Date;

import net.sf.taverna.service.datastore.bean.AbstractDated;
import net.sf.taverna.service.xml.Dated;

import org.apache.xmlbeans.XmlObject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

public abstract class AbstractDatedResource<DatedClass extends AbstractDated>
	extends AbstractResource {
	public AbstractDatedResource(Context context, Request request,
		Response response) {
		super(context, request, response);
	}

	private DatedClass resource;

	public void setResource(DatedClass resource) {
		this.resource = resource;
	}

	public DatedClass getResource() {
		return resource;
	}
	
	public Date getModificationDate() {
		if (getResource() == null) {
			return null;
		}
		return getResource().getLastModified();
	}
	
	public static Calendar dateToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
	
	public abstract class AbstractDatedREST<DatedType extends Dated> extends AbstractREST<DatedType> {
		
		@Override
		public XmlObject getXML() {
			if (getResource() == null) {
				throw new NullPointerException("Resource is null in " + this);
			}
			return super.getXML();
		}
		
		@Override
		public void addElements(DatedType element) {
			element.setCreated(dateToCalendar(getResource().getCreated()));
			element.setModified(dateToCalendar(getResource().getLastModified()));
		}
		
	}
	
}
