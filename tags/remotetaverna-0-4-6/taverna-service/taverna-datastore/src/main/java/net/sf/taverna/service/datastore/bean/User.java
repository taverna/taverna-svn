package net.sf.taverna.service.datastore.bean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;

@Entity
@Table(name = "Users")
@NamedQueries(value = {
		@NamedQuery(name = User.NAMED_QUERY_ALL, query = "SELECT u FROM User u ORDER BY u.created DESC"),
		@NamedQuery(name = User.NAMED_QUERY_USER, query = "SELECT u FROM User u WHERE u.username=:username"), 
		@NamedQuery(name = User.NAMED_QUERY_ADMINS,query = "SELECT u FROM User u WHERE u.admin = true")})
public class User extends AbstractDated {

	private static final int SALT_SIZE = 16;

	private static final String UTF_8 = "UTF-8";

	private static final String HASH = "SHA-1";

	public static final String NAMED_QUERY_ALL = "allUsers";

	public static final String NAMED_QUERY_USER = "userByName";
	
	public static final String NAMED_QUERY_ADMINS = "userByAdmin";

	@NotNull
	@Column(unique = true, nullable = false)
	private String username;

	private String email;

	@NotNull
	private byte[] salt;

	@NotNull
	private byte[] passwordHash;
	
	private Date lastSeen = new Date();

	@OneToMany(mappedBy = "owner")
	@OrderBy("created")
	protected Collection<Job> jobs = new ArrayList<Job>();


	@OneToMany(mappedBy = "owner")
	@OrderBy("created")
	private Collection<Workflow> workflows = new ArrayList<Workflow>();

	@OneToMany(mappedBy = "owner")
	@OrderBy("created")
	private Collection<DataDoc> datas = new ArrayList<DataDoc>();

	/**
	 * Construct a user with a system generated (ie. UUID-based) username.
	 *
	 */
	public User() {
		super();
		username = getId();
	}

	/**
	 * Construct a user with the given username.
	 * 
	 * @param username
	 */
	public User(String username) {
		super();
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Set this user's password. The password will not be stored in clear-text,
	 * but hashed using the {@value #HASH} algorithm and a freshly generated salt,
	 * {@value #SALT_SIZE} bytes big.
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		salt = generateSalt(); // always fresh salt on the table
		passwordHash = hash(password);
		setLastModified();
	}

	/**
	 * Check if the given string is this users's password. The user's password
	 * must have been set with {@link #setPassword(String)} first.
	 * 
	 * @see #setPassword(String)
	 * @param password
	 * @return True if the password matches.
	 */
	public boolean checkPassword(String password) {
		byte[] suggestedHash = hash(password);
		return Arrays.equals(passwordHash, suggestedHash);
	}


	
	private boolean admin = false;


	/**
	 * Generate a random salt. Each user normally has a unique salt, that is
	 * updated every time he changes his password.
	 * 
	 * @return An array of {@value #SALT_SIZE} bytes containing random values
	 */
	private byte[] generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_SIZE];
		random.nextBytes(salt);
		return salt;
	}

	/**
	 * Generate a new, random password.
	 * 
	 * @return A semi-random string that can be used with {@link #setPassword(String)}
	 */
	public static String generatePassword() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Generate a hash based on this user's salt and the given password.
	 * Uses the hashing algorithm {@value #HASH}.
	 * 
	 * @param password
	 * @return A byte[] array of the hashed digest
	 */
	private byte[] hash(String password) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(HASH);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Can't find hash algorithm " + HASH, e);
		}
		if (salt != null) {
			digest.update(salt);
		}
		try {
			digest.update(password.getBytes(UTF_8));
		} catch (UnsupportedEncodingException e) {
			// Should never happen
			throw new RuntimeException("Can't encode password in " + UTF_8, e);
		}
		return digest.digest();
	}

	public Collection<Job> getJobs() {
		return jobs;
	}
	

	public Collection<Workflow> getWorkflows() {
		return workflows;
	}
	
	public Collection<DataDoc> getDatas() {
		return datas;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (!email.contains("@")) {
			// FIXME: Test stronger if email is valid
			throw new IllegalArgumentException("Invalid email address");
		}
		this.email = email;
	}

	
	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen() {
		setLastSeen(new Date());
	}
	
	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;		
	}

	public boolean isAdmin() {
		return admin;
	}


	
}
