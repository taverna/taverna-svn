/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.shared;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

/**
 * Contains utility methods to deal with opening windows and suchlike in a way
 * that makes no assumptions about the existance of a JDesktop pane.
 * @author Tom Oinn
 */
public class UIUtils {	
	
	public static FrameCreator DEFAULT_FRAME_CREATOR = new FrameCreator() {
		public void createFrame(ScuflModel targetModel,
				UIComponentSPI targetComponent, int posX, int posY,
				int sizeX, int sizeY) {
			final UIComponentSPI component = targetComponent;
			final ScuflModel model = targetModel;
			JFrame newFrame = new JFrame(component.getName());
			newFrame.getContentPane().setLayout(new BorderLayout());
			newFrame.getContentPane().add(
					new JScrollPane((JComponent) targetComponent),
					BorderLayout.CENTER);
			newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			newFrame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					component.onDispose();
				}
			});

			newFrame.setSize(sizeX, sizeY);
			newFrame.setLocation(posX, posY);
			newFrame.setVisible(true);
			// Set the icon AFTER making it visible to avoid TAV-395
			if (component.getIcon() != null) {
				newFrame.setIconImage(component.getIcon().getImage());
			}
			if (component instanceof WorkflowModelViewSPI) {
				((WorkflowModelViewSPI)component).attachToModel(model);
			}
		}
	};

	/**
	 * Implement this interface and set the DEFAULT_FRAME_CREATOR field to
	 * change the behaviour of the windowing system used by the Taverna
	 * Workbench
	 */
	public interface FrameCreator {
		public void createFrame(ScuflModel targetModel,
				UIComponentSPI targetComponent, int posX, int posY,
				int sizeX, int sizeY);
	}

	/**
	 * Create a top level window using the configured default frame creator. For
	 * platforms such as Mac OS X where the expected windowing behaviour is
	 * different from the default desktop pane the default frame creator can be
	 * overridden to produce whatever top level window is required - the
	 * Workbench class contains code to do this in the case of both OS X and
	 * other window systems where the desktop pane is not required.
	 * <p>
	 * This method will handle the appropriate logic to bind to and unbind from
	 * a model when given an implementation of the ScuflUIComponent interface.
	 * It's worth noting that, in addition to implementing this interface, any
	 * object passed in as the target component must also be a subclass of
	 * JComponent!
	 */
	public static void createFrame(ScuflModel targetModel,
			UIComponentSPI targetComponent, int posX, int posY, int sizeX,
			int sizeY) {
		DEFAULT_FRAME_CREATOR.createFrame(targetModel, targetComponent, posX,
				posY, sizeX, sizeY);
	}

	/**
	 * As for the method above but allows a non-ScuflUIComponent JComponent.
	 * Internally this component is wrapped up in a trivial ScuflUIComponent
	 * which entirely ignores the workflow model settings.
	 */
	public static void createFrame(JComponent rawComponent, int posX, int posY,
			int width, int height) {
		UIComponentSPI p = new WrapperFrame(rawComponent);
		createFrame(null, p, posX, posY, width, height);
	}

	/**
	 * Trivial implementation of ScuflUIComponent to wrap a JComponent, ignores
	 * all model handling methods.
	 */
	@SuppressWarnings("serial")
	static class WrapperFrame extends JPanel implements UIComponentSPI {
		public WrapperFrame(JComponent component) {
			super(new BorderLayout());
			add(component, BorderLayout.CENTER);
		}

		public ImageIcon getIcon() {
			return null;
		}

		public String getName() {
			return "";
		}

		public void onDisplay() {
			//
		}

		public void onDispose() {
			//
		}
	}
	
	/**
	 * Determines, if possible, the parent window of an ActionEvent. Usually determined via the JPopupMenu invoker.
	 * This is useful for making JOptionPane dialogues modal when diplayed from menu item.
	 * 
	 * @param ae
	 * @return Component, or null if it cannot be determined.
	 */
	public static Component getActionEventParentWindow(ActionEvent ae) {
		Component parent = null;
		if (ae.getSource() instanceof Component) {
			Component source=(Component)ae.getSource();
			if (source.getParent() instanceof JPopupMenu) {
				parent = ((JPopupMenu)source.getParent()).getInvoker();
			}
		}
		return parent;		
	}	
	
	/**
	 * Launches the address in a browser. Currently very crude. On Linux will open in firefox
	 * and in Windows in IExplorer using an example taken from (http://www.javaworld.com/javaworld/javatips/jw-javatip66.html).
	 * <br>Ultimately this should be done using BasicService of javaws.jnlp
	 * e.g. BasicService.showDocument(address), but unable to do this at the moment due to licensing concerns with
	 * the sun jnlp jar (we are not able to host it in a Maven repository).
	 * 
	 * @param address to launch
	 */
	public static void launchBrowser(String address) {
		
		BrowserControl.displayURL(address);
	}
	
	/**
	 * A temporary solution until licensing concerns around distributing jnlp with Raven are resolved.
	 * @author http://www.javaworld.com/javaworld/javatips/jw-javatip66.html
	 *
	 */
	private static class BrowserControl
	{
	    /**
	     * Display a file in the system browser.  If you want to display a
	     * file, you must include the absolute path name.
	     *
	     * @param url the file's url (the url must start with either "http://"
	or
	     * "file://").
	     */
	    public static void displayURL(String url)
	    {
	        boolean windows = isWindowsPlatform();
	        String cmd = null;
	        try
	        {
	            if (windows)
	            {
	                // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
	                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
	                Runtime.getRuntime().exec(cmd);
	            }
	            else
	            {
	                // Under Unix, Netscape has to be running for the "-remote"
	                // command to work.  So, we try sending the command and
	                // check for an exit value.  If the exit command is 0,
	                // it worked, otherwise we need to start the browser.
	                // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
	                cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
	                Process p = Runtime.getRuntime().exec(cmd);
	                try
	                {
	                    // wait for exit code -- if it's 0, command worked,
	                    // otherwise we need to start the browser up.
	                    int exitCode = p.waitFor();
	                    if (exitCode != 0)
	                    {
	                        // Command failed, start up the browser
	                        // cmd = 'netscape http://www.javaworld.com'
	                        cmd = UNIX_PATH + " "  + url;
	                        p = Runtime.getRuntime().exec(cmd);
	                    }
	                }
	                catch(InterruptedException x)
	                {
	                    System.err.println("Error bringing up browser, cmd='" +
	                                       cmd + "'");
	                    System.err.println("Caught: " + x);
	                }
	            }
	        }
	        catch(IOException x)
	        {
	            // couldn't exec browser
	            System.err.println("Could not invoke browser, command=" + cmd);
	            System.err.println("Caught: " + x);
	        }
	    }
	    /**
	     * Try to determine whether this application is running under Windows
	     * or some other platform by examing the "os.name" property.
	     *
	     * @return true if this application is running under a Windows OS
	     */
	    public static boolean isWindowsPlatform()
	    {
	        String os = System.getProperty("os.name");
	        if ( os != null && os.startsWith(WIN_ID))
	            return true;
	        else
	            return false;
	    }
	    /**
	     * Simple example.
	     */
	    public static void main(String[] args)
	    {
	        displayURL("http://www.javaworld.com");
	    }
	    // Used to identify the windows platform.
	    private static final String WIN_ID = "Windows";
	    // The default system browser under windows.
	    private static final String WIN_PATH = "rundll32";
	    // The flag to display a url.
	    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
	    // The default browser under unix.
	    private static final String UNIX_PATH = "firefox";
	    // The flag to display a url.
	    private static final String UNIX_FLAG = "-remote openURL";
	}

}
