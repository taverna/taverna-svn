package net.sf.taverna.t2.workbench.reference.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.raven.appconfig.config.Log4JConfiguration;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * A set of utility methods related to basic data management.
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 *
 */
public class DataManagementHelper {
	
	private static final String DERBY_SHUTDOWN = "jdbc:derby:;shutdown=true";

	private final static Logger logger = Logger.getLogger(DataManagementHelper.class); 
			
	private static NetworkServerControl server;
	
	public static void setupDataSource() {
		
		DataManagementConfiguration config = DataManagementConfiguration.getInstance();
		setDerbyPaths();
		
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.osjava.sj.memory.MemoryContextFactory");
            System.setProperty("org.osjava.sj.jndi.shared", "true");

            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(config.getDriverClassName());
            
            System.setProperty("hibernate.dialect", config.getHibernateDialect());

            ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            ds.setMaxActive(config.getPoolMaxActive());
            ds.setMinIdle(config.getPoolMinIdle());
            ds.setMaxIdle(config.getPoolMaxIdle());
            ds.setDefaultAutoCommit(true);
            ds.setInitialSize(config.getPoolMinIdle());
            //Derby blows up if the username of password is empty (even an empty string thats not null).
            if (config.getUsername()!=null && config.getUsername().length()>=1) ds.setUsername(config.getUsername());
            if (config.getPassword()!=null && config.getPassword().length()>=1) ds.setPassword(config.getPassword());
            
            ds.setUrl(config.getJDBCUri());
            InitialContext context = new InitialContext();
            context.rebind(DataManagementConfiguration.JNDI_NAME, ds);
            
        } catch (NamingException ex) {
            logger.error("", ex);
        }
    }
	
	private static void setDerbyPaths() {
		if (DataManagementConfiguration.getInstance().getConnectorType().equals(DataManagementConfiguration.CONNECTOR_DERBY)) {
			String homeDir=ApplicationRuntime.getInstance().getApplicationHomeDir().getAbsolutePath();
			System.setProperty("derby.system.home",homeDir);			
			File logFile = new File(Log4JConfiguration.getInstance().getLogDir(), "derby.log");
			System.setProperty("derby.stream.error.file", logFile.getAbsolutePath());
		}
		
	}

	public static boolean isRunning() {
		if (server==null) {
			return false;			
		}
		else {
			try {
				server.ping();
				return true;
			} catch (Exception e) {
				return false;
			}	
		}
	}
	
	public static Connection openConnection() throws NamingException, SQLException {
		return ((DataSource)new InitialContext().lookup(DataManagementConfiguration.JNDI_NAME)).getConnection();
	}
	
	public synchronized static void startDerbyNetworkServer() {
		setDerbyPaths();
		
        System.setProperty("derby.drda.host","localhost");
        System.setProperty("derby.drda.minThreads","5");
        System.setProperty("derby.drda.maxThreads",String.valueOf(DataManagementConfiguration.getInstance().getPoolMaxActive()));        
        int port=DataManagementConfiguration.getInstance().getPort();
        int maxPort = port+50;
        
        try {
        	System.setProperty("derby.drda.portNumber",String.valueOf(port));
            if (server==null) server = new NetworkServerControl();
            while(port<maxPort) { //loop to find another available port on which Derby isn't already running
            	if (!isRunning()) break; 
            	logger.info("Derby connection port: "+port+" is currently not available for Taverna, trying next value");
            	port++;
            	System.setProperty("derby.drda.portNumber",String.valueOf(port));
            	server = new NetworkServerControl();
            }
            server.start(null);
            DataManagementConfiguration.getInstance().setCurrentPort(port);
        } catch (Exception ex) {
            logger.error("Error starting up Derby network server",ex);
        }
    }
	
	public static void stopDerbyNetworkServer() {
		try {
			server.shutdown();
		} catch (Exception e) {
			logger.error("Error shutting down Derby network server",e);
		}
	}
	
	public static ReferenceService checkDatabaseAvailability() {
		ReferenceService referenceServiceWithDatabase = null;
		try {
			String databasecontext = DataManagementConfiguration.HIBERNATE_CONTEXT;
		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
				databasecontext);
		referenceServiceWithDatabase = (ReferenceService) appContext
				.getBean("t2reference.service.referenceService");


		T2Reference testRef = referenceServiceWithDatabase.register("Hello", 0, true, null);
		referenceServiceWithDatabase.delete(testRef);
	}
	catch (ReferenceServiceException e) {
		logger.error("Unable to connect to database");
		throw new ReferenceServiceException("Unable to connect to database");
	}
		return referenceServiceWithDatabase;
	}

	public static synchronized final boolean checkDatabase() {
		ReferenceService databaseService = null;
		try {
			databaseService = checkDatabaseAvailability();
		}
		catch (Exception e) {
			databaseService = null;
			logger.error(e);
		}
		if (databaseService == null) {
			return false;
		}
		return true;
	}
	
	public static final void unlockDatabase() {
		try {
			// Should be OK even if not using derby as any exception will just be logged
			DriverManager.getConnection(DERBY_SHUTDOWN);
		} catch (SQLException e) {
			// Ignore
		}
		
	}

}
