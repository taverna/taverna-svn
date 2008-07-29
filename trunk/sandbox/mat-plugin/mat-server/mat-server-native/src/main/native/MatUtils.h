#include <jni.h>
#include "matrix.h"
#include "engine.h"

/*Class IDs*/
jclass matArrayClass, matEngineImplClass, jstringClass;

/*MatArray FieldIDs*/
jfieldID matarray_typeFID, matarray_maxNonZeroFID, matarray_dimensionsFID,
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
jmethodID matarray_constructorMID, matarray_isSparseMID, matarray_isNumericMID, matarray_isComplexMID,
matarray_isCharMID, matarray_getFieldMID;

/*MatEngineImpl method ids*/
jmethodID matengineimpl_getVarMID, matengineimpl_setVarMID,
matengineimpl_getVarNamesMID, matengineimpl_getVarValuesMID, matengine_setFigureMID,
matengineimpl_getOutputNamesMID, matengineimpl_setOutputVarMID;

/* Java to Matlab conversion functions */
mxArray* jtomArray(JNIEnv* env, jobject matarray);
mxArray* jtomCharArray(JNIEnv* env, jobject matarray);
mxArray* jtomCellArray(JNIEnv* env, jobject matarray);
mxArray* jtomLogicalArray(JNIEnv* env, jobject matarray);
mxArray* jtomStructArray(JNIEnv* env, jobject matarray);
mxArray* jtomDoubleArray(JNIEnv* env, jobject matarray);
mxArray* jtomSingleArray(JNIEnv* env, jobject matarray);
mxArray* jtomInt8Array(JNIEnv* env, jobject matarray);
mxArray* jtomUint8Array(JNIEnv* env, jobject matarray);
mxArray* jtomInt16Array(JNIEnv* env, jobject matarray);
mxArray* jtomUint16Array(JNIEnv* env, jobject matarray);
mxArray* jtomInt32Array(JNIEnv* env, jobject matarray);
mxArray* jtomUint32Array(JNIEnv* env, jobject matarray);
mxArray* jtomInt64Array(JNIEnv* env, jobject matarray);
mxArray* jtomUint64Array(JNIEnv* env, jobject matarray);

/* Matlab to Java conversion functions */
jobject mtojArray(JNIEnv* env, mxArray* mxarr);
jobject mtojCharArray(JNIEnv* env, mxArray* mxarr);
jobject mtojCellArray(JNIEnv* env, mxArray* mxarr);
jobject mtojLogicalArray(JNIEnv* env, mxArray* mxarr);
jobject mtojStructArray(JNIEnv* env, mxArray* mxarr);
jobject mtojDoubleArray(JNIEnv* env, mxArray* mxarr);
jobject mtojSingleArray(JNIEnv* env, mxArray* mxarr);
jobject mtojInt8Array(JNIEnv* env, mxArray* mxarr);
jobject mtojUint8Array(JNIEnv* env, mxArray* mxarr);
jobject mtojInt16Array(JNIEnv* env, mxArray* mxarr);
jobject mtojUint16Array(JNIEnv* env, mxArray* mxarr);
jobject mtojInt32Array(JNIEnv* env, mxArray* mxarr);
jobject mtojUint32Array(JNIEnv* env, mxArray* mxarr);
jobject mtojInt64Array(JNIEnv* env, mxArray* mxarr);
jobject mtojUInt64Array(JNIEnv* env, mxArray* mxarr);

/* utilitiy functions */
mxClassID getMxClassID(JNIEnv *env, jobject matArray);


/*Error checking*/
void setError(int err);

#define OK 0
#define OUT_OF_MEMORY_ERROR 1
#define DIMENSIONS_ERROR 2

int errorCode;


