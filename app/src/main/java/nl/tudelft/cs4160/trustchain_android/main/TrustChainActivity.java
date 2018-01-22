package nl.tudelft.cs4160.trustchain_android.main;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.Network.CrawlRequestListener;
import nl.tudelft.cs4160.trustchain_android.Network.Network;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;

import nl.tudelft.cs4160.trustchain_android.Util.Key;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;

import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock.GENESIS_SEQ;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock.createBlock;

public class TrustChainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, CrawlRequestListener {

    public final static int DEFAULT_PORT = 1873;
    private final static String TAG = TrustChainActivity.class.toString();
    private Context context;
    boolean developerMode = false;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Network network;
    private InboxItem inboxItemOtherPeer;
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
    KeyPair kp;
    TrustChainDBHelper dbHelper;

    /**
     * Listener for the connection button.
     * On click a block is created and send to a inboxItemOtherPeer.
     * When we encounter an unknown inboxItemOtherPeer, send a crawl request to that inboxItemOtherPeer in order to get its
     * public key.
     * Also, when we want to send a block always send our last 5 blocks to the inboxItemOtherPeer so the block
     * request won't be rejected due to NO_INFO error.
     * <p>
     * This is code to simulate dispersy, note that this does not work properly with a busy network,
     * because the time delay between sending information to the inboxItemOtherPeer and sending the actual
     * to-be-signed block could cause gaps.
     * <p>
     * Also note that whatever goes wrong we will never get a valid full block, so the integrity of
     * the network is not compromised due to not using dispersy.
     */

    public void onClickSend(View view) throws UnsupportedEncodingException {
        Log.d("testLogs", "onClickSend");
        PublicKey publicKey = Key.loadKeys(this).getPublic();
        byte[] transactionData = messageEditText.getText().toString().getBytes("UTF-8");
        final MessageProto.TrustChainBlock block = createBlock(transactionData, dbHelper, publicKey.getEncoded(), null, ByteArrayConverter.hexStringToByteArray(inboxItemOtherPeer.getPublicKey()));
        TrustChainBlock.sign(block, Key.loadKeys(getApplicationContext()).getPrivate());
        messageEditText.setText("");
        messageEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    network = Network.getInstance(getApplicationContext());
                    network.sendBlockMessage(inboxItemOtherPeer.getPeerAppToApp(), block, true);
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout),
                            "Half block send!", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void requestChain() {
        network = Network.getInstance(getApplicationContext());
        network.setCrawlRequestListener(this);
        network.updateConnectionType((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

        int sq = -5;
        MessageProto.TrustChainBlock block = dbHelper.getBlock(inboxItemOtherPeer.getPublicKey().getBytes(), dbHelper.getMaxSeqNum(inboxItemOtherPeer.getPublicKey().getBytes()));
        if (block != null) {
            sq = block.getSequenceNumber();
        } else {
            sq = GENESIS_SEQ;
        }

        final MessageProto.CrawlRequest crawlRequest =
                MessageProto.CrawlRequest.newBuilder()
                        .setPublicKey(ByteString.copyFrom(getMyPublicKey()))
                        .setRequestedSequenceNumber(sq)
                        .setLimit(100).build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("BCrawlTest", "Sent crawl request");
                    network.sendCrawlRequest(inboxItemOtherPeer.getPeerAppToApp(), crawlRequest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public byte[] getMyPublicKey() {
        if (kp == null) {
            kp = Key.loadKeys(this);
        }
        return kp.getPublic().getEncoded();
    }

    /**
     * Load all blocks which contain the peer's public key.
     * The peer's public key is either in the communication if Trustchain blocks have been exchanged,
     * or will else likely be in the PubkeyAndAddress storage.
     *
     * @param view
     */
    public void onClickViewChain(View view) {
        String publicKey = null;

        // Try to instantiate public key.
        if (this.inboxItemOtherPeer.getPublicKey() != null) {
            publicKey = this.inboxItemOtherPeer.getPublicKey();
            if (publicKey != null) {
                Intent intent = new Intent(context, ChainExplorerActivity.class);
                intent.putExtra("publicKey", publicKey);
                startActivity(intent);
            }
        }
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
        setContentView(R.layout.activity_main);
        this.context = this;
        inboxItemOtherPeer = (InboxItem) getIntent().getSerializableExtra("inboxItem");
        InboxItemStorage.markHalfBlockAsRead(this, inboxItemOtherPeer);
        initVariables();
        init();
        initializeMutualBlockRecycleView();
        requestChain();
    }

    /**
     * Initialize the recycle view that will show the mutual blocks of the user and the other peer.
     */
    private void initializeMutualBlockRecycleView() {
        ArrayList<MutualBlockItem> mutualBlockList = findMutualBlocks(dbHelper);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MutualBlockAdapter(this, mutualBlockList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Find blocks that both the user and the other peer have in common.
     *
     * @param dbHelper the database helper.
     * @return a list of mutual blocks.
     */
    private ArrayList<MutualBlockItem> findMutualBlocks(TrustChainDBHelper dbHelper) {
        ArrayList<MutualBlockItem> mutualBlocks = new ArrayList<>();
        for (MessageProto.TrustChainBlock block : dbHelper.getAllBlocks()) {
            if (ByteArrayConverter.bytesToHexString(block.getLinkPublicKey().toByteArray()).equals(inboxItemOtherPeer.getPublicKey())
                    || ByteArrayConverter.byteStringToString(block.getPublicKey()).equals(inboxItemOtherPeer.getPublicKey())) {
                String blockStatus = "Status of Block: ";
                int validationResultStatus = ValidationResult.NO_INFO;

                try {
                    validationResultStatus = TrustChainBlock.validate(block, dbHelper).getStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (validationResultStatus != ValidationResult.VALID) {
                    blockStatus += "Not signed by both parties";
                } else {
                    blockStatus += "Signed by both parties";
                }

                mutualBlocks.add(new MutualBlockItem(inboxItemOtherPeer.getUserName(), block.getSequenceNumber(), block.getLinkSequenceNumber(), blockStatus, block.getTransaction().toStringUtf8()));
            }
        }
        return mutualBlocks;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trustchain_menu, menu);
        return true;
    }

    /**
     * Initializes the menu on the upper right corner.
     *
     * @param item
     * @return
     */
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
        messageEditText = (EditText) findViewById(R.id.message_edit_text);
        extraInformationPanel = (LinearLayout) findViewById(R.id.extra_information_panel);
        developerModeText = (TextView) findViewById(R.id.developer_mode_text);
        mRecyclerView = (RecyclerView) findViewById(R.id.mutualBlocksRecyclerView);
        switchDeveloperMode = (SwitchCompat) findViewById(R.id.switch_developer_mode);
        switchDeveloperMode.setOnCheckedChangeListener(this);
        editTextDestinationIP = (EditText) findViewById(R.id.destination_IP);
        editTextDestinationPort = (EditText) findViewById(R.id.destination_port);

        dbHelper = new TrustChainDBHelper(this);
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
     */
    public void updateIP() {
        Thread thread = new Thread(new Runnable() {
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

    @Override
    public void handleCrawlRequestBlockMessageRequest(PeerAppToApp peer, BlockMessage message) throws IOException, MessageException {
        MessageProto.Message msg = message.getMessageProto();
        MessageProto.TrustChainBlock block = msg.getHalfBlock();
        if (dbHelper.getBlock(msg.getHalfBlock().getPublicKey().toByteArray(), msg.getHalfBlock().getSequenceNumber()) == null) {
            dbHelper.insertInDB(block);
        }
    }

}
