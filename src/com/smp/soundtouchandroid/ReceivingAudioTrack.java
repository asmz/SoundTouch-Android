package com.smp.soundtouchandroid;

import com.smp.soundtouchandroid.SoundTouchPlayableBase.AudioReceiver;

import android.media.AudioTrack;


public class ReceivingAudioTrack extends AudioTrack implements AudioReceiver
{
	public ReceivingAudioTrack(int streamType, int sampleRateInHz, int channelConfig, 
			int audioFormat, int bufferSizeInBytes, int mode) throws IllegalArgumentException
	{
		super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
	}
}
