package net.sf.taverna.t2.persist.hibernate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.peer.LocationalContextImpl;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Stores blobs in a mysql database. See {@link PersistentDataManager}.
 * 
 * @author Ian Dunlop
 * 
 */
public class PersistentBlobStore implements BlobStore {

	private static final String DATABASE_NAME = "blobStore";
	private Set<LocationalContext> locationalContexts;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PersistentBlobStore.class);
	private final String namespace;
	private String password;
	private String user;
	private String databaseURL;
	private boolean databaseCreated = false;
	private String driver;

	/**
	 * Create blob store and assign database parameters later
	 * 
	 * @param namespace
	 * @throws Exception
	 */
	public PersistentBlobStore(String namespace) {
		this.namespace = namespace;
		try {
			initContext();
		} catch (IOException e) {
			// might need better exception handling, no point getting a bust
			// blob store
			logger.warn("BlobStore failed to initialise properly");
		}
	}

	/**
	 * Loads the settings from property file called db_settings.txt. This
	 * contains the database connection settings.
	 * 
	 * @throws IOException
	 */
	private void loadSettings() throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(
				"META-INF/db_settings.txt");
		Properties props = new Properties();
		try {
			props.load(stream);
		} catch (IOException e) {
			throw new IOException("could not load properties " + e);
		}
		password = props.getProperty("password");
		user = props.getProperty("user");
		databaseURL = props.getProperty("databaseURL");
		driver = props.getProperty("driver");
	}

	private void initContext() throws IOException {
		String uuid = UUID.randomUUID().toString();
		Map<String, String> contextMap = new HashMap<String, String>();
		contextMap.put(LOCATIONAL_CONTEXT_KEY_UUID, uuid);
		LocationalContext locationalContext = new LocationalContextImpl(
				LOCATIONAL_CONTEXT_TYPE, contextMap);
		locationalContexts = Collections.singleton(locationalContext);

		loadSettings();
	}

	private Connection getDBConnection() throws Exception {
		Connection con = null;
		if (!databaseCreated) {
			try {
				Class.forName(driver);
			} catch (Exception e) {
				throw new Exception(
						"Could not load database driver, please check classpath etc driver:",
						e);
			}
			try {
				con = DriverManager.getConnection(databaseURL, user, password);
			} catch (Exception e) {
				throw new Exception(
						"Could not connect to database, please check URL etc",
						e);
			}

			try {
				Statement statement2 = con.createStatement();

				String insertStatement = "create table things (ID VARCHAR(60), CHARSET VARCHAR(20), DATA BLOB, BLOBSIZE BIGINT)";
				try {
					statement2.execute(insertStatement);
				} catch (Exception e) {
					// table probably exists (hopefully!) so just carry on.
					// mySQL can use IF NOT EXISTS in SQL create but I don't
					// think derby can
				}
			} catch (Exception e) {
				throw new Exception(
						"Could not create table for blobs in database", e);
			}
			databaseCreated = true;
			con.close();
		}
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(databaseURL, user, password);

		} catch (Exception e) {
			throw new Exception("Could not create database connection", e);
		}
		return con;
	}

	/**
	 * Where is this blob store valid?
	 */
	public Set<LocationalContext> getLocationalContexts() {
		return locationalContexts;
	}

	/**
	 * Check if a blob reference is within the database
	 */
	public boolean hasBlob(BlobReferenceScheme<?> reference)
			throws RetrievalException {
		try {
			Connection connection = getDBConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM things WHERE ID=?");
			preparedStatement.setString(1, reference.getId().toString());
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				// if there are any results then the blob exists
				return true;
			}
		} catch (Exception e) {
			throw new RetrievalException("Could not retrieve " + reference);
		}
		return false;
	}

	/**
	 * Get the blob in the reference back as a byte array
	 */
	public byte[] retrieveAsBytes(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException {
		InputStream stream = retrieveAsStream(reference);
		try {
			return IOUtils.toByteArray(retrieveAsStream(reference));
		} catch (IOException e) {
			throw new RetrievalException("Failed to retrieve blob as stream", e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	/**
	 * Get the blob in the reference back as a input stream
	 */
	public InputStream retrieveAsStream(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException {
		Connection connection = null;
		try {
			connection = getDBConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM things WHERE ID=?");
			preparedStatement.setString(1, reference.getId().toString());

			ResultSet rs = null;
			try {
				rs = preparedStatement.executeQuery();
			} catch (Exception e) {
				throw new RetrievalException("Could not retrieve " + reference,
						e);
			}
			if (rs == null) {
				throw new NotFoundException(reference
						+ " could not be found in the database");
			}
			while (rs.next()) {
				try {
					InputStream blob = rs.getBinaryStream("DATA");

					return blob;
				} catch (Exception e) {
					throw new RetrievalException("Could not retrieve "
							+ reference, e);
				}
			}
		} catch (Exception e) {
			throw new NotFoundException(reference);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.warn(e);
			}
		}
		return null;
	}

	/**
	 * Retreive the blob as a string without any character encoding info
	 */
	public String retrieveAsString(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException,
			IllegalArgumentException {
		String charset = null;
		try {
			charset = reference.getCharset();
		} catch (DereferenceException e) {
			// TODO Auto-generated catch block
			logger.warn(e);
			// is this really the best exception for this?
			throw new RetrievalException("Could not retrieve reference "
					+ reference, e);
		}
		if (charset == null) {
			throw new IllegalArgumentException(reference
					+ " did not have character set.");
		}
		return retrieveAsString(reference, charset);
	}

	/**
	 * Get the blob in the reference back as a string with the supplied
	 * character encoding
	 */
	public String retrieveAsString(BlobReferenceScheme<?> reference,
			String charset) throws RetrievalException, NotFoundException,
			IllegalArgumentException {
		try {
			return IOUtils.toString(retrieveAsStream(reference), charset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn(e);
		}
		return null;
	}

	/**
	 * How big is the blob?
	 */
	public long sizeOfBlob(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException {
		try {
			Connection connection = getDBConnection();
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT length(DATA) from things WHERE ID=?");
			preparedStatement.setString(1, reference.getId().toString());
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				return rs.getInt(1);
				// return rs.getInt("BLOBSIZE");
			}
		} catch (Exception e) {

		}
		return 0;
	}

	/**
	 * Get the blob in the reference back as a byte array with the appropriate
	 * character encoding
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes, String charset)
			throws StorageException {
		String id = UUID.randomUUID().toString();
		BlobReferenceSchemeImpl blobReferenceSchemeImpl = new BlobReferenceSchemeImpl(
				namespace, id, charset);
		int length = bytes.length;
		try {
			Connection connection = getDBConnection();
			PreparedStatement ps = connection
					.prepareStatement("INSERT INTO things (ID, CHARSET, DATA, BLOBSIZE) VALUES (?,?,?,?)");
			ps.setString(1, id);
			ps.setString(2, charset);
			// is this safe enough?
			ps.setObject(3, bytes);
			ps.setInt(4, length);
			ps.executeUpdate();
			ps.close();
			connection.close();
		} catch (Exception e) {
			throw new StorageException("Could not store "
					+ blobReferenceSchemeImpl, e);
		}
		return blobReferenceSchemeImpl;
	}

	/**
	 * Store the blob in the byte array without character encoding
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes)
			throws StorageException {
		return storeFromBytes(bytes, null);
	}

	/**
	 * Store the blob from a stream without character encoding
	 */
	public BlobReferenceScheme<?> storeFromStream(InputStream stream)
			throws StorageException {
		return storeFromStream(stream, null);
	}

	/**
	 * Store the blob in the stream with the supplied character encoding
	 */
	public BlobReferenceScheme<?> storeFromStream(InputStream stream,
			String charset) throws StorageException {
		String id = UUID.randomUUID().toString();
		BlobReferenceSchemeImpl blobReferenceSchemeImpl = new BlobReferenceSchemeImpl(
				namespace, id, charset);
		try {
			Connection connection = getDBConnection();
			PreparedStatement ps = connection
					.prepareStatement("INSERT INTO things (ID, CHARSET, DATA, BLOBSIZE) VALUES (?,?,?,?)");
			ps.setString(1, id);
			ps.setString(2, charset);
			InputStream inputStream = new BufferedInputStream(stream);
			// is this safe enough (will it even work)?
			int available = inputStream.available();
			ps.setBinaryStream(3, inputStream, available);
			ps.setInt(4, available);
			ps.executeUpdate();
			ps.close();
			connection.close();
		} catch (Exception e) {
			throw new StorageException("Could not store "
					+ blobReferenceSchemeImpl, e);
		}
		return blobReferenceSchemeImpl;

	}

	/**
	 * Store the blob in the string without character encoding
	 */
	public BlobReferenceScheme<?> storeFromString(String string)
			throws StorageException {
		InputStream stream;
		try {
			stream = IOUtils.toInputStream(string, STRING_CHARSET);
		} catch (IOException e) {
			throw new StorageException("Failed to store from string", e);
		}
		return storeFromStream(stream, STRING_CHARSET);
	}

}
