/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 */

package de.doerte.veranstaltungstools;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;
import de.doerte.veranstaltungstools.Const.IntentValues;
import de.doerte.veranstaltungstools.Const.ShowCaseElement;
import de.doerte.veranstaltungstools.Spectrogram.Spectrogram;

public class AudioActivity extends AppCompatActivity implements PitchDetectionHandler {

    private boolean micPermission;

    private AudioDispatcher dispatcher;
    private PitchProcessor.PitchEstimationAlgorithm estimationAlgorithm;
    private final int SAMPLE_RATE = 48000;
    private final int BUFFER_SIZE = 4000/2;
    private final int OVERLAP = 4;
    private double pitch;

    private Spectrogram spectrogram;

    private ShowcaseView.Builder showcaseView;
    private final int SHOWCASEVIEW_ID = 2206;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        ActionBar bar = getSupportActionBar();
        bar.hide();

        final Bundle parentIntent = getIntent().getExtras();
        if (parentIntent != null) {
            micPermission = parentIntent.getBoolean(IntentValues.MIC_PERM_BOOL);
        } else {
            micPermission = false;
        }

        estimationAlgorithm = PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET;

        initChart();
        if (micPermission) {
            createDispatcher();
        }
        showShowcaseView();

        final Switch bwToggle = (Switch) findViewById(R.id.bw_toggle);
        bwToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                spectrogram.setColored(!bwToggle.isChecked());
            }
        });
        spectrogram.setColored(!bwToggle.isChecked());
    }
    private void initChart() {
        spectrogram = (Spectrogram) findViewById(R.id.spectrogram);
        spectrogram.setPermission(micPermission);
        spectrogram.setPixelSize(4);
    }

    private void showShowcaseView() {
        showcaseView = new ShowcaseView.Builder(this)
                .withHoloShowcase()
                .setTarget(new ViewTarget(spectrogram))
                .setContentTitle("Der richtige Sound.")
                .setContentText("Hier siehst du immer die Lautstärke der Frequenzen deines Sounds.\n\nBald wird dir hier eine KI helfen, automatisch Störgeräusche" +
                        " und Feedback zu erkennen.")
                .setContentTextPaint(ShowCaseElement.textPaint())
                .setContentTitlePaint(ShowCaseElement.titleTextPaint())
                .singleShot(SHOWCASEVIEW_ID);
        showcaseView.build();
    }

    public void paint(double pitch, float[] amplitudes, FFT fft) {
        ArrayList<Float> values = new ArrayList<>();

        int size = spectrogram.getPixelSize();

        for (int y = 0; y < (amplitudes.length/size) - 10; y++) {
            values.add(amplitudes[y*size]);
        }
        if (values.size() > 0)
            spectrogram.push(values);

    }

    AudioProcessor fftProcessor = new AudioProcessor() {

        FFT fft = new FFT(BUFFER_SIZE);
        float[] ampluitudes = new float[BUFFER_SIZE /2];

        @Override
        public boolean process(AudioEvent audioEvent) {

            float[] audioFloatBuffer = audioEvent.getFloatBuffer();
            float[] transformBuffer = new float[BUFFER_SIZE * 2];
            System.arraycopy(audioFloatBuffer, 0, transformBuffer, 0, audioFloatBuffer.length);
            fft.forwardTransform(transformBuffer);
            fft.modulus(transformBuffer, ampluitudes);

            paint(pitch, ampluitudes, fft);

            return true;
        }

        @Override
        public void processingFinished() {

        }
    };

    private void createDispatcher() {
        if (dispatcher != null) {
            dispatcher.stop();
        }

        final TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(Float.valueOf(SAMPLE_RATE), 16, 1, true, false);

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP);
        dispatcher.addAudioProcessor(new PitchProcessor(estimationAlgorithm, SAMPLE_RATE, BUFFER_SIZE, this));
        dispatcher.addAudioProcessor(fftProcessor);

        new Thread(dispatcher, "Audio dispatching").start();
    }

    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        if (pitchDetectionResult.isPitched()) {
            pitch = pitchDetectionResult.getPitch();
        } else {
            pitch = -1;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_back_b, R.anim.slide_back_a);
    }
}