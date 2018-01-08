package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.BootstrapIPStorage;

public class BootstrapActivity extends AppCompatActivity {
    private EditText bootstrapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootstrap);
    }

    /**
     * When the user clicks the button to submit the ip address
     * this activity is finished and the ip is passed on as data to the previous activity.
     * @param view
     */
    public void onClickConnect(View view) {
        bootstrapView = (EditText) findViewById(R.id.bootstrap_IP);
        try{
            Object res = InetAddress.getByName(bootstrapView.getText().toString());
            if(!(res instanceof Inet4Address) && !(res instanceof Inet6Address)){
                Log.i("Bootstrap IP Adress: ", res.toString());
                throw new Exception("Bootstrap IP is not a valid IP4 or IP6 address.");
            }
            Intent returnIntent = new Intent();
            BootstrapIPStorage.setIP(this, bootstrapView.getText().toString());
            returnIntent.putExtra("ConnectableAddress",bootstrapView.getText().toString());
            setResult(OverviewConnectionsActivity.RESULT_OK,returnIntent);
            finish();
        } catch (Exception e){
            Toast.makeText(this, "The bootstrap IP address is not a valid IP address: " + bootstrapView.getText().toString(), Toast.LENGTH_SHORT).show();
//             e.printStackTrace();
        }
    }
}
