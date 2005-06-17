/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class BioMobyServiceTreeCustomRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    private Color leafForeground = Color.blue;

    private Color rootColor = Color.black;

    private Color feedsIntoColor = Color.gray;

    private Color producedColor = Color.lightGray;

    private Color authorityColor = Color.orange;

    private Color serviceColor = Color.magenta;

    private Color objectColor = Color.green;

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        // Allow the original renderer to set up the label
        Component c = super.getTreeCellRendererComponent(tree, value, selected,
                expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() instanceof MobyServiceTreeNode) {
                // service node
                c.setForeground(serviceColor);
                ((JComponent) c).setToolTipText(((MobyServiceTreeNode) node
                        .getUserObject()).getDescription());
                setIcon(new ImageIcon(
                        this
                                .getClass()
                                .getClassLoader()
                                .getResource(
                                        "org/biomoby/client/taverna/plugin/service.png")));
            } else if (node.getUserObject() instanceof MobyObjectTreeNode) {
                // object node
                c.setForeground(objectColor);
                ((JComponent) c).setToolTipText(((MobyObjectTreeNode) node
                        .getUserObject()).getDescription());
            } else if (node.getUserObject() instanceof String) {
                // check for feeds into and produced by nodes
                String string = (String) node.getUserObject();
                if (string.equalsIgnoreCase("inputs")) {
                    setIcon(new ImageIcon(
                            this
                                    .getClass()
                                    .getClassLoader()
                                    .getResource(
                                            "org/biomoby/client/taverna/plugin/input.png")));
                    ((JComponent) c).setToolTipText(null);
                } else if (string.equalsIgnoreCase("outputs")) {
                    setIcon(new ImageIcon(
                            this
                                    .getClass()
                                    .getClassLoader()
                                    .getResource(
                                            "org/biomoby/client/taverna/plugin/output.png")));
                    ((JComponent) c).setToolTipText(null);
                } else {

                    ((JComponent) c).setToolTipText(null);

                    if (!leaf) {
                        if (string.startsWith("Collection('")) {
                            setIcon(new ImageIcon(
                                    this
                                            .getClass()
                                            .getClassLoader()
                                            .getResource(
                                                    "org/biomoby/client/taverna/plugin/collection.png")));
                        }
                    }
                }

            } else {
                ((JComponent) c).setToolTipText("nothing node");
            }
        }
        if (leaf)
            c.setForeground(this.leafForeground);
        return c;
    }
}
