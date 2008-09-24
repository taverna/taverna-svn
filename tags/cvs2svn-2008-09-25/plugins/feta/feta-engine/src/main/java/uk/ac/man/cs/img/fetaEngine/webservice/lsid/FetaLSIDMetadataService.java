package uk.ac.man.cs.img.fetaEngine.webservice.lsid;

import java.io.ByteArrayInputStream;

import uk.ac.man.cs.img.fetaEngine.store.IFetaModel;

import com.ibm.lsid.LSID;
import com.ibm.lsid.MetadataResponse;
import com.ibm.lsid.server.LSIDMetadataService;
import com.ibm.lsid.server.LSIDRequestContext;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDServiceConfig;

public class FetaLSIDMetadataService implements LSIDMetadataService {

	public MetadataResponse getMetadata(LSIDRequestContext ctx, String[] formats)
			throws LSIDServerException {

		LSID lsid = ctx.getLsid();
		String lsidStr = lsid.toString();

		try {
			// do a query over the store for this LSID

			IFetaModel fetta = (IFetaModel) uk.ac.man.cs.img.fetaEngine.store.impl.sesame2.SesameModelImpl
					.getInstance();
			String resultRDF = fetta.retrieveByLSID(lsidStr);

			if (resultRDF == null)
				throw new LSIDServerException(
						LSIDServerException.NO_METADATA_AVAILABLE,
						"No metadata in msdl for " + lsid);

			byte[] bytes = resultRDF.getBytes();

			// check the requested formats
			// feta only supports RDF/XML

			if (formats != null) {
				boolean found = false;
				for (int i = 0; i < formats.length; i++) {
					if (formats[i].equals("application/rdf+xml"))
						found = true;
					break;
				}
				if (!found)
					throw new LSIDServerException(
							LSIDServerException.NO_METADATA_AVAILABLE_FOR_FORMATS,
							"No metadata found for given format");
			}

			return new MetadataResponse(new ByteArrayInputStream(bytes), null,
					"application/rdf+xml");
		} catch (LSIDServerException e) {
			throw e;
		} catch (Exception exp) {
			throw new LSIDServerException(
					LSIDServerException.NO_METADATA_AVAILABLE,
					"No metadata in msdl for " + lsid);

		}
	}

	/**
	 * @see com.ibm.lsid.server.LSIDService#initService(LSIDServiceConfig)
	 */
	public void initService(LSIDServiceConfig config)
			throws LSIDServerException {

		// do initialization stuff if needed.
	}

}