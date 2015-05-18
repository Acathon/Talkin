package indie.pfe.talkin;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class LauncherActivity extends Activity {

    //public static String EXTRA_DISPLAYNAME;
    //public EXTRA_USERNAME extraUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ConnectivityManager mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo iNet = mConnect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!iNet.isConnected()) {
            if (HotspotController.isApOn(LauncherActivity.this) == false) {

                String Message;
                //Toast.makeText(getApplicationContext(), "Wi-Fi not enabled", Toast.LENGTH_LONG).show();
                Message = "You must be connected to a WI-FI network in order to get connected with other peers";
                AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
                builder.setMessage(Message).setTitle("Not connected to a WI-FI network");
                builder.setNegativeButton("WI-FI settings", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.i("WIFI", "Open WIFI settings");
                        Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }

                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.i("ALERDIALOG", "OK clicked");
                    }

                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        }

        final EditText editText = (EditText) findViewById(R.id.nextName);
        ImageButton imageButton = (ImageButton) findViewById(R.id.nextBtn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (editText.length() < 3) {
                    Toast.makeText(getApplicationContext(), "  You need to write at least 4 chars name", Toast.LENGTH_SHORT).show();
                    editText.setText("");
                } else {
                    String name = editText.getText().toString();
                    Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                    Bundle bundle = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.push_left_in, R.anim.push_left_out).toBundle();

                    EXTRA_USERNAME extra_username = (EXTRA_USERNAME) getApplication();
                    extra_username.setDisplayname(name);


                    //intent.putExtra(EXTRA_DISPLAYNAME, name);
                    //extraUsername.setDisplayname(name);
                    startActivity(intent, bundle);
                }
            }
        });
    }


}
