package org.embl.ebi.escience.scuflui.facets;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.beans.*;
import java.awt.*;

/**
 * Extract matches to a regular expression. The details of what to do with the
 * regular expression are encapsulated in the Scanner objects.
 *
 * @see Scanner
 *
 * @author Matthew Pocock
 */
public class RegexScanner
        implements FacetFinderSPI
{
    private static final int PATTERN_FLAGS = Pattern.MULTILINE & Pattern.DOTALL;

    private static final DataThing EMPTY_STRING = DataThingFactory.bake("");

    static
    {
        // register editor for Pattern
        if(PropertyEditorManager.findEditor(Pattern.class) == null) {
            PropertyEditorManager.registerEditor(Pattern.class,
                                                 PatternEditor.class);
        }
    }

    public boolean canMakeFacets(DataThing dataThing)
    {
        return dataThing.getDataObject() instanceof CharSequence;
    }

    public Set getStandardColumns(DataThing dataThing)
    {
        return Collections.EMPTY_SET;
    }

    public boolean hasColumn(ColumnID colID)
    {
        return colID instanceof Scanner;
    }

    public ColumnID newColumn(DataThing dataThing)
    {
        return new Scanner();
    }

    public DataThing getFacet(DataThing dataThing, ColumnID colID)
    {
        if(!hasColumn(colID) || !canMakeFacets(dataThing)) {
            return null;
        }

        Scanner scanner = (Scanner) colID;
        CharSequence chars = (CharSequence) dataThing.getDataObject();
        Matcher matcher = scanner.getPattern().matcher(chars);

        if(!scanner.joinValues) {
            if(scanner.makeCollection) {
                List hits = new ArrayList();
                while(matcher.find()) {
                    hits.add(matcher.group(scanner.getGroup()));
                }
                return DataThingFactory.bake(hits);
            } else {
                if(!matcher.find()) {
                    return EMPTY_STRING;
                }

                return DataThingFactory.bake(matcher.group(scanner.getGroup()));
            }
        } else {
            if(!matcher.find()) {
                return EMPTY_STRING;
            }

            StringBuffer res = new StringBuffer();
            res.append(matcher.group(scanner.getGroup()));

            while(matcher.find()) {
                res.append(scanner.getJoinText());
                res.append(matcher.group(scanner.getGroup()));
            }

            return DataThingFactory.bake(res.toString());
        }
    }

    public String getName()
    {
        return "RegexScanner";
    }

    /**
     * A column that describes how to turn regular expression matches into
     * facets.
     * <p>
     * In the case of no matches, the empty string is returned. If the
     * joinValues option is enabled, then all matches are concatenated into a
     * single string, seperated by the value of joinText. If joinValues is not
     * enabled, then makeCollections is considered. When makeCollections is
     * enabled, a List containing every match is returned. If it is not enabled,
     * then the first match only is returned.
     *
     * @author Matthew Pocock
     */
    public static class Scanner
            implements ColumnID
    {
        static
        {
            PropertyEditorManager.registerEditor(
                    Scanner.class, PropertySheet.Editor.class);
        }

        private final PropertyChangeSupport pcs
                = new PropertyChangeSupport(this);
        private Pattern pattern;
        private int group;
        private boolean joinValues;
        private String joinText;
        private boolean makeCollection;

        public Scanner()
        {
            pattern = Pattern.compile(".*", PATTERN_FLAGS);
            group = 0;
            joinValues = false;
            joinText = "";
            makeCollection = false;
        }

        public Scanner(Pattern pattern, int group, String joinText)
        {
            this.pattern = pattern;
            this.group = group;
            this.joinValues = true;
            this.joinText = joinText;
            this.makeCollection = false;
        }

        public Scanner(Pattern pattern, int group, boolean makeCollection)
        {
            this.pattern = pattern;
            this.group = group;
            this.joinValues = false;
            this.joinText = "";
            this.makeCollection = makeCollection;
        }


        public void addPropertyChangeListener(
                PropertyChangeListener listener)
        {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(
                PropertyChangeListener listener)
        {
            pcs.removePropertyChangeListener(listener);
        }

        public void addPropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener)
        {
            pcs.addPropertyChangeListener(propertyName, listener);
        }

        public void removePropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener)
        {
            pcs.removePropertyChangeListener(propertyName, listener);
        }

        public Pattern getPattern()
        {
            return pattern;
        }

        public void setPattern(Pattern pattern)
        {
            if(pattern == null) {
                throw new NullPointerException("Can't set pattern to null");
            }

            PropertyChangeEvent pce = new PropertyChangeEvent(
                    this, "pattern", this.pattern, pattern);
            this.pattern = pattern;
            pcs.firePropertyChange(pce);
        }

        public int getGroup()
        {
            return group;
        }

        public void setGroup(int group)
        {
            PropertyChangeEvent pce = new PropertyChangeEvent(
                    this, "group", new Integer(this.group), new Integer(group));
            this.group = group;
            pcs.firePropertyChange(pce);
        }

        public boolean getJoinValues()
        {
            return joinValues;
        }

        public void setJoinValues(boolean joinValues)
        {
            PropertyChangeEvent pce = new PropertyChangeEvent(
                    this, "joinValues",
                    new Boolean(this.joinValues), new Boolean(joinValues));
            this.joinValues = joinValues;
            pcs.firePropertyChange(pce);
        }

        public String getJoinText()
        {
            return joinText;
        }

        public void setJoinText(String joinText)
        {
            if(joinText == null) {
                throw new NullPointerException(
                        "Can't set join text to null. " +
                        "Use the empty string instead");
            }

            PropertyChangeEvent pce = new PropertyChangeEvent(
                    this, "joinText", this.joinText, joinText);
            this.joinText = joinText;
            pcs.firePropertyChange(pce);
        }

        public boolean getMakeCollection()
        {
            return makeCollection;
        }

        public void setMakeCollection(boolean makeCollection)
        {
            PropertyChangeEvent pce = new PropertyChangeEvent(
                    this, "makeCollection",
                    new Boolean(this.makeCollection),
                    new Boolean(makeCollection));
            this.makeCollection = makeCollection;
            pcs.firePropertyChange(pce);
        }

        public Component getCustomiser(DataThing dataThing)
        {
            PropertyEditor editor = PropertyEditorManager.findEditor(
                    Scanner.class);
            if(editor != null) {
                editor.setValue(this);
                return editor.getCustomEditor();
            } else {
                return null;
            }
        }

        public String getName()
        {
            if(getPattern() == null) {
                return "Regex";
            } else {
                return "Regex " + getPattern().pattern();
            }
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof Scanner)) return false;

            final Scanner scanner = (Scanner) o;

            if (group != scanner.group) return false;
            if (joinValues != scanner.joinValues) return false;
            if (makeCollection != scanner.makeCollection) return false;
            if (!joinText.equals(scanner.joinText)) return false;
            if (!pattern.equals(scanner.pattern)) return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = pattern.hashCode();
            result = 29 * result + group;
            result = 29 * result + (joinValues ? 1 : 0);
            result = 29 * result + joinText.hashCode();
            result = 29 * result + (makeCollection ? 1 : 0);
            return result;
        }
    }

    public static final class PatternEditor
            implements PropertyEditor
    {
        private final PropertyChangeSupport pcs;
        private Pattern pattern;

        public PatternEditor()
        {
            this.pcs = new PropertyChangeSupport(this);
        }

        public void setValue(Object value)
        {
            Pattern oldPattern = pattern;
            pattern = (Pattern) value;
            pcs.firePropertyChange(null, oldPattern, pattern);
        }

        public Object getValue()
        {
            return pattern;
        }

        public boolean isPaintable()
        {
            return false;
        }

        public void paintValue(Graphics gfx, Rectangle box)
        {
            return;
        }

        public String getJavaInitializationString()
        {
            return "Pattern.compile(\""
                    + pattern.pattern() + "\", "
                    + PATTERN_FLAGS + ")";
        }

        public String getAsText()
        {
            return pattern.pattern();
        }

        public void setAsText(String text) throws IllegalArgumentException
        {
            setValue(Pattern.compile(text, PATTERN_FLAGS));
        }

        public String[] getTags()
        {
            return null;
        }

        public Component getCustomEditor()
        {
            return null;
        }

        public boolean supportsCustomEditor()
        {
            return false;
        }

        public void addPropertyChangeListener(
                    PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(
                    PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }
}
