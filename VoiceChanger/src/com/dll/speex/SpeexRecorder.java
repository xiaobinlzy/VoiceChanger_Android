package com.dll.speex;

import java.nio.ShortBuffer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.dll.speex.encode.SpeexEncoder;
import com.dll.voicechanger.SoundTouch;
import com.dll.voicechanger.VoiceChanger;

public class SpeexRecorder implements Runnable {
    // private Logger log = LoggerFactory.getLogger(SpeexRecorder.class);
    private volatile boolean isRecording;
    private final Object mutex = new Object();
    private static final int frequency = 8000;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static int packagesize = 160;
    private String fileName = null;
    private float volume;
    private int speechTime;
    private int mRecorderTimeout;
    private boolean mIsTimeout;
    private boolean mHasException;
    private VoiceChanger mVoiceChanger;

    private SpeexRecorderListener mListener;

    public SpeexRecorder(String fileName) {
	super();
	this.fileName = fileName;
    }

    /**
     * ���¼��ʱ��
     * 
     * @return
     */
    public int getSpeechTime() {
	return speechTime;
    }

    public String getFileName() {
	return fileName;
    }

    public void setRecorderListener(SpeexRecorderListener listener) {
	this.mListener = listener;
    }

    public void run() {
	// ���������߳�
	mHasException = false;
	SpeexEncoder encoder = new SpeexEncoder(this.fileName);
	encoder.setRecorder(this);
	Thread encodeThread = new Thread(encoder);
	encoder.setRecording(true);
	encodeThread.start();
	long startTime = System.currentTimeMillis();
	mIsTimeout = false;
	synchronized (mutex) {
	    while (!this.isRecording) {
		try {
		    mutex.wait();
		} catch (InterruptedException e) {
		    throw new IllegalStateException("Wait() interrupted!", e);
		}
	    }
	}
	android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	int bufferRead = 0;
	int bufferSize = AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_IN_MONO,
		audioEncoding);
	short[] tempBuffer = new short[packagesize];
	ShortBuffer shortBuffer = ShortBuffer.allocate(packagesize);
	AudioRecord recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
		AudioFormat.CHANNEL_IN_MONO, audioEncoding, bufferSize);
	SoundTouch soundTouch = null;
	try {
	    if (mVoiceChanger != null) {
		soundTouch = new SoundTouch();
		soundTouch.setSampleRate(recordInstance.getSampleRate());
		soundTouch.setChannels(recordInstance.getChannelCount());
		soundTouch.setPitch(mVoiceChanger.getPitch());
		soundTouch.setRate(mVoiceChanger.getRate());
		soundTouch.setTempo(mVoiceChanger.getTempo());
	    }
	    recordInstance.startRecording();
	    if (mListener != null) {
		mListener.onRecordingStart(fileName);
	    }
	    while (this.isRecording) {
		// log.debug("start to recording.........");
		bufferRead = recordInstance.read(tempBuffer, 0, packagesize);
		if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
		    throw new IllegalStateException(
			    "read() returned AudioRecord.ERROR_INVALID_OPERATION");
		} else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
		    throw new IllegalStateException("read() returned AudioRecord.ERROR_BAD_VALUE");
		} else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
		    throw new IllegalStateException(
			    "read() returned AudioRecord.ERROR_INVALID_OPERATION");
		}
		// 计算音量和时间
		float volume = 0;
		for (short s : tempBuffer) {
		    volume += s * s;
		}
		this.volume = (float) Math.log(volume / bufferRead);
		speechTime = (int) (System.currentTimeMillis() - startTime);

		if (soundTouch != null) {
		    soundTouch.putSamples(tempBuffer, bufferRead);
		    short[] temp;
		    int tempOffset = 0;
		    while ((temp = soundTouch.receiveSamples()).length != 0) {
			while (tempOffset < temp.length) {
			    int writeLength = Math.min(shortBuffer.remaining(), temp.length
				    - tempOffset);
			    shortBuffer.put(temp, tempOffset, writeLength);
			    tempOffset += writeLength;
			    if (shortBuffer.remaining() == 0) {
				encoder.putData(shortBuffer.array(), shortBuffer.limit());
				shortBuffer.clear();
			    }
			}
		    }
		} else {
		    encoder.putData(tempBuffer, bufferRead);
		}
		if (mRecorderTimeout > 0 && speechTime > mRecorderTimeout) {
		    mIsTimeout = true;
		    break;
		}
	    }
	    short[] writeArray = shortBuffer.array();
	    encoder.putData(writeArray, writeArray.length);
	    recordInstance.stop();
	} catch (Exception e) {
	    e.printStackTrace();
	    mHasException = true;
	} finally {
	    if (soundTouch != null) {
		soundTouch.release();
		soundTouch = null;
	    }
	    // tell encoder to stop.
	    encoder.setRecording(false);
	    if (recordInstance != null) {
		recordInstance.release();
		recordInstance = null;
	    }
	}
    }

    public SpeexRecorderListener getRecorderListener() {
	return mListener;
    }

    public boolean isTimeout() {
	return mIsTimeout;
    }

    public void setTimeout(int milliSecond) {
	this.mRecorderTimeout = milliSecond;
    }

    public boolean hasException() {
	return mHasException;
    }

    public void setRecording(boolean isRecording) {
	synchronized (mutex) {
	    this.isRecording = isRecording;
	    if (this.isRecording) {
		mutex.notify();
	    }
	}
    }

    public boolean isRecording() {
	synchronized (mutex) {
	    return isRecording;
	}
    }

    public float getVolume() {
	return volume;
    }

    public void setVoiceChanger(VoiceChanger voiceChanger) {
	this.mVoiceChanger = voiceChanger;
    }
}
