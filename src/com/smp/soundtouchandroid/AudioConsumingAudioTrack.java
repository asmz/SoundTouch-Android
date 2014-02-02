package com.smp.soundtouchandroid;

import com.smp.soundtouchandroid.SoundTouchPlayableBase.AudioConsumer;

import android.media.AudioTrack;


public class AudioConsumingAudioTrack extends AudioTrack implements AudioConsumer
{
	public AudioConsumingAudioTrack(int streamType, int sampleRateInHz, int channelConfig, 
			int audioFormat, int bufferSizeInBytes, int mode) throws IllegalArgumentException
	{
		super(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
	}
}
