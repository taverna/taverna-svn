/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;

public class OverlayListener extends MouseInputAdapter {
    JTree tree;

    Component oldGlassPane;

    TreePath path;

    int row;

    Rectangle bounds;

    public OverlayListener(JTree tree) {
        this.tree = tree;
        tree.addMouseListener(this);
        tree.addMouseMotionListener(this);
    }

    JComponent c = new JComponent() {

        private static final long serialVersionUID = 3545800978927530288L;

        public void paint(Graphics g) {
            boolean selected = tree.isRowSelected(row);
            Component renderer = tree
                    .getCellRenderer()
                    .getTreeCellRendererComponent(
                            tree,
                            path.getLastPathComponent(),
                            tree.isRowSelected(row),
                            tree.isExpanded(row),
                            tree.getModel().isLeaf(path.getLastPathComponent()),
                            row, selected);
            c.setFont(tree.getFont());
            Rectangle paintBounds = SwingUtilities.convertRectangle(tree,
                    bounds, this);
            SwingUtilities.paintComponent(g, renderer, this, paintBounds);
            if (selected)
                return;

            g.setColor(Color.blue);
            ((Graphics2D) g).draw(paintBounds);
        }
    };

    public void mouseExited(MouseEvent e) {
        resetGlassPane();
    }

    private void resetGlassPane() {
        if (oldGlassPane != null) {
            c.setVisible(false);
            tree.getRootPane().setGlassPane(oldGlassPane);
            oldGlassPane = null;
        }
    }

    public void mouseMoved(MouseEvent me) {
        path = tree.getPathForLocation(me.getX(), me.getY());
        if (path == null) {
            resetGlassPane();
            return;
        }
        row = tree.getRowForPath(path);
        bounds = tree.getPathBounds(path);
        if (!tree.getVisibleRect().contains(bounds)) {
            if (oldGlassPane == null) {
                oldGlassPane = tree.getRootPane().getGlassPane();
                c.setOpaque(false);
                tree.getRootPane().setGlassPane(c);
                c.setVisible(true);
            } else
                tree.getRootPane().repaint();
        } else {
            resetGlassPane();
        }
    }
}
