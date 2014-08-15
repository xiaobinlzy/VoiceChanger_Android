////////////////////////////////////////////////////////////////////////////////
///
/// Example Interface class for SoundTouch native compilation
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// WWW           : http://www.surina.net
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: soundtouch-jni.cpp 173 2013-06-15 11:44:11Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////

#include <jni.h>
#include <android/log.h>

#include "include/SoundTouch.h"

#define LOGV(...)   __android_log_print((int)ANDROID_LOG_INFO, "SOUNDTOUCH", __VA_ARGS__)
//#define LOGV(...)

#define DLL_PUBLIC __attribute__ ((visibility ("default")))

using namespace soundtouch;



extern "C"
JNIEXPORT jstring JNICALL Java_com_dll_voicechanger_SoundTouch_getVersionString
  (JNIEnv *env, jobject thiz) {
	const char *verStr;

	// Call example SoundTouch routine
	verStr = SoundTouch::getVersionString();

	// return version as string
	return env->NewStringUTF(verStr);
}

extern "C"
JNIEXPORT jlong JNICALL Java_com_dll_voicechanger_SoundTouch_initSoundTouchObject
  (JNIEnv *env, jobject thiz) {
	SoundTouch* soundTouch = new SoundTouch();
	soundTouch->setSetting(SETTING_SEQUENCE_MS, 40);
	soundTouch->setSetting(SETTING_SEEKWINDOW_MS, 15);
	soundTouch->setSetting(SETTING_OVERLAP_MS, 8);
	return (jlong) soundTouch;
}

extern "C"
JNIEXPORT void JNICALL Java_com_dll_voicechanger_SoundTouch_freeSoundTouchObject
  (JNIEnv *env, jobject thiz, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	if (soundTouch != 0) {
		delete(soundTouch);
	}
}

extern "C"
JNIEXPORT void JNICALL Java_com_dll_voicechanger_SoundTouch_setSampleRate
  (JNIEnv *env, jobject thiz, jint sampleRate, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	soundTouch->setSampleRate(sampleRate);
}

extern "C"
JNIEXPORT void JNICALL Java_com_dll_voicechanger_SoundTouch_setChannels
  (JNIEnv *env, jobject thiz, jint channels, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	soundTouch->setChannels(channels);
}

extern "C"
JNIEXPORT void JNICALL Java_com_dll_voicechanger_SoundTouch_setTempo
  (JNIEnv *env, jobject thiz, jfloat tempo, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	soundTouch->setTempo(tempo);
}

extern "C"
JNIEXPORT void JNICALL Java_com_dll_voicechanger_SoundTouch_setPitch
  (JNIEnv *env, jobject thiz, jfloat pitch, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	soundTouch->setPitchSemiTones(pitch);
}

extern "C"
JNIEXPORT void JNICALL Java_com_dll_voicechanger_SoundTouch_setRate
  (JNIEnv *env, jobject thiz, jfloat rate, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	soundTouch->setRate(rate);
}

extern "C"
JNIEXPORT void JNICALL Java_com_dll_voicechanger_SoundTouch_putSamples
  (JNIEnv *env, jobject thiz, jshortArray samples, jint length, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	// 转换为本地数组
	jshort *input_samples = env->GetShortArrayElements(samples, NULL);
	soundTouch->putSamples(input_samples, length);
	// 释放本地数组(避免内存泄露)
	env->ReleaseShortArrayElements(samples, input_samples, 0);
}


#define BUFFER_SIZE 4096

extern "C"
JNIEXPORT jshortArray JNICALL Java_com_dll_voicechanger_SoundTouch_receiveSamples
  (JNIEnv *env, jobject thiz, jlong objectPtr) {
	SoundTouch *soundTouch = (SoundTouch *)objectPtr;
	short buffer[BUFFER_SIZE];
	jint nSamples = soundTouch->receiveSamples(buffer, BUFFER_SIZE);
	// 局部引用，创建一个short数组
	jshortArray receiveSamples = env->NewShortArray(nSamples);
	// 给short数组设置值
	env->SetShortArrayRegion(receiveSamples, 0, nSamples, buffer);

	return receiveSamples;
}
