package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.BUFFER_SIZE_TRACK;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.smp.soundtouchandroid.Constants.*;
import com.smp.soundtouchandroid.SoundTouchPlayableBase.PlaybackProgressListener;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class SoundTouchPlayableMultiPlay extends SoundTouchPlayableBase
{	
	private static final int TIMEOUT_MS = 250;
	
	AudioConsumingBlockingQueue queue;
	{
		queue = new AudioConsumingBlockingQueue(MAX_OUTPUT_BUFFER_SIZE);
	}
	public SoundTouchPlayableMultiPlay(String fileName, int id, float tempo, float pitchSemi) throws IOException
	{
		initDecoder(fileName);
		init(queue, fileName, id, tempo, pitchSemi);	
	}
	public SoundTouchPlayableMultiPlay(PlaybackProgressListener playbackListener, String fileName, 
			int id, float tempo, float pitchSemi) throws IOException
	{
		initDecoder(fileName);
		init(playbackListener, queue, fileName, id, tempo, pitchSemi);
	}
	
	public Byte poll()
	{
		try
		{
			return queue.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
