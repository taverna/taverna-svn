package idaservicetype.idaservicetype.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import uk.ac.manchester.cs.elico.utilities.configuration.RapidAnalyticsPreferences;
import uk.ac.manchester.cs.elico.utilities.configuration.RapidMinerPluginConfiguration;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.AnnotationChainImpl;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.impl.EditManagerImpl;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;


import idaservicetype.idaservicetype.IDAActivity;
import idaservicetype.idaservicetype.IDAActivityConfigurationBean;
import idaservicetype.idaservicetype.ui.converter.IDAWorkflowConfiguration;
import idaservicetype.idaservicetype.ui.idawizard.wizard.IDAWizard;

@SuppressWarnings("serial")
public class IDAConfigureAction
		extends
		ActivityConfigurationAction<IDAActivity,
		IDAActivityConfigurationBean> {

	private RapidAnalyticsPreferences preferences;
	private UsernamePassword username_password;

	public IDAConfigureAction(IDAActivity activity, Frame owner) {
		super(activity);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		
		System.out.println("	CONFIGURATION ACTION " );
		final Dataflow owningDataflow = FileManager.getInstance().getCurrentDataflow();

		ActivityIconManager.getInstance().resetIcon(activity);

		/*
		ActivityConfigurationDialog<ExampleActivity, ExampleActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		ExampleConfigurationPanel panel = new ExampleConfigurationPanel(
				getActivity());
		ActivityConfigurationDialog<ExampleActivity,
        ExampleActivityConfigurationBean> dialog = new ActivityConfigurationDialog<ExampleActivity, ExampleActivityConfigurationBean>(
				getActivity(), panel);

		ActivityConfigurationAction.setDialog(getActivity(), dialog);
		*/
		
		//RapidMinerPluginConfiguration config = RapidMinerPluginConfiguration.getInstance();
        //String floraLocation = config.getProperty(RapidMinerPluginConfiguration.FL_LOCATION);
        //String floraTempLocation = config.getProperty(RapidMinerPluginConfiguration.FL_TEMPDIR);
        //String repos = config.getProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION);

       // RapidAnalyticsPreferences pref = new RapidAnalyticsPreferences();
        //pref.setRepositoryLocation(repos);
        
		RapidAnalyticsPreferences prefs = new RapidAnalyticsPreferences();
		prefs.setRepositoryLocation("http://rpc295.cs.man.ac.uk:8081");
        prefs.setUsername("rishi");
        prefs.setPassword("rishipwd");
        prefs.setPathToTmpDir("/Users/Rishi/Desktop/e-LICO_Development/IDA_stuff/tmp/");
        prefs.setPathToFlora("/Users/Rishi/Desktop/e-LICO_Development/IDA_stuff/flora2/");
        
        IDAWizard wizard;
		if (activity.getConfiguration().isTemplate()) {
			
	         String task = activity.getConfiguration().getSelectedTask();
			
			 wizard = new IDAWizard(new IDAWorkflowConfiguration(), prefs, new JFrame(), true);
			 wizard.setPredefinedTaskName(task);
	         wizard.showModalDialog();

		} else {
			
			 wizard = new IDAWizard(new IDAWorkflowConfiguration(), prefs, new JFrame());
	         wizard.showModalDialog();
		}
  
        Dataflow df = wizard.getFinalDataflow();
        IDAActivityConfigurationBean bean = new IDAActivityConfigurationBean();
        bean.setDataflow(df);
        
        ActivityConfigurationDialog.configureActivityStatic(owningDataflow, activity, bean);
        
	}
	
	public void sortPreferences() {
        
		preferences = getPreferences();
        if (preferences != null) {
            CredentialManager credManager;
            try {
                credManager = CredentialManager.getInstance();
                username_password = credManager.getUsernameAndPasswordForService(URI.create(preferences.getBrowserServiceLocation()), true, null);

                preferences.setUsername(username_password.getUsername());
                preferences.setPassword(username_password.getPasswordAsString());
            } catch (CMException e) {
                e.printStackTrace();

            }
        }
        else {
            JOptionPane.showMessageDialog(new JFrame(),
                    new JLabel("<html>Please set the Rapid Analytics repository location <br> " +
                            " and flora location in the preferences panel</html>"));
        }
		
	}
	
    private RapidAnalyticsPreferences getPreferences() {

        RapidMinerPluginConfiguration config = RapidMinerPluginConfiguration.getInstance();
        String repos = config.getProperty(RapidMinerPluginConfiguration.RA_REPOSITORY_LOCATION);
        System.err.println("Got repository location: " + repos);
        if (repos.equals("")) {
            return null;
        }

        RapidAnalyticsPreferences pref = new RapidAnalyticsPreferences();
        pref.setRepositoryLocation(repos);
        return pref;

    }

}
