package com.dll.voicechanger;

/**
 * 负责记录改变声音参数的类
 * 
 * @author DLL email: xiaobinlzy@163.com
 * 
 */
public class VoiceChanger {

    private float mRate = 1;
    private float mTempo = 1;
    private float mPitch = 0;

    public VoiceChanger() {
    }

    /**
     * 设置速率，变速变声
     * 
     * @param rate
     *            音频变化率，标准值为1
     */
    public void setRate(float rate) {
	this.mRate = rate;
    }

    /**
     * 设置语速，变速不变声
     * 
     * @param tempo
     *            语速变化率，标准值为1
     */
    public void setTempo(float tempo) {
	this.mTempo = tempo;
    }

    /**
     * 设置音高
     * 
     * @param pitch
     *            音高变化，从-12到12，标准值为0
     */
    public void setPitch(float pitch) {
	this.mPitch = pitch;
    }

    /**
     * 获取音频速率
     * 
     * @return
     */
    public float getRate() {
	return mRate;
    }

    /**
     * 获取变调
     * 
     * @return
     */
    public float getPitch() {
	return mPitch;
    }

    /**
     * 获取语速
     * 
     * @return
     */
    public float getTempo() {
	return mTempo;
    }
}
