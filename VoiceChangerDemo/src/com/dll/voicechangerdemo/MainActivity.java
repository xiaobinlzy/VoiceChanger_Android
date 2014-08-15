package com.dll.voicechangerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dll.voicechanger.PlayerListener;
import com.dll.voicechanger.PlayerManager;
import com.dll.voicechanger.RecorderListener;
import com.dll.voicechanger.RecorderManager;
import com.dll.voicechanger.VoiceChanger;

public class MainActivity extends Activity implements RecorderListener, PlayerListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mButtonRecord;
    private Button mButtonPlay;
    private RecorderManager mRecorderManager;
    private PlayerManager mPlayerManager;
    private VoiceChanger mVoiceChanger = new VoiceChanger();

    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	findView();

	mRecorderManager = RecorderManager.getInstance(this);
	mRecorderManager.setRecorderListener(this);

	mPlayerManager = new PlayerManager();
	mPlayerManager.setPlayerListener(this);
    }

    private void findView() {
	mButtonPlay = (Button) findViewById(R.id.button_play);
	mButtonRecord = (Button) findViewById(R.id.button_record);
    }

    public void onClickRecord(View view) {
	mButtonRecord.setEnabled(false);
	if (!mRecorderManager.isRecording()) {
	    mVoiceChanger.setPitch(5);
	    mVoiceChanger.setTempo(0.6f);
	    mVoiceChanger.setRate(1f);
	    mRecorderManager.startRecording(mRecorderManager.getDefaultFilePath(), mVoiceChanger);
	} else {
	    mRecorderManager.stopRecording();
	}
    }

    public void onClickPlay(View view) {
	if (mFilePath == null) {
	    Toast.makeText(this, "您还没录音", Toast.LENGTH_SHORT).show();
	    return;
	}
	if (!mPlayerManager.isPlaying()) {
	    mPlayerManager.startPlaying(mFilePath, true);
	    mButtonPlay.setText(R.string.button_stopPlaying);
	} else {
	    mPlayerManager.stopPlaying();
	    mButtonPlay.setText(R.string.button_startPlaying);
	}
    }

    @Override
    public void onPlayerFinished(String fileName) {
	Log.i(TAG, "播放完成");
	mButtonPlay.setText(R.string.button_startPlaying);

    }

    @Override
    public void onPlayerFailed(String fileName) {
	Log.i(TAG, "播放失败");
	mButtonPlay.setText(R.string.button_startPlaying);

    }

    @Override
    public void onPlayerStoped(String fileName) {
	Log.i(TAG, "播放停止");
	mButtonPlay.setText(R.string.button_startPlaying);
    }

    @Override
    public void onRecordingFinished(String filePath, int millisecond) {
	Log.i(TAG, "录音完成：" + filePath + "\n时长：" + millisecond);
	this.mFilePath = filePath;
	mButtonRecord.setEnabled(true);
	mButtonRecord.setText(R.string.button_startRecording);
    }

    @Override
    public void onRecordingFailed() {
	Log.i(TAG, "录音失败");
	mButtonRecord.setEnabled(true);
	mButtonRecord.setText(R.string.button_startRecording);
    }

    @Override
    public void onRecordingStart(String filePath) {
	Log.i(TAG, "录音开始");
	mButtonRecord.setEnabled(true);
	mButtonRecord.setText(R.string.button_stopRecording);
    }

    @Override
    public void onRecordingTimeout(String filePath) {
	Log.i(TAG, "录音超时：" + filePath);
	this.mFilePath = filePath;
	mButtonRecord.setEnabled(true);
	mButtonRecord.setText(R.string.button_startRecording);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayerManager.registSensorManager(this);
    }
    
    @Override
    protected void onPause() {
	mPlayerManager.unregistSensorManager();
        super.onPause();
    }
}
