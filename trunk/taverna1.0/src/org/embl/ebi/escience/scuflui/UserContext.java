/*
 * Created on Jul 14, 2004
 */
package org.embl.ebi.escience.scuflui;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 */
public class UserContext
{
	private String personLSID;
	private String organizationLSID;
	private String experiementDesignLSID;

	public String getExperiementDesignLSID()
	{
		return experiementDesignLSID;
	}

	public void setExperiementDesignLSID(String experiementDesignLSID)
	{
		this.experiementDesignLSID = experiementDesignLSID;
	}

	public String getOrganizationLSID()
	{
		return organizationLSID;
	}

	public void setOrganizationLSID(String organizationLSID)
	{
		this.organizationLSID = organizationLSID;
	}

	public String getPersonLSID()
	{
		return personLSID;
	}

	public void setPersonLSID(String personLSID)
	{
		this.personLSID = personLSID;
	}
}