package de.doerte.veranstaltungstools;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

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
    private String mParam1;
    private String mParam2;

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

    private final int SHOWCASEVIEW_ID = 2206;

    public StartFragment() {
    }

    public static StartFragment newInstance(String param1, String param2) {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_start, container, false);

        estimationAlgorithm = PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET;

        initChart();
        createDispatcher();
        showShowcaseView();
        
        return root;

    }

    private void initChart() {
        //realTimeDataChart = (LineChart) root.findViewById(R.id.realTimeChart);

        //List<Entry> entries = new ArrayList<Entry>();
        //
        //entries.add(new Entry(0, 0));

        //YAxis yAxis = realTimeDataChart.getAxisLeft();
        //yAxis.setDrawZeroLine(false);
        //yAxis.setAxisMinimum(0);
        //yAxis.setAxisMaximum(80);
        //yAxis.mAxisRange = 80;
        //yAxis.setDrawLabels(true);

        //Legend legend = realTimeDataChart.getLegend();
        //legend.setEnabled(true);

        //realTimeDataChart.getAxisRight().setEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            graphColor = Color.rgb(1f, 0.5f, 0.6f);
        } else {
            graphColor = Color.RED;
        }

        //final LineDataSet dataSet = new LineDataSet(entries, "TestEntries");
        //dataSet.setColor(graphColor);
        //dataSet.setValueTextColor(Color.BLACK);
        //dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        //dataSet.setLabel("Zufälige Werte.");
        //
        //final LineData lineData = new LineData(dataSet);
        ////realTimeDataChart.setData(lineData);
        //Description description = new Description();
        //description.setText("Oszillator");
        ////realTimeDataChart.setDescription(description);
        ////realTimeDataChart.invalidate();

        spectrogram = (Spectrogram) root.findViewById(R.id.spectrogram);
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
        //List<Entry> newEntries = new ArrayList<Entry>();
        //
        //for (int i = 0; i < (amplitudes.length-1)/2; i++) {
        //    int x = Mathematics.map(i, 0, (amplitudes.length-1), 1, sampleRate/bufferSize*1000);
        //    newEntries.add(new Entry(x, amplitudes[i]));
        //}
        //
        ////System.out.println(newEntries.size());
        //
        //LineDataSet dataSet = new LineDataSet(newEntries, "TestEntries");
        //dataSet.setColor(graphColor);
        //dataSet.setDrawCircles(false);
        //dataSet.setValueTextColor(Color.BLACK);
        //dataSet.setLabel("Lautstärke der Frequenz");
        //
        //LineData lineData = new LineData(dataSet);
        ////realTimeDataChart.setData(lineData);
        ////realTimeDataChart.invalidate();

        ArrayList<Float> values = new ArrayList<>();
        for (int y = 0; y < (amplitudes.length/4) - 10; y++) {
            values.add(amplitudes[y*4]);
        }
        if (values.size() > 0)
            spectrogram.push(values);

    }

    AudioProcessor fftProcessor = new AudioProcessor() {

        FFT fft = new FFT(bufferSize);
        float[] ampluitudes = new float[bufferSize/2];

        @Override
        public boolean process(AudioEvent audioEvent) {
            float[] audioFloatBuffer = audioEvent.getFloatBuffer();
            float[] transformBuffer = new float[bufferSize*2];
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
