package net.sourceforge.taverna.scuflui.actions;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;
import javax.swing.text.EditorKit;



/**
 * Represents the default action
 * @author  mfortner
 * @version $Revision: 1.2 $
 */

public abstract class DefaultAction extends AbstractAction {

  public DefaultAction() {
    super();
  }

  
  /**
   * This constructor is used whenever there is an action that needs to be wrapped with different
   * icons and labels.  This is useful when you want to take one of the EditorKit actions and
   * customise them.
   * @param act  The action that you want to wrap.
   */
  public DefaultAction(Action act) {
    init(act);
  }

  
  /**
   * Constructor
   * @param _name     	The name of the action.
   * @param _iconUrl  	The relative URL for the small icon used by the action.  This icon
   * 					will be loaded via the classpath and must be relative to the classpath.
   * @param _longDesc	The long description for the action
   * @param _shortDesc  The short description for the action
   */
  public DefaultAction(String _name, String _iconUrl, String _longDesc,
                       String _shortDesc) {
    setName(_name);
    setSmallIcon(_iconUrl);
    setShortDescription(_shortDesc);
    setLongDescription(_longDesc);
  }

  
  /**
   * Constructor
   * @param _name			The name of the action.
   * @param _smallIconUrl	The relative URL for the small icon used by the action.  This icon
   * 						will be loaded via the classpath and must be relative to the classpath.
   * @param _lrgIconUrl		The relative URL for the large icon used by the action.  This icon
   * 						will be loaded via the classpath and must be relative to the classpath.
   * @param _longDesc		The long description for the action
   * @param _shortDesc		The short description for the action
   */
  public DefaultAction(String _name, String _smallIconUrl, String _lrgIconUrl,
                       String _longDesc, String _shortDesc) {
    init(_name, _smallIconUrl, _lrgIconUrl, _longDesc, _shortDesc);
  }

  /**
   * This method is used to initialise the action.
   * @param _name String
   * @param _smallIconUrl String
   * @param _lrgIconUrl String
   * @param _longDesc String
   * @param _shortDesc String
   */
  public void init(String _name, String _smallIconUrl, String _lrgIconUrl,
                   String _longDesc, String _shortDesc) {
    setName(_name);
    setSmallIcon(_smallIconUrl);
    setLargeIcon(_lrgIconUrl);
    setShortDescription(_shortDesc);
    setLongDescription(_longDesc);

  }

  /**
   * This method uses an action to initialise the action.
   * @param act Action
   */
  public void init(Action act) {
    setSmallIcon( (ImageIcon) act.getValue(Action.SMALL_ICON));
    setName( (String) act.getValue(Action.NAME));
    setLongDescription( (String) act.getValue(Action.LONG_DESCRIPTION));
    setShortDescription( (String) act.getValue(Action.SHORT_DESCRIPTION));
    this.setAcceleratorKey( (KeyStroke) act.getValue(Action.ACCELERATOR_KEY));
    this.subAction = act;
  }

  /**
   * This method sets the small icon for the action, used for toolbar buttons.
   * @param iconImageUrl  A relative url for the icon.
   */
  public void setSmallIcon(String iconImageUrl) {
    this.putValue(Action.SMALL_ICON, new ImageIcon(iconImageUrl));
  }

  /**
   * This method sets the small icon for the action.
   * @param icon ImageIcon
   */
  public void setSmallIcon(ImageIcon icon) {
    if (icon != null) {
      this.putValue(Action.SMALL_ICON, icon);
    }
  }

  /**
   * This method sets the name for the action, used for a menu or button.
   * @param _name
   */
  public void setName(String _name) {
    this.putValue(Action.NAME, _name);
  }

  /**
   * This method sets a longer description for the action, could be used for
   * context-sensitive help.
   * @param longDescription a longer description for the action
   */
  public void setLongDescription(String longDescription) {
    this.putValue(Action.LONG_DESCRIPTION, longDescription);
  }

  /**
   * This method sets the short description (tooltip) for action.
   * @param _desc A short description for the action, used for tooltip text.
   */
  public void setShortDescription(String _desc) {
    this.putValue(Action.SHORT_DESCRIPTION, _desc);
  }

  /**
   * This method inserts the mnemonic used for the action.
   * @param mnemonic Integer
   */
  public void setMnemonic(Integer mnemonic) {
    this.putValue(Action.MNEMONIC_KEY, mnemonic);
  }

  /**
   * This method sets the keystroke to be used as an accelerator for the action.
   * @param keystroke  The keystroke to be used as an accelerator for the action.
   */
  public void setAcceleratorKey(KeyStroke keystroke) {
    this.putValue(Action.ACCELERATOR_KEY, keystroke);
  }

