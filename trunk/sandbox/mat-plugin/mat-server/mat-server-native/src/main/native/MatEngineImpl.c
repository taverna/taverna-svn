#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "matrix.h"
#include "engine.h"
#include "net_sf_taverna_matserver_MatEngineImpl.h"


/*Class IDs*/
jclass matArrayClass, matEngineImplClass, jstringClass;

/*MatArray FieldIDs*/
jfieldID matarray_typeFID, matarray_maxNonZeroFID, matarray_nNonZeroFID, matarray_dimensionsFID,
matarray_rowIdsFID, matarray_colIdsFID, matarray_double_data_reFID, matarray_double_data_imFID,
matarray_char_dataFID, matarray_logical_dataFID, matarray_cell_dataFID, matarray_field_namesFID,
matarray_single_data_reFID, matarray_single_data_imFID, matarray_int8_data_reFID, matarray_int8_data_imFID,
matarray_int16_data_reFID, matarray_int16_data_imFID, matarray_int32_data_reFID, matarray_int32_data_imFID,
matarray_int64_data_reFID, matarray_int64_data_imFID;
/*MatArray static field IDs*/
jfieldID matarray_UNKNOWN_TYPE_FID, matarray_STRUCT_TYPE_FID, matarray_CELL_TYPE_FID,
matarray_LOGICAL_TYPE_FID, matarray_CHAR_TYPE_FID, matarray_DOUBLE_TYPE_FID,
matarray_SINGLE_TYPE_FID, matarray_INT8_TYPE_FID, matarray_UINT8_TYPE_FID,
matarray_INT16_TYPE_FID, matarray_UINT16_TYPE_FID, matarray_INT32_TYPE_FID,
matarray_UINT32_TYPE_FID, matarray_INT64_TYPE_FID, matarray_UINT64_TYPE_FID,
matarray_FUNTION_TYPE_FID;
/*MatArray method ids*/
jmethodID matarray_contrsuctorMID, matarray_isSparseMID, matarray_isNumericMID, matarray_isComplexMID,
matarray_isCharMID, matarray_getFieldMID;

/*MatEngineImpl method ids*/
jmethodID matengineimpl_getVarMID, matengineimpl_setVarMID,
matengineimpl_getVarNamesMID, matengineimpl_getVarValuesMID, matengine_setFigureMID,
matengineimpl_getOutputNamesMID, matengineimpl_setOutputVarMID;




mxArray* createMxArray(JNIEnv* env, jobject matArray);
mxClassID getMxClassID(JNIEnv *env, jobject matArray);
mxArray* createMxArrayNumeric(JNIEnv *env, jobject matArray, mxClassID mxclass);
mxArray* createMxArrayChar(JNIEnv *env, jobject matArray);
jobject createMatArray(JNIEnv* env, mxArray* mxarr);
jobject mtojCharArray(JNIEnv *env, mxArray *mxarr);

