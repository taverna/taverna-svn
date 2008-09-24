package net.sf.taverna.dalec.configurator;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA. User: Tony Burdett Date: 04-Sep-2005 Time: 12:44:22 To change this template use File |
 * Settings | File Templates.
 */
public class DalecModel
{
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String MAPMASTER = "mapMaster";
    public static final String XSCUFLFILE = "xscuflFile";
    public static final String DBLOCATION = "sequenceDBLocation";

    public static final String VERSION = "version";
    public static final String STYLESHEET = "stylesheet";

    private Map attribs;

    public DalecModel()
    {
        attribs = new HashMap();
        // set attributes for dalec which don't change & don't need configuring
        attribs.put(VERSION, "default");
        attribs.put(STYLESHEET, "dalec.style");
    }

    public void setName(String name)
    {
        attribs.put(NAME, name);
    }

    public void setDescription(String description)
    {
        attribs.put(DESCRIPTION, description);
    }

    public void setMapMaster(String mapMaster)
    {
        attribs.put(MAPMASTER, mapMaster);
    }

    public void setXScuflFile(String xscuflFile)
    {
        attribs.put(XSCUFLFILE, xscuflFile);
    }

    public void setDBLocation(String sequenceDBLocation)
    {
        attribs.put(DBLOCATION, sequenceDBLocation);
    }

    public Map getAttributes()
    {
        return attribs;
    }
}
