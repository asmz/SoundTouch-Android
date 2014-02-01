package com.smp.soundtouchandroid;

import static com.smp.soundtouchandroid.Constants.BUFFER_SIZE_TRACK;

import java.io.IOException;
import static com.smp.soundtouchandroid.Constants.*;
import com.smp.soundtouchandroid.SoundTouchPlayableBase.PlaybackProgressListener;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class SoundTouchPlayableMultiPlay extends SoundTouchPlayableBase
{	
	public SoundTouchPlayableMultiPlay(ReceivingBlockingQueue queue, String fileName, int id, float tempo, float pitchSemi) throws IOException
	{
		initDecoder(fileName);
		init(queue, fileName, id, tempo, pitchSemi);	
	}
	public SoundTouchPlayableMultiPlay(ReceivingBlockingQueue queue, PlaybackProgressListener playbackListener, String fileName, 
			int id, float tempo, float pitchSemi) throws IOException
	{
		initDecoder(fileName);
		init(playbackListener, queue, fileName, id, tempo, pitchSemi);
	}
}
