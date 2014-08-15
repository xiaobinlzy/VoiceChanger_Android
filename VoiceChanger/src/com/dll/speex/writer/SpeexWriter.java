package com.dll.speex.writer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.dll.speex.SpeexRecorder;
import com.dll.speex.SpeexRecorderListener;

/**
 * 
 * @author Gauss ʹ��OGG��װ��д�ļ�
 * 
 */
public class SpeexWriter implements Runnable {

    // private Logger log = LoggerFactory.getLogger(SpeexWriter.class);
    private final Object mutex = new Object();

    // д�ļ�
    private SpeexWriteClient client = new SpeexWriteClient();
    private volatile boolean isRecording;
    private processedData pData;
    private List<processedData> list;
    private SpeexRecorder mRecorder;

    public static int write_packageSize = 1024;

    public SpeexWriter(String fileName) {
	super();
	list = Collections.synchronizedList(new LinkedList<processedData>());

	client.setSampleRate(8000);

	client.start(fileName);
    }

    public void setRecorder(SpeexRecorder recorder) {
	this.mRecorder = recorder;
    }

    public void run() {
	// log.debug("write thread runing");
	while (this.isRecording() || list.size() > 0) {

	    if (list.size() > 0) {
		pData = list.remove(0);
		// gauss_packageSize/2
		// log.info("pData size=" + pData.size);

		client.writeTag(pData.processed, pData.size);

		// log.debug("list size = {}", list.size());
	    } else {
		try {
		    Thread.sleep(20);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	}

	// log.debug("write thread exit");
	stop();

	if (mRecorder != null) {
	    SpeexRecorderListener listener = mRecorder.getRecorderListener();
	    if (listener != null) {
		if (mRecorder.hasException()) {
		    listener.onRecordingFailed();
		} else if (mRecorder.isTimeout()) {
		    listener.onRecordingTimeout(mRecorder.getFileName());
		} else {
		    listener.onRecordingFinished(mRecorder.getFileName(), mRecorder.getSpeechTime());
		}
	    }
	}
    }

    public void putData(final byte[] buf, int size) {

	// log.debug("after convert. size=====================[640]:" + size);

	processedData data = new processedData();
	// data.ts = ts;
	data.size = size;
	System.arraycopy(buf, 0, data.processed, 0, size);
	list.add(data);
    }

    public void stop() {
	client.stop();
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

    class processedData {
	// private long ts;
	private int size;
	private byte[] processed = new byte[write_packageSize];
    }

}
