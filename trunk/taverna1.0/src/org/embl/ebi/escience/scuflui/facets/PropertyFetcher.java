package org.embl.ebi.escience.scuflui.facets;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.Set;
import java.util.Collections;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 *
 *
 * @author Matthew Pocock
 */
public class PropertyFetcher
        implements FacetFinderSPI
{
    public boolean canMakeFacets(DataThing dataThing)
    {
        return true;
    }

    public Set getStandardColumns()
    {
        return Collections.EMPTY_SET;
    }

    public boolean hasColumn(Object colID)
    {
        return colID instanceof Closure;
    }

    public DataThing getFacet(DataThing dataThing, Object colID)
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
    {
        private Method method;
        private Object[] args;

        public Closure()
        {
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
    }
}
