/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.matserver;

/**
 *
 * @author user
 */
public class SparseMatArray extends MatArray
{

    private int nzmax;
    private int nnz;
    /** indices of corresponding nonzero elements in data array */
    protected int[] ir;
    /** 
     * Indices of first nonzero elements in columns. 
     * Used for indexing ir array. */
    protected int[] jc;
}
