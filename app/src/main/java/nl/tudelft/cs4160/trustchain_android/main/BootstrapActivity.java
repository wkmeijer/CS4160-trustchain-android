package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.sharedpreferences.BootstrapIPStorage;

public class BootstrapActivity extends AppCompatActivity {
    private EditText bootstrapView;
    private final String TAG = this.getClass().getName();

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
        bootstrapView = findViewById(R.id.bootstrap_IP);
        try{
            Object res = InetAddress.getByName(bootstrapView.getText().toString());
            if(!(res instanceof Inet4Address) && !(res instanceof Inet6Address)){
                Log.i(TAG, res.toString());
                throw new Exception("Bootstrap IP is not a valid IP4 or IP6 address.");
            }
            Intent returnIntent = new Intent();
            String newBootstrap = bootstrapView.getText().toString();
            BootstrapIPStorage.setIP(this, newBootstrap);
            returnIntent.putExtra("ConnectableAddress",newBootstrap);
            setResult(OverviewConnectionsActivity.RESULT_OK,returnIntent);
            Log.i(TAG, "Updated bootstrap IP to: " + newBootstrap);
            finish();
        } catch (Exception e){
            Toast.makeText(this, "The bootstrap IP address is not a valid IP address: " + bootstrapView.getText().toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
