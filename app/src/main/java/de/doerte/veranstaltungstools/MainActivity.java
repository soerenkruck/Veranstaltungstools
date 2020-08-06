package de.doerte.veranstaltungstools;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import de.doerte.veranstaltungstools.Const.IntentValues;

public class MainActivity extends AppCompatActivity {

    boolean microphonePermission = false,
            cameraPermission = false;

    private AppBarLayout appbar;

    private final int AUDIO_ACTIVITY_ID = 161;
    private final int RADIO_ACTIVITY_ID = 894;

    // TODO: Camera Permission in die RadioActivity
    // TODO: Microphone Permission in die AudioActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5316);
            } else {
                microphonePermission = false;
            }
        } else {
            microphonePermission = true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1614);
            } else {
                cameraPermission = false;
            }
        } else {
            cameraPermission = true;
        }

        ActionBar bar = getSupportActionBar();
        bar.hide();

        appbar = (AppBarLayout) findViewById(R.id.appbar);

        initUI();
    }

    private void initUI() {
        CardView audioCard = (CardView) findViewById(R.id.audioCard);
        audioCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAudioActivity = new Intent(MainActivity.this, AudioActivity.class);
                toAudioActivity.putExtra(IntentValues.MIC_PERM_BOOL, microphonePermission);
                startActivityForResult(toAudioActivity, AUDIO_ACTIVITY_ID);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
        CardView radioCard = (CardView) findViewById(R.id.radioCard);
        radioCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRadioActivity = new Intent(MainActivity.this, RadioActivity.class);
                toRadioActivity.putExtra(IntentValues.CAM_PERM_BOOL, cameraPermission);
                startActivityForResult(toRadioActivity, RADIO_ACTIVITY_ID);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
        CardView dmxCard = (CardView) findViewById(R.id.dmxCard);
        dmxCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toDMXActivity = new Intent(MainActivity.this, DmxControlActivity.class);
                startActivity(toDMXActivity);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
        CardView helpCard = (CardView) findViewById(R.id.helpCard);
        helpCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toHelpActivity = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(toHelpActivity);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
        ImageButton audioButton, radioButton, dmxButton;
        audioButton = (ImageButton) findViewById(R.id.audioButton);
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAudioActivity = new Intent(MainActivity.this, AudioActivity.class);
                toAudioActivity.putExtra(IntentValues.MIC_PERM_BOOL, microphonePermission);
                startActivityForResult(toAudioActivity, AUDIO_ACTIVITY_ID);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
        radioButton = (ImageButton) findViewById(R.id.radioButton);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRadioActivity = new Intent(MainActivity.this, RadioActivity.class);
                toRadioActivity.putExtra(IntentValues.CAM_PERM_BOOL, cameraPermission);
                startActivityForResult(toRadioActivity, RADIO_ACTIVITY_ID);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
        dmxButton = (ImageButton) findViewById(R.id.dmxButton);
        dmxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toDMXActivity = new Intent(MainActivity.this, DmxControlActivity.class);
                startActivity(toDMXActivity);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                appbar.setBackgroundColor(Color.rgb(61, 220, 132));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                appbar.setBackgroundColor(Color.rgb(61, 64, 61));
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 5316: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.microphonePermission = true;
                } else {
                    this.microphonePermission = false;
                }
                return;
            }
            case 1614: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.cameraPermission = true;
                } else {
                    this.cameraPermission = false;
                }
                return;
            }
        }
    }
}