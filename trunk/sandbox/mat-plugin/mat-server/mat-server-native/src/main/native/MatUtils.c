#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "matrix.h"
#include "engine.h"

#include "MatUtils.h"

static jobject createJCellArray(JNIEnv* env);

/* Java to Matlab conversion functions */
mxArray* jtomArray(JNIEnv* env, jobject matarray) {
    mxArray* mxarr = NULL;
    mxClassID mxarr_class;
    jboolean isNumeric, isChar;
    jstring typeName;

    typeName = (jstring) (*env)->GetObjectField(env, matarray, matarray_typeFID);

    mxarr_class = getMxClassID(env, matarray);

    switch (mxarr_class) {
        case mxDOUBLE_CLASS:
            mxarr = jtomDoubleArray(env, matarray);
            break;
        case mxSINGLE_CLASS:
            mxarr = jtomSingleArray(env, matarray);
            break;
        case mxCHAR_CLASS:
            mxarr = jtomCharArray(env, matarray);
            break;
        case mxCELL_CLASS:
            mxarr = jtomCellArray(env, matarray);
            break;
        case mxLOGICAL_CLASS:
            mxarr = jtomLogicalArray(env, matarray);
            break;
        case mxSTRUCT_CLASS:
            mxarr = jtomStructArray(env, matarray);
            break;
        case mxINT8_CLASS:
            mxarr = jtomInt8Array(env, matarray);
            break;
        case mxUINT8_CLASS:
            mxarr = jtomUint8Array(env, matarray);
            break;
        case mxINT16_CLASS:
            mxarr = jtomInt16Array(env, matarray);
            break;
        case mxUINT16_CLASS:
            mxarr = jtomUint16Array(env, matarray);
            break;
        case mxINT32_CLASS:
            mxarr = jtomInt32Array(env, matarray);
            break;
        case mxUINT32_CLASS:
            mxarr = jtomUint32Array(env, matarray);
            break;
        case mxINT64_CLASS:
            mxarr = jtomInt64Array(env, matarray);
            break;
        case mxUINT64_CLASS:
        case mxFUNCTION_CLASS:
        default: /*mxUNKNOWN_CLASS and others*/
            mxarr = NULL;
            break;
    }
    return mxarr;
}

mxArray* jtomCharArray(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    int len;
    int ndims;
    int *dims;
    jintArray jdims;
    char** data;
    jobjectArray jdata;
    int i;

    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL)
        return NULL;
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);

    jdata = (jobjectArray) (*env)->GetObjectField(env, matarray, matarray_char_dataFID);
    len = (*env)->GetArrayLength(env, jdata);
    data = (char**) mxCalloc(len, sizeof (char*));
    for (i = 0; i < len; i++) {
        jstring jstr = (*env)->GetObjectArrayElement(env, jdata, i);
        int jstrlen = (*env)->GetStringLength(env, jstr);
        data[i] = (char*) mxCalloc(jstrlen + 1, sizeof (char));
        (*env)->GetStringUTFRegion(env, jstr, 0, jstrlen, data[i]);
        (*env)->DeleteLocalRef(env, jstr);
    }

    mxarr = mxCreateCharMatrixFromStrings(dims[0], data);

    return mxarr;
}