JNIEXPORT void JNICALL Java_net_sf_taverna_matserver_MatEngineImpl_execute(JNIEnv *env, jobject this, jstring script) {
    Engine* eng;
    const char *strScript;

    jobjectArray inputValues;
    jobjectArray inputNames, outputNames;
    jint inputLen, outputLen;
    int i;
    mxArray** inputs;

    /*open session*/
    eng = engOpen("");
    if (!eng) {
        fprintf(stderr, "\nUnable to start Matlab Engine\n");
        return;
    }

    /*setup workspace*/
    inputNames = (jobjectArray) (*env)->CallObjectMethod(env, this, matengineimpl_getVarNamesMID);
    if (inputNames == NULL) return;
    inputValues = (jobjectArray) (*env)->CallObjectMethod(env, this, matengineimpl_getVarValuesMID);
    if (inputValues == NULL) return;
    inputLen = (*env)->GetArrayLength(env, inputValues);

    inputs = (mxArray**) malloc(inputLen * sizeof (mxArray*));
    if (inputs == NULL)
        return;

    for (i = 0; i < inputLen; i++) {
        mxArray *mxarr;
        jobject matArray = (*env)->GetObjectArrayElement(env, inputValues, i);
        jstring jname = (jstring) (*env)->GetObjectArrayElement(env, inputNames, i);
        const char* name;

        mxarr = createMxArray(env, matArray);
        if (mxarr == NULL) {
            return;
        }
        (*env)->DeleteLocalRef(env, matArray);

        name = (*env)->GetStringUTFChars(env, jname, NULL);
        engPutVariable(eng, name, mxarr);
        inputs[i] = mxarr;
        (*env)->ReleaseStringUTFChars(env, jname, name);
        (*env)->DeleteLocalRef(env, matArray);
        (*env)->DeleteLocalRef(env, jname);
    }

    /*execute*/
    strScript = (*env)->GetStringUTFChars(env, script, NULL);
    if (strScript == NULL)
        return;
    engEvalString(eng, strScript);
    (*env)->ReleaseStringUTFChars(env, script, strScript);

    /*setup results*/

    outputNames = (jobjectArray) (*env)->CallObjectMethod(env, this, matengineimpl_getOutputNamesMID);
    outputLen = (*env)->GetArrayLength(env, outputNames);

    for (i = 0; i < outputLen; i++) {
        jstring jname;
        const char* name;
        mxArray* mxarr;
        jobject matArray;

        jname = (jstring) (*env)->GetObjectArrayElement(env, outputNames, i);
        name = (*env)->GetStringUTFChars(env, jname, NULL);
        mxarr = engGetVariable(eng, name);
        (*env)->ReleaseStringUTFChars(env, jname, name);

        matArray = createMatArray(env, mxarr);
        mxDestroyArray(mxarr); /*XXX ???*/

        (*env)->CallVoidMethod(env, this, matengineimpl_setOutputVarMID, jname, matArray);
        (*env)->DeleteLocalRef(env, jname);
        (*env)->DeleteLocalRef(env, matArray);
    }
    /*clean up*/
    for (i = 0; i < inputLen; i++)
        if (inputs[i] != NULL)
            mxDestroyArray(inputs[i]);
    free(inputs);
    engClose(eng);
}

mxArray* createMxArray(JNIEnv* env, jobject matArray) {
    mxArray *mxarr = NULL;
    mxClassID mxarray_class;
    jboolean isNumeric, isChar;
    jstring typeName;


    typeName = (jstring) (*env)->GetObjectField(env, matArray, matarray_typeFID);

    isNumeric = (*env)->CallBooleanMethod(env, matArray, matarray_isNumericMID);
    isChar = (*env)->CallBooleanMethod(env, matArray, matarray_isCharMID);

    mxarray_class = getMxClassID(env, matArray);

    if (isNumeric)
        mxarr = createMxArrayNumeric(env, matArray, mxarray_class);
    else if (isChar)
        mxarr = createMxArrayChar(env, matArray);

    return mxarr;
}

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

/**
 * Creates numeric mxArray from java MatArray.
 * @param env - JNIEnv pointer
 * @param matArray - MatArray array to be converted
 * @param mxclass - mxClassID of the array.
 * @return mxArray
 */
