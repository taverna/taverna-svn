package org.embl.ebi.escience.scuflui.facets;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 *
 *
 * @author Matthew Pocock
 */
public class PropertySheet
        extends JPanel
{
    private static Logger LOG = Logger.getLogger(PropertySheet.class);

    public PropertySheet(final Object bean)
    {
        LOG.info("Creating property sheet for bean " + bean.getClass().getName());

        try {
            setLayout(new GridBagLayout());
            GridBagConstraints lhc = new GridBagConstraints();
            GridBagConstraints rhc = new GridBagConstraints();

            lhc.gridx = 0;
            rhc.gridx = 1;
            lhc.gridy = 0;
            rhc.gridy = 0;
            lhc.anchor = GridBagConstraints.EAST;
            rhc.anchor = GridBagConstraints.WEST;

            BeanInfo info = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for(int i = 0; i < props.length; i++) {
                final PropertyDescriptor pd = props[i];
                final Method read = pd.getReadMethod();
                final Method write = pd.getWriteMethod();

                LOG.info("Property: " + pd.getName());

                if(read != null && write != null) {
                    LOG.info("mutable");
                    Class propType = read.getDeclaringClass();
                    LOG.info("of type: " + propType.getName());
                    final PropertyEditor pe = PropertyEditorManager.findEditor(propType);
                    if(pe != null) {
                        LOG.info("got editor: " + pe);
                        pe.setValue(read.invoke(bean, new Object[] {}));
                        if(pe.supportsCustomEditor()) {
                            LOG.info("custom editor");
                            Component cmp = pe.getCustomEditor();
                            if(cmp != null) {
                                // if this fails, give up
                                LOG.info("got editor comopnent");
                                add(new JLabel(pd.getDisplayName()), lhc);
                                add(cmp, rhc);
                                lhc.gridy++;
                                rhc.gridy++;
                                pe.addPropertyChangeListener(new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent evt)
                                    {
                                        try {
                                            write.invoke(bean,
                                                         new Object[] { pe.getValue() });
                                        } catch (IllegalAccessException e) {
                                            LOG.error("Unable to set property " + pd.getDisplayName(), e);
                                        } catch (InvocationTargetException e) {
                                            LOG.error("Unable to set property " + pd.getDisplayName(), e);
                                        }
                                    }
                                });
                            }
                        } else if(pe.isPaintable()) {
                            // how do I handle this?
                            LOG.info("Paintable - ignoring for now");
                        } else {
                            // try to use plain text view I guess
                            String[] vals = pe.getTags();
                            if(vals != null) {
                                // we have a list of legal items
                                LOG.info("One of several options");
                                final JComboBox options = new JComboBox(vals);
                                options.setEditable(false);
                                options.addItemListener(new ItemListener() {
                                    public void itemStateChanged(ItemEvent ie)
                                    {
                                        try {
                                            pe.setAsText((String) options.getSelectedItem());
                                            write.invoke(bean,
                                                         new Object[] { pe.getValue() });
                                        } catch (IllegalAccessException e) {
                                            LOG.error("Unable to set property " + pd.getDisplayName(), e);
                                        } catch (InvocationTargetException e) {
                                            LOG.error("Unable to set property " + pd.getDisplayName(), e);
                                        }
                                    }
                                });
                                add(new JLabel(pd.getDisplayName()), lhc);
                                add(options, rhc);
                                lhc.gridy++;
                                rhc.gridy++;
                            } else {
                                // we have to work with the raw text
                                LOG.info("Text input");
                                final JTextField input = new JTextField(pe.getAsText());
                                input.setEditable(true);
                                input.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent ae)
                                    {
                                        try {
                                            pe.setAsText(input.getText());
                                            write.invoke(bean,
                                                         new Object[]{pe.getValue()});
                                        } catch (IllegalAccessException e) {
                                            LOG.error("Unable to set property " + pd.getDisplayName(), e);
                                        } catch (InvocationTargetException e) {
                                            LOG.error("Unable to set property " + pd.getDisplayName(), e);
                                        }
                                    }
                                });
                                add(new JLabel(pd.getDisplayName()), lhc);
                                add(input, rhc);
                                lhc.gridy++;
                                rhc.gridy++;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            setLayout(new BorderLayout());
            add(new JLabel("Unable to edit bean"), BorderLayout.CENTER);
        }
    }

    public static class Editor
            extends PropertyEditorSupport
    {
        final JPanel child;

        public Editor()
        {
            LOG.info("Creating new PropertySheet.Editor");
            child = new JPanel();
            child.setLayout(new BorderLayout());
        }

        public boolean supportsCustomEditor()
        {
            return true;
        }

        public Component getCustomEditor()
        {
            return child;
        }

        public void setValue(Object value)
        {
            super.setValue(value);

            LOG.info("Seting value in PropertySheet.Editor to " + value);

            child.removeAll();
            child.add(new PropertySheet(value), BorderLayout.CENTER);
        }
    }
}
