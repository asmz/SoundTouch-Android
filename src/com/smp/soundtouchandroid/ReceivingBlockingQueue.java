package com.smp.soundtouchandroid;

import java.util.concurrent.ArrayBlockingQueue;

import com.smp.soundtouchandroid.SoundTouchPlayableBase.AudioReceiver;

public class ReceivingBlockingQueue extends ArrayBlockingQueue<Byte> implements AudioReceiver
{
	private static final long serialVersionUID = 956871709771983993L;

	public ReceivingBlockingQueue(int capacity)
	{
		super(capacity);
	}

	@Override
	public int write(byte[] audioData, int offsetInBytes, int sizeInBytes)
	{
		int bytesWritten = 0;
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
