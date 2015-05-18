package indie.pfe.talkin;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;


public class PhoneActivity extends ActionBarActivity {

    public static final int LISTENER_PORT = 50003;
    public static final int BUF_SIZE = 1024;
    //public EXTRA_USERNAME extraUsername;
    public static String EXTRA_IP = "indie.pfe.talkin.IP";
    public static String EXTRA_NAME = "indie.pfe.talkin.CONTACT";
    private static long back_pressed;
    //public static String EXTRA_DISPLAYNAME = "indie.pfe.talkin.DISPLAYNAME";
    private String displayName;
    private ClientManager contactManager;
    private boolean STARTED = true;
    private boolean IN_CALL = false;
    private boolean LISTEN = false;
    private int var = 0;

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
        setContentView(R.layout.activity_phone);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        displayName = intent.getStringExtra(MainActivity.EXTRA_USER);
        //Toast.makeText(getApplicationContext(), displayName, Toast.LENGTH_LONG).show();
        //EXTRA_USERNAME extra_username = (EXTRA_USERNAME) getApplication();
        //displayName = extra_username.getDisplayname();

        //Toast.makeText(getApplicationContext(), displayName , Toast.LENGTH_SHORT).show();
        if (displayName != null) {
            contactManager = new ClientManager(displayName, getBroadcastIP());
            callListen();
            updateList();
            final FloatingActionButton call = (FloatingActionButton) findViewById(R.id.callBtn);
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.contactList);
                    int selectedButton = radioGroup.getCheckedRadioButtonId();
                    if (selectedButton == -1) {
                        final AlertDialog.Builder dialogAlert = new AlertDialog.Builder(PhoneActivity.this);
                        dialogAlert.setMessage("Select a contact to call").setTitle("No contact selected");
                        dialogAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog Alertdialog = dialogAlert.create();
                        Alertdialog.show();
                        return;
                    }
                    RadioButton radioButton = (RadioButton) findViewById(selectedButton);
                    String peerContact = radioButton.getText().toString();
                    InetAddress ip = contactManager.getContacts().get(peerContact);
                    IN_CALL = true;

                    Intent intent = new Intent(PhoneActivity.this, PhoneCallActivity.class);
                    intent.putExtra(EXTRA_NAME, peerContact);
                    String address = ip.toString();
                    address = address.substring(1, address.length());
                    intent.putExtra(EXTRA_IP, address);
                    //intent.putExtra(EXTRA_DISPLAYNAME, displayName);
                    startActivity(intent);
                }
            });
            final FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.refreshBtn);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateList();
                }
            });
        } else {
            Intent inten = new Intent(PhoneActivity.this, LauncherActivity.class);
            startActivity(inten);
        }


    }

    private InetAddress getBroadcastIP() {
        try {
            WifiManager mWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo iWifi = mWifi.getConnectionInfo();
            int adr = iWifi.getIpAddress();
            String address = MainActivity.ipToString(adr, true);
            InetAddress broadcastAddress = InetAddress.getByName(address);
            return broadcastAddress;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateList() {
        ConnectivityManager mConnect = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo nWifi = mConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!nWifi.isConnected()) {
            if (HotspotController.isApOn(this) == false) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(PhoneActivity.this);
                dialog.setMessage("You must be connected to see online users").setTitle("Not connected");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog1 = dialog.create();
                alertDialog1.show();
                return;
            } else {
                HashMap<String, InetAddress> peers = contactManager.getContacts();
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.contactList);
                Toast.makeText(getApplicationContext(), "Available contacts :" + String.valueOf(peers.size()), Toast.LENGTH_LONG).show();
                radioGroup.removeAllViews();
                for (String name : peers.keySet()) {
                    RadioButton radioButton = new RadioButton(getBaseContext());
                    radioButton.setText(name);
                    radioButton.setTextColor(Color.BLACK);
                    View v = new View(this);
                    v.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    v.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    radioGroup.addView(radioButton);
                }
                radioGroup.clearCheck();
            }
        } else {
            HashMap<String, InetAddress> peers = contactManager.getContacts();
            RadioGroup radioGroup = (RadioGroup) findViewById(R.id.contactList);
            Toast.makeText(getApplicationContext(), "Available contacts :" + String.valueOf(peers.size()), Toast.LENGTH_LONG).show();
            radioGroup.removeAllViews();
            for (String name : peers.keySet()) {
                RadioButton radioButton = new RadioButton(getBaseContext());
                radioButton.setText(name);
                radioButton.setTextColor(Color.BLACK);
                View v = new View(this);
                v.setLayoutParams(new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                v.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                radioGroup.addView(radioButton);
            }
            radioGroup.clearCheck();
        }
    }

    public void callListen() {
        LISTEN = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket(LISTENER_PORT);
                    socket.setSoTimeout(1000);
                    byte[] buffer = new byte[BUF_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, BUF_SIZE);
                    while (LISTEN) {
                        try {

                            socket.receive(packet);
                            String data = new String(buffer, 0, packet.getLength());
                            String action = data.substring(0, 4);
                            if (action.equals("CAL:")) {
                                String address = packet.getAddress().toString();
                                String name = data.substring(4, packet.getLength());
                                Intent intent = new Intent(PhoneActivity.this, PhoneInCalllActivity.class);
                                intent.putExtra(PhoneActivity.EXTRA_NAME, name);
                                intent.putExtra(PhoneActivity.EXTRA_IP, address.substring(1, address.length()));
                                IN_CALL = true;
                                startActivity(intent);
                            } else {
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    socket.disconnect();
                    socket.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void callListenStop() {
        LISTEN = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_phone, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_messenger) {
            Intent intent = new Intent(PhoneActivity.this, MainActivity.class);
            Bundle bundle = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.push_up_in, R.anim.push_up_out).toBundle();
            //intent.putExtra(EXTRA_DISPLAYNAME, displayName);
            startActivity(intent, bundle);
            return true;
        }
        if (id == R.id.action_refresh) {
            updateList();
            return true;
        }
        if (id == R.id.action_about) {
            Toast.makeText(getApplicationContext(), "Application par Mustapha Essouri & Helmi Khamassi au cadre d'un projet fin des etudes 2015 au Polytech Centrale!", Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_hotspot) {
            switch (var) {
                case 0:
                    Toast.makeText(getApplicationContext(), "Starting Hotspot..", Toast.LENGTH_SHORT).show();
                    HotspotController.isApOn(PhoneActivity.this);
                    HotspotController.configApState(PhoneActivity.this);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        EXTRA_USERNAME extra_username = (EXTRA_USERNAME) getApplication();
        displayName = extra_username.getDisplayname();
        STARTED = false;
        callListenStop();
        if (!IN_CALL) {
            finish();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        IN_CALL = false;
        STARTED = true;
        contactManager = new ClientManager(displayName, getBroadcastIP());
        callListen();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (STARTED) {
            if (displayName != null) {
                Log.i("onPause", displayName);
                contactManager.bye(displayName);
                contactManager.stopBroadcasting();
                contactManager.stopListening();
            } else {
                Toast.makeText(getApplicationContext(), "Connection Lost!", Toast.LENGTH_LONG).show();
            }
        }
        callListenStop();
    }


}
