package net.sf.taverna.matlabactivity.matserver.api;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author petarj
 */
public class MatArray implements Serializable {

    private static final long serialVersionUID = 1937488594685810458L;
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
    private String type = UNKNOWN_TYPE;
    private int maxNonZero;
    //public int NNonZero;
    private int[] dims;
    /** row indices of nonzero elements */
    private int[] rowIds;
    /** column indexing info for nonzero elements */
    private int[] colIds;
    /** real parts */
    private double[] doubleDataRe;
    /** imaginary parts */
    private double[] doubleDataIm;
    private float[] singleDataRe;
    private byte[] int8DataRe;
    private short[] int16DataRe;
    private int[] int32DataRe;
    private long[] int64DataRe;
    private String[] charData;
    private boolean[] logicalData;
    private MatArray[] cellData;
    private String[] fieldNames;
    //TODO functionHandles[]...
    public MatArray() {
    }

    public int[] getDimensions() {
        return dims;
    }

    public void setDimensions(int[] dimensions) {
        this.dims = dimensions;
    }

    public MatArray[] getCellData() {
        return cellData;
    }

    public void setCellData(MatArray[] cellData) {
        this.cellData = cellData;
    }

    public String[] getCharData() {
        return charData;
    }

    public void setCharData(String[] charData) {
        this.charData = charData;
    }

    public double[] getDoubleDataIm() {
        return doubleDataIm;
    }

    public void setDoubleDataIm(double[] doubleDataIm) {
        this.doubleDataIm = doubleDataIm;
    }

    public double[] getDoubleDataRe() {
        return doubleDataRe;
    }

    public void setDoubleDataRe(double[] doubleDataRe) {
        this.doubleDataRe = doubleDataRe;
    }

    public int[] getDims() {
        return dims;
    }

