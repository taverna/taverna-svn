/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.baclava.store.BaclavaDataService;
import org.embl.ebi.escience.baclava.store.JDBCBaclavaDataService;
import org.embl.ebi.escience.baclava.store.BaclavaDataServiceFactory;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.ScuflIcons;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.2 $
 */
public class LoadWorkflowAction extends ScuflModelAction
{
    final JFileChooser fc = new JFileChooser();
	
	/**
	 * @param model
	 */
	public LoadWorkflowAction(ScuflModel model)
	{
		super(model);
		putValue(SMALL_ICON, ScuflIcons.openIcon);
		putValue(NAME, "Load");
		putValue(SHORT_DESCRIPTION, "Load a workflow...");
	}
	
	public void actionPerformed(ActionEvent e)
	{
		BaclavaDataService store=BaclavaDataServiceFactory.getStore();
		boolean jdbcStoreExists = (store !=null && store instanceof JDBCBaclavaDataService);
		String [] possibleValues;
		
		if (jdbcStoreExists)
		{ 
			possibleValues = new String [] { "From File", "From the Web", "From Database" };
		}		
		else
		{
			possibleValues = new String [] { "From File", "From the Web"};
		}
		
		int chosenOption=JOptionPane.showOptionDialog(null,"Which source do you wish to load from?","Workflow Source",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,possibleValues,possibleValues[0]);
		
		
		
		if (chosenOption==0) loadFromFile();
		else if (chosenOption==1) loadFromWeb();
		else if (chosenOption==2) loadFromDatabase((JDBCBaclavaDataService)store);
				
	}
	
	/*
	 * Select a workflow from the database and opens it
	 */
	protected void loadFromDatabase(JDBCBaclavaDataService store)
	{
		String LSID=(String)JOptionPane.showInputDialog(null,"Enter the LSID", "Workflow LSID",JOptionPane.QUESTION_MESSAGE,null,null,"");
		if (LSID!=null)
		{
			String xml=store.fetchWorkflow(LSID);
			if (xml==null)
			{
				JOptionPane.showMessageDialog(null,"Cannot find workflow for LSID "+LSID+" in the database.","Error!",JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				ByteArrayInputStream instr = new ByteArrayInputStream(xml.getBytes());
				try
				{
					XScuflParser.populate(instr,model,null);
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null, "Problem opening workflow from the database : \n"
							+ e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
				}			
			}
		}
	}
	 
	
	/*
	 * Asks for a url and loads a workflow from the xml based at that url
	 */
	protected void loadFromWeb()
	{
		try
		{
			String name = (String) JOptionPane.showInputDialog(null,
					"Enter the URL of a workflow definition to load", "Workflow URL",
					JOptionPane.QUESTION_MESSAGE, null, null, "http://");
			if (name != null)
			{
				XScuflParser.populate((new URL(name)).openStream(), model, null);
			}
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "Problem opening workflow from web : \n"
					+ ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/*
	 * Prompts for a file and loads a workflow from that file
	 */
	protected void loadFromFile()
	{
		Preferences prefs = Preferences.userNodeForPackage(ScuflIcons.class);
		String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		fc.setDialogTitle("Open Workflow");
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			final File file = fc.getSelectedFile();
			// mrp Refactored to do the heavy-lifting in a new thread
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						// todo: does the update need running in the AWT thread?
						// perhaps this thread should be spawned in populate?
						XScuflParser.populate(file.toURL().openStream(), model, null);
					}
					catch (Exception ex)
					{
						JOptionPane
								.showMessageDialog(
										null,
										"Problem opening workflow from file : \n\n"
												+ ex.getMessage()
												+ "\n\nTo load this workflow try setting offline mode, this will allow you to load and remove any defunct operations.",
										"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}).start();

		}
	}

	/*
	public void actionPerformed(ActionEvent e)
	{
		// Load an XScufl definition here
		

	}
	*/

}
