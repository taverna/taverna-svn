 package org.embl.ebi.escience.scuflui.workbench; 

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

// Utility Imports
import java.util.Hashtable;

import java.lang.Object;
import java.lang.String;



/**
 * ScrollableDesktopPane.java
 *
 * This class gives a DesktopPane which responds properly when placed within a 
 * scrollpane. It does this by calculating a preferred size which the JDesktopPane
 * doesnt really. 
 *
 * (PENDING)It could do with implementing a little more cleverly. At the moment it 
 * validates a lot when unnecessary. The preferred should probably be calculated in 
 * the ComponentListeners. It should probably also add an InternalFrameListener also
 *
 *
 * Created: Fri Feb 19 15:08:09 1999
 *
 * @author Phillip Lord
 * @version $Id: ScrollableDesktopPane.java,v 1.3 2004-10-01 13:38:20 mereden Exp $
 */
public class ScrollableDesktopPane extends JDesktopPane
{

  protected Hashtable listeners = new Hashtable();
  
  public ScrollableDesktopPane()
  {
    super();
  }

  /**
   * Set the preferred size of the desktop to the right-bottom-corner of the
   * internal-frame with the "largest" right-bottom-corner.
   *
   * @return The preferred desktop dimension.
   */
  private Dimension preferredSizeOfAllFrames()
  {
    JInternalFrame [] array = getAllFrames();
    
    int maxX = 0;
    int maxY = 0;
    for (int i = 0; i < array.length; i++){
      if ( array[ i ].isVisible() ){
	  int x = array[i].getX() + array[i].getWidth();
	  if (x > maxX) maxX = x;
	  int y = array[i].getY() + array[i].getHeight();
	  if (y > maxY) maxY = y;
	}
    }
    return new Dimension(maxX, maxY);
  }
  
  public void paint( Graphics g )
  {
    setPreferredSize( preferredSizeOfAllFrames() );
    super.paint( g );
  }
  
  /**
   * Add an internal-frame to the desktop. Sets a component-listener on it,
   * which resizes the desktop if a frame is resized.
   */
  public void add( Component comp, Object constraints )
  {
    super.add(comp, constraints);
    registerListener( comp );
  }
  
  public Component add( Component comp )
  {
    registerListener( comp );
    return super.add( comp );
  }
  
  public void add( Component comp, Object constraints, int index )
  {
    super.add( comp, constraints, index );
    registerListener( comp );
  }
  
  public Component add( String name, Component comp )
  {
    registerListener( comp );
    return super.add( name, comp );
  }

  public void registerListener( Component comp )
  {
    
    ComponentListener listener = new ComponentListener()
    {
      public void componentResized(ComponentEvent e)
      { 
	// Layout the JScrollPane
	ScrollableDesktopPane.this.revalidate();
	ScrollableDesktopPane.this.repaint();
      }
      public void componentMoved(ComponentEvent e)
      {
	componentResized( e );
      }
      public void componentShown(ComponentEvent e) 
      {
	componentResized( e );
      }
      public void componentHidden(ComponentEvent e) 
      {
	componentResized( e );
      }
    };
    listeners.put(comp, listener);
    comp.addComponentListener(listener);
    revalidate();
  }

  /**
   * Remove an internal-frame from the desktop. Removes the
   * component-listener and resizes the desktop.
   */
  public void remove(Component comp)
  {
    super.remove(comp);
    validate();
    comp.removeComponentListener((ComponentListener)
                                 listeners.get(comp));
  }
} // ScrollableDesktopPane
