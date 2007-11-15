package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.net.MalformedURLException;
import java.net.URL;

import net.sf.taverna.t2.cloudone.gui.entity.view.HttpRefSchemeView;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

/**
 * Model (in MVC terms) for the {@link HttpReferenceScheme} being added or
 * removed from a {@link HttpRefSchemeView}. Interested parties can register
 * with it (delegated to the {@link MultiCaster}) to receive notifications when
 * this model changes (typically due to the URL being modified)
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class HttpRefSchemeModel extends ReferenceSchemeModel<URL> {

	private MultiCaster<URL> multiCaster = new MultiCaster<URL>(this);

	private URL url = null;
	private final DataDocumentModel parentModel;

	/**
	 * An {@link HttpRefSchemeModel} knows who its parent
	 * {@link DataDocumentModel} is so that events can be propogated up and down
	 * the chain
	 * 
	 * @param parentModel
	 */
	public HttpRefSchemeModel(DataDocumentModel parentModel) {
		this.parentModel = parentModel;
	}

	/**
	 * What URL of the {@link HttpReferenceScheme} is
	 * 
	 * @return
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Register as an observer to be informed of changes to this model
	 */
	public void registerObserver(Observer<URL> observer) {
		multiCaster.registerObserver(observer);

	}

	/**
	 * Remove this {@link HttpRefSchemeModel} from the model and inform the
	 * parent model that it has happened
	 */
	@Override
	public void remove() {
		parentModel.removeReferenceScheme(this);
	}

	/**
	 * Tell the model that you no longer wish to be informed about changes
	 */
	public void removeObserver(Observer<URL> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * Set the URL of the required {@link HttpReferenceScheme} and use the
	 * {@link MultiCaster} to send out notifications that it has changed
	 * 
	 * @param text
	 * @throws MalformedURLException
	 */
	public void setURL(String text) throws MalformedURLException {
		url = new URL(text);
		multiCaster.notify(url);
	}

	@Override
	public String getStringRepresentation() {
		if (url == null) {
			return "(none)";
		}
		return url.toString();
	}

}
