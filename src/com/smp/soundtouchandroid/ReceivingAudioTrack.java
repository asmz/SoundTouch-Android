package com.smp.soundtouchandroid;

import com.smp.soundtouchandroid.SoundTouchPlayableBase.AudioReceiver;

import android.media.AudioTrack;


public class ReceivingAudioTrack implements AudioReceiver
{
	private AudioTrack track;
	
	public ReceivingAudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode) throws IllegalArgumentException
	{
		track = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
	}

	@Override
	public int writeAudioData(byte[] audioData, int offsetInBytes, int sizeInBytes)
	{
		return track.write(audioData, offsetInBytes, sizeInBytes);
	}
	
	
	public void play()
	{
		track.play();
	}
	
	public void pause()
	{
		track.pause();
	}
	
	public void flush()
	{
		track.flush();
	}
	
	public int setStereoVolume(float left, float right)
	{
		return track.setStereoVolume(left, right);
	}
	
	public void release()
	{
		track.release();
	}
	
	public int getSessionId()
	{
		return track.getAudioSessionId();
	}
	
}
