package com.smp.soundtouchandroid;

import java.io.IOException;

public interface AudioFormatChangedListener
{
	void onAudioFormatChanged(int samplingRate, int channels) throws IOException;
}
