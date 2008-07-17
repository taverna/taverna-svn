package net.sf.taverna.t2.security.credentialmanager.ui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Custom cell renderer for the headers of the tables displaying Keystore/Truststore contents.
 * 
 * @author Alexandra Nenadic
 */
public class TableHeaderRenderer
    extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = -7691713691433621006L;
	
	private final ImageIcon entryTypeIcon = new ImageIcon(getClass().getResource(
	"/images/table/entry_heading.png"));
    
    /**
     * Returns the rendered header cell for the supplied value and column.
     *
     * @param jtKeyStore The JTable
     * @param value The value to assign to the cell
     * @param bIsSelected True if cell is selected
     * @param iRow The row of the cell to render
     * @param iCol The column of the cell to render
     * @param bHasFocus If true, render cell appropriately
     ** @return The renderered cell
     */
    public Component getTableCellRendererComponent(JTable jtKeyStoreTable,
        Object value, boolean bIsSelected, boolean bHasFocus, int iRow,
        int iCol)
    {
        // Get header renderer
        JLabel header = (JLabel) jtKeyStoreTable.getColumnModel().getColumn(iCol).getHeaderRenderer();

        // The entry type header contains an icon
        if (iCol == 0) {
            header.setText("");
            header.setIcon(entryTypeIcon); // entry type icon (header for the first column of the table)
            header.setHorizontalAlignment(CENTER);
            header.setVerticalAlignment(CENTER);
            header.setToolTipText("Entry type");

        }
        // All other headers contain text
        else {
            header.setText((String) value);
            header.setHorizontalAlignment(LEFT);
            
            // Passwords table has 5 colums, Key pairs and Trusted Certificates tables have 3 each
            if (jtKeyStoreTable.getModel() instanceof PasswordsTableModel){
                if (iCol == 1) { //Service URL column
                    header.setToolTipText("Service URL for the password entry");
                }
                else if (iCol == 2 ){ //Username column 
                    header.setToolTipText("Username for the password entry");                	
                }
                else if (iCol == 3){ // Last modified column
                    header.setToolTipText("Password entry's last modification date and time");
                }            	
            }
            else if(jtKeyStoreTable.getModel() instanceof KeyPairsTableModel){
                if (iCol == 1) { //Owner:Serial Number column
                    header.setToolTipText("Certificate's owner and serial number");
                }
                else if(iCol == 2) { // Last modified column
                    header.setToolTipText("Key pair entry's last modification date and time");
                }         	
            }          
            else if(jtKeyStoreTable.getModel() instanceof TrustCertsTableModel){
                if (iCol == 1) { //Owner column
                    header.setToolTipText("Certificate's owner and serial number");
                }
                else if (iCol == 2){ // Last modified column
                    header.setToolTipText("Trusted certificate's last modification date and time");
                }         	
            }         
        }
        header.setBorder(new CompoundBorder(
            new BevelBorder(BevelBorder.RAISED), new EmptyBorder(0, 5, 0, 5)));

        return header;
    }
}