mxArray* jtomCellArray(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    int nelements;
    jobjectArray jdata;
    int ndims;
    int* dims;
    jintArray jdims;
    int i;

    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxMalloc(ndims * sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateCellArray(ndims, dims);
    if (mxarr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    jdata = (jobjectArray) (*env)->GetObjectField(env, matarray, matarray_cell_dataFID);
    nelements = (*env)->GetArrayLength(env, jdata);
    for (i = 0; i < nelements; i++) {
        jobject tmpMA = (*env)->GetObjectArrayElement(env, jdata, i);
        mxArray* tmpMX = jtomArray(env, tmpMA);
        mxSetCell(mxarr, i, tmpMX);
    }
    (*env)->DeleteLocalRef(env, jdata);
    return mxarr;
}

mxArray* jtomLogicalArray(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    int nelements;
    mxLogical* data;
    int ndims, i;
    int* dims;
    jintArray jdims;
    jbooleanArray jdata;
    jboolean *jbs, isSparse;

    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxMalloc(ndims * sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    jdata = (jbooleanArray) (*env)->GetObjectField(env, matarray, matarray_logical_dataFID);
    nelements = (*env)->GetArrayLength(env, jdata);

    isSparse = (*env)->CallBooleanMethod(env, matarray, matarray_isSparseMID);
    if (isSparse) {
        int *ir, *jc;
        jintArray jir, jjc;
        int ncolumns, nrows;
        int len;

        nrows = dims[0];
        ncolumns = dims[1];

        if (ndims != 2) {
            setError(DIMENSIONS_ERROR);
            return NULL;
        }
        mxarr = mxCreateSparseLogicalMatrix(dims[0], dims[1], nelements);

        jir = (jintArray) (*env)->GetObjectField(env, matarray, matarray_rowIdsFID);
        len = (*env)->GetArrayLength(env, jir);
        ir = (int*) mxCalloc(len, sizeof (int));
        if (ir == NULL) {
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }
        (*env)->GetIntArrayRegion(env, jir, 0, len, ir);
        (*env)->DeleteLocalRef(env, jir);
        mxSetIr(mxarr, ir);

        jjc = (jintArray) (*env)->GetObjectField(env, matarray, matarray_colIdsFID);
        len = (*env)->GetArrayLength(env, jjc);
        jc = (int*) mxCalloc(len, sizeof (int));
        if (jc == NULL) {
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }
        (*env)->GetIntArrayRegion(env, jjc, 0, len, jc);
        (*env)->DeleteLocalRef(env, jjc);
        mxSetJc(mxarr, jc);
    } else
        mxarr = mxCreateLogicalArray(ndims, dims);

    data = (mxLogical*) mxCalloc(nelements, sizeof (mxLogical));
    if (data == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    jbs = (jboolean*) malloc(nelements * sizeof (jboolean));
    if (jbs == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetBooleanArrayRegion(env, jdata, 0, nelements, jbs);
    (*env)->DeleteLocalRef(env, jdata);

    for (i = 0; i < nelements; i++)
        data[i] = jbs[i] ? 1 : 0;
    mxSetData(mxarr, data);

    return mxarr;
}

mxArray* jtomStructArray(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    jobjectArray jfieldnames;
    char** fieldnames;
    int ndims, nfields, i, j, nelements;
    int* dims;
    jintArray jdims;
    jobjectArray jdata;

    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    jfieldnames = (jobjectArray) (*env)->GetObjectField(env, matarray, matarray_field_namesFID);
    if (jfieldnames == NULL)
        return NULL;
    nfields = (*env)->GetArrayLength(env, jfieldnames);
    fieldnames = (char**) calloc(nfields, sizeof (char*));
    if (fieldnames == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    for (i = 0; i < nfields; i++) {
        jstring jfname = (jstring) (*env)->GetObjectArrayElement(env, jfieldnames, i);
        int len = (*env)->GetStringLength(env, jfname);

        fieldnames[i] = (char*) calloc(len + 1, sizeof (char));
        if (fieldnames[i] == NULL) {
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }
        (*env)->GetStringUTFRegion(env, jfname, 0, len, fieldnames[i]);
        fieldnames[i][len] = '\0';
        (*env)->DeleteLocalRef(env, jfname);
    }
    /*
        (*env)->DeleteLocalRef(env, jfieldnames);
     */

    mxarr = mxCreateStructArray(ndims, dims, nfields, fieldnames);

    jdata = (jobjectArray) (*env)->GetObjectField(env, matarray, matarray_cell_dataFID);
    /*TODO: mxSetFieldByNumber()...*/
    nelements = (*env)->CallIntMethod(env, matarray, matarray_getNumberOfElementsMID);
    for (i = 0; i < nfields; i++) {
        jobject fieldData;

        fieldData = (*env)->GetObjectArrayElement(env, jdata, i);
        if (fieldData == NULL)
            continue;
        if ((*env)->CallBooleanMethod(env, fieldData, matarray_isCellMID)) {
            for (j = 0; j < nelements; j++) {
                jobject tmpMA;
                mxArray* tmpMX;

                tmpMA = (*env)->CallObjectMethod(env, matarray, matarray_getFieldMID, (jstring) (*env)->GetObjectArrayElement(env, jfieldnames, i), j);

                if (tmpMA == NULL) {
                    (*env)->DeleteLocalRef(env, tmpMA);
                    continue;
                }

                tmpMX = jtomArray(env, tmpMA);
                mxSetField(mxarr, j, fieldnames[i], tmpMX);
                (*env)->DeleteLocalRef(env, tmpMA);
            }
        } else {
            mxArray* tmpMX = jtomArray(env, fieldData); /*TODO error checking*/
            for (j = 0; j < nelements; j++) {
                mxSetField(mxarr, j, fieldnames[i], tmpMX);
            }
        }
        (*env)->DeleteLocalRef(env, fieldData);
        /*
                jobject tmpStruct;

                tmpStruct = (*env)->GetObjectArrayElement(env, jdata, i);
                for (j = 0; j < nfields; j++) {
                    mxArray* tmpMX;
                    jobject tmpMA = (*env)->CallObjectMethod(env, matarray, matarray_getFieldMID, fieldnames[j], i);
                    if (tmpMA == NULL) {
                        (*env)->DeleteLocalRef(env, tmpMA);
                        continue;
                    }
                    tmpMX = jtomArray(env, tmpMA);
                    mxSetField(mxarr, i, fieldnames[j], tmpMX);
                    (*env)->DeleteLocalRef(env, tmpMA);
                }

                (*env)->DeleteLocalRef(env, tmpStruct);
         */
    }
    (*env)->DeleteLocalRef(env, jfieldnames);
    (*env)->DeleteLocalRef(env, jdata);
    free(fieldnames);

    return mxarr;
}

mxArray* jtomDoubleArray(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jboolean isSparse;
    jdoubleArray matarray_pr, matarray_pi;
    double *pr = NULL, *pi = NULL;
    int *ir = NULL, *jc = NULL;
    int matarray_nzmax;
    int len;
    jintArray jdims, matarray_ir, matarray_jc;
    int ndims;
    int *dims;

    mxclass = mxDOUBLE_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    isSparse = (*env)->CallBooleanMethod(env, matarray, matarray_isSparseMID);
    if (isSparse) {
        if (ndims > 2) {
            setError(DIMENSIONS_ERROR);
            return NULL;
        }
        matarray_nzmax = (*env)->GetIntField(env, matarray, matarray_maxNonZeroFID);
        mxarr = mxCreateSparse(dims[0], dims[1], matarray_nzmax, cplxFlag);
        matarray_ir = (jintArray) (*env)->GetObjectField(env, matarray, matarray_rowIdsFID);
        matarray_jc = (jintArray) (*env)->GetObjectField(env, matarray, matarray_colIdsFID);
        if (matarray_ir == NULL || matarray_jc == NULL) {
            return NULL;
        }

        len = (*env)->GetArrayLength(env, matarray_ir);
        ir = (int*) mxCalloc(len, sizeof (int));
        if (ir == NULL) {
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }
        (*env)->GetIntArrayRegion(env, matarray_ir, 0, len, ir);
        (*env)->DeleteLocalRef(env, matarray_ir);

        mxSetIr(mxarr, ir);

        len = (*env)->GetArrayLength(env, matarray_jc);
        jc = (int*) mxCalloc(len, sizeof (int));
        if (jc == NULL) {
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }
        (*env)->GetIntArrayRegion(env, matarray_jc, 0, len, jc);
        (*env)->DeleteLocalRef(env, matarray_jc);

        mxSetJc(mxarr, jc);
    } else {
        mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);
    }

    matarray_pr = (jdoubleArray) (*env)->GetObjectField(env, matarray, matarray_double_data_reFID);
    if (matarray_pr != NULL) {
        len = (*env)->GetArrayLength(env, matarray_pr);
        pr = (double*) mxCalloc(len, sizeof (double));
        if (pr == NULL) {
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }
        (*env)->GetDoubleArrayRegion(env, matarray_pr, 0, len, pr);
        (*env)->DeleteLocalRef(env, matarray_pr);
        mxSetPr(mxarr, pr);

        if (cplxFlag) {
            matarray_pi = (jdoubleArray) (*env)->GetObjectField(env, matarray, matarray_double_data_imFID);
            len = (*env)->GetArrayLength(env, matarray_pi);
            pi = (double*) mxCalloc(len, sizeof (double));
            if (pi == NULL) {
                setError(OUT_OF_MEMORY_ERROR);
                return NULL;
            }
            (*env)->GetDoubleArrayRegion(env, matarray_pi, 0, len, pi);
            (*env)->DeleteLocalRef(env, matarray_pi);
            mxSetPi(mxarr, pi);
        }
    }
    return mxarr;
}

mxArray* jtomSingleArray(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jfloatArray matarray_pr;
    float *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;
    void* throwAway;

    mxclass = mxSINGLE_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jfloatArray) (*env)->GetObjectField(env, matarray, matarray_single_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    pr = (float*) mxCalloc(len, sizeof (float));
    if (pr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetFloatArrayRegion(env, matarray_pr, 0, len, pr);
    (*env)->DeleteLocalRef(env, matarray_pr);

    throwAway = mxGetData(mxarr);
    mxFree(throwAway);
    mxSetData(mxarr, (void*) pr);

    return mxarr;
}

mxArray* jtomInt8Array(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jbyteArray matarray_pr;
    char *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;

    mxclass = mxINT8_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jbyteArray) (*env)->GetObjectField(env, matarray, matarray_int8_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    pr = (char*) mxCalloc(len, sizeof (char));
    if (pr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetByteArrayRegion(env, matarray_pr, 0, len, pr);
    (*env)->DeleteLocalRef(env, matarray_pr);
    mxSetData(mxarr, (void*) pr);

    return mxarr;
}

mxArray* jtomUint8Array(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jshortArray matarray_pr;
    short *spr = NULL;
    unsigned char *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;
    int i;

    mxclass = mxUINT8_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jshortArray) (*env)->GetObjectField(env, matarray, matarray_int16_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    spr = (short*) calloc(len, sizeof (short));
    if (spr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetShortArrayRegion(env, matarray_pr, 0, len, spr);
    (*env)->DeleteLocalRef(env, matarray_pr);
    pr = (unsigned char*) mxCalloc(len, sizeof (unsigned char));
    if (pr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    for (i = 0; i < len; i++) {
        pr[i] = (unsigned char) spr[i];
    }
    free(spr);
    mxSetData(mxarr, (void*) pr);
    return mxarr;
}

mxArray* jtomInt16Array(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jshortArray matarray_pr;
    short *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;

    mxclass = mxINT16_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jshortArray) (*env)->GetObjectField(env, matarray, matarray_int16_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    pr = (short*) mxCalloc(len, sizeof (short));
    if (pr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetShortArrayRegion(env, matarray_pr, 0, len, pr);
    (*env)->DeleteLocalRef(env, matarray_pr);
    mxSetData(mxarr, (void*) pr);

    return mxarr;
}

mxArray* jtomUint16Array(JNIEnv* env, jobject matarray) {
    mxArray *mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jintArray matarray_pr;
    jint *spr = NULL;
    unsigned short *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;
    int i;

    mxclass = mxUINT16_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jintArray) (*env)->GetObjectField(env, matarray, matarray_int32_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    spr = (jint*) calloc(len, sizeof (jint));
    if (spr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, matarray_pr, 0, len, spr);
    (*env)->DeleteLocalRef(env, matarray_pr);
    pr = (unsigned short*) mxCalloc(len, sizeof (unsigned short));
    if (pr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    for (i = 0; i < len; i++) {
        pr[i] = (unsigned short) spr[i];
    }
    free(spr);
    mxSetData(mxarr, (void*) pr);
    return mxarr;
}

mxArray* jtomInt32Array(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jintArray matarray_pr;
    int *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;

    mxclass = mxINT32_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jintArray) (*env)->GetObjectField(env, matarray, matarray_int32_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    pr = (int*) mxCalloc(len, sizeof (int));
    if (pr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, matarray_pr, 0, len, pr);
    (*env)->DeleteLocalRef(env, matarray_pr);
    mxSetData(mxarr, (void*) pr);

    return mxarr;
}

mxArray* jtomUint32Array(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jlongArray matarray_pr;
    jlong *spr = NULL;
    unsigned long *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;
    int i;

    mxclass = mxUINT32_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jlongArray) (*env)->GetObjectField(env, matarray, matarray_int64_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    spr = (jlong*) calloc(len, sizeof (jlong));
    if (spr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetLongArrayRegion(env, matarray_pr, 0, len, spr);
    (*env)->DeleteLocalRef(env, matarray_pr);
    pr = (unsigned long*) mxCalloc(len, sizeof (unsigned long));
    if (pr == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    for (i = 0; i < len; i++) {
        pr[i] = (unsigned long) spr[i];
    }
    free(spr);
    mxSetData(mxarr, (void*) pr);
    return mxarr;
}

mxArray* jtomInt64Array(JNIEnv* env, jobject matarray) {
    mxArray* mxarr;
    mxClassID mxclass;
    int cplxFlag;
    jlongArray matarray_pr;
    jlong *pr = NULL;
    int len;
    jintArray jdims;
    int ndims;
    int *dims;

    mxclass = mxINT64_CLASS;
    cplxFlag = (*env)->CallBooleanMethod(env, matarray, matarray_isComplexMID) ? 1 : 0;
    jdims = (jintArray) (*env)->GetObjectField(env, matarray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    if (dims == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->DeleteLocalRef(env, jdims);

    mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);

    matarray_pr = (jlongArray) (*env)->GetObjectField(env, matarray, matarray_int64_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    pr = (jlong*) mxCalloc(len, sizeof (jlong));
    if (pr == NULL) {
        fprintf(stderr, ">>MARK !\n");
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    (*env)->GetLongArrayRegion(env, matarray_pr, 0, len, pr);
    (*env)->DeleteLocalRef(env, matarray_pr);
    mxSetData(mxarr, (void*) pr);

    fprintf(stderr, ">>MARK15\n");
    return mxarr;
}
/*
mxArray* jtomUint64Array(JNIEnv* env, jobject matarray) {
    TODO in 64 bit architecture specific sources
}*/

/* Matlab to Java conversion functions ****************************************/
jobject mtojArray(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    mxClassID mxarr_class;

    mxarr_class = mxGetClassID(mxarr);

    switch (mxarr_class) {
        case mxDOUBLE_CLASS:
            matarray = mtojDoubleArray(env, mxarr);
            break;
        case mxSINGLE_CLASS:
            matarray = mtojSingleArray(env, mxarr);
            break;
        case mxCHAR_CLASS:
            matarray = mtojCharArray(env, mxarr);
            break;
        case mxCELL_CLASS:
            matarray = mtojCellArray(env, mxarr);
            break;
        case mxLOGICAL_CLASS:
            matarray = mtojLogicalArray(env, mxarr);
            break;
        case mxSTRUCT_CLASS:
            matarray = mtojStructArray(env, mxarr);
            break;
        case mxINT8_CLASS:
            matarray = mtojInt8Array(env, mxarr);
            break;
        case mxUINT8_CLASS:
            matarray = mtojUint8Array(env, mxarr);
            break;
        case mxINT16_CLASS:
            matarray = mtojInt16Array(env, mxarr);
            break;
        case mxUINT16_CLASS:
            matarray = mtojUint16Array(env, mxarr);
            break;
        case mxINT32_CLASS:
            matarray = mtojInt32Array(env, mxarr);
            break;
        case mxUINT32_CLASS:
            matarray = mtojUint32Array(env, mxarr);
            break;
        case mxINT64_CLASS:
            matarray = mtojInt64Array(env, mxarr);
            break;
        case mxUINT64_CLASS:
        case mxFUNCTION_CLASS:
        default: /*mxUNKNOWN_CLASS and others*/
            matarray = NULL;
            break;
    }
    return matarray;
}

jobject mtojCharArray(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeStr;
    int ndims;
    const int* dims;
    jintArray jdims;
    int nstrings, nchars, m, n, i, j;
    char* data;
    char* dataSeg;
    jobjectArray jdata;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL)
        return NULL;

    typeStr = (jstring) (*env)->GetStaticObjectField(env, matArrayClass, matarray_CHAR_TYPE_FID);
    if (typeStr == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeStr);
    (*env)->DeleteLocalRef(env, typeStr);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    m = mxGetM(mxarr);
    n = mxGetN(mxarr);
    nstrings = m;
    nchars = mxGetNumberOfElements(mxarr);
    data = (char*) calloc(nchars + 1, sizeof (char));
    mxGetString(mxarr, data, nchars + 1);
    jdata = (*env)->NewObjectArray(env, nstrings, jstringClass, NULL);
    if (jdata == NULL)
        return NULL;
    dataSeg = (char*) calloc(n + 1, sizeof (char)); /*XXX mxchars maybe?*/
    if (dataSeg == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    for (i = 0; i < nstrings; i++) {
        for (j = 0; j < n; j++)
            dataSeg[j] = data[j * m + i];
        dataSeg[n] = '\0';
        (*env)->SetObjectArrayElement(env, jdata, i, (*env)->NewStringUTF(env, dataSeg));
    }
    free(dataSeg);
    free(data);
    (*env)->SetObjectField(env, matarray, matarray_char_dataFID, jdata);
    (*env)->DeleteLocalRef(env, jdata);

    return matarray;
}

jobject mtojCellArray(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeStr;
    int nelements;
    int ndims;
    const int* dims;
    jintArray jdims;
    mxArray** data;
    jobjectArray jdata;
    int i;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL)
        return NULL;

    typeStr = (jstring) (*env)->GetStaticObjectField(env, matArrayClass, matarray_CELL_TYPE_FID);
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeStr);
    (*env)->DeleteLocalRef(env, typeStr);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);
    jdata = (*env)->NewObjectArray(env, nelements, matArrayClass, NULL);
    if (jdata == NULL)
        return NULL;

    for (i = 0; i < nelements; i++) {
        mxArray* tmpMX;
        jobject tmpMA;
        tmpMX = mxGetCell(mxarr, i);
        tmpMA = mtojArray(env, tmpMX);
        (*env)->SetObjectArrayElement(env, jdata, i, tmpMA);
        (*env)->DeleteLocalRef(env, tmpMA);
    }
    (*env)->SetObjectField(env, matarray, matarray_cell_dataFID, jdata);
    (*env)->DeleteLocalRef(env, jdata);

    return matarray;
}

jobject mtojLogicalArray(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeStr;
    int ndims;
    const int* dims;
    jintArray jdims;
    int nelements, i;
    jbooleanArray jdata;
    mxLogical* data;
    jboolean* jbs;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL)
        return NULL;

    typeStr = (jstring) (*env)->GetStaticObjectField(env, matArrayClass, matarray_LOGICAL_TYPE_FID);
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeStr);
    (*env)->DeleteLocalRef(env, typeStr);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);
    jdata = (*env)->NewBooleanArray(env, nelements);
    if (jdata == NULL)
        return NULL;

    jbs = (jboolean*) malloc(nelements * sizeof (jboolean));
    if (jbs == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    data = mxGetLogicals(mxarr);
    for (i = 0; i < nelements; i++)
        jbs[i] = data[i] ? JNI_TRUE : JNI_FALSE;
    (*env)->SetBooleanArrayRegion(env, jdata, 0, nelements, jbs);
    free(jbs);

    (*env)->SetObjectField(env, matarray, matarray_logical_dataFID, jdata);
    (*env)->DeleteLocalRef(env, jdata);

    return matarray;
}

jobject mtojStructArray(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeStr;
    int ndims;
    const int* dims;
    jintArray jdims;
    int nelements, i, j;
    jobjectArray jdata;
    int nfields;
    jobjectArray jfieldnames;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL)
        return NULL;

    typeStr = (jstring) (*env)->GetStaticObjectField(env, matArrayClass, matarray_STRUCT_TYPE_FID);
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeStr);
    (*env)->DeleteLocalRef(env, typeStr);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    /*
        (*env)->DeleteLocalRef(env, jdims);
     */

    nfields = mxGetNumberOfFields(mxarr);
    jfieldnames = (*env)->NewObjectArray(env, nfields, jstringClass, NULL);
    if (jfieldnames == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    for (i = 0; i < nfields; i++) {
        jstring jfname;
        const char* fname;
        fname = mxGetFieldNameByNumber(mxarr, i);
        jfname = (*env)->NewStringUTF(env, fname);
        if (jfname == NULL) {
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }
        (*env)->SetObjectArrayElement(env, jfieldnames, i, jfname);
        (*env)->DeleteLocalRef(env, jfname);
    }
    (*env)->SetObjectField(env, matarray, matarray_field_namesFID, jfieldnames);
    (*env)->DeleteLocalRef(env, jfieldnames);

    nelements = mxGetNumberOfElements(mxarr);
    jdata = (*env)->NewObjectArray(env, nfields, matArrayClass, NULL);
    if (jdata == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    for (i = 0; i < nfields; i++) {
        jobjectArray jdataElement;
        jobject jfieldCell;

        jfieldCell = createJCellArray(env);
        (*env)->SetObjectField(env, jfieldCell, matarray_dimensionsFID, jdims);

        jdataElement = (*env)->NewObjectArray(env, nelements, matArrayClass, NULL);
        if (jdataElement == NULL) {
            fprintf(stderr, "not enough heap space\n");
            setError(OUT_OF_MEMORY_ERROR);
            return NULL;
        }

        for (j = 0; j < nelements; j++) {
            jobject tmpMA;
            mxArray* tmpMX;

            tmpMX = mxGetFieldByNumber(mxarr, j, i);
            if (tmpMX == NULL)
                continue;
            tmpMA = mtojArray(env, tmpMX);
            (*env)->SetObjectArrayElement(env, jdataElement, j, tmpMA);
            (*env)->DeleteLocalRef(env, tmpMA);
        }
        (*env)->SetObjectField(env, jfieldCell, matarray_cell_dataFID, jdataElement);
        (*env)->DeleteLocalRef(env, jdataElement);
        (*env)->SetObjectArrayElement(env, jdata, i, jfieldCell);
        (*env)->DeleteLocalRef(env, jfieldCell);
    }
    /*
        for (i = 0; i < nelements; i++) {
            jobject tmpMA;
            jobjectArray tmpData;
            int j;

            tmpMA = createJCellArray(env);
            tmpData = (*env)->NewObjectArray(env, nfields, matArrayClass, NULL);
            if (tmpData == NULL) {
                setError(OUT_OF_MEMORY_ERROR);
                return NULL;
            }
            for (j = 0; j < nfields; j++) {
                jobject tmpVal;
                mxArray* tmpMxVal;

                tmpMxVal = mxGetFieldByNumber(mxarr, i, j);
                tmpVal = mtojArray(env, tmpMxVal);
                (*env)->SetObjectArrayElement(env, tmpData, j, tmpVal);
                (*env)->DeleteLocalRef(env, tmpVal);
            }
            (*env)->SetObjectField(env, tmpMA, matarray_cell_dataFID, tmpData);
            (*env)->DeleteLocalRef(env, tmpData);
            (*env)->SetObjectArrayElement(env, jdata, i, tmpMA);
            (*env)->DeleteLocalRef(env, tmpMA);
        }
     */
    (*env)->SetObjectField(env, matarray, matarray_cell_dataFID, jdata);
    (*env)->DeleteLocalRef(env, jdata);
    (*env)->DeleteLocalRef(env, jdims);

    return matarray;
}

jobject mtojDoubleArray(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    double *pr, *pi;
    jdoubleArray jpr, jpi;
    int nelements;
    int ndims;
    const int *dims;
    jintArray jdims;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_DOUBLE_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = mxGetPr(mxarr);
    jpr = (*env)->NewDoubleArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetDoubleArrayRegion(env, jpr, 0, nelements, pr);
    (*env)->SetObjectField(env, matarray, matarray_double_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    if (mxIsComplex(mxarr)) {
        pi = mxGetPi(mxarr);
        jpi = (*env)->NewDoubleArray(env, nelements);
        if (jpi == NULL)
            return NULL;
        (*env)->SetDoubleArrayRegion(env, jpi, 0, nelements, pi);
        (*env)->SetObjectField(env, matarray, matarray_double_data_imFID, jpi);
        (*env)->DeleteLocalRef(env, jpi);
    }

    if (mxIsSparse(mxarr)) {
        int nzmax, nnz, ncolumns;
        int *ir, *jc;
        jintArray jir, jjc;

        nzmax = mxGetNzmax(mxarr);
        ncolumns = mxGetN(mxarr);

        ir = mxGetIr(mxarr);
        jc = mxGetJc(mxarr);

        nnz = jc[ncolumns];

        /*
                (*env)->SetObjectField(env, matarray, matarray_maxNonZeroFID, nzmax);
         */
        (*env)->CallVoidMethod(env, matarray, matarray_setMaxNonZeroMID, nzmax);

        jir = (*env)->NewIntArray(env, nzmax);
        if (jir == NULL)
            return NULL;
        (*env)->SetIntArrayRegion(env, jir, 0, nzmax, ir);
        (*env)->SetObjectField(env, matarray, matarray_rowIdsFID, jir);
        (*env)->DeleteLocalRef(env, jir);

        jjc = (*env)->NewIntArray(env, ncolumns + 1);
        if (jjc == NULL)
            return NULL;
        (*env)->SetIntArrayRegion(env, jjc, 0, ncolumns + 1, jc);
        (*env)->SetObjectField(env, matarray, matarray_colIdsFID, jjc);
        (*env)->DeleteLocalRef(env, jjc);
    }

    return matarray;
}

jobject mtojSingleArray(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    float *pr;
    jfloatArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_SINGLE_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (float*) mxGetData(mxarr);
    jpr = (*env)->NewFloatArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetFloatArrayRegion(env, jpr, 0, nelements, pr);
    (*env)->SetObjectField(env, matarray, matarray_single_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    /*
        if (mxIsComplex(mxarr)) {
            pi = mxGetPi(mxarr);
            jpi = (*env)->NewFloatArray(env, nelements);
            if (jpi == NULL)
                return NULL;
            (*env)->SetFloatArrayRegion(env, jpi, 0, nelements, pi);
            (*env)->SetObjectField(env, matarray, matarray_single_data_imFID, jpi);
            (*env)->DeleteLocalRef(env, jpi);
        }
     */

    return matarray;
}

jobject mtojInt8Array(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    char *pr;
    jbyteArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_INT8_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (char*) mxGetData(mxarr);
    jpr = (*env)->NewByteArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetByteArrayRegion(env, jpr, 0, nelements, pr);
    (*env)->SetObjectField(env, matarray, matarray_int8_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    /*
        if (mxIsComplex(mxarr)) {
            pi = mxGetPi(mxarr);
            jpi = (*env)->NewByteArray(env, nelements);
            if (jpi == NULL)
                return NULL;
            (*env)->SetByteArrayRegion(env, jpi, 0, nelements, pi);
            (*env)->SetObjectField(env, matarray, matarray_int8_data_imFID, jpi);
            (*env)->DeleteLocalRef(env, jpi);
        }
     */

    return matarray;
}

jobject mtojUint8Array(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    unsigned char *pr;
    jshort *spr;
    jshortArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;
    int i;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_UINT8_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (unsigned char*) mxGetData(mxarr);
    spr = (jshort*) calloc(nelements, sizeof (jshort));
    if (spr == NULL) {
        return NULL;
    }
    for (i = 0; i < nelements; i++) {
        spr[i] = pr[i];
    }

    jpr = (*env)->NewShortArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetShortArrayRegion(env, jpr, 0, nelements, spr);
    (*env)->SetObjectField(env, matarray, matarray_int16_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    return matarray;
}

jobject mtojInt16Array(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    short *pr;
    jshortArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_INT16_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (short*) mxGetData(mxarr);
    jpr = (*env)->NewShortArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetShortArrayRegion(env, jpr, 0, nelements, pr);
    (*env)->SetObjectField(env, matarray, matarray_int16_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    /*
        if (mxIsComplex(mxarr)) {
            pi = mxGetPi(mxarr);
            jpi = (*env)->NewShortArray(env, nelements);
            if (jpi == NULL)
                return NULL;
            (*env)->SetShortArrayRegion(env, jpi, 0, nelements, pi);
            (*env)->SetObjectField(env, matarray, matarray_int16_data_imFID, jpi);
            (*env)->DeleteLocalRef(env, jpi);
        }
     */

    return matarray;
}

jobject mtojUint16Array(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    unsigned short *pr;
    jint *spr;
    jintArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;
    int i;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_UINT16_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (unsigned short*) mxGetData(mxarr);
    spr = (jint*) calloc(nelements, sizeof (jint));
    if (spr == NULL) {
        return NULL;
    }
    for (i = 0; i < nelements; i++) {
        spr[i] = pr[i];
    }

    jpr = (*env)->NewIntArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jpr, 0, nelements, spr);
    (*env)->SetObjectField(env, matarray, matarray_int32_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    return matarray;
}

jobject mtojInt32Array(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    int *pr;
    jintArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_INT32_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (int*) mxGetData(mxarr);
    jpr = (*env)->NewIntArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jpr, 0, nelements, pr);
    (*env)->SetObjectField(env, matarray, matarray_int32_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    /*
        if (mxIsComplex(mxarr)) {
            pi = mxGetPi(mxarr);
            jpi = (*env)->NewIntArray(env, nelements);
            if (jpi == NULL)
                return NULL;
            (*env)->SetIntArrayRegion(env, jpi, 0, nelements, pi);
            (*env)->SetObjectField(env, matarray, matarray_int32_data_imFID, jpi);
            (*env)->DeleteLocalRef(env, jpi);
        }
     */

    return matarray;
}

jobject mtojUint32Array(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    unsigned long *pr;
    jlong *spr;
    jlongArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;
    int i;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }

    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_UINT32_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (unsigned long*) mxGetData(mxarr);
    spr = (jlong*) calloc(nelements, sizeof (jint));
    if (spr == NULL) {
        return NULL;
    }
    for (i = 0; i < nelements; i++) {
        spr[i] = pr[i];
    }

    jpr = (*env)->NewLongArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    (*env)->SetLongArrayRegion(env, jpr, 0, nelements, spr);
    (*env)->SetObjectField(env, matarray, matarray_int64_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    return matarray;
}

jobject mtojInt64Array(JNIEnv* env, mxArray* mxarr) {
    jobject matarray;
    jstring typeJString;
    jlong *pr;
    jlongArray jpr;
    int nelements, ndims;
    const int *dims;
    jintArray jdims;

    fprintf(stderr, ">>MARK1\n");
    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    fprintf(stderr, ">>MARK2\n");
    typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_INT64_TYPE_FID);
    if (typeJString == NULL)
        return NULL;
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeJString);
    (*env)->DeleteLocalRef(env, typeJString);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    if (jdims == NULL)
        return NULL;
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matarray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nelements = mxGetNumberOfElements(mxarr);

    pr = (jlong*) mxGetData(mxarr);
    jpr = (*env)->NewLongArray(env, nelements);
    if (jpr == NULL)
        return NULL;
    
    (*env)->SetLongArrayRegion(env, jpr, 0, nelements, pr);
    (*env)->SetObjectField(env, matarray, matarray_int64_data_reFID, jpr);
    (*env)->DeleteLocalRef(env, jpr);

    return matarray;
}

/*
jobject mtojUInt64Array(JNIEnv* env, mxArray* mxarr) {
    TODO in 64bit architecture specific sources.
}
 */

/* utilitiy functions */

/**
 * Finds mxClassID for a given MatArray.
 * @param env JNIEnv pointer
 * @param matArray
 * @return mxClassID
 */
mxClassID getMxClassID(JNIEnv *env, jobject matArray) {
    const char *typeNameStr;
    jstring typeName;
    mxClassID mxclass;

    typeName = (jstring) (*env)->GetObjectField(env, matArray, matarray_typeFID);

    typeNameStr = (*env)->GetStringUTFChars(env, typeName, NULL);
    if (strcmp(typeNameStr, "DOUBLE") == 0) {
        mxclass = mxDOUBLE_CLASS;
    }else if (strcmp(typeNameStr, "SINGLE") == 0) {
        mxclass = mxSINGLE_CLASS;
    }else if (strcmp(typeNameStr, "CELL") == 0) {
        mxclass = mxCELL_CLASS;
    }else if (strcmp(typeNameStr, "CHAR") == 0) {
        mxclass = mxCHAR_CLASS;
    }else if (strcmp(typeNameStr, "LOGICAL") == 0) {
        mxclass = mxLOGICAL_CLASS;
    }else if (strcmp(typeNameStr, "STRUCT") == 0) {
        mxclass = mxSTRUCT_CLASS;
    }else if (strcmp(typeNameStr, "INT8") == 0) {
        mxclass = mxINT8_CLASS;
    }else if (strcmp(typeNameStr, "UINT8") == 0) {
        mxclass = mxUINT8_CLASS;
    }else if (strcmp(typeNameStr, "INT16") == 0) {
        mxclass = mxINT16_CLASS;
    }else if (strcmp(typeNameStr, "UINT16") == 0) {
        mxclass = mxUINT16_CLASS;
    }else if (strcmp(typeNameStr, "INT32") == 0) {
        mxclass = mxINT32_CLASS;
    }else if (strcmp(typeNameStr, "UINT32") == 0) {
        mxclass = mxUINT32_CLASS;
    }else if (strcmp(typeNameStr, "INT64") == 0) {
        mxclass = mxINT64_CLASS;
    }else if (strcmp(typeNameStr, "FUNCTION") == 0) {
        mxclass = mxFUNCTION_CLASS;
    } else { /*type UNKNOWN*/
        mxclass = mxUNKNOWN_CLASS;
    }
    (*env)->ReleaseStringUTFChars(env, typeName, typeNameStr);
    return mxclass;
}

/*error checking*/
void setError(int err) {
    errorCode = err;
}

/*MISC*/
jobject createJCellArray(JNIEnv* env) {
    jobject matarray;
    jstring typeStr;

    matarray = (*env)->NewObject(env, matArrayClass, matarray_constructorMID);
    if (matarray == NULL) {
        setError(OUT_OF_MEMORY_ERROR);
        return NULL;
    }
    typeStr = (*env)->GetStaticObjectField(env, matArrayClass, matarray_CELL_TYPE_FID);
    (*env)->SetObjectField(env, matarray, matarray_typeFID, typeStr);
    (*env)->DeleteLocalRef(env, typeStr);

    return matarray;
}

