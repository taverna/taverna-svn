/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.matserver;

/**
 *
 * @author user
 */
public class SparseMatArrayComplexDouble extends SparseMatArray
{

    /** real parts */
    double[] re;
    /** imaginary parts */
    double[] im;

    public SparseMatArrayComplexDouble()
    {
        type = MatType.SPARSE_COMPLEX_DOUBLE;
    }
}
