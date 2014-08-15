package com.dll.voicechanger;

/**
 * 播放事件监听接口
 * 
 * @author DLL email: xiaobinlzy@163.com
 * 
 */
public interface PlayerListener {

    /**
     * 播放完成时回调，总是在主线程中被调用。
     * 
     * @param fileName
     */
    public void onPlayerFinished(String fileName);

    /**
     * 播放失败时回调，总是在主线程中被调用。
     * 
     * @param fileName
     */
    public void onPlayerFailed(String fileName);

    /**
     * 播放被人为停止时回调，总是在主线程中被调用。
     * 
     * @param fileName
     */
    public void onPlayerStoped(String fileName);
}
