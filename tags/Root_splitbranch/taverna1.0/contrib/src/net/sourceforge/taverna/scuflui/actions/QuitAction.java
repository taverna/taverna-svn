package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;

public class QuitAction extends DefaultAction{
  public QuitAction() {
    super("Quit", "etc/icons/stock_exit-16.png", "etc/icons/stock_exit.png", "Quit Application", "QuitApplication");
  }
  
  public void actionPerformed(ActionEvent ae){
      System.exit(0);
  }
}
