package de.doerte.veranstaltungstools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

import de.doerte.veranstaltungstools.QR.CodeTransformer;
import de.doerte.veranstaltungstools.QR.QRCodeEncoder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private boolean camPermission;

    public View root;

    private IntentIntegrator integrator;

    private String channelID = null;
    private String ownID;

    private static final String END_CODE = "$LEFT[]";
    private static final String NO_PASSWORD_CODE = "$NO_PASSWORD[]_key";

    private boolean permissionsAccepted;

    private TextView statusView;
    private FloatingActionButton qrViewButton;
    private TextView msgView;
    private ImageView securityView;

    private final String CONNECTION_CODE = "con_72020";
    private boolean conncted = false;
    private ImageView conncetionView;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference = database.getReference("communicator");
    private DatabaseReference currentChannel = null;
    private ValueEventListener valueEventListener;

    public ChatFragment() {
        // Required empty public constructor
    }
    public static ChatFragment newInstance(boolean param1) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            camPermission = getArguments().getBoolean(ARG_PARAM1);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_chat, container, false);

        init();

        return root;
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
            Toast.makeText(getContext(), R.string.no_cam_perm, Toast.LENGTH_SHORT).show();
        }
    }

    private void initScan() {
        integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Funkkanal mit Code beitreten.");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
    }

    private void initUI() {

        // Informational views
        conncetionView = (ImageView) root.findViewById(R.id.connectionImageView);
        msgView = (TextView) root.findViewById(R.id.msgBox);
        statusView = (TextView) root.findViewById(R.id.statusView);
        statusView.setText(this.getResources().getString(R.string.noConnection));
        securityView = (ImageView) root.findViewById(R.id.securityView);

        // actional obejcts
        qrViewButton = (FloatingActionButton) root.findViewById(R.id.qrViewButton);
        qrViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQRDialog();
            }
        });
        Button createButton = (Button) root.findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPasswordOption();
            }
        });
        FloatingActionButton joinButton = (FloatingActionButton) root.findViewById(R.id.joinButton);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan();
            }
        });

        //Quick-Action buttons
        Button startButton = (Button) root.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conncted) {
                    currentChannel.setValue("Start");
                }
            }
        });
        Button stopButton = (Button) root.findViewById(R.id.readyButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conncted) {
                    currentChannel.setValue("$USER" + " ist bereit."); // TODO Rollen einfügen + konstanten für Message-Codes
                }
            }
        });
        Button failButton = (Button) root.findViewById(R.id.fehlerButton);
        failButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conncted) {
                    currentChannel.setValue("Es gab einen schwerwiegenden fehler.");
                }
            }
        });
        Button helpButton = (Button) root.findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conncted)
                    currentChannel.setValue("$USER" + " benötigt Hilfe.");
            }
        });

        // Custom Message-Inputs:
        final EditText msgSendInput = (EditText) root.findViewById(R.id.msgInput);
        final Button sendButton = (Button) root.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conncted) {
                    currentChannel.setValue(msgSendInput.getText().toString());
                    msgSendInput.setText("");
                } else {
                    msgSendInput.setError("Nicht mit dem Server verbunden.");
                }
            }
        });
    }

    private void viewPasswordOption() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.passwordQuestion))
                .setTitle("Passwortsicherung");
        builder.setPositiveButton("Ja, Bitte.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder pwADBuilder = new AlertDialog.Builder(getActivity());
                pwADBuilder.setTitle("Passwort");
                LayoutInflater inflater = requireActivity().getLayoutInflater();
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
        if (conncted) {
            currentChannel.setValue(ownID + END_CODE);
        }
        setStatusMessage(getResources().getString(R.string.createdRoom));
        qrViewButton.setVisibility(View.VISIBLE);
        setChannel(channelID);
        currentChannel.setValue(CONNECTION_CODE);
        setConnectionStatus(conncted);

        showQRDialog();
    }
    private void createChannelWithPassword(String password) {
        channelID = UUID.randomUUID().toString();
        if (conncted) {
            currentChannel.setValue(ownID + END_CODE);
        }
        setStatusMessage(getResources().getString(R.string.createdRoom));
        qrViewButton.setVisibility(View.VISIBLE);
        setChannel(channelID);
        DatabaseReference pw = reference.child(channelID).child("password");
        pw.setValue(password);
        currentChannel.setValue(CONNECTION_CODE);
        setConnectionStatus(conncted);
        securityView.setVisibility(View.VISIBLE);
        showQRDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result.getContents() != null) {
            Toast.makeText(root.getContext().getApplicationContext(),
                    this.getResources().getString(R.string.joinMsg) + " \n(" + (result.getContents()) + ")",
                    Toast.LENGTH_LONG).show();
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
        Dialog qrDialog = new Dialog(root.getContext());
        qrDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        qrDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
        if (!(channelID == null)) {
            qrViewButton.setVisibility(View.VISIBLE);
            ImageView qrView = new ImageView(getContext());
            qrView.setImageBitmap(getBitmapByCode(channelID));
            qrView.setPadding(0, 0, 0, 144);
            TextView uuidView = new TextView(getContext());
            uuidView.setText("ID: " + channelID + "\nShort Code: " + CodeTransformer.getShortCode(channelID));
            uuidView.setPadding(16, 0, 16, 32);
            uuidView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            qrDialog.addContentView(qrView,
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            qrDialog.addContentView(uuidView,
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            TextView alertText = new TextView(getContext());
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
            conncetionView.setImageDrawable(getResources().getDrawable(R.drawable.round_leak_add_black_48));
            conncetionView.setColorFilter(ContextCompat.getColor(getContext(),
                    (R.color.primaryColor)), PorterDuff.Mode.SRC_IN);
        } else {
            conncetionView.setImageDrawable(getResources().getDrawable(R.drawable.round_leak_remove_black_48));
            conncetionView.setColorFilter(ContextCompat.getColor(getContext(),
                    (R.color.secondaryColor)), PorterDuff.Mode.SRC_IN);
        }
    }

    private void showPasswordCheck(final String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
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
        conncted = true;
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
                statusView.setText(getResources().getString(R.string.connceted) + "\nSC: " + CodeTransformer.getShortCode(value));
                conncted = true;
                setConnectionStatus(conncted);
            } else if (value.equals(ownID + END_CODE)) {
                currentChannel.removeEventListener(valueEventListener);
            } else if (value.endsWith(END_CODE) || value.startsWith(ownID)) {

            } else {
                msgView.setText(value);
            }
        }
    }
}
