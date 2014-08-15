package com.dll.speex;

public interface SpeexPlayerListener {
    /**
     * 播放完成时回调。
     * 
     * @param fileName
     */
    public void onPlayerFinished(String fileName);

    /**
     * 播放失败时回调。
     * 
     * @param fileName
     */
    public void onPlayerFailed(String fileName);
}
