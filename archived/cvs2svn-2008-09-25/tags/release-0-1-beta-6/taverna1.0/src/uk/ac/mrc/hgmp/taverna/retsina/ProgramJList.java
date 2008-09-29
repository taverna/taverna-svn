package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;




/**
*
*
*/
public class ProgramJList extends JList implements DragGestureListener,
                           DragSourceListener, DropTargetListener
{

  public DefaultListModel model;
  private String progs[];
  private String fs = new String(System.getProperty("file.separator"));
  private JPopupMenu popup;
  final Cursor cbusy = new Cursor(Cursor.WAIT_CURSOR);
  final Cursor cdone = new Cursor(Cursor.DEFAULT_CURSOR);


  public ProgramJList(String progs[]) 
  {
    super(progs);
    this.progs = progs;

    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(
               this,                             // component where drag originates
               DnDConstants.ACTION_COPY_OR_MOVE, // actions
               this);                            // drag gesture recognizer

//  setDropTarget(new DropTarget(this,this));
    addListSelectionListener(new ListSelectionListener(){
      public void valueChanged(ListSelectionEvent e)
      {
        if (e.getValueIsAdjusting())
          return;

        JList theList = (JList)e.getSource();
        if (theList.isSelectionEmpty())
        {
          System.out.println("Empty selection");
        }
        else
        {
          int index = theList.getSelectedIndex();
        }
      }
    });
    this.getSelectionModel().setSelectionMode
                  (ListSelectionModel.SINGLE_SELECTION);

    // Popup menu
    addMouseListener(new PopupListener());
    popup = new JPopupMenu();

    //Listen for when a file is selected
    MouseListener mouseListener = new MouseAdapter() 
    {
      public void mouseClicked(MouseEvent me) 
      {
        if(me.getClickCount() == 2 && 
           !me.isPopupTrigger()) 
        {
          JList t = (JList)me.getSource();
          String selected = (String)t.getSelectedValue();
          System.out.println(selected);
        }
      }
    };
    this.addMouseListener(mouseListener);
  }

// drag source
  public void dragGestureRecognized(DragGestureEvent e) 
  {
    // ignore if mouse popup trigger
    InputEvent ie = e.getTriggerEvent();
    if(ie instanceof MouseEvent) 
      if(((MouseEvent)ie).isPopupTrigger()) 
        return;

    // drag  
    e.startDrag(DragSource.DefaultCopyDrop, // cursor
                 (Transferable)getNodename(), // transferable data
                                       this); // drag source listener
  }
  public void dragDropEnd(DragSourceDropEvent e) {}
  public void dragEnter(DragSourceDragEvent e) {}
  public void dragExit(DragSourceEvent e) {}
  public void dragOver(DragSourceDragEvent e) {}
  public void dropActionChanged(DragSourceDragEvent e) {}

// drop sink
  public void dragEnter(DropTargetDragEvent e)
  {
  }

  public void drop(DropTargetDropEvent e)
  {
//  Transferable t = e.getTransferable();
//  Point ploc = e.getLocation();
//  TreePath dropPath = getPathForLocation(ploc.x,ploc.y);
//  if(t.isDataFlavorSupported(ProgNode.PROGNODE))
//  {
//  }
//  else
//  {
      e.rejectDrop();
      return;
//  }

  }

  class PopupListener extends MouseAdapter 
  {
    public void mousePressed(MouseEvent e) 
    {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) 
    {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) 
    {
      if(e.isPopupTrigger()) 
        popup.show(e.getComponent(),
                e.getX(), e.getY());
    }
  }

/**
*
* When a suitable DataFlavor is offered over a remote file
* node the node is highlighted/selected and the drag
* accepted. Otherwise the drag is rejected.
*
*/
  public void dragOver(DropTargetDragEvent e)
  {
    e.rejectDrag();
  }

  public void dropActionChanged(DropTargetDragEvent e) {}
  public void dragExit(DropTargetEvent e){}

  public ProgNode getNodename()
  {
    return new ProgNode((String)getSelectedValue());
  }

  public DefaultListModel getListModel () 
  {
    return model;
  }

}

