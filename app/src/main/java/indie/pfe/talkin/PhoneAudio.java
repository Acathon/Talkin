package indie.pfe.talkin;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by Mustapha Essouri on 01/05/2015.
 */
public class PhoneAudio {
    private static final int SAMPLE_RATE = 8000;
    private static final int SAMPLE_INTERVAL = 20;
    private static final int SAMPLE_SIZE = 2;
    private static final int BUF_SIZE = SAMPLE_SIZE * SAMPLE_INTERVAL * SAMPLE_INTERVAL * 2;
    private InetAddress address;
    private int port = 50000;
    private boolean mic = false;
    private boolean speakers = false;

    public PhoneAudio(InetAddress address) {
        this.address = address;
    }

    public void startCall() {
        startMic();
        startSpeakers();
    }

    public void endCall() {
        muteMic();
        muteSpeakers();
    }

    public void muteMic() {
        mic = false;
    }

    public void muteSpeakers() {
        speakers = false;
    }

    public void startMic() {
        mic = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10);
                int read_byte = 0;
                int write_byte = 0;
                byte[] buffer = new byte[BUF_SIZE];
                try {
                    DatagramSocket socket = new DatagramSocket();
                    audioRecord.startRecording();
                    while (mic) {
                        read_byte = audioRecord.read(buffer, 0, BUF_SIZE);
                        DatagramPacket packet = new DatagramPacket(buffer, read_byte, address, port);
                        socket.send(packet);
                        write_byte += read_byte;
                        Thread.sleep(SAMPLE_INTERVAL, 0);
                    }
                    audioRecord.stop();
                    audioRecord.release();
                    socket.disconnect();
                    socket.close();
                    mic = false;
                    return;
                } catch (InterruptedException e) {
                    mic = false;
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                    mic = false;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    mic = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    mic = false;
                }
            }
        });
        thread.start();
    }

    public void startSpeakers() {
        if (!speakers) {
            speakers = true;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                    audioTrack.play();
                    try {
                        DatagramSocket socket = new DatagramSocket(port);
                        byte[] buffer = new byte[BUF_SIZE];
                        while (speakers) {
                            DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                            socket.receive(packet);
                            audioTrack.write(packet.getData(), 0, BUF_SIZE);
                        }
                        socket.disconnect();
                        socket.close();
                        audioTrack.stop();
                        audioTrack.flush();
                        audioTrack.release();
                        speakers = false;
                        return;
                    } catch (SocketException e) {
                        speakers = false;
                        e.printStackTrace();
                    } catch (IOException e) {
                        speakers = false;
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }
}
