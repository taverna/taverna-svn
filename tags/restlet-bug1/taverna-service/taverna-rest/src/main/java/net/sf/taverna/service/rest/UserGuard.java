package net.sf.taverna.service.rest;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;

import com.noelios.restlet.http.HttpConstants;
import com.noelios.restlet.http.HttpRequest;

/**
 * Ensure current user is authenticated. Note that this guard does not do
 * authorization.
 * 
 * @author Stian Soiland
 */
public class UserGuard extends Guard {

	private static Logger logger = Logger.getLogger(UserGuard.class);

	public static final String AUTHENTICATED_USER =
		"UserGuard.authenticatedUser";

	private static DAOFactory daoFactory = DAOFactory.getFactory();

	public final static ChallengeScheme SCHEME = ChallengeScheme.HTTP_BASIC;

	public static final String REALM = "Taverna service";

	public UserGuard(Context context) {
		super(context, SCHEME, REALM);
	}

	@Override
	public int authenticate(Request req) {
		getContext().getAttributes().remove(AUTHENTICATED_USER);
		if (req.getChallengeResponse() == null) {
			return 0;
		}
		if (!SCHEME.equals(req.getChallengeResponse().getScheme())) {
			logger.warn("Unknown authentication scheme "
				+ req.getChallengeResponse().getScheme());
			return 0;
		}
		String userName = req.getChallengeResponse().getIdentifier();
		User user = daoFactory.getUserDAO().readByUsername(userName);
		if (user == null) {
			logger.warn("Unknown user " + userName);
			return 0;
		}
		// FIXME: Should not keep passwords as Strings (use char[] that are
		// deleted afterwards, as returned from getSecret)
		String password = new String(req.getChallengeResponse().getSecret());

		HttpRequest httpReq = (HttpRequest) req;

		String authorization =
			httpReq.getHttpCall().getRequestHeaders().getValues(
				HttpConstants.HEADER_AUTHORIZATION);

		if (user.checkPassword(password)) {
			getContext().getAttributes().put(AUTHENTICATED_USER, user);
			logger.debug("Authenticated " + userName);
			return 1;
		}
		logger.warn("Wrong password supplied for " + userName);
		return 0;
	}
}