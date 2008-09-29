package net.sf.taverna.platform.spring.jdbc;

import java.io.File;
import java.io.IOException;

/**
 * Create JDBC connection strings for temporary use (ie. from tests)
 * <p>
 * {@link #getTemporaryDerbyJDBC()} creates a temporary directory that is used
 * to construct the JDBC connection string for a local Derby database.
 * </p>
 * <p>
 * This is most useful from a spring configuration, for example when using
 * {@link InterpolatingDriverManagerDataSource}:
 * </p>
 * 
 * <pre>
 * &lt;!-- Apache Derby rooted at a temporary directory --&gt;
 *  &lt;bean id=&quot;t2reference.jdbc.temporaryjdbc&quot;
 *  class=&quot;net.sf.taverna.platform.spring.jdbc.TemporaryJDBC&quot;&gt;
 *  &lt;/bean&gt;
 *  &lt;bean id=&quot;t2reference.jdbc.url&quot; class=&quot;java.lang.String&quot;
 *  factory-bean=&quot;t2reference.jdbc.temporaryjdbc&quot;
 *  factory-method=&quot;getTemporaryDerbyJDBC&quot; /&gt;
 *  &lt;bean id=&quot;t2reference.jdbc.datasource&quot;
 *  class=&quot;net.sf.taverna.platform.spring.jdbc.InterpolatingDriverManagerDataSource&quot;&gt;
 *  &lt;property name=&quot;driverClassName&quot;&gt;
 *  &lt;value&gt;org.apache.derby.jdbc.EmbeddedDriver&lt;/value&gt;
 *  &lt;/property&gt;
 *  &lt;property name=&quot;url&quot;&gt;
 *  &lt;ref bean=&quot;t2reference.jdbc.url&quot; /&gt;
 *  &lt;/property&gt;
 *  &lt;property name=&quot;repository&quot;&gt;
 *  &lt;ref bean=&quot;raven.repository&quot; /&gt;
 *  &lt;/property&gt;
 *  &lt;property name=&quot;driverArtifact&quot;&gt;
 *  &lt;value&gt;org.apache.derby:derby:10.4.1.3&lt;/value&gt;
 *  &lt;/property&gt;
 *  &lt;/bean&gt;
 * </pre>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TemporaryJDBC {
	public String getTemporaryDerbyJDBC() throws IOException {
		File tmpDir = File.createTempFile("t2platform-", ".db");
		tmpDir.delete();
		if (!tmpDir.mkdir()) {
			throw new IOException("Could not create temporary directory "
					+ tmpDir);
		}
		return "jdbc:derby:" + tmpDir.getPath() + "/database;create=true";
	}
}
