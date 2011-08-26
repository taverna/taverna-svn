package net.sf.taverna.platform.spring.jdbc;

import static net.sf.taverna.platform.spring.PropertyInterpolator.interpolate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.ClassUtils;

/**
 * Subclass of the DriverManagerDataSource so we can use interpolated properties
 * in e.g. database URLs from the spring configuration. Interpolation is applied
 * to all set-able string properties.
 * <p>
 * In addition this class can be used to register JDBC drivers loaded from Raven
 * artifacts. If the optional 'repository' and 'driverArtifact' properties are
 * injected (of type Repository and String respectively) the specified driver
 * will be loaded from the artifact, including any download or other activity
 * needed to resolve it properly. The driver will be loaded in a temporary
 * classloader and a proxy to it loaded in the platform classloader, making it
 * visible to systems which can't see inside the artifact class loaders such as
 * Hibernate. If these properties are undefined the data source reverts to
 * default behaviour for a DriverManagerDataSource
 * 
 * @author Tom Oinn
 * 
 */
public class InterpolatingDriverManagerDataSource extends
		DriverManagerDataSource {

	private Log log = LogFactory
			.getLog(InterpolatingDriverManagerDataSource.class);

	@Override
	public void setUrl(String newUrl) {
		super.setUrl(interpolate(newUrl));
	}

	@Override
	public void setUsername(String newUsername) {
		super.setUsername(interpolate(newUsername));
	}

	@Override
	public void setPassword(String newPassword) {
		super.setPassword(interpolate(newPassword));
	}

	private String myDriverClassName = null;

	@Override
	public void setDriverClassName(String newDriverClassName) {
		this.myDriverClassName = newDriverClassName.trim();
	}

	/**
	 * Prevent automatic registration of class on load, defer until asked for so
	 * we know whether we have an artifact specifier or not
	 */
	@Override
	public String getDriverClassName() {
		return this.myDriverClassName;
	}

	private Repository repository = null;
	private Artifact driverArtifact = null;

	/**
	 * Inject a raven repository object to fetch the driver artifact from if
	 * specified
	 * 
	 * @param rep
	 */
	public void setRepository(Repository rep) {
		this.repository = rep;
	}

	/**
	 * Inject a driver artifact specification, used if not null and as long as
	 * there is an appropriate repository also injected.
	 * 
	 * @param a
	 */
	public void setDriverArtifact(String a) {
		String[] parts = a.split(":");
		driverArtifact = new BasicArtifact(parts[0], parts[1], parts[2]);
	}

	private boolean initialized = false;

	@Override
	protected synchronized Connection getConnectionFromDriverManager(
			String url, Properties props) throws SQLException {
		if (!initialized) {
			if (repository == null || driverArtifact == null) {
				// Perform regular style initialization through loading the
				// class by name
				try {
					Class.forName(getDriverClassName(), true, ClassUtils
							.getDefaultClassLoader());
				} catch (ClassNotFoundException cnfe) {
					log.error(
							"Can't fine the regular (non raven) JDBC driver class "
									+ getDriverClassName(), cnfe);
					IllegalStateException ise = new IllegalStateException(
							"Could not load JDBC driver class ["
									+ getDriverClassName() + "]");
					ise.initCause(cnfe);
					throw ise;
				}
				logger.info("Loaded non raven JDBC driver: "
						+ getDriverClassName());
			} else {
				// Loading through raven using the funky proxied drivers
				ProxyDriverManager.registerDriver(repository, driverArtifact,
						getDriverClassName());
			}
			initialized = true;
		}
		return DriverManager.getConnection(url, props);
	}
}
