#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "matrix.h"
#include "engine.h"
#include "MatUtils.h"
#include "net_sf_taverna_matserver_MatEngineImpl.h"

JNIEXPORT void JNICALL Java_net_sf_taverna_matserver_MatEngineImpl_execute(JNIEnv *env, jobject this, jstring script) {
    Engine* eng;
    const char *strScript;

    jobjectArray inputValues;
    jobjectArray inputNames, outputNames;
    jint inputLen, outputLen;
    int i, r = 0;
    mxArray** inputs;

    /*open session*/
    eng = engOpen("");
/*
    eng = engOpenSingleUse(NULL, NULL, &r);
*/
    if (!eng) {
        fprintf(stderr, "\nUnable to start Matlab Engine\n");
        return;
    }
    engSetVisible(eng, 0);

    /*setup workspace*/
    inputNames = (jobjectArray) (*env)->CallObjectMethod(env, this, matengineimpl_getVarNamesMID);
    if (inputNames == NULL) return;
    inputValues = (jobjectArray) (*env)->CallObjectMethod(env, this, matengineimpl_getVarValuesMID);
    if (inputValues == NULL) return;
    inputLen = (*env)->GetArrayLength(env, inputValues);

    inputs = (mxArray**) mxCalloc(inputLen, sizeof (mxArray*));
    if (inputs == NULL)
        return;

    for (i = 0; i < inputLen; i++) {
        mxArray *mxarr;
        jobject matArray = (*env)->GetObjectArrayElement(env, inputValues, i);
        jstring jname = (jstring) (*env)->GetObjectArrayElement(env, inputNames, i);
        const char* name;

        mxarr = jtomArray(env, matArray);
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
        matArray = mtojArray(env, mxarr);
        mxDestroyArray(mxarr); /*XXX ???*/

        (*env)->CallVoidMethod(env, this, matengineimpl_setOutputVarMID, jname, matArray);
        (*env)->DeleteLocalRef(env, jname);
        (*env)->DeleteLocalRef(env, matArray);
    }

    /*clean up*/
    (*env)->DeleteLocalRef(env, outputNames);
    (*env)->DeleteLocalRef(env, inputNames);
    for (i = 0; i < inputLen; i++)
        if (inputs[i] != NULL)
            mxDestroyArray(inputs[i]);
    mxFree(inputs);

    engClose(eng);
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
    // matarray_nNonZeroFID = (*env)->GetFieldID(env, matArrayClass, "NNonZero", "I");
    matarray_dimensionsFID = (*env)->GetFieldID(env, matArrayClass, "dims", "[I");
    matarray_rowIdsFID = (*env)->GetFieldID(env, matArrayClass, "rowIds", "[I");
    matarray_colIdsFID = (*env)->GetFieldID(env, matArrayClass, "colIds", "[I");
    matarray_double_data_reFID = (*env)->GetFieldID(env, matArrayClass, "doubleDataRe", "[D");
    matarray_double_data_imFID = (*env)->GetFieldID(env, matArrayClass, "doubleDataIm", "[D");
    matarray_char_dataFID = (*env)->GetFieldID(env, matArrayClass, "charData", "[Ljava/lang/String;");
    matarray_logical_dataFID = (*env)->GetFieldID(env, matArrayClass, "logicalData", "[Z");
    matarray_cell_dataFID = (*env)->GetFieldID(env, matArrayClass, "cellData", "[Lnet/sf/taverna/matserver/MatArray;");
    matarray_field_namesFID = (*env)->GetFieldID(env, matArrayClass, "fieldNames", "[Ljava/lang/String;");
    matarray_single_data_reFID = (*env)->GetFieldID(env, matArrayClass, "singleDataRe", "[F");
    matarray_int8_data_reFID = (*env)->GetFieldID(env, matArrayClass, "int8DataRe", "[B");
    matarray_int16_data_reFID = (*env)->GetFieldID(env, matArrayClass, "int16DataRe", "[S");
    matarray_int32_data_reFID = (*env)->GetFieldID(env, matArrayClass, "int32DataRe", "[I");
    matarray_int64_data_reFID = (*env)->GetFieldID(env, matArrayClass, "int64DataRe", "[J");

    matarray_constructorMID = (*env)->GetMethodID(env, matArrayClass, "<init>", "()V");
    matarray_isSparseMID = (*env)->GetMethodID(env, matArrayClass, "checkSparse", "()Z");
    matarray_isNumericMID = (*env)->GetMethodID(env, matArrayClass, "checkNumeric", "()Z");
    matarray_isComplexMID = (*env)->GetMethodID(env, matArrayClass, "checkComplex", "()Z");
    matarray_isCharMID = (*env)->GetMethodID(env, matArrayClass, "checkChar", "()Z");
    matarray_getFieldMID = (*env)->GetMethodID(env, matArrayClass, "getField", "(Ljava/lang/String;I)Lnet/sf/taverna/matserver/MatArray;");
    matarray_setMaxNonZeroMID=(*env)->GetMethodID(env,matArrayClass,"setMaxNonZero","(I)V");

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

    setError(OK);
}
