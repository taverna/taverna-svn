/**
 * 
 */
package net.sf.taverna.t2.semantic.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.semantic.profile.AnnotationProfile;
import net.sf.taverna.t2.semantic.profile.annotationbean.AnnotationProfileAssertion;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;

/**
 * @author alanrw
 *
 */
public class ProfileConfigureMenuAction extends AbstractContextualMenuAction {

	public static final URI configureSection = URI
    .create("http://taverna.sf.net/2009/contextMenu/configure");
	private static final String ANNOTATION_PROFILE = "Profile...";
	
	private static final String URL_REGEX = "http(s)?:\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?";
	
	private static AnnotationTools annotationTools = new AnnotationTools();
	
	public ProfileConfigureMenuAction() {
		super(configureSection, 50);
	}
	
	@Override
	public boolean isEnabled() {
		return (getContextualSelection() != null) && (getContextualSelection().getSelection() instanceof Dataflow);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return new AbstractAction(ANNOTATION_PROFILE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Dataflow d = (Dataflow) getContextualSelection().getSelection();
				AnnotationProfile currentProfile = AnnotationProfile.getAnnotationProfile(d);
				UrlPanel urlPanel = new UrlPanel();
				urlPanel.setUrl(currentProfile.getUrl().toString());
				ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
						"Add an http URL", urlPanel);
				vuid.addTextComponentValidation(
								urlPanel.getUrlField(), "Set the URL.",
								null, "",URL_REGEX,	"Not a valid http URL.");
				vuid.setSize(new Dimension(400, 200));
				if (vuid.show((Component) e.getSource())) {
					String newUrl = urlPanel.getUrl();
					AnnotationProfile.setAnnotationProfile(d, newUrl);
				}
			}
		};
	}
}
