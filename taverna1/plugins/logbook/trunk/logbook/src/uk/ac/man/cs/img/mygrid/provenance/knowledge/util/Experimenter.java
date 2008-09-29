package uk.ac.man.cs.img.mygrid.provenance.knowledge.util;

import java.io.Serializable;

/**
 * This class contains information about a user. 
 * 
 * @author dturi
 * @version $Id: Experimenter.java,v 1.1 2007-12-14 12:49:14 stain Exp $
 */
public class Experimenter implements Serializable {

    private String id;
    
    private String username;

    private String cryptedPassword;

    private String firstName;

    private String middleName;

    private String familyName;

    private String organization;

    private String group;


    public Experimenter() {
    }

    /**
     * @param username
     * @param cryptedPassword
     * @param firstName
     * @param middleName
     * @param familyName
     * @param organization
     * @param group
     */
    public Experimenter(String username, String cryptedPassword,
            String firstName, String middleName, String familyName,
            String organization, String group) {
        super();
        this.username = username;
        this.cryptedPassword = cryptedPassword;
        this.firstName = firstName;
        this.middleName = middleName;
        this.familyName = familyName;
        this.organization = organization;
        this.group = group;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCryptedPassword() {
        return (cryptedPassword == null ? "" : cryptedPassword);
    }

    public void setCryptedPassword(String cryptedPassword) {
        this.cryptedPassword = cryptedPassword;
    }

    public String getGroup() {
        return (group == null ? "" : group);
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getOrganization() {
        return (organization == null ? "" : organization);
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getUsername() {
        return (username == null ? "" : username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFamilyName() {
        return (familyName == null ? "" : familyName);
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFirstName() {
        return (firstName == null ? "" : firstName);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return (middleName == null ? "" : middleName);
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String toString() {
        return "[" + getUsername() + ", " + getFirstName() + ", "
                + getMiddleName() + ", " + getFamilyName() + ", "
                + getOrganization() + ", " + getGroup() + "]";
    }

}
