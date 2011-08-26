/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Send an email from a workflow
 * @author Tom Oinn
 */
public class SendEmail implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"to","from","subject","body","smtpserver"};
    }
    public String[] inputTypes() {
	return new String[]{LocalWorker.STRING,
			    LocalWorker.STRING,
			    LocalWorker.STRING,
			    LocalWorker.STRING,
			    LocalWorker.STRING};
    }
    public String[] outputNames() {
	return new String[0];
    }
    public String[] outputTypes() {
	return new String[0];
    }
    public Map execute(Map inputs) throws TaskExecutionException {
	if (inputs.containsKey("to")==false || inputs.containsKey("from")==false) {
	    throw new TaskExecutionException("Inputs to this task must contain the 'to' and 'from' email addresses.");
	}
	String to = (String)((DataThing)inputs.get("to")).getDataObject();
	String from = (String)((DataThing)inputs.get("from")).getDataObject();
	
	String subject = "No subject";
	if (inputs.containsKey("subject")) {
	    subject = (String)((DataThing)inputs.get("subject")).getDataObject();
	}
	
	if (inputs.containsKey("body")==false) {
	    throw new TaskExecutionException("No body specified for message");
	}
	String body = (String)((DataThing)inputs.get("body")).getDataObject();
	
	Properties mailProps = System.getProperties();
	if (inputs.containsKey("smtpserver")) {
	    mailProps.put("mail.smtp.host",(String)((DataThing)inputs.get("smtpserver")).getDataObject());
	}
	try {
	    Session session = Session.getDefaultInstance(mailProps, null);
	    MimeMessage message = new MimeMessage(session);
	    message.setFrom(new InternetAddress(from));
	    message.addRecipient(Message.RecipientType.TO,
				 new InternetAddress(to));
	    message.setSubject(subject);
	    message.setText(body);
	    Transport.send(message);
	}
	catch (Exception ex) {
	    TaskExecutionException tee = new TaskExecutionException("Failed to send email! "+ex.getMessage());
	    tee.initCause(ex);
	    throw tee;
	}
	return new HashMap();
    }
}