mxArray* createMxArrayNumeric(JNIEnv *env, jobject matArray, mxClassID mxclass) {
    mxArray *mxarr;
    int cplxFlag;
    jboolean isSparse;
    double *pr = NULL, *pi = NULL;
    jdoubleArray matarray_pr, matarray_pi;
    jintArray matarray_ir, matarray_jc;
    int *ir = NULL, *jc = NULL;
    jint matarray_nzmax;
    jint len;
    jintArray jdims;
    int ndims;
    int *dims;

    cplxFlag = (*env)->CallBooleanMethod(env, matArray, matarray_isComplexMID);

    jdims = (jintArray) (*env)->GetObjectField(env, matArray, matarray_dimensionsFID);
    if (jdims == NULL)
        return NULL;
    ndims = (*env)->GetArrayLength(env, jdims);
    dims = (int*) mxCalloc(ndims, sizeof (int));
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);

    isSparse = (*env)->CallBooleanMethod(env, matArray, matarray_isSparseMID);
    if (isSparse) {
        if (ndims > 2)
            return NULL;
        matarray_nzmax = (*env)->GetIntField(env, matArray, matarray_maxNonZeroFID);
        mxarr = mxCreateSparse(dims[0], dims[1], matarray_nzmax, cplxFlag);
        matarray_ir = (jintArray) (*env)->GetObjectField(env, matArray, matarray_rowIdsFID);
        matarray_jc = (jintArray) (*env)->GetObjectField(env, matArray, matarray_colIdsFID);
        if (matarray_ir == NULL || matarray_jc == NULL)
            return NULL;

        len = (*env)->GetArrayLength(env, matarray_ir);
        ir = (int*) mxCalloc(len, sizeof (int));
        (*env)->GetIntArrayRegion(env, matarray_ir, 0, len, ir);
        mxSetIr(mxarr, ir);

        len = (*env)->GetArrayLength(env, matarray_jc);
        jc = (int*) mxCalloc(len, sizeof (int));
        (*env)->GetIntArrayRegion(env, matarray_jc, 0, len, jc);
        mxSetJc(mxarr, jc);
    } else {
        mxarr = mxCreateNumericArray(ndims, dims, mxclass, cplxFlag);
    }

    matarray_pr = (jdoubleArray) (*env)->GetObjectField(env, matArray, matarray_double_data_reFID);
    len = (*env)->GetArrayLength(env, matarray_pr);
    pr = (jdouble*) calloc(len, sizeof (jdouble));
    (*env)->GetDoubleArrayRegion(env, matarray_pr, 0, len, pr);
    mxSetPr(mxarr, pr);

    matarray_pi = (jdoubleArray) (*env)->GetObjectField(env, matArray, matarray_double_data_imFID);
    if (matarray_pi != NULL) {
        len = (*env)->GetArrayLength(env, matarray_pi);
        pi = (jdouble*) calloc(len, sizeof (jdouble));
        (*env)->GetDoubleArrayRegion(env, matarray_pi, 0, len, pi);
        mxSetPi(mxarr, pi);
    }

    return mxarr;
}

/**
 * Creates chsr mxArray from java MatArray.
 * @param env - JNIEnv pointer
 * @param matArray - MatArray array to be converted
 * @return mxArray
 */
mxArray* createMxArrayChar(JNIEnv *env, jobject matArray) {
    mxArray* mxarr = NULL;
    jint len;
    jintArray jdims;
    int ndims;
    int *dims = NULL;
    char** data = NULL;
    jobjectArray jdata;
    int i;

    jdims = (*env)->GetObjectField(env, matArray, matarray_dimensionsFID);
    ndims = (*env)->GetArrayLength(env, jdims);

    dims = (int*) mxMalloc(ndims * sizeof (int));
    (*env)->GetIntArrayRegion(env, jdims, 0, ndims, dims);

    jdata = (jobjectArray) (*env)->GetObjectField(env, matArray, matarray_char_dataFID);
    len = (*env)->GetArrayLength(env, jdata);
    data = (char**) mxCalloc(len, sizeof (char*));
    for (i = 0; i < len; i++) {
        jstring jstr = (*env)->GetObjectArrayElement(env, jdata, i);
        int jstrlen = (*env)->GetStringLength(env, jstr);
        data[i] = (char*) mxCalloc(jstrlen, sizeof (char));
        (*env)->GetStringUTFRegion(env, jstr, 0, jstrlen, data[i]);
        (*env)->DeleteLocalRef(env, jstr);
    }

    mxarr = mxCreateCharMatrixFromStrings(dims[0], data);

    return mxarr;
}

