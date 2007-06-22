package net.sf.taverna.service.servlet;

import net.sf.taverna.service.rest.RestApplication;

import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class TavernaServiceServletApplication extends RestApplication {
	
	private static Logger logger = Logger
			.getLogger(TavernaServiceServletApplication.class);
	
	@Override
	public Restlet createRoot() {
		return createComponent();
	}
	
	public TavernaServiceServletApplication(Context context) {
		super(context);
		logger.info("Initialising Taverna Service Servlet");
	}
	
	public class DaoCloseFilter extends Filter {
		public DaoCloseFilter(Context context, Restlet next) {
			super(context, next);
		}

		@Override
		protected void afterHandle(Request req, Response response) {
			super.afterHandle(req, response);
			try {
				daoFactory.rollback();
			} catch (IllegalStateException ex) { // Expected
			} finally {
				daoFactory.close();
			}
		}
	}

	@Override
	protected void attachFilters(Component component, Router router) {
		DaoCloseFilter daoCloser =
			new DaoCloseFilter(component.getContext(), router);
		component.getDefaultHost().attach("/v1", daoCloser);
	}

	@Override
	protected void attachHTMLSource(Component component) {
		//nothing needs to be done, this is handled by tomcat
	}
}
