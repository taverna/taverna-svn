package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

// IO Imports
import java.io.IOException;
import java.io.Serializable;




public class ProgNode 
                 implements Transferable, Serializable
{
    public static DataFlavor PROGNODE =
           new DataFlavor(ProgNode.class, "Program node");
    static DataFlavor flavors[] = { PROGNODE, DataFlavor.stringFlavor };

    private String pnode;

    public ProgNode(String prog)
    {
      pnode = new String(prog);
    }

    public String getProgramName()
    {
      return pnode;
    }

// Transferable
    public DataFlavor[] getTransferDataFlavors()
    {
      return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor f)
    {
      if(f.equals(PROGNODE) || f.equals(DataFlavor.stringFlavor))
        return true;
      return false;
    }

    public Object getTransferData(DataFlavor d)
        throws UnsupportedFlavorException, IOException
    {
      if(d.equals(PROGNODE))
        return this;
      else if(d.equals(DataFlavor.stringFlavor))
        return this;
      else throw new UnsupportedFlavorException(d);
    }

//Serializable
   private void writeObject(java.io.ObjectOutputStream out) throws IOException
   {
     out.defaultWriteObject();
   }

   private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException
   {
     in.defaultReadObject();
   }

}
