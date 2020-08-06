/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 */

package de.doerte.veranstaltungstools;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class DmxControlActivity extends AppCompatActivity {

    private ImageView s1, s2, s3, s4, s5, s6, s7, s8, s9;
    private boolean b1, b2, b3, b4, b5, b6, b7, b8, b9;
    private EditText dmxIn;
    private int DMX_channel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmx_control);

        ActionBar bar = getSupportActionBar();
        bar.hide();

        init();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_back_b, R.anim.slide_back_a);
    }

    private void init() {
        s1 = findViewById(R.id.switch_1);
        s2 = findViewById(R.id.switch_2);
        s3 = findViewById(R.id.switch_3);
        s4 = findViewById(R.id.switch_4);
        s5 = findViewById(R.id.switch_5);
        s6 = findViewById(R.id.switch_6);
        s7 = findViewById(R.id.switch_7);
        s8 = findViewById(R.id.switch_8);
        s9 = findViewById(R.id.switch_9);

        final TextView all = findViewById(R.id.textView2);
        final TextView t1 = findViewById(R.id.textView3);
        final TextView t2 = findViewById(R.id.textView4);
        final TextView t3 = findViewById(R.id.textView5);
        final TextView t4 = findViewById(R.id.textView6);
        final TextView t5 = findViewById(R.id.textView7);
        final TextView t6 = findViewById(R.id.textView8);
        final TextView t7 = findViewById(R.id.textView9);
        final TextView t8 = findViewById(R.id.textView10);
        final TextView t9 = findViewById(R.id.textView11);

        clearKeyboard();

        final CardView dip_card = findViewById(R.id.dip_cardview);
        dip_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dip_card.getCardBackgroundColor() != ColorStateList.valueOf(Color.RED)) {
                    dip_card.setCardBackgroundColor(Color.RED);
                } else {
                    dip_card.setCardBackgroundColor(Color.BLUE);
                }
                all.setTextColor(Color.WHITE);
                t1.setTextColor(Color.WHITE);
                t2.setTextColor(Color.WHITE);
                t3.setTextColor(Color.WHITE);
                t4.setTextColor(Color.WHITE);
                t5.setTextColor(Color.WHITE);
                t6.setTextColor(Color.WHITE);
                t7.setTextColor(Color.WHITE);
                t8.setTextColor(Color.WHITE);
                t9.setTextColor(Color.WHITE);
            }
        });

        Button plus = findViewById(R.id.plusButton);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DMX_channel+16 < 512) {
                    DMX_channel += 16;
                } else {
                    DMX_channel = 511;
                }
                setKeyboard(DMX_channel);
                dmxIn.setText(String.valueOf(DMX_channel));
            }
        });
        Button minus = findViewById(R.id.minusButton);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DMX_channel-16 >= 0) {
                    DMX_channel -= 16;
                } else {
                    DMX_channel = 0;
                }
                setKeyboard(DMX_channel);
                dmxIn.setText(String.valueOf(DMX_channel));
            }
        });

        dmxIn = findViewById(R.id.editText);
        dmxIn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (dmxIn.length() > 0) {
                    DMX_channel = Integer.valueOf(dmxIn.getText().toString());
                }
                setKeyboard(DMX_channel);
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        s1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b1 = !b1;
                updateDMXChannel();
            }
        });
        s2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b2 = !b2;
                updateDMXChannel();
            }
        });
        s3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b3 = !b3;
                updateDMXChannel();
            }
        });
        s4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b4 = !b4;
                updateDMXChannel();
            }
        });
        s5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b5 = !b5;
                updateDMXChannel();
            }
        });
        s6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b6 = !b6;
                updateDMXChannel();
            }
        });
        s7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b7 = !b7;
                updateDMXChannel();
            }
        });
        s8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b8 = !b8;
                updateDMXChannel();
            }
        });
        s9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b9 = !b9;
                updateDMXChannel();
            }
        });
    }

    private void updateDMXChannel() {
        int sum = 0;

        if (b1) {
            sum += 1;
        }
        if (b2) {
            sum += 2;
        }
        if (b3) {
            sum += 4;
        }
        if (b4) {
            sum += 8;
        }
        if (b5) {
            sum += 16;
        }
        if (b6) {
            sum += 32;
        }
        if (b7) {
            sum += 64;
        }
        if (b8) {
            sum += 128;
        }
        if (b9) {
            sum += 256;
        }
        DMX_channel = sum;
        dmxIn.setText(String.valueOf(DMX_channel));
    }

    private void setKeyboard(final int channel) {

        if (channel < 512) {
            dmxIn.setTextColor(Color.BLACK);
            clearKeyboard();

            int restSum = channel;

            if (restSum >= 256) {
                b9 = true;
                restSum -= 256;
            }
            if (restSum >= 128) {
                restSum -= 128;
                b8 = true;
            }
            if (restSum >= 64) {
                restSum -= 64;
                b7 = true;
            }
            if (restSum >= 32) {
                restSum -= 32;
                b6 = true;
            }
            if (restSum >= 16) {
                restSum -= 16;
                b5 = true;
            }
            if (restSum >= 8) {
                restSum -= 8;
                b4 = true;
            }
            if (restSum >= 4) {
                restSum -= 4;
                b3 = true;
            }
            if (restSum >= 2) {
                restSum -= 2;
                b2 = true;
            }
            if (restSum == 1) {
                restSum = 0;
                b1 = true;
            }
            update();
        } else {
            dmxIn.setError("Höchster Wert 511. Zählung von DMX512 beginnt bei 0");
        }
    }

    private void update() {
        if (b9) {
            s9.setImageResource(R.drawable.on);
        }
        if (b8) {
            s8.setImageResource(R.drawable.on);
        }
        if (b7) {
            s7.setImageResource(R.drawable.on);
        }
        if (b6) {
            s6.setImageResource(R.drawable.on);
        }
        if (b5) {
            s5.setImageResource(R.drawable.on);
        }
        if (b4) {
            s4.setImageResource(R.drawable.on);
        }
        if (b3) {
            s3.setImageResource(R.drawable.on);
        }
        if (b2) {
            s2.setImageResource(R.drawable.on);
        }
        if (b1) {
            s1.setImageResource(R.drawable.on);
        }
    }

    private void clearKeyboard() {
        s1.setImageResource(R.drawable.off);
        s2.setImageResource(R.drawable.off);
        s3.setImageResource(R.drawable.off);
        s4.setImageResource(R.drawable.off);
        s5.setImageResource(R.drawable.off);
        s6.setImageResource(R.drawable.off);
        s7.setImageResource(R.drawable.off);
        s8.setImageResource(R.drawable.off);
        s9.setImageResource(R.drawable.off);

        b1 = false;
        b2 = false;
        b3 = false;
        b4 = false;
        b5 = false;
        b6 = false;
        b7 = false;
        b8 = false;
        b9 = false;
    }
}