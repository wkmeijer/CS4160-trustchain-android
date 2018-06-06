package nl.tudelft.cs4160.trustchain_android.passport.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jmrtd.PassportService;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.passport.DocumentData;
import nl.tudelft.cs4160.trustchain_android.passport.PassportHolder;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.ManualInputActivity;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.util.Util;

public class PassportConActivity extends AppCompatActivity {

    private static final int GET_DOC_INFO = 1;
    private static final String TAG = PassportConActivity.class.getName();
    public static final int MAX_SIGN_BYTES = 8;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 0);
    }
    // Adapter for NFC connection
    private NfcAdapter mNfcAdapter;
    private DocumentData documentData;
    private ImageView progressView;
    private EditText dataToSignField;
    private PassportConActivity thisActivity;

    /**
     * This activity usually be loaded from the starting screen of the app.
     * This method handles the start-up of the activity, it does not need to call any other methods
     * since the activity onNewIntent() calls the intentHandler when a NFC chip is detected.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passport_con);

        Bundle extras = getIntent().getExtras();
        documentData = (DocumentData) extras.get(DocumentData.identifier);
        thisActivity = this;

        TextView notice = (TextView) findViewById(R.id.notice);
        progressView = (ImageView) findViewById(R.id.progress_view);
        dataToSignField = (EditText) findViewById(R.id.data_to_sign);

        dataToSignField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                byte[] bytes = text.getBytes();
                if (bytes.length > MAX_SIGN_BYTES) {
                    byte[] newbytes = Arrays.copyOfRange(bytes, 0, MAX_SIGN_BYTES);
                    editable.replace(0, editable.length(), new String(newbytes, StandardCharsets.UTF_8));
                }
            }
        });

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        checkNFCStatus();
        notice.setText(R.string.nfc_enabled);
    }

    /**
     * Some methods to ensure that when the activity is opened the ID is read.
     * When the activity is opened any nfc device held against the phone will cause, handleIntent to
     * be called.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // It's important, that the activity is in the foreground (resumed). Otherwise an IllegalStateException is thrown.
        setupForegroundDispatch(this, mNfcAdapter);
        checkNFCStatus();
    }

    @Override
    protected void onPause() {
        // Call this before super.onPause, otherwise an IllegalArgumentException is thrown as well.
        stopForegroundDispatch(this, mNfcAdapter);
        super.onPause();
    }

    /**
     * This method gets called, when a new Intent gets associated with the current activity instance.
     * Instead of creating a new activity, onNewIntent will be called. For more information have a look
     * at the documentation.
     *
     * In our case this method gets called, when the user attaches a Tag to the device.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Setup the recognition of nfc tags when the activity is opened (foreground)
     *
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Filter for nfc tag discovery
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link BaseActivity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    /**
     * Handle the intent following from a NFC detection.
     *
     */
    private void handleIntent(Intent intent) {
        progressView.setImageResource(R.drawable.nfc_icon_1);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        // if nfc tag holds no data, return
        if (tag == null) {
            return;
        }

        // Open a connection with the ID, return a PassportService object which holds the open connection
        PassportConnection pcon= new PassportConnection();
        PassportService ps;
        try {
            ps = pcon.openConnection(tag, documentData);
        } catch(Exception e) {
            handleConnectionFailed(e);
            ps = null;
        }

        if(ps != null) {
            try {
                String dataToSign = dataToSignField.getText().toString();
                byte[] bytesToSign = dataToSign.getBytes();
                // Get public key from dg15
                PublicKey pubKey = pcon.getAAPublicKey(ps);

                // Get voter information from dg1
//                Voter voter = pcon.getVoter(ps);
                Log.d(TAG, "Public key: " + pubKey);

                progressView.setImageResource(R.drawable.nfc_icon_2);

                Log.d(TAG, "Signing data: " + dataToSign);
                Log.d(TAG, "Signing bytes: " + bytesToSign);

                byte[] signed = pcon.signData(bytesToSign);

                Log.d(TAG, "Signed: " + signed);
                Toast.makeText(this, "Response: " + Util.byteArrayToHexString(signed), Toast.LENGTH_LONG).show();

//                SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE);
                Gson gson = new Gson();
//                String json = sharedPrefs.getString(getString(R.string.shared_preferences_key_election), "");
//                Election election = gson.fromJson(json, Election.class);
//
//                BlockChain bc = BlockChain.getInstance(null);
//                AssetBalance balance = bc.getVotingPassBalance(pubKey, election.getAsset());
//
//                ArrayList<byte[]> signedTransactions = bc.getSpendUtxoTransactions(pubKey, balance, pcon);
                progressView.setImageResource(R.drawable.nfc_icon_3);

                // when all data is loaded start ResultActivity
//                startResultActivity(pubKey, signedTransactions, voter);
            } catch (Exception ex) {
                handleConnectionFailed(ex);
            } finally {
                try {
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * When the connection fails, the exception gives more information about the error
      Display error messages to the user accordingly.
     * @param e - The exception that was raised when the passportconnectoin failed
     */
    public void handleConnectionFailed(Exception e) {
        e.printStackTrace();
        if(e.toString().toLowerCase().contains("authentication failed")){
            displayCheckInputSnackbar();
            progressView.setImageResource(R.drawable.nfc_icon_empty);
        } else if(e.toString().toLowerCase().contains("tag was lost")) {
            Toast.makeText(this, getString(R.string.NFC_error), Toast.LENGTH_LONG).show();
            progressView.setImageResource(R.drawable.nfc_icon_empty);
        } else {
            Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
            progressView.setImageResource(R.drawable.nfc_icon_empty);
        }
    }

    /**
     * Method to start the ResultActivity once all the data is loaded.
     * Creates new intent with the read data
     * @param pubKey The public key.
     * @param signedTransactions Signed data.
     * @param holder The PassportHolder.
     */
    public void startResultActivity(PublicKey pubKey, ArrayList<byte[]> signedTransactions, PassportHolder holder) {
        if(pubKey != null && signedTransactions != null) {

//            Intent intent = new Intent(getApplica
//            intent.putExtra("signedTransactions",tionContext(), ResultActivity.class);
////            intent.putExtra("voter", voter);
//            intent.putExtra("pubKey", pubKey); signedTransactions);
//            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.NFC_error), Toast.LENGTH_LONG).show();
            progressView.setImageResource(R.drawable.nfc_icon_empty);
        }
    }

    /**
     * Check if NFC is enabled and display error message when it is not.
     * This method should be called each time the activity is resumed, because people could change their
     * settings while the app is open.
     */
    public void checkNFCStatus() {
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, R.string.nfc_not_supported_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Display a notice that NFC is disabled and provide user with option to turn on NFC
        if (!mNfcAdapter.isEnabled()) {
            // Add listener for action in snackbar
            View.OnClickListener nfcSnackbarListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisActivity.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
            };

            Snackbar nfcDisabledSnackbar = Snackbar.make(findViewById(R.id.coordinator_layout),
                    R.string.nfc_disabled_error_snackbar, Snackbar.LENGTH_INDEFINITE);
            nfcDisabledSnackbar.setAction(R.string.nfc_disabled_snackbar_action, nfcSnackbarListener);
            nfcDisabledSnackbar.show();
        }
    }

    /**
     * This method displays a snackbar which has an action that starts the manual input activity.
     * It is meant to be displayed when the BAC-key is wrong.
     */
    public void displayCheckInputSnackbar() {
        // Add listener for action in snackbar
        View.OnClickListener inputSnackbarListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisActivity, ManualInputActivity.class);
                intent.putExtra(DocumentData.identifier, documentData);
                startActivityForResult(intent, GET_DOC_INFO);
            }
        };

        Snackbar inputSnackbar = Snackbar.make(findViewById(R.id.coordinator_layout),
                R.string.wrong_document_details, Snackbar.LENGTH_INDEFINITE);
        inputSnackbar.setAction(R.string.check_input, inputSnackbarListener);
        inputSnackbar.show();
    }

    /**
     * Update the documentdata in this activity and in the main activity.
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data The data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_DOC_INFO && resultCode == RESULT_OK) {
            documentData = (DocumentData) data.getExtras().get(DocumentData.identifier);
        }
    }
}
