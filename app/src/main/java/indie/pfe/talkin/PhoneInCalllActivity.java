package indie.pfe.talkin;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class PhoneInCalllActivity extends Activity {

    private static final int BROADCAST_PORT = 50002;
    private static final int BUF_SIZE = 1024;
    private String contactIP;
    private String contactName;
    private Boolean LISTEN = true;
    private Boolean IN_CALL = false;
    private PhoneAudio call;
    private Chronometer chronometer;
    private MediaPlayer audioP;
    private AudioManager Microp;
    private AudioManager Speakp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_in_calll);

        audioP = MediaPlayer.create(this, R.raw.incoming_call);
        audioP.setLooping(true);

        Intent intent = getIntent();
        contactName = intent.getStringExtra(PhoneActivity.EXTRA_NAME);
        contactIP = intent.getStringExtra(PhoneActivity.EXTRA_IP);


        final ImageButton setMicro = (ImageButton) findViewById(R.id.setMic);
        setMicro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Microp.isMicrophoneMute() == false) {
                    Microp.setMicrophoneMute(true);
                    setMicro.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_off_white_24dp));
                } else {
                    Microp.setMicrophoneMute(false);
                    setMicro.setImageDrawable(getResources().getDrawable(R.drawable.ic_mic_white_24dp));
                }
            }
        });

        final ImageButton setSpeakers = (ImageButton) findViewById(R.id.setSpeakers);
        setSpeakers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Speakp.isSpeakerphoneOn() == false) {
                    Speakp.setSpeakerphoneOn(true);
                    setSpeakers.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_up_white_24dp));
                } else {
                    Speakp.setSpeakerphoneOn(false);
                    setSpeakers.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_off_white_24dp));
                }
            }
        });


        TextView textView = (TextView) findViewById(R.id.txtCaller);
        textView.setText(contactName);
        audioP.start();
        final Chronometer chronometer = (Chronometer) findViewById(R.id.chrono);
        chronometer.setVisibility(View.GONE);
        ImageButton rejectCAll = (ImageButton) findViewById(R.id.btnRejectCall);
        ImageButton acceptCall = (ImageButton) findViewById(R.id.btnAcceptCall);
        final ImageButton btnEndCall = (ImageButton) findViewById(R.id.btnEndCall1);
        acceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    send("ACC:");
                    InetAddress address = InetAddress.getByName(contactIP);
                    IN_CALL = true;
                    call = new PhoneAudio(address);
                    call.startCall();
                    audioP.stop();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chronometer.setVisibility(View.VISIBLE);
                        }
                    });
                    chronometer.start();
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llin);
                    linearLayout.setVisibility(View.GONE);
                    btnEndCall.setVisibility(View.VISIBLE);
                    LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.llCalloptions);
                    linearLayout1.setVisibility(View.VISIBLE);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        rejectCAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("REJ:");
                endCall();
            }
        });

        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
    }

    private void endCall() {
        stopListen();
        if (IN_CALL) {
            chronometer.stop();
            call.endCall();
        }
        send("END:");
        finish();
    }

    private void startListen() {
        LISTEN = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
                    socket.setSoTimeout(1500);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while (LISTEN) {
                        try {
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            String action = data.substring(0, 4);
                            if (action.equals("END:")) {
                                endCall();
                            } else {
                                return;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    socket.disconnect();
                    socket.close();
                    return;
                } catch (SocketException e) {
                    e.printStackTrace();
                    endCall();
                    return;
                }
            }
        });
        thread.start();
    }

    public void stopListen() {
        LISTEN = false;
    }

    private void send(final String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(contactIP);
                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, BROADCAST_PORT);
                    socket.send(packet);
                    socket.disconnect();
                    socket.close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    return;
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioP != null) {
            audioP.release();
        }
    }
}
