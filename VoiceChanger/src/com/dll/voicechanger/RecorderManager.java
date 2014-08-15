package com.dll.voicechanger;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dll.speex.SpeexRecorder;
import com.dll.speex.SpeexRecorderListener;
import com.dll.util.FilePathUtil;

/**
 * 负责录音的类，单例，使用{@link #getInstance(Context)}获取对象。
 * 
 * @author DLL email: xiaobinlzy@163.com
 * 
 */
public class RecorderManager {

    private static final int MAX_RECORD_MILLIS_SECOND = 60000;
    private static RecorderManager gRecorderManager;
    private Context mContext;
    private boolean mIsRecording = false;
    private SpeexRecorder mRecorder;
    private int mTimeoutMillisecond;
    private RecorderHandler mHandler = new RecorderHandler();

    private RecorderManagerListener mRecorderListener = new RecorderManagerListener();

    private RecorderManager(Context context) {
	this.mContext = context.getApplicationContext();
    }

    /**
     * 获取{@link RecorderManager}对象实例。
     * 
     * @param context
     * @return 返回RecorderManager单例。
     */
    public static RecorderManager getInstance(Context context) {
	if (gRecorderManager == null) {
	    gRecorderManager = new RecorderManager(context);
	}
	return gRecorderManager;
    }

    /**
     * 是否正在录音
     * 
     * @return
     */
    public boolean isRecording() {
	return mIsRecording;
    }

    /**
     * 开始录音，会自动在后台异步执行
     * 
     * @param filePath
     *            存储声音文件的路径
     * @param voiceChanger
     *            变声，如果不变声可以传null。
     */
    public synchronized void startRecording(String filePath, VoiceChanger voiceChanger) {
	if (mIsRecording || mRecorder != null) {
	    return;
	}
	mIsRecording = true;
	mRecorder = new SpeexRecorder(filePath);
	mRecorder.setVoiceChanger(voiceChanger);
	mRecorder.setTimeout(getTimeoutMillisecond());
	mRecorder.setRecorderListener(mRecorderListener);
	mRecorder.setRecording(true);
	new Thread(mRecorder).start();
    }

    /**
     * 停止录音
     */
    public synchronized void stopRecording() {
	if (!mIsRecording || mRecorder == null) {
	    return;
	}
	mRecorder.setRecording(false);
	mRecorder = null;
	mIsRecording = false;
    }

    /**
     * 设置回调接口
     * 
     * @param listener
     */
    public void setRecorderListener(RecorderListener listener) {
	mHandler.mWrListener = new WeakReference<RecorderListener>(listener);
    }

    /**
     * 录音超时时间
     * 
     * @param timeoutSecond
     */
    public void setTimeoutSecond(int timeoutSecond) {
	this.mTimeoutMillisecond = timeoutSecond;
    }

    /**
     * 获取默认的语音文件路径
     * 
     * @return 默认文件路径
     */
    public String getDefaultFilePath() {
	return FilePathUtil.makeFilePath(mContext, "voice", System.currentTimeMillis() + ".spx");
    }

    /**
     * 获取当前录音音量
     * 
     * @return 当前录音音量
     */
    public float getVolumn() {
	return mRecorder == null || !mIsRecording ? 0 : mRecorder.getVolume();
    }

    /**
     * 获取当前录音超时时长
     * 
     * @return
     */
    public int getTimeoutMillisecond() {
	return mTimeoutMillisecond == 0 ? MAX_RECORD_MILLIS_SECOND : mTimeoutMillisecond;
    }

    private class RecorderManagerListener implements SpeexRecorderListener {

	@Override
	public void onRecordingFinished(String filePath, int millisecond) {
	    mIsRecording = false;
	    mHandler.obtainMessage(RecorderHandler.HANDLE_RECORDING_FINISH, millisecond, 0,
		    filePath).sendToTarget();
	}

	@Override
	public void onRecordingFailed() {
	    mRecorder = null;
	    mIsRecording = false;
	    mHandler.obtainMessage(RecorderHandler.HANDLE_RECORDING_FAILED).sendToTarget();
	}

	@Override
	public void onRecordingStart(String fileName) {
	    mHandler.obtainMessage(RecorderHandler.HANDLE_RECORDING_START, fileName).sendToTarget();
	}

	@Override
	public void onRecordingTimeout(String filePath) {
	    mHandler.obtainMessage(RecorderHandler.HANDLE_RECORDING_TIMEOUT, filePath)
		    .sendToTarget();
	}
    }

    private static class RecorderHandler extends Handler {

	private static final int HANDLE_RECORDING_FINISH = 1;
	private static final int HANDLE_RECORDING_FAILED = 2;
	private static final int HANDLE_RECORDING_START = 3;
	private static final int HANDLE_RECORDING_TIMEOUT = 4;
	private WeakReference<RecorderListener> mWrListener;

	public RecorderHandler() {
	    super(Looper.getMainLooper());
	}

	@Override
	public void handleMessage(Message msg) {
	    super.handleMessage(msg);
	    RecorderListener listener = null;
	    if (mWrListener == null || (listener = mWrListener.get()) == null) {
		return;
	    }
	    switch (msg.what) {
	    case HANDLE_RECORDING_FAILED:
		listener.onRecordingFailed();
		break;
	    case HANDLE_RECORDING_FINISH:
		listener.onRecordingFinished((String) msg.obj, msg.arg1);
		break;
	    case HANDLE_RECORDING_START:
		listener.onRecordingStart((String) msg.obj);
		break;
	    case HANDLE_RECORDING_TIMEOUT:
		listener.onRecordingTimeout((String) msg.obj);
		break;
	    }
	}
    }
}
