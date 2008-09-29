package net.sf.taverna.service.rest.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.service.xml.Data;
import net.sf.taverna.service.xml.Datas;

import org.apache.log4j.Logger;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.InputRepresentation;

public class DatasREST extends LinkedREST<Datas> implements Iterable<DataREST> {

	private static Logger logger = Logger.getLogger(DatasREST.class);
	
	public DatasREST(RESTContext context, Datas document) {
		super(context, document);
	}

	public DatasREST(RESTContext context, Reference uri) {
		super(context, uri, Datas.class);
	}

	public DatasREST(RESTContext context, Reference uri, Datas document) {
		super(context, uri, document);
	}

	public List<DataREST> getDatas() {
		List<DataREST> datas = new ArrayList<DataREST>();
		for (Data data : getDocument().getDataArray()) {
			datas.add(new DataREST(context, data));
		}
		return datas;
	}

	public Iterator<DataREST> iterator() {
		return getDatas().iterator();
	}

	public DataREST add(String baclava) throws NotSuccessException {
		Response response = context.post(getURIReference(), baclava, RESTContext.baclavaType);
		return handleAddResponse(response);
	}
	
	public DataREST add(InputStream stream) throws NotSuccessException {
		InputRepresentation dataStream = new InputRepresentation(stream, RESTContext.baclavaType);
		Response response = context.post(getURIReference(), dataStream);
		return handleAddResponse(response);
	}

	private DataREST handleAddResponse(Response response) {
		if (! response.getStatus().equals(Status.SUCCESS_CREATED)) {
			logger.warn("Did not create baclava document");
			return null;
		}
		if (response.getRedirectRef() == null) {
			logger.error("Did not get redirect reference for data document");
			return null;
		}
		invalidate();
		return new DataREST(context, response.getRedirectRef());
	}
	
	public DatasREST clone() {
		return new DatasREST(context, getURIReference());
	}

}
