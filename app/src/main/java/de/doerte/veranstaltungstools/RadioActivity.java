/*
 * Copyright (c) 2020.  ChemnitzSued Veranstaltungen
 */

package de.doerte.veranstaltungstools;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.UUID;

import de.doerte.veranstaltungstools.Const.IntentValues;
import de.doerte.veranstaltungstools.QR.CodeTransformer;
import de.doerte.veranstaltungstools.QR.QRCodeEncoder;

public class RadioActivity extends AppCompatActivity {

    // TODO: getApplicationContext() durch den Context von RadioActivity.class ersetzen.
    // TODO: Showcase View implementieren

    private boolean camPermission;

    private IntentIntegrator integrator;

    String channelID = null;
    private String ownID;

    private static final String END_CODE = "$LEFT[]";
    private static final String NO_PASSWORD_CODE = "$NO_PASSWORD[]_key";
    private final String CONNECTION_CODE = "con_062020";

    private boolean connected = false;

    // Firebase vars
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference = database.getReference("communicator");
    private DatabaseReference currentChannel = null;
    private ValueEventListener valueEventListener;

    private TextView statusView, msgView; // TODO: Messagecenter
    private FloatingActionButton qrViewButton;
    private ImageView securityView, connectionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            camPermission = bundle.getBoolean(IntentValues.CAM_PERM_BOOL);
        } else {
            camPermission = false;
        }

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
        ownID = UUID.randomUUID().toString();
        currentChannel = null;
        initScan();
        initUI();
    }

    private Bitmap getBitmapByCode(String code) {
        Bitmap qrBitmap = null;
        try {
            qrBitmap = QRCodeEncoder.encodeAsBitmap(code, BarcodeFormat.QR_CODE, 896, 896);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return qrBitmap;
        //imageView.setImageBitmap(myBitmap);
    }

    private void scan() {
        if (camPermission) {
            integrator.initiateScan();
        } else {
            Snackbar.make(findViewById(R.id.radio_layout), R.string.no_cam_perm, BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    private void initScan() {
        integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Funkkanal mit Code beitreten.");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
    }

    private void initUI() {

        // Informational views
        connectionView = (ImageView) findViewById(R.id.connectionImageView);
        msgView = (TextView) findViewById(R.id.msgBox);
        statusView = (TextView) findViewById(R.id.statusView);
        statusView.setText(this.getResources().getString(R.string.noConnection));
        securityView = (ImageView) findViewById(R.id.securityView);

        // actional obejcts
        qrViewButton = (FloatingActionButton) findViewById(R.id.qrViewButton);
        qrViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQRDialog();
            }
        });
        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPasswordOption();
            }
        });
        FloatingActionButton joinButton = (FloatingActionButton) findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });

        //Quick-Action buttons
        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    currentChannel.setValue("Start");
                }
            }
        });
        Button stopButton = (Button) findViewById(R.id.readyButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    currentChannel.setValue("-USER" + " ist bereit."); // TODO Rollen einfügen + konstanten für Message-Codes
                }
            }
        });
        Button failButton = (Button) findViewById(R.id.fehlerButton);
        failButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    currentChannel.setValue("Es gab einen schwerwiegenden fehler.");
                }
            }
        });
        Button helpButton = (Button) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected)
                    currentChannel.setValue("-USER" + " benötigt Hilfe.");
            }
        });

        // Custom Message-Inputs:
        final EditText msgSendInput = (EditText) findViewById(R.id.msgInput);
        final Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (connected) {
                    currentChannel.setValue(msgSendInput.getText().toString());
                    msgSendInput.setText("");
                } else {
                    msgSendInput.setError("Nicht mit dem Server verbunden.");
                }
            }
        });
    }

    private void viewPasswordOption() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.passwordQuestion))
                .setTitle("Passwortsicherung");
        builder.setPositiveButton("Ja, Bitte.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder pwADBuilder = new AlertDialog.Builder(getApplicationContext());
                pwADBuilder.setTitle("Passwort");
                LayoutInflater inflater = RadioActivity.this.getLayoutInflater();
                final View pwRoot = inflater.inflate(R.layout.dialog_password, null);
                final EditText pwIn = pwRoot.findViewById(R.id.passwordEdit);
                pwIn.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (pwIn.length() > 12) {
                            pwIn.setError("Das Passwort darf nicht größer als 12 Zeichen lang sein.");
                            String actualText = pwIn.getText().toString().substring(0, 12);
                            pwIn.setText(actualText);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                pwADBuilder.setView(pwRoot)
                        .setPositiveButton("Akzeptieren.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                createChannelWithPassword(pwIn.getText().toString());
                            }
                        });
                AlertDialog passwordInDialog = pwADBuilder.create();
                passwordInDialog.show();
            }
        });
        builder.setNegativeButton("Nein, Danke.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                createChannel();
                DatabaseReference pw = reference.child(channelID).child("password");
                pw.setValue(NO_PASSWORD_CODE);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createChannel() {
        channelID = UUID.randomUUID().toString();
        if (connected) {
            currentChannel.setValue(ownID + END_CODE);
        }
        setStatusMessage(getResources().getString(R.string.createdRoom));
        qrViewButton.setVisibility(View.VISIBLE);
        setChannel(channelID);
        currentChannel.setValue(CONNECTION_CODE);
        setConnectionStatus(connected);

        showQRDialog();
    }
    private void createChannelWithPassword(String password) {
        channelID = UUID.randomUUID().toString();
        if (connected) {
            currentChannel.setValue(ownID + END_CODE);
        }
        setStatusMessage(getResources().getString(R.string.createdRoom));
        qrViewButton.setVisibility(View.VISIBLE);
        setChannel(channelID);
        DatabaseReference pw = reference.child(channelID).child("password");
        pw.setValue(password);
        currentChannel.setValue(CONNECTION_CODE);
        setConnectionStatus(connected);
        securityView.setVisibility(View.VISIBLE);
        showQRDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result.getContents() != null) {
            Snackbar.make(findViewById(R.id.radio_layout), getResources().getString(R.string.joinMsg) + "\n(" + (result.getContents()) + ")", BaseTransientBottomBar.LENGTH_LONG).show();
            channelID = result.getContents();

            statusView.setText("Verbindet...");
            qrViewButton.setVisibility(View.VISIBLE);

            DatabaseReference pwReference = reference.child(channelID).child("password");
            pwReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.getValue(String.class).equals(NO_PASSWORD_CODE)) {
                        showPasswordCheck(snapshot.getValue(String.class));
                    } else {
                        setChannel(result.getContents());
                        securityView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void showQRDialog() {
        Dialog qrDialog = new Dialog(this);
        qrDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        qrDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
        if (!(channelID == null)) {
            qrViewButton.setVisibility(View.VISIBLE);
            ImageView qrView = new ImageView(this);
            qrView.setImageBitmap(getBitmapByCode(channelID));
            qrView.setPadding(0, 0, 0, 144);
            TextView uuidView = new TextView(this);
            uuidView.setText("ID: " + channelID + "\nShort Code: " + CodeTransformer.getShortCode(channelID));
            uuidView.setPadding(16, 0, 16, 32);
            uuidView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            qrDialog.addContentView(qrView,
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            qrDialog.addContentView(uuidView,
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            TextView alertText = new TextView(this);
            alertText.setText("Bitte zuerst einen neuen Raum erstellen oder einem beitreten.");
            alertText.setPadding(64, 64, 64, 64);
            qrDialog.addContentView(alertText,
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        qrDialog.show();
    }
    private void setStatusMessage(String text) {
        statusView.setText(text);
    }
    private void setConnectionStatus(boolean isConnected) {
        if (isConnected) {
            connectionView.setImageDrawable(getResources().getDrawable(R.drawable.round_leak_add_black_48));
            connectionView.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                    (R.color.primaryColor)), PorterDuff.Mode.SRC_IN);
        } else {
            connectionView.setImageDrawable(getResources().getDrawable(R.drawable.round_leak_remove_black_48));
            connectionView.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                    (R.color.secondaryColor)), PorterDuff.Mode.SRC_IN);
        }
    }

    private void showPasswordCheck(final String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater dialogInflater = this.getLayoutInflater();
        View dialogRoot = dialogInflater.inflate(R.layout.dialog_password, null);
        EditText pwIn = dialogRoot.findViewById(R.id.passwordEdit);
        pwIn.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final EditText tmp = pwIn;
        builder.setTitle("Passwort")
                .setView(dialogRoot)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (tmp.length() > 0 && tmp.length() < 12) {
                            if (tmp.getText().toString().equals(String.valueOf(password))) {
                                setChannel(channelID);
                                securityView.setVisibility(View.VISIBLE);
                            } else {
                                showPasswordCheck(password);
                            }
                        } else {
                            showPasswordCheck(password);
                        }
                    }
                });
        AlertDialog passwordDialog = builder.create();
        passwordDialog.show();
    }

    private void setChannel(String channelUUID) {
        connected = true;
        if (currentChannel != null && valueEventListener != null) {
            currentChannel.removeEventListener(valueEventListener);
            valueEventListener = null;

            currentChannel = reference.child(channelUUID).child("msg");
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    interpretor(snapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            currentChannel.addValueEventListener(valueEventListener);
        } else {
            currentChannel = reference.child(channelUUID).child("msg");
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    interpretor(snapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            currentChannel.addValueEventListener(valueEventListener);
        }
        currentChannel.setValue(CONNECTION_CODE);
    }

    private void interpretor(String cmd) {
        String value = cmd;
        if (value != null) {
            if (value.equals(CONNECTION_CODE)) {
                statusView.setText(getResources().getString(R.string.connceted) + "\nSC: " + CodeTransformer.getShortCode(channelID));
                connected = true;
                setConnectionStatus(connected);
            } else if (value.equals(ownID + END_CODE)) {
                currentChannel.removeEventListener(valueEventListener);
            } else if (value.endsWith(END_CODE) || value.startsWith(ownID)) {

            } else {
                msgView.setText(value);
            }
        }
    }
}