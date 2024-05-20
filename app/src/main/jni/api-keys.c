#include <jni.h>




JNIEXPORT jstring JNICALL
Java_com_you4me_you4me_utils_UtilityParam_getGooglePlacesSecretKey(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "AIzaSyAjzOVHaTnlRwL6ArgGsRXOAt3PCJ5FujI");
}