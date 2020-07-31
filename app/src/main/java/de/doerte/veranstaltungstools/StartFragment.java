package de.doerte.veranstaltungstools;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.drive.events.ChangeListener;

import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import de.doerte.veranstaltungstools.Const.ShowCaseElement;
import de.doerte.veranstaltungstools.Spectrogram.Spectrogram;
import de.doerte.veranstaltungstools.analyse.Mathematics;

public class StartFragment extends Fragment implements PitchDetectionHandler {

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private boolean micPermission;

    public View root;

    private AudioDispatcher dispatcher;
    //private LineChart realTimeDataChart;

    private Spectrogram spectrogram;

    int sampleRate = 48000;
    int bufferSize = 4000/2;
    int overlap = 4;

    private int graphColor;

    private ShowcaseView.Builder showcaseView;

    private PitchProcessor.PitchEstimationAlgorithm estimationAlgorithm;

    private double pitch;


    public boolean run = true;

    private final int SHOWCASEVIEW_ID = 2206;

    public StartFragment() {
    }

    public static StartFragment newInstance(boolean micPerm) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, micPerm);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            micPermission = getArguments().getBoolean(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_start, container, false);

        estimationAlgorithm = PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET;

        initChart();
        if (micPermission) {
            createDispatcher();
        }
        showShowcaseView();

        final Switch bwToggle = (Switch) root.findViewById(R.id.bw_toggle);
        bwToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                spectrogram.setColored(!bwToggle.isChecked());
            }
        });
        spectrogram.setColored(!bwToggle.isChecked());
        
        return root;

    }

    private void initChart() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            graphColor = Color.rgb(1f, 0.5f, 0.6f);
        } else {
            graphColor = Color.RED;
        }

        spectrogram = (Spectrogram) root.findViewById(R.id.spectrogram);
        spectrogram.setPixelSize(4);
    }

    private void showShowcaseView() {
        showcaseView = new ShowcaseView.Builder(getActivity())
                .withHoloShowcase()
                .setTarget(new ViewTarget(spectrogram))
                .setContentTitle("Der richtige Sound.")
                .setContentText("Hier siehst du immer die Lautstärke der Frequenzen deines Sounds.\n\nBald wird dir hier eine KI helfen, automatisch Störgeräusche" +
                        " und Feedback zu erkennen.")
                .setContentTextPaint(ShowCaseElement.textPaint())
                .setContentTitlePaint(ShowCaseElement.titleTextPaint())
                .singleShot(SHOWCASEVIEW_ID)
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        ShowcaseView.Builder scndSV = new ShowcaseView.Builder(getActivity())
                                .withHoloShowcase()
                                .setTarget(new ViewTarget(R.id.textView17, getActivity()))
                                .setContentTitle("Support")
                                .setContentTitlePaint(ShowCaseElement.titleTextPaint())
                                .setContentText("Wenn du hilfe brauchst oder uns dein Feedback zukommen lassen möchtest, schreibe uns doch gerne.")
                                .setContentTextPaint(ShowCaseElement.textPaint());
                        scndSV.build();
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                    }
                });
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

        FFT fft = new FFT(bufferSize);
        float[] ampluitudes = new float[bufferSize/2];

        @Override
        public boolean process(AudioEvent audioEvent) {

            if (run) {
                float[] audioFloatBuffer = audioEvent.getFloatBuffer();
                float[] transformBuffer = new float[bufferSize * 2];
                System.arraycopy(audioFloatBuffer, 0, transformBuffer, 0, audioFloatBuffer.length);
                fft.forwardTransform(transformBuffer);
                fft.modulus(transformBuffer, ampluitudes);

                paint(pitch, ampluitudes, fft);
            }
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

        final TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(Float.valueOf(sampleRate), 16, 1, true, false);

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, overlap);
        dispatcher.addAudioProcessor(new PitchProcessor(estimationAlgorithm, sampleRate, bufferSize, this));
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
}
