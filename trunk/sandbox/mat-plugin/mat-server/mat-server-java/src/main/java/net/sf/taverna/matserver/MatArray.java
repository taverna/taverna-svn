package net.sf.taverna.matserver;

/**
 *
 * @author petarj
 */
public abstract class MatArray
{

    protected MatType type;
    protected int[] dimensions;

    public int[] getDimensions()
    {
        return dimensions;
    }

    public MatType getType()
    {
        return type;
    }
}
