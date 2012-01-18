package idaservicetype.idaservicetype.ui.idawizard.wizard;

import ch.uzh.ifi.ddis.ida.api.Goal;
import ch.uzh.ifi.ddis.ida.api.Task;
import ch.uzh.ifi.ddis.ida.api.Tree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Simon Jupp<br>
 * Date: Feb 24, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class OntologyBrowser extends JTree {


    private TreeSelectionListener listener;

    public OntologyBrowser (boolean multiSelect) {

        if (!multiSelect) {
            getSelectionModel().setSelectionMode
                    (TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        else {
            getSelectionModel().setSelectionMode(TreeSelectionModel.
                    CONTIGUOUS_TREE_SELECTION);
        }
        
        this.setRootVisible(true);
        this.setShowsRootHandles(true);


        getSelectionModel().addTreeSelectionListener(listener = new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {


            }
        });

        //ImageIcon icon = new ImageIcon(getClass().getResource("/class.gif"));
        ImageIcon icon =null;
        this.setCellRenderer(new MyCellRenderer(icon));
    }

    public void setRootNode(Tree nodeTree) {
        MyNode root = new MyNode(nodeTree.getData());

		this.setModel(new DefaultTreeModel(root));
        fillTree (root, nodeTree);

    }

    private void fillTree(MyNode currentNode, Tree children) {

        for (int x = 0; x < children.getNumberOfChildren(); x++) {

            MyNode childNode = new MyNode(children.getChildAt(x).getData());
            currentNode.add(childNode);
            fillTree(childNode, children.getChildAt(x));
        }
    }


    public Object getSelectedNode() {
        return ((MyNode) getLastSelectedPathComponent()).getNode();
    }

    public List<Object> getSelectedNodes() {

        ArrayList<Object> nodeList = new ArrayList<Object>();
        TreePath[] paths = this.getSelectionPaths();

        for (TreePath path : paths) {
            nodeList.add( ((MyNode) path.getLastPathComponent()).getNode());
        }
        
        return  nodeList;

    }

    public void expand() {
        for(int i=0;i<this.getRowCount();i++)
        {
            this.expandRow(i);
        }
    }

    public DefaultMutableTreeNode searchNode(String nodeStr) 
    { 
        DefaultMutableTreeNode node = null; 
        Object root = this.getModel().getRoot();
        //Get the enumeration 
        Enumeration enu = ((DefaultMutableTreeNode) root).breadthFirstEnumeration(); 
         
        //iterate through the enumeration 
        while(enu.hasMoreElements()) 
        { 
            //get the node 
            node = (DefaultMutableTreeNode)enu.nextElement(); 
             
            //match the string with the user-object of the node 
            if(nodeStr.equals(node.getUserObject().toString())) 
            { 
            	System.out.println(" HIT HIT HIT ");
                //tree node with string found 
                return node;                          
            } 
        } 
         
        //tree node with string node found return null 
        return null; 
    } 
    
    public class MyNode extends DefaultMutableTreeNode {

        public Object node;

        public MyNode(Object node) {
            this.node = node;
        }

        public Object getNode () {
            return node;
        }

        public String getToolTipText() {
            if (node instanceof Goal) {
                System.err.println("Long desc:" + ((Goal) node).getLongHelp());
                return ((Goal) node).getLongHelp();
            }
            else if (node instanceof Task) {
                System.err.println("Long desc:" + ((Task) node).getLongHelp());
                return ((Task) node).getLongHelp();
            }
            return "";

        }

        public String toString () {
            if (node instanceof Goal) {
                return ((Goal) node).getGoalName();
            }
            else if (node instanceof Task) {
                return ((Task) node).getTaskName();
            }
            return "";
        }
    }

    public class MyCellRenderer extends DefaultTreeCellRenderer{

        ImageIcon icon;

        public MyCellRenderer (ImageIcon icon) {
            this.icon = icon;

        }

        @Override
        public Component getTreeCellRendererComponent(JTree jTree, Object o, boolean b, boolean b1, boolean b2, int i, boolean b3) {
            super.getTreeCellRendererComponent(jTree, o, b, b1, b2, i, b3);    //To change body of overridden methods use File | Settings | File Templates
            setIcon(icon);
            setToolTipText(((MyNode) o).getToolTipText());
            return this;
        }
    }



}
