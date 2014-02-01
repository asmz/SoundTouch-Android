package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.*;

import java.io.IOException;
import java.util.Arrays;

import com.smp.soundtouchandroid.SoundTouchPlayableBase.PlaybackProgressListener;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

public class SoundTouchPlayableSinglePlay extends SoundTouchPlayableBase
{
	private ReceivingAudioTrack track;
	
	public SoundTouchPlayableSinglePlay(String fileName, int id, float tempo, float pitchSemi) throws IOException
	{
		setupAudioTrack(fileName, id, tempo, pitchSemi);
		init(track, fileName, id, tempo, pitchSemi);	
	}
	private void setupAudioTrack(String fileName, int id, float tempo, float pitchSemi) throws IOException
	{
		initDecoder(fileName);
		
		int channelFormat = -1;

		if (channels == 1) // mono
			channelFormat = AudioFormat.CHANNEL_OUT_MONO;
		else if (channels == 2) // stereo
			channelFormat = AudioFormat.CHANNEL_OUT_STEREO;
		else
			throw new SoundTouchAndroidException("Valid channel count is 1 or 2");
		
		track = new ReceivingAudioTrack(AudioManager.STREAM_MUSIC, samplingRate, channelFormat,
				AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE_TRACK, AudioTrack.MODE_STREAM);
			
	}
	public SoundTouchPlayableSinglePlay(PlaybackProgressListener playbackListener, String fileName, 
			int id, float tempo, float pitchSemi) throws IOException
	{
		setupAudioTrack(fileName, id, tempo, pitchSemi);
		init(playbackListener, track, fileName, id, tempo, pitchSemi);
	}
	
	public int getSessionId()
	{
		return track.getAudioSessionId();
	}
	
	public void setVolume(float left, float right)
	{
		synchronized (receiverLock)
		{
			track.setStereoVolume(left, right);
		}
	}
	
	@Override
	public void run()
	{
		try 
		{ 
			super.run(); 
		}
		finally
		{
			synchronized (receiverLock)
			{
				track.pause();
				track.flush();
				track.release();
			}
		}
	}

	@Override
	public void seekTo(long timeInUs)
	{
		super.seekTo(timeInUs);
		synchronized (receiverLock)
		{
			track.flush();
		}
	}
	
	@Override
	public void play()
	{
		//track.play() must be first.
		synchronized (receiverLock)
		{
			track.play();
		}
		
		super.play();
	}
	
	@Override
	public void pause()
	{
		synchronized (receiverLock)
		{
			track.pause();
		}
		super.pause();
	}
}
