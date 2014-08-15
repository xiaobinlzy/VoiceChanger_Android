////////////////////////////////////////////////////////////////////////////////
///
/// Example class that invokes native SoundTouch routines through the JNI
/// interface.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// WWW           : http://www.surina.net
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: SoundTouch.java 165 2012-12-28 19:55:23Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////

package com.dll.voicechanger;

public final class SoundTouch {
    // Load the native library upon startup
    
    private long mObjectPtr;

    static {
	System.loadLibrary("soundtouch");
    }

    public static SoundTouch getInstance() {
	return new SoundTouch();
    }

    /**
     * 记得调用{@link #release()}释放内存
     */
    public SoundTouch() {
	mObjectPtr = initSoundTouchObject();
    }

    // Native interface function that returns SoundTouch version string.
    // This invokes the native c++ routine defined in "soundtouch-jni.cpp".

    private native long initSoundTouchObject();

    private native void freeSoundTouchObject(long objectPtr);

    private native final String getVersionString();

    private native void setSampleRate(int sampleRate, long objectPtr);

    private native void setChannels(int channel, long objectPtr);

    private native void setTempo(float newTempo, long objectPtr);

    private native void setPitch(float newPitch, long objectPtr);

    private native void setRate(float newRate, long objectPtr);

    private native void putSamples(short[] samples, int length, long objectPtr);

    private native short[] receiveSamples(long objectPtr);
    
    public void setSampleRate(int sampleRate) {
	setSampleRate(sampleRate, mObjectPtr);
    }
    
    public void setChannels(int channel) {
	setChannels(channel, mObjectPtr);
    }
    
    public void setTempo(float newTempo) {
	setTempo(newTempo, mObjectPtr);
    }
    
    public void setPitch(float newPitch) {
	setPitch(newPitch, mObjectPtr);
    }
    
    public void setRate(float newRate) {
	setRate(newRate, mObjectPtr);
    }
    
    public void putSamples(short[] samples, int length) {
	putSamples(samples, length, mObjectPtr);
    }
    
    public short[] receiveSamples() {
	return receiveSamples(mObjectPtr);
    }
    
    public synchronized void release() {
	freeSoundTouchObject(mObjectPtr);
	mObjectPtr = 0;
    }
}
