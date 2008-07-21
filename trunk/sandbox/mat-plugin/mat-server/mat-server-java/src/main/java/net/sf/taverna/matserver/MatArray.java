package net.sf.taverna.matserver;

/**
 *
 * @author petarj
 */
public class MatArray {

    public static final String UNKNOWN_TYPE = "UNKNOWN";
    public static final String STRUCT_TYPE = "STRUCT";
    public static final String CELL_TYPE = "CELL";
    public static final String LOGICAL_TYPE = "LOGICAL";
    public static final String CHAR_TYPE = "CHAR";
    public static final String DOUBLE_TYPE = "DOUBLE";
    public static final String SINGLE_TYPE = "SINGLE";
    public static final String INT8_TYPE = "INT8";
    public static final String UINT8_TYPE = "UINT8";
    public static final String INT16_TYPE = "INT16";
    public static final String UINT16_TYPE = "UINT16";
    public static final String INT32_TYPE = "INT32";
    public static final String UINT32_TYPE = "UINT32";
    public static final String INT64_TYPE = "INT64";
    public static final String UINT64_TYPE = "UINT64";
    public static final String FUNCTION_TYPE = "FUNCTION";
    String type = UNKNOWN_TYPE;
    int maxNonZero;
    int nNonZero;
    int[] dims;
    /** row indices of nonzero elements */
    int[] rowIds;
    /** column indexing info for nonzero elements */
    int[] colIds;
    /** real parts */
    double[] data_re;
    /** imaginary parts */
    double[] data_im;
    String[] char_data;
    boolean[] logical_data;
    MatArray[] cell_data;
    //TODO functionHandles[]...
    public MatArray() {
    }

    public int[] getDimensions() {
        return dims;
    }

    public void setDimensions(int[] dimensions) {
        this.dims = dimensions;
    }

    public MatArray[] getCell_data() {
        return cell_data;
    }

    public void setCell_data(MatArray[] cell_data) {
        this.cell_data = cell_data;
    }

    public String[] getChar_data() {
        return char_data;
    }

    public void setChar_data(String[] char_data) {
        this.char_data = char_data;
    }

    public double[] getData_im() {
        return data_im;
    }

    public void setData_im(double[] data_im) {
        this.data_im = data_im;
    }

    public double[] getData_re() {
        return data_re;
    }

    public void setData_re(double[] data_re) {
        this.data_re = data_re;
    }

    public int[] getRowIds() {
        return rowIds;
    }

    public void setRowIds(int[] rowIds) {
        this.rowIds = rowIds;
    }

    public boolean isSparse() {
        return rowIds != null;
    }

    public int[] getColIds() {
        return colIds;
    }

    public void setColIds(int[] colIds) {
        this.colIds = colIds;
    }

    public boolean[] getLogical_data() {
        return logical_data;
    }

    public void setLogical_data(boolean[] logical_data) {
        this.logical_data = logical_data;
    }

    public int getMaxNonZero() {
        return maxNonZero;
    }

    public void setMaxNonZero(int maxNonZero) {
        this.maxNonZero = maxNonZero;
    }

    public int getNNonZero() {
        return colIds[colIds.length - 1];
    }

    public void setNNonZero(int nNonZero) {
        this.colIds[colIds.length - 1] = nNonZero;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    //<editor-fold desc="type checking methods" defaultstate="collapsed">
    public boolean isNumeric() {
        return (type.equals(DOUBLE_TYPE) || type.equals(SINGLE_TYPE) ||
                type.equals(INT8_TYPE) || type.equals(INT16_TYPE) ||
                type.equals(INT32_TYPE) || type.equals(INT64_TYPE) ||
                type.equals(UINT8_TYPE) || type.equals(UINT16_TYPE) ||
                type.equals(UINT32_TYPE) || type.equals(UINT64_TYPE));
    }

    public boolean isComplex() {
        return data_im != null;
    }

    public boolean isChar() {
        return type.equals(CHAR_TYPE);
    }

    public boolean isCell() {
        return type.equals(CELL_TYPE);
    }

    public boolean isLogical() {
        return type.equals(LOGICAL_TYPE);
    }

    public boolean isDouble() {
        return type.equals(DOUBLE_TYPE);
    }

    public boolean isSingle() {
        return type.equals(SINGLE_TYPE);
    }

    public boolean isInt8() {
        return type.equals(INT8_TYPE);
    }

    public boolean isInt16() {
        return type.equals(INT16_TYPE);
    }

    public boolean isInt32() {
        return type.equals(INT32_TYPE);
    }

    public boolean isInt64() {
        return type.equals(INT64_TYPE);
    }

    public boolean isUint8() {
        return type.equals(UINT8_TYPE);
    }

    public boolean isUint16() {
        return type.equals(UINT16_TYPE);
    }

    public boolean isUint32() {
        return type.equals(UINT32_TYPE);
    }

    public boolean isUint64() {
        return type.equals(UINT64_TYPE);
    }
    //</editor-fold>
}
