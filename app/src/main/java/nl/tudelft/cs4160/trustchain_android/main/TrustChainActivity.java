package nl.tudelft.cs4160.trustchain_android.main;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.Peer;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerAdapter;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationListener;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationSingleton;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public class TrustChainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, CommunicationListener {


    public static String TRANSACTION_DATA = "Hello world!";
    private final static String TAG = TrustChainActivity.class.toString();
    private Context context;

    boolean developerMode = false;
    TextView externalIPText;
    TextView localIPText;
    TextView statusText;
    TextView developerModeText;
    Button sendButton;
    EditText editTextDestinationIP;
    EditText editTextDestinationPort;
    EditText messageEditText;
    SwitchCompat switchDeveloperMode;
    LinearLayout extraInformationPanel;

    TrustChainActivity thisActivity;
    InboxItem inboxItem;
    Peer peer;

    /**
     * Listener for the connection button.
     * On click a block is created and send to a inboxItem.
     * When we encounter an unknown inboxItem, send a crawl request to that inboxItem in order to get its
     * public key.
     * Also, when we want to send a block always send our last 5 blocks to the inboxItem so the block
     * request won't be rejected due to NO_INFO error.
     * <p>
     * This is code to simulate dispersy, note that this does not work properly with a busy network,
     * because the time delay between sending information to the inboxItem and sending the actual
     * to-be-signed block could cause gaps.
     * <p>
     * Also note that whatever goes wrong we will never get a valid full block, so the integrity of
     * the network is not compromised due to not using dispersy.
     */

    public void onClickSend(View view) throws UnsupportedEncodingException {
        Log.d("testLogs", "onClickSend");
        if (isConnected()) {
            TRANSACTION_DATA = messageEditText.getText().toString();
            byte[] transactionData = TRANSACTION_DATA.getBytes("UTF-8");
            CommunicationSingleton.getInstance(this,this).communication.signBlock(transactionData, peer);
        } else {
            peer = new Peer(null, editTextDestinationIP.getText().toString(),
                    Integer.parseInt(editTextDestinationPort.getText().toString()));
            CommunicationSingleton.getInstance(this,this).communication.connectToPeer(peer);
        }
    }

    /**
     * Load all blocks which contain the peer's public key.
     * The peer's public key is either in the communication if Trustchain blocks have been exchanged,
     * or will else likely be in the PubkeyAndAddress storage.
     * @param view
     */
    public void onClickViewChain(View view) {
        byte[] publicKey = null;

        // Try to instantiate public key.
        if (peer != null && peer.getIpAddress() != null) {
            publicKey =
                    CommunicationSingleton.getInstance(this,this).communication.getPublicKey(peer.getIpAddress());
        } else {
            String pubkeyStr = PubKeyAndAddressPairStorage.getPubKeyByAddress(context, inboxItem.getAddress());
            if(pubkeyStr != null) {
                publicKey = ChainExplorerAdapter.hexStringToByteArray(pubkeyStr);
            }
        }

        if (publicKey != null) {
            Intent intent = new Intent(context, ChainExplorerActivity.class);
            intent.putExtra("publicKey", publicKey);
            startActivity(intent);
        }
    }

    private boolean isConnected() {
        if (peer != null) {
            if (CommunicationSingleton.getInstance(this,this).communication.getPublicKey(peer.getIpAddress()) != null) {
                return true;
            } else {
                Log.d("testLogs", "getPublicKey == null");
            }
        } else {

            Log.d("testLogs", "peer == null");
        }
        return false;
    }

    private void enableMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageEditText.setVisibility(View.VISIBLE);
                sendButton.setText(getResources().getString(R.string.send));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_main);
        initVariables();
        init();
        setPeerDetails();
    }

    private void setPeerDetails() {
        inboxItem = (InboxItem) getIntent().getSerializableExtra("inboxItem");
        if (inboxItem != null) {
            String address = inboxItem.getAddress().toString();
         //   int port = inboxItem.getAddress().getPort();
            editTextDestinationIP.setText(address);
         //   editTextDestinationPort.setText(port + "");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trustchain_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chain_menu:
                Intent chainExplorerActivity = new Intent(this, ChainExplorerActivity.class);
                startActivity(chainExplorerActivity);
                return true;
            case R.id.close:
                if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                    ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                            .clearApplicationUserData();
                } else {
                    Toast.makeText(getApplicationContext(), "Requires at least API 19 (KitKat)", Toast.LENGTH_LONG).show();
                }
            default:
                return true;
        }
    }

    private void initVariables() {
        thisActivity = this;
        localIPText = (TextView) findViewById(R.id.my_local_ip);
        externalIPText = (TextView) findViewById(R.id.my_external_ip);
        statusText = (TextView) findViewById(R.id.status);
        statusText.setMovementMethod(new ScrollingMovementMethod());
        editTextDestinationIP = (EditText) findViewById(R.id.destination_IP);
        editTextDestinationPort = (EditText) findViewById(R.id.destination_port);
        sendButton = (Button) findViewById(R.id.send_button);
        messageEditText = (EditText) findViewById(R.id.message_edit_text);
        extraInformationPanel = (LinearLayout) findViewById(R.id.extra_information_panel);
        developerModeText = (TextView) findViewById(R.id.developer_mode_text);
        switchDeveloperMode = (SwitchCompat) findViewById(R.id.switch_developer_mode);
        switchDeveloperMode.setOnCheckedChangeListener(this);

    }

    private void init() {
        updateIP();
        updateLocalIPField(getLocalIPAddress());
    }

    /**
     * Updates the external IP address textfield to the given IP address.
     */
    public void updateExternalIPField(String ipAddress) {
        externalIPText.setText(ipAddress);
        Log.i(TAG, "Updated external IP Address: " + ipAddress);
    }

    /**
     * Updates the internal IP address textfield to the given IP address.
     */
    public void updateLocalIPField(String ipAddress) {
        localIPText.setText(ipAddress);
        Log.i(TAG, "Updated local IP Address:" + ipAddress);
    }

    /**
     * Finds the external IP address of this device by making an API call to https://www.ipify.org/.
     * The networking runs on a separate thread.
     *
     * @return a string representation of the device's external IP address
     */
    public void updateIP() {
        Thread thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try (java.util.Scanner s = new java.util.Scanner(new java.net.URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A")) {
                    final String ip = s.next();
                    // new thread to handle UI updates
                    TrustChainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateExternalIPField(ip);
                        }
                    });
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * Finds the local IP address of this device, loops trough network interfaces in order to find it.
     * The address that is not a loopback address is the IP of the device.
     *
     * @return a string representation of the device's IP address
     */
    public String getLocalIPAddress() {
        try {
            List<NetworkInterface> netInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface netInt : netInterfaces) {
                List<InetAddress> addresses = Collections.list(netInt.getInetAddresses());
                for (InetAddress addr : addresses) {
                    if (addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void updateLog(final String msg) {
        //just to be sure run it on the ui thread
        //this is not necessary when this function is called from a AsyncTask
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.status)).append(msg);
            }
        });
    }

    @Override
    public void requestPermission(final MessageProto.TrustChainBlock block, final Peer peer) {
        //just to be sure run it on the ui thread
        //this is not necessary when this function is called from a AsyncTask
        final TrustChainActivity trustChainActivity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(context);
                }
                try {
                    builder.setMessage("Do you want to sign Block[ " + block.getTransaction().toString("UTF-8") + " ] from " + peer.getName() + "?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    CommunicationSingleton.getInstance(trustChainActivity,trustChainActivity).communication.acceptTransaction(block, peer);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // do nothing?
                                }
                            });
                    builder.create();
                    builder.show();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void connectionSuccessful(byte[] publicKey) {
        this.peer.setPublicKey(publicKey);
        enableMessage();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        developerMode = isChecked;
        if (isChecked) {
            extraInformationPanel.setVisibility(View.VISIBLE);
            developerModeText.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            extraInformationPanel.setVisibility(View.GONE);
            developerModeText.setTextColor(getResources().getColor(R.color.colorGray));
        }
    }

}
