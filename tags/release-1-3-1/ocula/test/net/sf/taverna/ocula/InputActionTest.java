package net.sf.taverna.ocula;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.PlainDocument;

import junit.framework.TestCase;
import net.sf.taverna.ocula.action.ProcessInput;
import net.sf.taverna.ocula.ui.OculaPanel;

public class InputActionTest extends TestCase	{
    private JFrame frame;
    private Ocula o;
    
    private void setUpFrame() throws Exception {
	frame = new JFrame();
	o = new Ocula(frame);
	frame.getContentPane().add(o);
	frame.setSize(new Dimension(400,300));
	URL exampleURL = Thread.currentThread().getContextClassLoader().
	    getResource("net/sf/taverna/ocula/action/InputAction.xml");
	o.load(exampleURL);
	frame.setVisible(true);
    }
    
    public void testProcessFieldsAction() throws Exception {
	setUpFrame();
	EventQueue.invokeAndWait(new Runnable() {
	    public void run() {
		getButton("Submit").doClick();
	    }
	});
	String correctName = "myusername";
	String correctEmail = "email@provider.co.uk";
	ProcessInput input = null;
	synchronized(o) {
	    input = (ProcessInput) o.getContext("inputHandler");
	}
	String actualName = input.getUserName();
	String actualEmail = input.getEmail();
	String errorMessage = "Actual userName is: '" + actualName + "'. Correct " +
	"userName is: '" + correctName + "'.";
	assertTrue(errorMessage, actualName.equals(correctName));
	errorMessage = "Actual email is: '" + actualEmail + "'. Correct " +
	"email is: '" + correctEmail + "'.";
	assertTrue(errorMessage, correctEmail.equals(actualEmail));
    }
    
    public void testClearFieldsAction() throws Exception {
	setUpFrame();
	EventQueue.invokeAndWait(new Runnable() {
	    public void run() {
		getButton("Clear").doClick();
	    }
	});
	String emptyString = "";
	String actualName = null;
	String actualEmail = null;
	synchronized (o) {
	    PlainDocument doc = (PlainDocument) o.getContext("userNameField");
	    actualName = doc.getText(0, doc.getLength());
	    doc = (PlainDocument) o.getContext("emailField");
	    actualEmail = doc.getText(0, doc.getLength());
	}
	String errorMessage = "Actual userName is: ' " + actualName + "'. Correct " +
			"userName is an empty String.";
	assertTrue(errorMessage, actualName.equals(emptyString));
	errorMessage = "Actual email is: '" + actualEmail + "'. Correct " +
	"email is an empty String.";
	assertTrue(errorMessage, emptyString.equals(actualEmail));
    }
    
    private JButton getButton(String name) {
	OculaPanel panel = (OculaPanel) o.mainPanel.getComponent(0);
	JPanel main = panel.getContents();
	JButton button = null;
	for (int i = 0; i < main.getComponentCount(); ++i) {
	    Component comp = main.getComponent(i);
	    if (comp instanceof JButton) {
		button = (JButton) comp;
	    	if (button.getText().equals(name)) {
	    	    return button;
	    	}
	    }
	}
	fail("No Button named " + name + " was found.");
	return null;
    }
}
