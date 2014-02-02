package com.smp.soundtouchandroid;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.smp.soundtouchandroid.SoundTouchPlayableBase.AudioConsumer;

public class AudioConsumingBlockingQueue extends LinkedBlockingQueue<Byte> implements AudioConsumer
{
	private static final long serialVersionUID = 956871709771983993L;

	public AudioConsumingBlockingQueue(int capacity)
	{
		super(capacity);
	}

	@Override
	public int write(byte[] audioData, int offsetInBytes, int sizeInBytes)
	{
		int bytesWritten = 0;
		if (offsetInBytes == 0 && sizeInBytes == audioData.length)
		{
			//
		}
		for(int i = offsetInBytes; i < sizeInBytes; ++i)
		{
			try
			{
				put(audioData[i]);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return bytesWritten;
	}

}
