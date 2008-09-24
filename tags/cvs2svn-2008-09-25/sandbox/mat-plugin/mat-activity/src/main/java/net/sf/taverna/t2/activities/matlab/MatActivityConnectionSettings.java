package net.sf.taverna.t2.activities.matlab;

import java.io.Serializable;

/**
 *
 * @author petarj
 */
public class MatActivityConnectionSettings implements Serializable {

    private static final long serialVersionUID = -6854465427193353083L;
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8194;
    public static final String DEFAULT_USERNAME = "";
    public static final String DEFAULT_PASSWORD = "";
    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;

    public MatActivityConnectionSettings() {
    }

    public MatActivityConnectionSettings(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MatActivityConnectionSettings other = (MatActivityConnectionSettings) obj;
        if (this.host != other.host && (this.host == null || !this.host.equals(other.host))) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (this.username != other.username && (this.username == null || !this.username.equals(other.username))) {
            return false;
        }
        if (this.password != other.password && (this.password == null || !this.password.equals(other.password))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 73 * hash + this.port;
        hash = 73 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 73 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }
}