    public void setDims(int[] dims) {
        this.dims = dims;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

    public short[] getInt16DataRe() {
        return int16DataRe;
    }

    public void setInt16DataRe(short[] int16DataRe) {
        this.int16DataRe = int16DataRe;
    }

    public int[] getInt32DataRe() {
        return int32DataRe;
    }

    public void setInt32DataRe(int[] int32DataRe) {
        this.int32DataRe = int32DataRe;
    }

    public long[] getInt64DataRe() {
        return int64DataRe;
    }

    public void setInt64DataRe(long[] int64DataRe) {
        this.int64DataRe = int64DataRe;
    }

    public byte[] getInt8DataRe() {
        return int8DataRe;
    }

    public void setInt8DataRe(byte[] int8DataRe) {
        this.int8DataRe = int8DataRe;
    }

    public float[] getSingleDataRe() {
        return singleDataRe;
    }

    public void setSingleDataRe(float[] singleDataRe) {
        this.singleDataRe = singleDataRe;
    }

    public boolean[] getLogicalData() {
        return logicalData;
    }

    public void setLogicalData(boolean[] logicalData) {
        this.logicalData = logicalData;
    }

    public int[] getRowIds() {
        return rowIds;
    }

    public void setRowIds(int[] rowIds) {
        this.rowIds = rowIds;
    }

    public boolean checkSparse() {
        return rowIds != null;
    }

    public int[] getColIds() {
        return colIds;
    }

    public void setColIds(int[] colIds) {
        this.colIds = colIds;
    }

    public int getMaxNonZero() {
        return maxNonZero;
    }

    public void setMaxNonZero(int maxNonZero) {
        this.maxNonZero = maxNonZero;
    }

    public int getNNonZero() {
        if (colIds == null) {
            return 0;
        }
        return colIds[colIds.length - 1];
    }

    public void setNNonZero(int nNonZero) {
        if (colIds != null) {
            this.colIds[colIds.length - 1] = nNonZero;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MatArray getField(String name, int idx) {
        int j = -1;
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNames[i].equals(name)) {
                j = i;
                break;
            }
        }
        if (j != -1) {
            return cellData[j].cellData[idx]; //XXX Ugly as hell, think about it...
        }
        return null;
    }

    public int numberOfElements() {
        if (dims != null) {
            int n = 1;
            for (int i = 0; i < dims.length; i++) {
                n *= dims[i];
            }
            return n;
        }
        return 0;
    }

    @Override
    public boolean equals(Object arg) {
        boolean eq;
        MatArray ma = (MatArray) arg;

        eq = type.equals(ma.getType()) && Arrays.equals(dims, ma.getDimensions()) && (maxNonZero == ma.getMaxNonZero()) && Arrays.equals(rowIds, ma.getRowIds()) && Arrays.equals(colIds, ma.getColIds()) && Arrays.deepEquals(cellData, ma.getCellData()) && Arrays.equals(charData, ma.getCharData()) && Arrays.equals(logicalData, ma.getLogicalData()) && Arrays.equals(singleDataRe, singleDataRe) && Arrays.equals(int8DataRe, ma.getInt8DataRe()) && Arrays.equals(int16DataRe, ma.getInt16DataRe()) && Arrays.equals(int32DataRe, ma.getInt32DataRe()) && Arrays.equals(int64DataRe, ma.getInt64DataRe()) && Arrays.deepEquals(fieldNames, ma.getFieldNames());

        if (eq && (doubleDataRe != null)) {
            if (colIds != null) {
                for (int i = 0; i < getNNonZero(); i++) {
                    if (doubleDataRe[i] != ma.getDoubleDataRe()[i]) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < numberOfElements(); i++) {
                    if (doubleDataRe[i] != ma.getDoubleDataRe()[i]) {
                        return false;
                    }
                }
            }
        }
        if (eq && (doubleDataIm != null)) {
            if (colIds != null) {
                for (int i = 0; i < getNNonZero(); i++) {
                    if (doubleDataIm[i] != ma.getDoubleDataIm()[i]) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < numberOfElements(); i++) {
                    if (doubleDataIm[i] != ma.getDoubleDataIm()[i]) {
                        return false;
                    }
                }
            }

        }
        return eq;
    }

    @Override
    public String toString() {
        StringBuffer rep = new StringBuffer();
        rep.append("type: " + type + "\n");
        rep.append("dimensions: " + Arrays.toString(dims) + "\n");
        rep.append("row ids: " + Arrays.toString(rowIds) + "\n");
        rep.append("col. ids: " + Arrays.toString(colIds) + "\n");

        rep.append("MaxNonZero: " + maxNonZero + "\n");
        rep.append("char data: " + Arrays.toString(charData) + "\n");
        rep.append("cell data: " + Arrays.deepToString(cellData) + "\n");
        rep.append("logical data: " + Arrays.toString(logicalData) + "\n");
        rep.append("double im: " + Arrays.toString(doubleDataIm) + "\n");
        rep.append("single data: " + Arrays.toString(singleDataRe) + "\n");
        rep.append("int8 data: " + Arrays.toString(int8DataRe) + "\n");
        rep.append("int16 data: " + Arrays.toString(int16DataRe) + "\n");
        rep.append("int32 data: " + Arrays.toString(int32DataRe) + "\n");
        rep.append("int64 data: " + Arrays.toString(int64DataRe) + "\n");
        rep.append("field names: " + Arrays.deepToString(fieldNames) + "\n");
        rep.append("double re: " + Arrays.toString(doubleDataRe) + "\n");

        return rep.toString();
    }
    //<editor-fold desc="type checking methods" defaultstate="collapsed">
    public boolean checkNumeric() {
        return (type.equals(DOUBLE_TYPE) || type.equals(SINGLE_TYPE) ||
                type.equals(INT8_TYPE) || type.equals(INT16_TYPE) ||
                type.equals(INT32_TYPE) || type.equals(INT64_TYPE) ||
                type.equals(UINT8_TYPE) || type.equals(UINT16_TYPE) ||
                type.equals(UINT32_TYPE) || type.equals(UINT64_TYPE));
    }

    public boolean checkComplex() {
        return doubleDataIm != null;
    }

    public boolean checkChar() {
        return type.equals(CHAR_TYPE);
    }

    public boolean checkCell() {
        return type.equals(CELL_TYPE);
    }

    public boolean checkStruct() {
        return type.equals(STRUCT_TYPE);
    }
    //</editor-fold>
}
