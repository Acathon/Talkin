package indie.pfe.talkin;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class MainActivity extends ActionBarActivity {

    //public EXTRA_USERNAME extraUsername;
    public final static String EXTRA_USER = "indie.pfe.talkin.user";
    public final static int SOCKET_PORT = 10080;
    public final static String TAG = "Messenger";
    public final static int BUFFER_SIZE = 1800;
    private static long back_pressed;
    public final boolean RECEIVER = true;
    public ConnectivityManager mConnect;
    public NetworkInfo iNet;
    public WifiManager mWifi;
    public EditText editText;
    public String message;
    public ImageView loader;
    private boolean connected;
    private String Texto = null;
    private MessageArrayAdapter adapter;
    private MessageArrayAdapterRight adapterR;
    private int var = 0;
    private String contactName;
    private String displayName;
    private PhoneActivity phoneActivity;

    public static String ipToString(int ip, boolean broadcast) {
        String result = new String();
        Integer[] address = new Integer[4];
        for (int i = 0; i < 4; i++) {
            address[i] = (ip >> 8 * i) & 0xFF;
        }
        for (int i = 0; i < 4; i++) {
            if (i != 3) {
                result = result.concat(address[i] + ".");
            } else {
                result = result.concat("255.");
            }
        }
        return result.substring(0, result.length() - 1);
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) super.onBackPressed();
        else
            Toast.makeText(getBaseContext(), "Press once again to exit!", Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);


        editText = (EditText) findViewById(R.id.txtMsg);
        loader = (ImageView) findViewById(R.id.loader);
        final LinearLayout linearLayout14 = (LinearLayout) findViewById(R.id.linear14);
        final ListView listView = (ListView) findViewById(R.id.listMessages);
        final TextView textView = (TextView) findViewById(R.id.textView);
        final TextView textView1 = (TextView) findViewById(R.id.textView1);
        final ImageButton imageButton = (ImageButton) findViewById(R.id.btnSend);

        mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        iNet = mConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!iNet.isConnected()) {
            if (HotspotController.isApOn(MainActivity.this) == false) {
                //Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT).show();
                editText.setVisibility(View.GONE);
                imageButton.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                linearLayout14.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.INVISIBLE);
                textView1.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.VISIBLE);
                linearLayout14.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);

                editText.clearFocus();
                Intent intent = getIntent();
                EXTRA_USERNAME extra_username = (EXTRA_USERNAME) getApplication();
                displayName = extra_username.getDisplayname();
                Toast.makeText(getApplicationContext(), "Welcome " + displayName + " to the group", Toast.LENGTH_SHORT).show();
                adapter = new MessageArrayAdapter(getApplicationContext(), R.layout.activity_main_item_message);
                adapterR = new MessageArrayAdapterRight(getApplicationContext(), R.layout.activity_main_item_right_message);
                listView.setAdapter(adapter);
                listView.setAdapter(adapterR);
                messengerReceive();
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
                        if (editText.length() == 0) {
                            //Toast.makeText(getApplicationContext(), displayName + " Write your message.. ", Toast.LENGTH_SHORT).show();
                            editText.requestFocus();
                        } else {
                            message = "@" + displayName + " " + editText.getText().toString();
                            String localmessage = editText.getText().toString();
                            //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            adapterR.add(new MessageItem(false, localmessage));
                            messengerSend(message);
                            loader.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        } else {
            //Toast.makeText(getApplicationContext(), "you are connected!", Toast.LENGTH_LONG).show();
            textView.setVisibility(View.INVISIBLE);
            textView1.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.VISIBLE);
            linearLayout14.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);

            editText.clearFocus();

            Intent intent = getIntent();
            //displayName = intent.getStringExtra(LauncherActivity.EXTRA_DISPLAYNAME);
            //displayName = extraUsername.getDisplayname();
            EXTRA_USERNAME extra_username = (EXTRA_USERNAME) getApplication();
            displayName = extra_username.getDisplayname();
            //EXTRA_USER = displayName;
            Toast.makeText(getApplicationContext(), "Welcome " + displayName + " to the group", Toast.LENGTH_SHORT).show();
            adapter = new MessageArrayAdapter(getApplicationContext(), R.layout.activity_main_item_message);
            //adapter = ListHelper.buildViewHolderAdapter(this, android.R.layout.simple_list_item_1,messengerReceive());
            listView.setAdapter(adapter);
            listView.setAdapter(adapterR);
            //PhoneActivity.callListen();
            messengerReceive();
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
                    if (editText.length() == 0) {
                        //Toast.makeText(getApplicationContext(), displayName + " Write your message.. ", Toast.LENGTH_SHORT).show();
                        editText.requestFocus();
                    } else {
                        message = " @" + displayName + " " + editText.getText().toString();
                        String localmessage = editText.getText().toString();
                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        adapterR.add(new MessageItem(true, localmessage));
                        messengerSend(message);


                        loader.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_hotspot:
                switch (var) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "Starting Hotspot..", Toast.LENGTH_SHORT).show();
                        HotspotController.isApOn(MainActivity.this);
                        HotspotController.configApState(MainActivity.this);
                        var = 1;
                        item.setTitle("Stop hotspot");
                        return true;
                    case 1:
                        var = 0;
                        item.setTitle("Start Hotspot");
                        Toast.makeText(getApplicationContext(), "Stopping Hotspot..", Toast.LENGTH_SHORT).show();
                        WifiManager mWifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                        mWifi.setWifiEnabled(true);
                        return true;
                }
                return true;
            case R.id.action_about:
                Toast.makeText(getApplicationContext(), "Application par Mustapha Essouri & Helmi Khamassi au cadre d'un projet fin des etudes 2015 au Polytech Centrale!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_peer:
                Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
                Bundle bundle = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.push_left_in, R.anim.push_left_out).toBundle();
                intent.putExtra(EXTRA_USER, displayName);
                startActivity(intent, bundle);
                return true;
            //case R.id.action_attachment:
            //    return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    public void messengerReceive() {
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {

                WifiManager wMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (wMan != null) {
                    WifiManager.MulticastLock lock = wMan.createMulticastLock(TAG);
                    lock.acquire();
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                MulticastSocket socket;
                try {
                    socket = new MulticastSocket(SOCKET_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                while (RECEIVER) {
                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (!RECEIVER)
                        break;

                    byte data[] = packet.getData();
                    int i;
                    for (i = 0; i < data.length; i++) {
                        if (data[i] == '\0')
                            break;
                    }

                    final String packetIp = packet.getAddress().toString();
                    final long deviceIp = mWifi.getConnectionInfo().getIpAddress();
                    final String ipDevice = String.valueOf(deviceIp);

                    try {
                        Texto = new String(data, 0, packet.getLength(), "UTF-8");
                        //Toast.makeText(getApplicationContext(), Texto.toString(), Toast.LENGTH_LONG).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (packetIp == ipDevice) {
                                    //Toast.makeText(getApplicationContext(), packetIp+ " == " +ipDevice,Toast.LENGTH_LONG).show();
                                    //adapter.add(new MessageItem(true, Texto.toString()));
                                } else if (packetIp != ipDevice) {
                                    //Toast.makeText(getApplicationContext(), packetIp+ " == " +ipDevice,Toast.LENGTH_LONG).show();
                                    adapter.add(new MessageItem(true, Texto.toString()));
                                }
                            }
                        });

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        });
        listener.start();
    }

    public boolean messengerSend(String message) throws IllegalArgumentException {
        //Toast.makeText(getApplicationContext(), "im in", Toast.LENGTH_SHORT).show();
        if (message == null || message.length() == 0)
            throw new IllegalArgumentException();

        if (iNet == null || !iNet.isConnected() || displayName == null) {
            String alertMessage = "You must be Connected to a Wi-Fi in order to send your message";
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(alertMessage).setTitle("Not connected to a Wi-Fi network");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return false;
        } else {
            new myTask().execute();

        }
        return true;
    }

    private class myTask extends AsyncTask<Void, Void, Void> {
        private DatagramSocket socket;
        private DatagramPacket packet;


        @Override
        protected Void doInBackground(Void... params) {
            int ip = mWifi.getConnectionInfo().getIpAddress();

            try {
                socket = new DatagramSocket();
                byte[] data = message.getBytes();
                int data_length = message.length();
                packet = new DatagramPacket(data, data_length, InetAddress.getByName(ipToString(ip, true)), SOCKET_PORT);
//                Toast.makeText(getApplicationContext(), ipToString(ip, true), Toast.LENGTH_SHORT).show();
                socket.send(packet);
                socket.disconnect();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // adapter.add(new MessageItem(true, message));
            loader.setVisibility(View.GONE);
            editText.setText("");
            super.onPostExecute(result);
        }

    }


}
