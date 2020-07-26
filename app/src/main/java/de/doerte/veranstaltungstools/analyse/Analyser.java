/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 *
 *  Documentation of TarsosDSP:
 *  https://0110.be/releases/TarsosDSP/TarsosDSP-latest/TarsosDSP-latest-Documentation/
 *  GitHub:
 *  https://github.com/JorenSix/TarsosDSP
 */

package de.doerte.veranstaltungstools.analyse;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.Oscilloscope;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static com.google.android.gms.internal.zzhu.runOnUiThread;

public class Analyser implements Oscilloscope.OscilloscopeEventHandler {

    final int SAMPLE_RATE = 44100;
    final int AUDIO_BUFFER_SIZE = 7168;
    final int BUFFER_OVERLAP = AUDIO_BUFFER_SIZE / 2;

    private float values[];

    public Analyser() {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, AUDIO_BUFFER_SIZE, BUFFER_OVERLAP);

        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        };
        AudioProcessor processor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                SAMPLE_RATE, AUDIO_BUFFER_SIZE, pitchDetectionHandler);
        dispatcher.addAudioProcessor(processor);
        new Thread(dispatcher, "Audio Dispatcher").start();

        values = new float[SAMPLE_RATE];
        values[0] = 0;
    }

    public float[] getVolume() {
        return values;
    }

    @Override
    public void handleEvent(float[] floats, AudioEvent audioEvent) {
        values = floats;
    }
}
