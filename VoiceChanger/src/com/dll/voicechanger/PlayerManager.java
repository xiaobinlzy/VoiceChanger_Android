package com.dll.voicechanger;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dll.speex.SpeexPlayer;
import com.dll.speex.SpeexPlayerListener;

/**
 * 负责播放的类。
 * 
 * @author DLL email: xiaobinlzy@163.com
 * 
 */
public class PlayerManager {

    private PlayerManagerHandler mHandler = new PlayerManagerHandler();

    private boolean mIsPlaying = false;
    private SpeexPlayer mSpeexPlayer;

    private int mStream = AudioManager.STREAM_MUSIC;

    private AudioManager audioManager;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float distance;
    private int originAudioMode;
    private boolean isOroginSpeekerPhoneOn;
    private PlayerManagerCallback mCallback = new PlayerManagerCallback();

    public PlayerManager() {
    }

    /**
     * 如果要根据距离感应切换听筒和扬声器放音，在{@link Activity#onResume()}中调用{@link #registSensorManager(Activity)}
     * 注册监听， 在 {@link Activity#onPause()}中调用{@link #unregistSensorManager()}解除监听。
     * 
     * @param activity
     *            需要监听距离的Acitivity
     */
    public void registSensorManager(Activity activity) {
	mCallback.mWrActivity = new WeakReference<>(activity);
	audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
	isOroginSpeekerPhoneOn = audioManager.isSpeakerphoneOn();
	audioManager.setSpeakerphoneOn(false);
	originAudioMode = audioManager.getMode();
	sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
	sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	sensorManager.registerListener(mCallback, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 如果要根据距离感应切换听筒和扬声器放音，在{@link Activity#onResume()}中调用{@link #registSensorManager(Activity)}
     * 注册监听， 在 {@link Activity#onPause()}中调用{@link #unregistSensorManager()}解除监听。
     */
    public void unregistSensorManager() {
	audioManager.setSpeakerphoneOn(isOroginSpeekerPhoneOn);
	audioManager.setMode(originAudioMode);
	Activity activity = mCallback.mWrActivity.get();
	if (activity != null) {
	    activity.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
	    sensorManager.unregisterListener(mCallback);
	    mCallback.mWrActivity = null;
	}
    }

    /**
     * 是否正在播放音频。
     * 
     * @return
     */
    public boolean isPlaying() {
	return mIsPlaying;
    }

    /**
     * 播放音频
     * 
     * @param filePath
     *            传入文件路径
     * @param isForceStart
     *            是否强制开始。当当前正在播放音频时，若为true则先停止之前的播放，若为false则不停止之前的播放并且放弃这次播放。
     */
    public synchronized void startPlaying(String filePath, boolean isForceStart) {
	if (mIsPlaying) {
	    if (isForceStart) {
		stop(false);
	    } else {
		return;
	    }
	}
	File file = new File(filePath);
	if (!file.exists() || !file.isFile()) {
	    mHandler.obtainMessage(PlayerManagerHandler.HANDLE_PLAYING_FAILED, filePath)
		    .sendToTarget();
	    return;
	}

	mIsPlaying = true;
	mSpeexPlayer = new SpeexPlayer(file);
	mSpeexPlayer.setPlayerListener(mCallback);
	mSpeexPlayer.startPlay(mStream);
    }

    /**
     * 停止播放当前的音频
     */
    public synchronized void stopPlaying() {
	stop(true);
    }

    private void stop(boolean needCallback) {
	if (mIsPlaying && mSpeexPlayer != null) {
	    mSpeexPlayer.stopPlay();
	    if (needCallback) {
		mHandler.obtainMessage(PlayerManagerHandler.HANDLE_PLAYING_FINISHED,
			mSpeexPlayer.getFileName()).sendToTarget();
	    }
	    mSpeexPlayer = null;
	    mIsPlaying = false;
	}
    }

    /**
     * 设置播放的监听对象
     * 
     * @param listener
     */
    public void setPlayerListener(PlayerListener listener) {
	this.mHandler.mWrListener = new WeakReference<PlayerListener>(listener);
    }

    /**
     * 设置声音播放使用的音频流，默认是{@link AudioManager#STREAM_MUSIC}。
     * 
     * @param stream
     */
    public void setStream(int stream) {
	this.mStream = stream;
    }

    /**
     * 获取声音播放使用的音频流，默认是{@link AudioManager#STREAM_MUSIC}。
     * 
     * @return
     */
    public int getStream() {
	return mStream;
    }

    private void onPlayStop() {
	mIsPlaying = false;
    }

    private class PlayerManagerCallback implements SpeexPlayerListener, SensorEventListener {

	private WeakReference<Activity> mWrActivity;

	@Override
	public void onPlayerFinished(String fileName) {
	    onPlayStop();
	    mHandler.obtainMessage(PlayerManagerHandler.HANDLE_PLAYING_FINISHED, fileName)
		    .sendToTarget();
	}

	@Override
	public void onPlayerFailed(String fileName) {
	    onPlayStop();
	    mHandler.obtainMessage(PlayerManagerHandler.HANDLE_PLAYING_FAILED, fileName)
		    .sendToTarget();

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
	    Activity activity = null;
	    if (mWrActivity == null || (activity = mWrActivity.get()) == null) {
		return;
	    }
	    distance = event.values[0];
	    if (distance >= sensor.getMaximumRange()) {
		audioManager.setMode(AudioManager.MODE_NORMAL);
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	    } else {
		audioManager.setMode(AudioManager.MODE_IN_CALL);
		activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	    }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
    }

    private static class PlayerManagerHandler extends Handler {

	private static final int HANDLE_PLAYING_FINISHED = 1;
	private static final int HANDLE_PLAYING_FAILED = 2;
	private static final int HANDLE_PLAYING_STOPED = 3;
	private WeakReference<PlayerListener> mWrListener;

	public PlayerManagerHandler() {
	    super(Looper.getMainLooper());
	}

	@Override
	public void handleMessage(Message msg) {
	    super.handleMessage(msg);
	    PlayerListener listener = null;
	    if (mWrListener == null || (listener = mWrListener.get()) == null) {
		return;
	    }
	    String fileName = (String) msg.obj;
	    switch (msg.what) {
	    case HANDLE_PLAYING_FINISHED:
		listener.onPlayerFinished(fileName);
		break;
	    case HANDLE_PLAYING_FAILED:
		listener.onPlayerFailed(fileName);
		break;
	    case HANDLE_PLAYING_STOPED:
		listener.onPlayerStoped(fileName);
		break;
	    }
	}
    }
}
