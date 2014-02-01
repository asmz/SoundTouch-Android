package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.*;

import java.io.IOException;
import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

public abstract class SoundTouchPlayableBase implements Runnable
{
	public interface PlaybackProgressListener
	{
		void onProgressChanged(int track, double currentPercentage, long position);
	}

	public interface AudioReceiver
	{
		int write(byte[] audioData, int offsetInBytes, int sizeInBytes);
	}

	private Object pauseLock;
	private Object decodeLock;

	private Handler handler;
	private PlaybackProgressListener playbackListener;
	private SoundTouch soundTouch;
	private Mp3Decoder decoder;
	private AudioReceiver audioReceiver;
	private String fileName;
	private int id;

	private volatile boolean paused, finished;
	
	protected int channels, samplingRate;

	public void setTempo(float tempo)
	{
		soundTouch.setTempo(tempo);
	}

	public void setTempoChange(float tempoChange)
	{
		soundTouch.setTempoChange(tempoChange);
	}

	public void setPitchSemi(float pitchSemi)
	{
		soundTouch.setPitchSemi(pitchSemi);
	}

	public String getFileName()
	{
		return fileName;
	}

	public boolean isPaused()
	{
		return paused;
	}

	public long getDuration()
	{
		return decoder.getDuration();
	}
	
	protected void initDecoder(String fileName) throws IOException
	{
		if (Build.VERSION.SDK_INT >= 16)
		{
			decoder = new MediaCodecMp3Decoder(fileName);
		}
		else
		{
			decoder = new JLayerMp3Decoder(fileName);
		}
		
		channels = decoder.getChannels();
		samplingRate = decoder.getSamplingRate();
	}
	protected void init(PlaybackProgressListener playbackListener, AudioReceiver receiver, String fileName, 
			int id, float tempo, float pitchSemi) throws IOException
	{
		this.playbackListener = playbackListener;
		init(receiver, fileName, id, tempo, pitchSemi);
	}
	
	protected void init(AudioReceiver audioReceiver, String fileName, int id, float tempo, float pitchSemi) 
	{	
		this.audioReceiver = audioReceiver;
		this.fileName = fileName;
		this.id = id;

		handler = new Handler();

		pauseLock = new Object();
		decodeLock = new Object();

		paused = true;
		finished = false;

		setupSoundTouch(id, tempo, pitchSemi);
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

	@Override
	public void run()
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		try
		{
			while (!finished)
			{
				playFile();

				paused = true;
				decoder.resetEOS();
			}
		}
		catch (SoundTouchAndroidException e)
		{
			// need to notify...something?
			e.printStackTrace();
		}
		finally
		{
			soundTouch.clearBuffer();
			/*
			 * synchronized (trackLock) { track.pause(); track.flush();
			 * track.release(); }
			 */
			decoder.close();
		}
	}

	public void seekTo(double percentage) // 0.0 - 1.0
	{
		long timeInUs = (long) (decoder.getDuration() * percentage);
		seekTo(timeInUs);
	}

	public void seekTo(long timeInUs)
	{
		if (timeInUs < 0 || timeInUs >= decoder.getDuration())
			throw new SoundTouchAndroidException("" + timeInUs + " Not a valid seek time.");

		this.pause();

		soundTouch.clearBuffer();

		synchronized (decodeLock)
		{
			decoder.seek(timeInUs);
		}
	}

	public void play()
	{
		synchronized (pauseLock)
		{
			paused = false;
			finished = false;
			pauseLock.notifyAll();
		}
	}

	public void pause()
	{
		synchronized (pauseLock)
		{
			paused = true;
		}
	}

	public void stop()
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

	private void setupSoundTouch(int id, float tempo, float pitchSemi)
	{
		soundTouch = new SoundTouch(id, channels, samplingRate, DEFAULT_BYTES_PER_SAMPLE, tempo, pitchSemi);
	}

	private void playFile() throws SoundTouchAndroidException
	{
		int bytesReceived = 0;
		byte[] input = null;

		do
		{
			pauseWait();

			if (finished)
				break;

			bytesReceived = advancePlayback(input);
		}
		while (!decoder.sawOutputEOS());

		soundTouch.finish();

		do
		{
			if (finished)
				break;

			bytesReceived = processChunk(input, false);
		}
		while (bytesReceived > 0);
	}

	// return the number of bytes ready to be written
	private int advancePlayback(byte[] input)
	{
		if (soundTouch.getOutputBufferSize() <= MAX_OUTPUT_BUFFER_SIZE)
		{
			synchronized (decodeLock)
			{
				input = decoder.decodeChunk();

				if (playbackListener != null)
				{
					handler.post(new Runnable()
					{

						@Override
						public void run()
						{
							long pd = decoder.getPlayedDuration();
							long d = decoder.getDuration();
							double cp = pd == 0 ? 0 : (double) pd / d;
							playbackListener.onProgressChanged(id, cp, pd);
						}
					});
				}
			}

			return processChunk(input, true);
		}
		else
		{
			return processChunk(input, false);
		}
	}

	private int processChunk(final byte[] input, boolean putBytes) throws SoundTouchAndroidException
	{
		int bytesReceived = 0;

		if (input != null)
		{
			if (putBytes)
				soundTouch.putBytes(input);

			bytesReceived = soundTouch.getBytes(input);
		}
		
		audioReceiver.write(input, 0, bytesReceived);
		
		return bytesReceived;
	}
}
