package net.sf.taverna.service.datastore.bean;

import javax.persistence.Entity;

import org.hibernate.validator.NotNull;

@Entity
public class Configuration extends AbstractDated {

	@NotNull
	private boolean allowRegister = false;
	private boolean allowEmail = false;
	
	private String fromEmail;
	private String smtpServer;
	private boolean smtpAuthRequired;
	private String smtpUser;
	private String smtpPassword;
	private String baseuri;
	private String workerMemory="500";
	private String tavernaHome="/tmp/tavernaHome";
	
	public String getTavernaHome() {
		return tavernaHome;
	}
	public void setTavernaHome(String tavernaHome) {
		this.tavernaHome = tavernaHome;
	}
	public String getWorkerMemory() {
		return workerMemory;
	}
	public void setWorkerMemory(String workerMemory) {
		this.workerMemory = workerMemory;
	}
	public String getBaseuri() {
		return baseuri;
	}
	public void setBaseuri(String baseuri) {
		this.baseuri = baseuri;
	}
	public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public boolean isAllowEmailNotifications() {
		return allowEmail;
	}
	public void setAllowEmailNotifications(boolean allowEmailNotifications) {
		this.allowEmail = allowEmailNotifications;
	}
	public boolean isAllowRegister() {
		return allowRegister;
	}
	public void setAllowRegister(boolean allowRegister) {
		this.allowRegister = allowRegister;
	}
	public boolean isSmtpAuthRequired() {
		return smtpAuthRequired;
	}
	public void setSmtpAuthRequired(boolean smtpAuthRequired) {
		this.smtpAuthRequired = smtpAuthRequired;
	}
	public String getSmtpPassword() {
		return smtpPassword;
	}
	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
	public String getSmtpServer() {
		return smtpServer;
	}
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}
	public String getSmtpUser() {
		return smtpUser;
	}
	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}
}
