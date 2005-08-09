package net.sf.taverna.ocula.action;

import java.util.Map;

import net.sf.taverna.ocula.Ocula;

public class ProcessInput implements Processor {
    String userName;
    String email;
    
    public void process(Map map, Ocula ocula) throws Exception {
	userName = (String) map.get("userNameField");
	email = (String) map.get("emailField");
    }

    public synchronized String getEmail() {
        return email;
    }

    public synchronized String getUserName() {
        return userName;
    }

}
