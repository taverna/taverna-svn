package org.embl.ebi.escience.scuflui.facets;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Set;
import java.util.Collections;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 *
 *
 * @author Matthew Pocock
 */
public class PropertyFetcher
        implements FacetFinderSPI
{
    private static final Logger LOG = Logger.getLogger(PropertyFetcher.class);

    public boolean canMakeFacets(DataThing dataThing)
    {
        return true;
    }

    public Set getStandardColumns(DataThing dataThing)
    {
        return Collections.EMPTY_SET;
    }

    public boolean hasColumn(ColumnID colID)
    {
        if(!(colID instanceof Closure)) {
            return false;
        }

        Closure closure = (Closure) colID;
        if(closure.getArgs() == null || closure.getMethod() == null) {
            return false;
        }

        return true;
    }

    public FacetFinderSPI.ColumnID newColumn(DataThing dataThing)
    {
        return new Closure();
    }

    public DataThing getFacet(DataThing dataThing, ColumnID colID)
    {
        if(!hasColumn(colID)) {
            return null;
        }

        Closure closure = (Closure) colID;
        try {
            return DataThingFactory.bake(
                    closure.getMethod().invoke(
                            dataThing.getDataObject(), closure.getArgs()));
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        }
    }

    public String getName()
    {
        return "Properties";
    }

    public static final class Closure
            implements ColumnID
    {
        private Method method;
        private Object[] args;

        public Closure()
        {
            try {
                this.method = Object.class.getMethod("toString", new Class[] {});
            } catch (NoSuchMethodException e) {
                throw new AssertionError("toString is missing from Object!");
            }
            args = new Object[] {};
        }

        public Closure(Method method, Object[] args)
        {
            this.method = method;
            this.args = args;
        }

        public Method getMethod()
        {
            return method;
        }

        public void setMethod(Method method)
        {
            this.method = method;
        }

        public Object[] getArgs()
        {
            return args;
        }

        public void setArgs(Object[] args)
        {
            this.args = args;
        }

        public Component getCustomiser(DataThing dataThing)
        {
            try {
                Box editor = Box.createVerticalBox();
                CheckboxGroup cbg = new CheckboxGroup();
                BeanInfo beanInfo = Introspector.getBeanInfo(
                        dataThing.getDataObject().getClass());
                PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
                for(int i = 0; i < props.length; i++) {
                    final PropertyDescriptor prop = props[i];
                    Checkbox cb = new Checkbox(prop.getName(), false, cbg);
                    cb.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e)
                        {
                            if (e.getStateChange() == e.SELECTED) {
                                LOG.info("Selecting property " + prop + " with read method " + prop.getReadMethod());
                                setMethod(prop.getReadMethod());
                            }
                        }
                    });
                    editor.add(cb);
                }

                return editor;
            } catch (IntrospectionException e) {
                return null;
            }
        }

        public String getName()
        {
            if(getMethod() == null) {
                return "Property";
            } else {
              return "Property(" + getMethod().getName() + ")";
            }
        }
    }
}
