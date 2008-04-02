/*
 * WebDAVPublishManager.java
 *
 * Created on February 24, 2005, 5:58 PM
 */

package uk.ac.man.cs.img.fetaClient.publisher;

import java.io.File;

import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavResource;

/**
 * 
 * @author alperp
 */
public class WebDAVPublishManager {

	private String webDAVLocation;

	private String userName;

	private String password;

	/** Creates a new instance of WebDAVPublishManager */
	public WebDAVPublishManager(String WebDAVLocation, String usr, String pwd) {

		this.webDAVLocation = WebDAVLocation;

		this.userName = usr;
		this.password = pwd;

	}

	public String put(File fileToPut) {
		String errMessage = "";
		try {
			HttpURL hrl = new HttpURL(webDAVLocation);
			hrl.setUserinfo(userName, password);
			WebdavResource wdr = new WebdavResource(hrl);
			String path = wdr.getPath() + "/" + fileToPut.getName();
			boolean succeeded = wdr.putMethod(path, fileToPut);

			wdr.close();
			if (succeeded) {
				return null;
			} else {
				errMessage = errMessage + " WebDAV PUT command failed. ";
			}

		} catch (Exception e) {
			errMessage = errMessage + e.getMessage();
		}
		return errMessage;

	}

	// Burada taverna feta gui'nin instance'ini bulup onunla communicate etmem
	// lazim
	// webdav location ya da UDDI location onun engine'inin ilgi alanina giriyor
	// mu? bulmak lazim??

	public String mkcol(String directoryName) {
		String errMessage = "";
		try {
			HttpURL hrl = new HttpURL(webDAVLocation);
			hrl.setUserinfo(userName, password);

			WebdavResource wdr = new WebdavResource(hrl);

			boolean succeeded = wdr.mkcolMethod(directoryName);
			wdr.close();
			if (succeeded) {
				return null;
			} else {
				errMessage = errMessage
						+ " WebDAV MKCOL command failed. Error messsage is: ";
			}
		} catch (Exception e) {
			errMessage = errMessage + e.getMessage();
		}
		return errMessage;

	}

}
