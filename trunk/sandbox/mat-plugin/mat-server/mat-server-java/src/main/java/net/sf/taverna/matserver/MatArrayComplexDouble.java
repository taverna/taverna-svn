package net.sf.taverna.matserver;

/**
 * Array of complex doubles.
 * 
 * @author petarj
 */
public class MatArrayComplexDouble extends MatArray
{

    /** real parts */
    private double[] re;
    /** imaginary parts */
    private double[] im;

    public MatArrayComplexDouble()
    {
        type = MatType.COMPLEX_DOUBLE;
    }
}
