package idaservicetype.idaservicetype.ui.idawizard.components;


import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashSet;
import java.util.Set;/*
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
 * Date: Feb 23, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public abstract class AbstractWizardPanel extends WizardPanel {

    private JTextArea instructionArea;

    private JPanel marginPanel;

    private JLabel marginLabel;

    private Icon backgroundImage;

    private boolean notifyDisplaying;

    private String title;

//    private EditorKit editorKit;

    private boolean setTransparency;

    public AbstractWizardPanel(Object id, String title) {
        super(id);
//        this.editorKit = editorKit;
        this.title = title;
        notifyDisplaying = true;
        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if (isShowing()) {
                    if (notifyDisplaying) {
                        displayingPanel();
                    }
                    else {
                        notifyDisplaying = false;
                    }
                }
                else {
                    notifyDisplaying = true;
                }
            }
        });
        createUI();
        setTransparency = false;
    }


//    public ModelManager getModelManager() {
//        return editorKit.getModelManager();
//    }
//
//
//    public EditorKit getEditorKit() {
//        return editorKit;
//    }
//
    public void setBackgroundImage(String name) {
//        backgroundImage = Icons.getIcon(name);
    }

    public void setMarginImage(Icon icon) {
        marginLabel.setIcon(icon);
    }

    final protected void createUI() {
        backgroundImage = new ImageIcon(getClass().getResource("/elico-logo4-2-480px-240ppi-blackOnWhite-4Wabi-topspace40px.png"));
        setLayout(new BorderLayout(7, 7));
        marginPanel = new JPanel(new BorderLayout());
        marginPanel.setPreferredSize(new Dimension(150, 400));
        add(marginPanel, BorderLayout.WEST);
        marginPanel.setOpaque(false);
        marginPanel.setEnabled(false);
        marginLabel = new JLabel();
        marginPanel.add(marginLabel, BorderLayout.NORTH);
        marginLabel.setBorder(BorderFactory.createEmptyBorder(30, 8, 0, 0));
        instructionArea = new JTextArea("");
        instructionArea.setOpaque(false);
        instructionArea.setWrapStyleWord(true);
        instructionArea.setLineWrap(true);
        instructionArea.setEditable(false);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel containerPanel = new JPanel(new BorderLayout(7, 7));
        add(containerPanel);
        containerPanel.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setOpaque(false);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14.0f));
        containerPanel.add(label, BorderLayout.NORTH);

        JPanel contentAndInstructionHolder = new HolderPanel();
        contentAndInstructionHolder.add(instructionArea, BorderLayout.NORTH);
        JPanel contentBorderPanel = new JPanel(new BorderLayout());
        contentBorderPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 0));
        JPanel content = new JPanel();
        contentBorderPanel.add(content);
        contentBorderPanel.setOpaque(false);
        content.setOpaque(false);
        contentAndInstructionHolder.add(contentBorderPanel, BorderLayout.CENTER);
        containerPanel.add(contentAndInstructionHolder);
        createUI(content);
        setComponentTransparency(content);
    }


    private static Set<Class> nonTransparentComponents;


    static {
        nonTransparentComponents = new HashSet<Class>();
        nonTransparentComponents.add(JTextComponent.class);
        nonTransparentComponents.add(JList.class);
        nonTransparentComponents.add(JTree.class);
        nonTransparentComponents.add(JTable.class);
        nonTransparentComponents.add(JScrollPane.class);
        nonTransparentComponents.add(JComboBox.class);
    }


    protected void setComponentTransparency(Component component) {
        if (component instanceof JComponent) {
            for (Class c : nonTransparentComponents) {
                if (c.isInstance(component)) {
                    return;
                }
            }
            ((JComponent) component).setOpaque(false);
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            Component [] components = container.getComponents();
            for (int i = 0; i < components.length; i++) {
                setComponentTransparency(components[i]);
            }
        }
    }


    public void setBackgroundImage(Icon imageIcon) {
        backgroundImage = imageIcon;
    }


    public void setInstructions(String instructions) {
        instructionArea.setText(instructions);
    }


    public Dimension getPreferredSize() {
        return new Dimension(800, 550);
    }


    protected abstract void createUI(JComponent parent);


    private class HolderPanel extends JPanel {

        private Color color;


        public HolderPanel() {
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY),
                                                         BorderFactory.createEmptyBorder(20, 20, 20, 20)));
            setLayout(new BorderLayout(7, 20));
            setOpaque(false);
            color = new Color(255, 255, 255, 230);
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle r = g.getClipBounds();
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            g2.fillRect(r.x, r.y, r.width, r.height);
        }
    }


    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            backgroundImage.paintIcon(this, g, 0, 0);
        }
    }
}