jobject createMatArray(JNIEnv* env, mxArray* mxarr) {
    jobject matArray;
    mxClassID mxclass;

    jstring typeJString;
    double *pr, *pi;
    jdoubleArray jpr, jpi;
    int nelements;
    int ndims;
    const int *dims;
    jintArray jdims;

    matArray = (*env)->NewObject(env, matArrayClass, matarray_contrsuctorMID);

    /*setup the array*/
    mxclass = mxGetClassID(mxarr);
    switch (mxclass) {
        case mxDOUBLE_CLASS:
            typeJString = (*env)->GetStaticObjectField(env, matArrayClass, matarray_DOUBLE_TYPE_FID);
            (*env)->SetObjectField(env, matArray, matarray_typeFID, typeJString);
            (*env)->DeleteLocalRef(env, typeJString);
            ndims = mxGetNumberOfDimensions(mxarr);
            dims = mxGetDimensions(mxarr);
            jdims = (*env)->NewIntArray(env, ndims);
            (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
            (*env)->SetObjectField(env, matArray, matarray_dimensionsFID, jdims);
            (*env)->DeleteLocalRef(env, jdims);

            nelements = mxGetNumberOfElements(mxarr);

            pr = mxGetPr(mxarr);
            jpr = (*env)->NewDoubleArray(env, nelements);
            (*env)->SetDoubleArrayRegion(env, jpr, 0, nelements, pr);
            (*env)->SetObjectField(env, matArray, matarray_double_data_reFID, jpr);
            (*env)->DeleteLocalRef(env, jpr);

            if (mxIsComplex(mxarr)) {
                pi = mxGetPi(mxarr);
                jpi = (*env)->NewDoubleArray(env, nelements);
                (*env)->SetDoubleArrayRegion(env, jpi, 0, nelements, pi);
                (*env)->SetObjectField(env, matArray, matarray_double_data_imFID, jpi);
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

                jir = (*env)->NewIntArray(env, ncolumns);
                (*env)->SetIntArrayRegion(env, jir, 0, ncolumns, ir);
                (*env)->SetObjectField(env, matArray, matarray_rowIdsFID, jir);
                (*env)->DeleteLocalRef(env, jir);

                jjc = (*env)->NewIntArray(env, ncolumns + 1);
                (*env)->SetIntArrayRegion(env, jjc, 0, ncolumns + 1, jc);
                (*env)->SetObjectField(env, matArray, matarray_colIdsFID, jjc);
                (*env)->DeleteLocalRef(env, jjc);
            }

            break;
        case mxSINGLE_CLASS:
            break;
        case mxCHAR_CLASS:
            matArray = mtojCharArray(env, mxarr);
            break;
        default:
            break;
    }
    return matArray;
}

jobject mtojCharArray(JNIEnv *env, mxArray *mxarr) {
    jobject matArray;
    jstring typeName;
    int ndims;
    const int *dims;
    jintArray jdims;
    jobjectArray jdata;
    char *data;
    char *dataSeg;
    int nstrings, i, j;
    int nchars, m, n;

    matArray = (*env)->NewObject(env, matArrayClass, matarray_contrsuctorMID);

    typeName = (jstring) (*env)->GetStaticObjectField(env, matArrayClass, matarray_CHAR_TYPE_FID);
    (*env)->SetObjectField(env, matArray, matarray_typeFID, typeName);
    (*env)->DeleteLocalRef(env, typeName);

    ndims = mxGetNumberOfDimensions(mxarr);
    dims = mxGetDimensions(mxarr);
    jdims = (*env)->NewIntArray(env, ndims);
    (*env)->SetIntArrayRegion(env, jdims, 0, ndims, dims);
    (*env)->SetObjectField(env, matArray, matarray_dimensionsFID, jdims);
    (*env)->DeleteLocalRef(env, jdims);

    nstrings = dims[0];
    m = mxGetM(mxarr);
    n = mxGetN(mxarr);
    nchars = m*n;
    data = (char*) malloc(nchars + 1);
    mxGetString(mxarr, data, nchars);
    jdata = (*env)->NewObjectArray(env, nstrings, jstringClass, NULL);
    dataSeg = (char*) calloc(n + 1, sizeof (char));
    for (i = 0; i < nstrings; i++) {
        for (j = 0; j < n; j++)
            dataSeg[j] = data[j * m + i];
        /*
                strncpy(dataSeg, data + i*n, n);
         */
        dataSeg[n] = '\0';
        (*env)->SetObjectArrayElement(env, jdata, i, (*env)->NewStringUTF(env, dataSeg));
    }
    free(dataSeg);
    free(data);

    (*env)->SetObjectField(env, matArray, matarray_char_dataFID, jdata);

    (*env)->DeleteLocalRef(env, jdata);
    return matArray;
}

JNIEXPORT void JNICALL Java_net_sf_taverna_matserver_MatEngineImpl_initIDs(JNIEnv *env, jclass cls) {
    jclass tmpClassRef;

    /*XXX cls also points to MatEngineImpl class*/
    tmpClassRef = (*env)->FindClass(env, "net/sf/taverna/matserver/MatEngineImpl");
    if (tmpClassRef == NULL)
        return;
    matEngineImplClass = (*env)->NewGlobalRef(env, tmpClassRef);
    if (matEngineImplClass == NULL)
        return;
    tmpClassRef = (*env)->FindClass(env, "net/sf/taverna/matserver/MatArray");
    if (tmpClassRef == NULL)
        return;
    matArrayClass = (*env)->NewGlobalRef(env, tmpClassRef);
    if (matArrayClass == NULL)
        return;
    tmpClassRef = (*env)->FindClass(env, "java/lang/String");
    if (tmpClassRef == NULL)
        return;
    jstringClass = (*env)->NewGlobalRef(env, tmpClassRef);
    if (jstringClass == NULL)
        return;

    /*MatArray field ids initialization*/
    matarray_typeFID = (*env)->GetFieldID(env, matArrayClass, "type", "Ljava/lang/String;");
    matarray_maxNonZeroFID = (*env)->GetFieldID(env, matArrayClass, "maxNonZero", "I");
    matarray_nNonZeroFID = (*env)->GetFieldID(env, matArrayClass, "nNonZero", "I");
    matarray_dimensionsFID = (*env)->GetFieldID(env, matArrayClass, "dims", "[I");
    matarray_rowIdsFID = (*env)->GetFieldID(env, matArrayClass, "rowIds", "[I");
    matarray_colIdsFID = (*env)->GetFieldID(env, matArrayClass, "colIds", "[I");
    matarray_double_data_reFID = (*env)->GetFieldID(env, matArrayClass, "double_data_re", "[D");
    matarray_double_data_imFID = (*env)->GetFieldID(env, matArrayClass, "double_data_im", "[D");
    matarray_char_dataFID = (*env)->GetFieldID(env, matArrayClass, "char_data", "[Ljava/lang/String;");
    matarray_logical_dataFID = (*env)->GetFieldID(env, matArrayClass, "logical_data", "[Z");
    matarray_cell_dataFID = (*env)->GetFieldID(env, matArrayClass, "cell_data", "[Lnet/sf/taverna/matserver/MatArray;");
    matarray_field_namesFID = (*env)->GetFieldID(env, matArrayClass, "field_names", "[Ljava/lang/String;");
    matarray_single_data_reFID=(*env)->GetFieldID(env,matArrayClass, "single_data_re","[F");
    matarray_single_data_imFID=(*env)->GetFieldID(env,matArrayClass,"single_data_im","[F");
    matarray_int8_data_reFID=(*env)->GetFieldID(env,matArrayClass,"int8_data_re","[B");
    matarray_int8_data_imFID=(*env)->GetFieldID(env,matArrayClass,"int8_data_im","[B");
    matarray_int16_data_reFID=(*env)->GetFieldID(env,matArrayClass,"int16_data_re","[S");
    matarray_int16_data_imFID=(*env)->GetFieldID(env,matArrayClass,"int16_data_im","[S");
    matarray_int32_data_reFID=(*env)->GetFieldID(env,matArrayClass,"int32_data_re","[I");
    matarray_int32_data_imFID=(*env)->GetFieldID(env,matArrayClass,"int32_data_im","[I");
    matarray_int64_data_reFID=(*env)->GetFieldID(env,matArrayClass,"int64_data_re","[J");
    matarray_int64_data_imFID=(*env)->GetFieldID(env,matArrayClass,"int64_data_im","[J");

    matarray_contrsuctorMID = (*env)->GetMethodID(env, matArrayClass, "<init>", "()V");
    matarray_isSparseMID = (*env)->GetMethodID(env, matArrayClass, "isSparse", "()Z");
    matarray_isNumericMID = (*env)->GetMethodID(env, matArrayClass, "isNumeric", "()Z");
    matarray_isComplexMID = (*env)->GetMethodID(env, matArrayClass, "isComplex", "()Z");
    matarray_isCharMID = (*env)->GetMethodID(env, matArrayClass, "isChar", "()Z");
    matarray_getFieldMID = (*env)->GetMethodID(env, matArrayClass, "getField", "(Ljava/lang/String;I)Lnet/sf/taverna/matserver/MatArray;");

    matarray_UNKNOWN_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "UNKNOWN_TYPE", "Ljava/lang/String;");
    matarray_STRUCT_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "STRUCT_TYPE", "Ljava/lang/String;");
    matarray_CELL_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "CELL_TYPE", "Ljava/lang/String;");
    matarray_LOGICAL_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "LOGICAL_TYPE", "Ljava/lang/String;");
    matarray_CHAR_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "CHAR_TYPE", "Ljava/lang/String;");
    matarray_DOUBLE_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "DOUBLE_TYPE", "Ljava/lang/String;");
    matarray_SINGLE_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "SINGLE_TYPE", "Ljava/lang/String;");
    matarray_INT8_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "INT8_TYPE", "Ljava/lang/String;");
    matarray_UINT8_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "UINT8_TYPE", "Ljava/lang/String;");
    matarray_INT16_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "INT16_TYPE", "Ljava/lang/String;");
    matarray_UINT16_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "UINT16_TYPE", "Ljava/lang/String;");
    matarray_INT32_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "INT32_TYPE", "Ljava/lang/String;");
    matarray_UINT32_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "UINT32_TYPE", "Ljava/lang/String;");
    matarray_INT64_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "INT64_TYPE", "Ljava/lang/String;");
    matarray_UINT64_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "UINT64_TYPE", "Ljava/lang/String;");
    matarray_FUNTION_TYPE_FID = (*env)->GetStaticFieldID(env, matArrayClass, "FUNCTION_TYPE", "Ljava/lang/String;");


    /*MatEngineImpl ids*/
    matengineimpl_getVarMID = (*env)->GetMethodID(env, matEngineImplClass, "getVar", "(Ljava/lang/String;)Lnet/sf/taverna/matserver/MatArray;");
    matengineimpl_setVarMID = (*env)->GetMethodID(env, matEngineImplClass, "setVar", "(Ljava/lang/String;Lnet/sf/taverna/matserver/MatArray;)V");
    matengineimpl_getVarNamesMID = (*env)->GetMethodID(env, matEngineImplClass, "getVarNames", "()[Ljava/lang/String;");
    matengineimpl_getVarValuesMID = (*env)->GetMethodID(env, matEngineImplClass, "getVarValues", "()[Lnet/sf/taverna/matserver/MatArray;");
    matengineimpl_getOutputNamesMID = (*env)->GetMethodID(env, matEngineImplClass, "getOutputNames", "()[Ljava/lang/String;");
    matengineimpl_setOutputVarMID = (*env)->GetMethodID(env, matEngineImplClass, "setOutputVar", "(Ljava/lang/String;Lnet/sf/taverna/matserver/MatArray;)V");

}
