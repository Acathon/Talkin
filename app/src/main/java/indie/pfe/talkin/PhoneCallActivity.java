package indie.pfe.talkin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class PhoneCallActivity extends Activity {


    //public EXTRA_USERNAME extraUsername;
    private static final int BROADCAST_PORT = 50002;
    private static final int BUF_SIZE = 1024;
    private String displayName;
    private String contactName;
    private String contactIP;
    private boolean LISTEN = true;
    private boolean IN_CALL = false;
    private PhoneAudio call;
    private Chronometer chronometer;
    private MediaPlayer audio;
    private AudioManager Microp;
    private AudioManager Speakp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_call);


        audio = MediaPlayer.create(this, R.raw.outgoing_call);
        audio.setLooping(true);

        Microp = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Speakp = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Intent intent = getIntent();
        //displayName = intent.getStringExtra(PhoneActivity.EXTRA_DISPLAYNAME);
        //displayName = extraUsername.getDisplayname();
        EXTRA_USERNAME extra_username = (EXTRA_USERNAME) getApplication();
        displayName = extra_username.getDisplayname();
        //Toast.makeText(getApplicationContext(), displayName, Toast.LENGTH_SHORT).show();
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
                    Speakp.setMode(AudioManager.MODE_NORMAL);
                    setSpeakers.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_up_white_24dp));
                } else {
                    Speakp.setSpeakerphoneOn(false);
                    Speakp.setMode(AudioManager.MODE_IN_CALL);
                    setSpeakers.setImageDrawable(getResources().getDrawable(R.drawable.ic_volume_off_white_24dp));
                }
            }
        });


        ImageButton endCall = (ImageButton) findViewById(R.id.btnEndCall);
        TextView textView = (TextView) findViewById(R.id.txtCalling);
        chronometer = (Chronometer) findViewById(R.id.chrono);
        textView.setText(contactName);

        startListen();
        startCall();
        audio.start();


        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
    }

    private void startCall() {
        send("CAL:" + displayName, 50003);
    }

    private void endCall() {
        endListen();
        if (IN_CALL) {
            chronometer.stop();
            call.endCall();
        }
        send("END:", BROADCAST_PORT);
        finish();
    }

    private void startListen() {
        LISTEN = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(BROADCAST_PORT);
                    socket.setSoTimeout(15000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while (LISTEN) {
                        try {
                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            String action = data.substring(0, 4);
                            if (action.equals("ACC:")) {
                                audio.stop();
                                call = new PhoneAudio(packet.getAddress());
                                call.startCall();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chronometer.setVisibility(View.VISIBLE);
                                    }
                                });
                                chronometer.start();
                                IN_CALL = true;
                            } else if (action.equals("REJ:")) {
                                audio.stop();
                                endCall();
                            } else if (action.equals("END:")) {
                                audio.stop();
                                endCall();
                            } else {
                                return;
                            }
                        } catch (SocketException e) {
                            e.printStackTrace();
                            endCall();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            endCall();
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

    private void endListen() {
        LISTEN = false;
    }

    private void send(final String message, final int port) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(contactIP);
                    byte[] data = message.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);
                    socket.disconnect();
                    socket.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                } catch (UnknownHostException e) {
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
        if (null != audio) {
            audio.release();
        }
    }

}
