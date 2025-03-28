#include <jni.h>




JNIEXPORT jstring JNICALL
Java_com_you4me_you4me_utils_UtilityParam_getGooglePlacesSecretKey(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "AIzaSyAjzOVHaTnlRwL6ArgGsRXOAt3PCJ5FujI");
}

JNIEXPORT jstring JNICALL
Java_com_you4me_you4me_utils_UtilityParam_getMixpanelSecretKey(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "d1800cfe7956f69cf896a35dd8653f17");
}

JNIEXPORT jstring JNICALL
Java_com_you4me_you4me_utils_UtilityParam_getApiKey(JNIEnv *env, jobject thiz) {
    return (*env) ->NewStringUTF(env, "12b30ff3-dc7d-4c0e-ae3f-2b0ce77818f6");
}

JNIEXPORT jstring JNICALL
Java_com_you4me_you4me_utils_UtilityParam_getBaseUrl(JNIEnv *env, jobject thiz) {
    return (*env) ->NewStringUTF(env, "https://j3rmo1mo2a.execute-api.us-east-1.amazonaws.com/prod/");
}