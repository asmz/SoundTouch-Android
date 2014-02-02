package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.BUFFER_SIZE_TRACK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioMixer implements Runnable
{
	private static AudioMixer mixer;

	private static final int BUFFER_SIZE = 4096;
	private static final int MAX_TRACKS = 16;
	private static final int SAMPLING_RATE = 44100;
	private static final int CHANNEL_FORMAT = AudioFormat.CHANNEL_OUT_STEREO;

	private volatile boolean isRunning;
	private volatile boolean paused, finished;

	private AudioTrack track;
	private ExecutorService executor;

	private Object pauseLock;
	private Object trackLock;

	SoundTouchPlayableMultiPlay[] playables = new SoundTouchPlayableMultiPlay[MAX_TRACKS];

	private AudioMixer()
	{
		pauseLock = new Object();
		trackLock = new Object();

		paused = true;
		finished = false;

		executor = Executors.newFixedThreadPool(MAX_TRACKS);

		track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLING_RATE, CHANNEL_FORMAT,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE_TRACK, AudioTrack.MODE_STREAM);
	}
	
	public synchronized boolean isTrackAvailable(int id)
	{
		return playables[id] == null;
	}

	public synchronized void newTrack(String fileName, int id, float tempo, float pitchSemi)
			throws IOException
	{
		checkPlayable(id);
		playables[id] = new SoundTouchPlayableMultiPlay(fileName, id, tempo, pitchSemi);

		executor.execute(playables[id]);
	}

	private void checkPlayable(int id)
	{
		if (id < 0 || id >= MAX_TRACKS)
			throw new SoundTouchAndroidException("Invalid track number: " + track +
					". Valid track numbers are 0 through " + MAX_TRACKS + ".");
		if (playables[id] != null)
		{
			throw new SoundTouchAndroidException("Track " + track + " is already in use, " +
					"call stop() on it first.");
		}
	}

	public synchronized void stop(int id)
	{
		if (id < 0 && id < MAX_TRACKS
				&& playables[id] != null)
		{
			playables[id].stop();
			playables[id] = null;
		}
	}

	public synchronized void pause(int id)
	{
		if (id < 0 && id < MAX_TRACKS
				&& playables[id] != null)
		{
			playables[id].pause();
		}
	}

	public synchronized void stop()
	{
		if (paused)
		{
			synchronized (pauseLock)
			{
				paused = false;
				pauseLock.notifyAll();
			}
		}

		finished = true;
	}

	public synchronized void play()
	{
		for (int i = 0; i < playables.length; ++i)
		{
			if (playables[i] != null)
			{
				playables[i].play();
			}
		}

		synchronized (pauseLock)
		{
			synchronized (trackLock)
			{
				track.play();
			}
			paused = false;
			finished = false;
			pauseLock.notifyAll();
		}
	}

	public synchronized void pause()
	{
		synchronized (pauseLock)
		{
			synchronized (trackLock)
			{
				track.pause();
			}
			paused = true;
		}
		for (int i = 0; i < playables.length; ++i)
		{
			if (playables[i] != null)
			{
				playables[i].pause();
			}
		}
	}

	public synchronized void play(int id)
	{
		checkPlayable(id);
		if (playables[id] != null)
		{
			playables[id].stop();
			playables[id] = null;
		}
	}

	public static synchronized AudioMixer getMixer()
	{
		if (mixer == null)
		{
			mixer = new AudioMixer();
		}

		if (mixer.isRunning)
		{
			throw new SoundTouchAndroidException("AudioMixer is already running.");
		}

		return mixer;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		isRunning = true;

		try
		{
			while (!finished)
			{
				pauseWait();

				mixPlayables();

				paused = true;
			}
		}
		finally
		{
			for (int i = 0; i < playables.length; ++i)
			{
				if (playables[i] != null)
				{
					playables[i].stop();
					playables[i] = null;
				}
			}

			track.pause();
			track.flush();
			track.release();
		}

		isRunning = false;
	}

	private void mixPlayables()
	{
		byte[] output = new byte[BUFFER_SIZE];
		int bytesMixed = 0;

		while (!finished)
		{
			pauseWait();

			bytesMixed = mixChunk(output);

			track.write(output, 0, bytesMixed);
		}
	}

	private int mixChunk(byte[] output)
	{
		int bytesMixed = 0;

		for (int i = 0; i < output.length; ++i)
		{
			float mixed = 0;
			
			for (SoundTouchPlayableMultiPlay playable : playables)
			{
				if (playable != null)
				{
					Byte b = playable.poll();

					if (b == null)
						return bytesMixed;

					float sample = b / 128.0f;

					mixed += sample;

					mixed *= 0.8;

					if (mixed > 1.0f)
						mixed = 1.0f;

					if (mixed < -1.0f)
						mixed = -1.0f;

					byte outputSample = (byte) (mixed * 128.0f);
					output[i] = outputSample;
				}
			}

			++bytesMixed;
		}
		return bytesMixed;
	}

	private void pauseWait()
	{
		synchronized (pauseLock)
		{
			while (paused)
			{
				try
				{
					pauseLock.wait();
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}
}
