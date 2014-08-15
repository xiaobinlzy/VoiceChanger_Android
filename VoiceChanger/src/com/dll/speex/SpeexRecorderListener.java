package com.dll.speex;

/**
 * Speex录音回调接口
 * 
 * @author DLL email: xiaobinlzy@163.com
 * 
 */
public interface SpeexRecorderListener {

    /**
     * 录音完成之后的回调方法。
     * 
     * @param filePath
     *            声音文件路径
     * @param millisecond
     *            录音时长
     */
    public void onRecordingFinished(String filePath, int millisecond);

    /**
     * 录音失败时的回调方法。
     * 
     */
    public void onRecordingFailed();

    /**
     * 初始化完成，录音开始的回调方法。
     */
    public void onRecordingStart(String filePath);

    /**
     * 录音时间超时回调方法。
     * 
     * @param filePath
     *            生成的录音文件
     */
    public void onRecordingTimeout(String filePath);
}
