/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 */

package de.doerte.veranstaltungstools;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ActionBar bar = getSupportActionBar();
        bar.hide();

        TextView copyrightView = (TextView) findViewById(R.id.copyrightView);
        String copyright = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            copyright = getResources().getString(R.string.copyright).replace("~", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        } else {
            copyright = getResources().getString(R.string.copyright).replace("~", "2020");
        }
        copyrightView.setText(copyright);

        String version = "0";

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView versionView = (TextView) findViewById(R.id.versionTextView);
        versionView.setText(getResources().getString(R.string.version) + " " + version);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_back_b, R.anim.slide_back_a);
    }
}