  /**
   * This method sets the url for the large icon.
   * @param lrgIconUrl A string containing a path similar to "etc/myicon.png".  The etc directory must
   * be part of the classpath in order for the icon to be found.
   */
  public void setLargeIcon(String lrgIconUrl) {
    this.putValue(LARGE_ICON, getIconInClassPath(lrgIconUrl));
  }

  /**
   * This method gets the large icon.
   * @return
   */
  public Object getLargeIcon() {
    return this.getValue(LARGE_ICON);
  }

  /**
   * This method executes each of the action listeners that have been added to it.
   * @param evt ActionEvent
   */
  public void actionPerformed(ActionEvent evt) {
    subAction.actionPerformed(evt);
    if (this.listeners.getListenerCount() != 0) {
      Object[] listenerList = this.listeners.getListenerList();

      // Recreate the ActionEvent and stuff the value of the ACTION_COMMAND_KEY
      ActionEvent e = new ActionEvent(evt.getSource(), evt.getID(),
                                      (String) getValue(Action.
          ACTION_COMMAND_KEY));
      for (int i = 0; i <= listenerList.length - 2; i += 2) {
        ( (ActionListener) listenerList[i + 1]).actionPerformed(e);
      }
    }
  }

  /**
   * Returns the Icon associated with the name from the resources.
   * The resouce should be in the path.
   * @param name Name of the icon file i.e., help16.gif
   * @return the name of the image or null if the icon is not found.
   */
  public ImageIcon getIcon(String name) {

    //String imagePath = JLF_IMAGE_DIR + name;
    String imagePath = name;
    //System.out.println("imagePath: "+ imagePath);
    ImageIcon icon = null;
    if (name != null) {
      // URL url = this.getClass().getResource(imagePath);

     
      //if (url != null)  {
      icon = new ImageIcon(ClassLoader.getSystemResource(name));
      icon.getImage();
      int loadStatus = icon.getImageLoadStatus();
      if (loadStatus == MediaTracker.ABORTED) {
        System.out.println("Aborted");
      }
      else if (loadStatus == MediaTracker.COMPLETE) {
        // DO NOTHING System.out.println("COMPLETE");
      }
      else if (loadStatus == MediaTracker.ERRORED) {
        System.out.println("ERRORED: " + name);
      }
      else if (loadStatus == MediaTracker.LOADING) {
        System.out.println("LOADING");
      }
      if (icon == null) {
        System.out.println("It's null.");
      }
      if (icon.getImage() == null) {
        System.out.println("Icon's null");
      }
    }
    return icon;
  }

  /**
   * This method is used to get an action that from a list of predefined actions.  This is used
   * when you're wrapping an action with a new action.
   * @param actionConstant  The key used to identify the action.
   * @param edKit			The editor kit where the action is located.
   * @return
   */
  protected Action getAction(String actionConstant, EditorKit edKit) {
    Action action = null;
    // init actionMap
    Action[] actionList = edKit.getActions();
    for (int i = 0; i < actionList.length; i++) {
      if (actionConstant.equals(actionList[i].getValue(Action.ACTION_COMMAND_KEY))) {
        action = actionList[i];
      }
    }

    if (action == null) {
      System.out.println("Action: " + actionConstant + " was null");

    }

    return action;

  }
  
  /**
   * This method gets the keystroke character to be used by the action.
   * @param keychar  The key character to be used to invoke the action.
   * @return
   */
  public KeyStroke getKeyStroke(Character keychar) {
      return KeyStroke.getKeyStroke(keychar, KeyEvent.META_MASK);
      //KeyStroke.getKeyStroke(79, KeyEvent.META_MASK, false)
  }
  
  /**
   * This method gets the icon URL from the classpath.
   * @param iconUrl String
   * @return ImageIcon
   */
  public static ImageIcon getIconInClassPath(String iconUrl){
    Image img = Toolkit.getDefaultToolkit().getImage(iconUrl);
    ImageIcon icon = new ImageIcon(img);
    return icon;
  }

  /**
   * This is a key used to retrieve the large icon from the Action.
   */
  public static final String LARGE_ICON = "LargeIcon";

  //protected static final String IMAGE_HOME = System.getProperty("image.home");
  protected EventListenerList listeners = new EventListenerList();
  ActionMap map = new ActionMap();

  // these are constants used to define the editor kit being used.
  public static final int HTML = 0;
  public static final int RTF = 1;
  public static final int TEXT = 2;
  Action subAction = null;
}
