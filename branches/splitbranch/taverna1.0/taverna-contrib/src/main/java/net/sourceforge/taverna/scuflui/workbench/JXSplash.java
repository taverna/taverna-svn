package net.sourceforge.taverna.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
public class JXSplash extends JWindow
  implements KeyListener, MouseListener, ActionListener
{
  public JXSplash(JFrame parent, String filename, int timeout)
  {
    super(parent);
    ImageIcon image = new ImageIcon(ClassLoader.getSystemResource(filename));
    int w = image.getIconWidth() + 5;
    int h = image.getIconHeight() + 5;

    Dimension screen =
      Toolkit.getDefaultToolkit().getScreenSize();
    int x = (screen.width - w) / 2;
    int y = (screen.height - h) / 2;
    setBounds(x, y, w, h);

    getContentPane().setLayout(new BorderLayout());
    JLabel picture = new JLabel(image);
    getContentPane().add("Center", picture);
    picture.setBorder(new BevelBorder(BevelBorder.RAISED));

    // Listen for key strokes
    addKeyListener(this);

    // Listen for mouse events from here and parent
    addMouseListener(this);
    if (parent != null){
    	parent.addMouseListener(this);
    }

    final int pause = timeout;
	final Runnable closerRunner = new Runnable() {
		public void run() {
			setVisible(false);
			dispose();
		}
	};
	Runnable waitRunner = new Runnable() {
		public void run() {
			try {
				Thread.sleep(pause);
				SwingUtilities.invokeAndWait(closerRunner);
			} catch (Exception e) {
				e.printStackTrace();
				// can catch InvocationTargetException
				// can catch InterruptedException
			}
		}
	};
	setVisible(true);
	Thread splashThread = new Thread(waitRunner, "SplashThread");
	splashThread.start();
   
  }

  public void block()
  {
    while(isVisible()) {}
  }

  // Dismiss the window on a key press
  public void keyTyped(KeyEvent event) {}
  public void keyReleased(KeyEvent event) {}
  public void keyPressed(KeyEvent event)
  {
    setVisible(false);
    dispose();
  }

  // Dismiss the window on a mouse click
  public void mousePressed(MouseEvent event) {}
  public void mouseReleased(MouseEvent event) {}
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mouseClicked(MouseEvent event)
  {
    setVisible(false);
    dispose();
  }

  // Dismiss the window on a timeout
  public void actionPerformed(ActionEvent event)
  {
    setVisible(false);
    dispose();
  }
}