package nl.tudelft.cs4160.trustchain_android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;

public class KeyActivity extends AppCompatActivity {

    private final static String TAG = KeyActivity.class.getName();

    private Button buttonNewKey;
    private Button signData;
    private TextView textPrivateKey;
    private TextView signedData;
    private Button verifySignature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);
        init();
    }

    private void init() {
        buttonNewKey = findViewById(R.id.new_key);
        textPrivateKey = findViewById(R.id.private_key);
        signData = findViewById(R.id.sign_data);
        signedData = findViewById(R.id.signed_data);
        verifySignature = findViewById(R.id.verify_sig);

        DualSecret kp = Key.ensureKeysExist(getApplicationContext());
        textPrivateKey.setText(Base64.encodeToString(kp.getPrivateKey().toBytes(), Base64.DEFAULT));

        verifySignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DualSecret kp = Key.loadKeys(getApplicationContext());
                byte[] sig = Base64.decode(signedData.getText().toString(), Base64.DEFAULT);
                byte[] data = new byte[] {0x30, 0x30, 0x30, 0x30,0x30, 0x30, 0x30, 0x30};
                if(Key.verify(kp.getVerifyKey(), data, sig)) {
                    Toast.makeText(getApplicationContext(), R.string.valid_signature, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.invalid_signature, Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonNewKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DualSecret kp = Key.createAndSaveKeys(getApplicationContext());
                textPrivateKey.setText(Base64.encodeToString(kp.getPrivateKey().toBytes(), Base64.DEFAULT));

            }
        });

        signData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DualSecret kp = Key.loadKeys(getApplicationContext());
                byte[] sig = Key.sign( kp.getSigningKey(), new byte[] {0x30, 0x30, 0x30, 0x30,0x30, 0x30, 0x30, 0x30});
                if(sig == null) {
                    Log.d(TAG,"No sig received");
                }
                signedData.setText(Base64.encodeToString(sig, Base64.DEFAULT));

            }
        });

    }


}
