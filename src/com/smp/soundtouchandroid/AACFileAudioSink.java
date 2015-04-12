package com.smp.soundtouchandroid;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class AACFileAudioSink implements AudioSink
{
	private AudioEncoder encoder;
	private ExecutorService exec;
	private volatile boolean finishedWriting;

	public AACFileAudioSink(String fileNameOut, int samplingRate, int channels)
			throws IOException
	{
		encoder = new MediaCodecAudioEncoder(samplingRate, channels);
		exec = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void abort()
	{
		encoder.close();
		exec.shutdownNow();
	}
	@Override
	public int write(byte[] input, final int offSetInBytes,
			final int sizeInBytes) throws IOException
	{
		//Log.i("MP3", "before write submitted");
		final byte[] tmp = Arrays.copyOf(input, input.length);
		if (!exec.isShutdown())
		{
			exec.submit(new Runnable()
			{

				@Override
				public void run()
				{
					/* TODO the catch needs to alert the user */
					try
					{
						encoder.writeChunk(tmp, offSetInBytes, sizeInBytes);
						//Log.i("MP3", "write finished");
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			});
		}

		return sizeInBytes - offSetInBytes;
	}

	@Override
	public void close() throws IOException
	{
		// an unorderly shutdown
		//if (!finishedWriting)
			//encoder.close();
	}

	public void finishWriting() throws IOException
	{
		//Log.i("MP3", "FINISH REACHED");
		finishedWriting = true;
		exec.submit(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					//Log.i("MP3", "FINISH IN EXEC REACHED");
					encoder.finishWriting();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});

		exec.shutdown();
		try
		{
			exec.awaitTermination(20, TimeUnit.MINUTES);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Log.i("MP3", "CLOSE REACHED");
		encoder.close();
	}

	public void initFileOutputStream(String fileNameOut) throws IOException
	{
		encoder.initFileOutput(fileNameOut);
	}
}
