/**
 * 
 */
package com.dll.speex;

import java.io.File;

import com.dll.speex.encode.SpeexDecoder;

/**
 * @author Gauss
 * 
 */
public class SpeexPlayer {
    private File file = null;
    private SpeexDecoder speexdec = null;
    private boolean isPlaying;
    private SpeexPlayerListener mListener;

    public SpeexPlayer(File file) {

	this.file = file;
	try {
	    speexdec = new SpeexDecoder(this.file);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void startPlay(int streamMode) {
	RecordPlayThread rpt = new RecordPlayThread(streamMode);

	Thread th = new Thread(rpt);
	th.start();
    }

    public void stopPlay() {
	if (speexdec != null) {
	    speexdec.stop();
	}
    }

    public boolean isPlaying() {
	return isPlaying;
    }

    public void setPlayerListener(SpeexPlayerListener listener) {
	this.mListener = listener;
    }

    class RecordPlayThread extends Thread {
	private int streamMode;

	public RecordPlayThread(int streamMode) {
	    this.streamMode = streamMode;
	}

	public void run() {
	    boolean hasException = false;
	    try {
		if (speexdec != null) {
		    isPlaying = true;
		    speexdec.decode(streamMode);
		}
	    } catch (Exception t) {
		hasException = true;
		t.printStackTrace();
	    } finally {
		isPlaying = false;
		if (mListener != null) {
		    if (hasException) {
			mListener.onPlayerFailed(getFileName());
		    } else {
			mListener.onPlayerFinished(getFileName());
		    }
		}
	    }
	}
    };

    public String getFileName() {
	return file.getAbsolutePath();
    }
}
