#include <jni.h>




JNIEXPORT jstring JNICALL
Java_com_you4me_you4me_utils_UtilityParam_getGooglePlacesSecretKey(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "AIzaSyAjzOVHaTnlRwL6ArgGsRXOAt3PCJ5FujI");
}

JNIEXPORT jstring JNICALL
Java_com_you4me_you4me_utils_UtilityParam_getMixpanelSecretKey(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "d1800cfe7956f69cf896a35dd8653f17");
}