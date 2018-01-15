package nl.tudelft.cs4160.trustchain_android.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.Network.Network;
import nl.tudelft.cs4160.trustchain_android.Network.NetworkCommunicationListener;
import nl.tudelft.cs4160.trustchain_android.Peer;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.BootstrapIPStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.Util.Key;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.PeerListener;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionResponse;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.PunctureRequest;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationSingleton;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock.GENESIS_SEQ;

public class OverviewConnectionsActivity extends AppCompatActivity implements NetworkCommunicationListener, PeerListener {

    public static String CONNECTABLE_ADDRESS = "145.94.193.165";
    public final static int DEFAULT_PORT = 1873;
    private static final int BUFFER_SIZE = 65536;
    private TextView mWanVote;
    private PeerListAdapter incomingPeerAdapter;
    private PeerListAdapter outgoingPeerAdapter;
    private DatagramChannel channel;
    private Thread sendThread;
    private Thread listenThread;
    private TrustChainDBHelper dbHelper;
    private Network network;

    /**
     * Initialize views, start send and receive threads if necessary.
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openChannel();
        setContentView(R.layout.activity_overview);
        initVariables(savedInstanceState);
        initExitButton();
        addInitialPeer();
        startListenThread();
        startSendThread();
        initPeerLists();
        if (savedInstanceState != null) {
            updatePeerLists();
        }
        CommunicationSingleton.initContextAndListener(getApplicationContext(), null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Define what should be executed when one of the item in the menu is clicked.
     *
     * @param item the item in the menu.
     * @return true if everything was executed.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chain_menu:
                Intent chainExplorerActivity = new Intent(this, ChainExplorerActivity.class);
                startActivity(chainExplorerActivity);
                return true;
            case R.id.connection_explanation_menu:
                Intent ConnectionExplanationActivity = new Intent(this, ConnectionExplanationActivity.class);
                startActivity(ConnectionExplanationActivity);
                return true;
            case R.id.find_peer:
                Intent bootstrapActivity = new Intent(this, BootstrapActivity.class);
                startActivityForResult(bootstrapActivity, 1);
            default:
                return true;
        }
    }

    public void onClickOpenInbox(View view) {
        Intent inboxActivityIntent = new Intent(this, InboxActivity.class);
        startActivity(inboxActivityIntent);
    }

    private void initKey() {
        KeyPair kp = Key.loadKeys(getApplicationContext());
        if (kp == null) {
            kp = Key.createAndSaveKeys(getApplicationContext());
        }
        if (isStartedFirstTime(dbHelper, kp)) {
            MessageProto.TrustChainBlock block = TrustChainBlock.createGenesisBlock(kp);
            dbHelper.insertInDB(block);
        }
    }

    /**
     * Checks if this is the first time the app is started and returns a boolean value indicating
     * this state.
     *
     * @return state - false if the app has been initialized before, true if first time app started
     */
    public boolean isStartedFirstTime(TrustChainDBHelper dbHelper, KeyPair kp) {
        // check if a genesis block is present in database
        MessageProto.TrustChainBlock genesisBlock = dbHelper.getBlock(kp.getPublic().getEncoded(), GENESIS_SEQ);
        return (genesisBlock == null);
    }

    private void openChannel() {
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(DEFAULT_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVariables(Bundle savedInstanceState) {
        mWanVote = (TextView) findViewById(R.id.wanvote);

        dbHelper = new TrustChainDBHelper(this);
        initKey();
        network = Network.getInstance(getApplicationContext(), channel);

        if (savedInstanceState != null) {
            ArrayList<PeerAppToApp> list = (ArrayList<PeerAppToApp>) savedInstanceState.getSerializable("peers");
            network.setPeersFromSavedInstance(list);
        }
        network.setPeerListener(this);
        network.setNetworkCommunicationListener(this);
        network.updateConnectionType((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        ((TextView) findViewById(R.id.peer_id)).setText(network.getPeerHandler().getHashId());
    }

    /**
     * Initialize the exit button.
     */
    private void initExitButton() {
        Button mExitButton = (Button) findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * Initialize the inboxItem lists.
     */
    private void initPeerLists() {
        ListView incomingPeerConnectionListView = (ListView) findViewById(R.id.incoming_peer_connection_list_view);
        ListView outgoingPeerConnectionListView = (ListView) findViewById(R.id.outgoing_peer_connection_list_view);
        incomingPeerAdapter = new PeerListAdapter(getApplicationContext(), R.layout.peer_connection_list_item, network.getPeerHandler().getIncomingList(), PeerAppToApp.INCOMING);
        incomingPeerConnectionListView.setAdapter(incomingPeerAdapter);
        outgoingPeerAdapter = new PeerListAdapter(getApplicationContext(), R.layout.peer_connection_list_item, network.getPeerHandler().getOutgoingList(), PeerAppToApp.OUTGOING);
        outgoingPeerConnectionListView.setAdapter(outgoingPeerAdapter);
    }


    /**
     * This method is the callback when submitting the ip address.
     * The method is called when leaving the BootstrapActivity.
     * The filled in ip address is passed on to this method.
     * When the callback of the bootstrap activity is successful
     * set this ip address as ConnectableAddress in the preferences.
     *
     * @param requestCode
     * @param resultCode
     * @param data        the data passed on by the previous activity, in this case the ip address
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ConnectableAddress", data.getStringExtra("ConnectableAddress"));
                editor.apply();
                addInitialPeer();
            }
        }
    }

    /**
     * Add the intial hard-coded connectable inboxItem to the inboxItem list.
     */
    public void addInitialPeer() {
        try {
            String address = BootstrapIPStorage.getIP(this);
            if (address != null && !address.equals("")) {
                network.getPeerHandler().addPeer(null, new InetSocketAddress(InetAddress.getByName(address), DEFAULT_PORT), PeerAppToApp.OUTGOING);
            }
            network.getPeerHandler().addPeer(null, new InetSocketAddress(InetAddress.getByName(CONNECTABLE_ADDRESS), DEFAULT_PORT), PeerAppToApp.OUTGOING);
        } catch (UnknownHostException e) {
            e.printStackTrace();
       }
    }


    /**
     * Start the thread send thread responsible for sending a {@link IntroductionRequest} to a random inboxItem every 5 seconds.
     */
    private void startSendThread() {
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        if (network.getPeerHandler().size() > 0) {
                            PeerAppToApp peer = network.getPeerHandler().getEligiblePeer(null);
                            if (peer != null) {
                                network.sendIntroductionRequest(peer);
                              //  sendBlockMessage(peer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                } while (!Thread.interrupted());
                Log.d("App-To-App Log", "Send thread stopped");
            }
        });
        sendThread.start();
        Log.d("App-To-App Log", "Send thread started");
    }



    /**
     * Start the listen thread. The thread opens a new {@link DatagramChannel} and calls {@link Network#dataReceived(Context, ByteBuffer,
     * InetSocketAddress)} for each incoming datagram.
     */
    private void startListenThread() {
        final Context context = this;

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    while (!Thread.interrupted()) {
                        inputBuffer.clear();
                        SocketAddress address = channel.receive(inputBuffer);
                        inputBuffer.flip();
                        network.dataReceived(context, inputBuffer, (InetSocketAddress) address);
                    }
                } catch (IOException e) {
                    Log.d("App-To-App Log", "Listen thread stopped");
                }
            }
        });
        listenThread.start();
        Log.d("App-To-App Log", "Listen thread started");
    }

    /**
     * Set the external ip field based on the WAN vote.
     *
     * @param ip the ip address.
     */
    private void setWanvote(final String ip) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mWanVote.setText(ip);
            }
        });
    }

    /**
     * Update the showed inboxItem lists.
     */
    @Override
    public void updatePeerLists() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                network.getPeerHandler().splitPeerList();
                incomingPeerAdapter.notifyDataSetChanged();
                outgoingPeerAdapter.notifyDataSetChanged();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        listenThread.interrupt();
        sendThread.interrupt();
        channel.socket().close();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("peers", network.getPeerHandler().getPeerList());

        super.onSaveInstanceState(outState);
    }

    public void updateWan(Message message) throws MessageException {
        if (network.getPeerHandler().getWanVote().vote(message.getDestination())) {
            Log.d("App-To-App Log", "Address changed to " + network.getPeerHandler().getWanVote().getAddress());
            updateInternalSourceAddress(network.getPeerHandler().getWanVote().getAddress().toString());
        }
        setWanvote(network.getPeerHandler().getWanVote().getAddress().toString());
    }

    @Override
    public PeerAppToApp getOrMakePeer(String id, InetSocketAddress address, boolean incoming) {
        return this.network.getPeerHandler().getOrMakePeer(id, address,incoming);
    }

    /**
     * Display connectionType
     * @param connectionType
     * @param typename
     * @param subtypename
     */
    @Override
    public void updateConnectionType(int connectionType, String typename, String subtypename) {
        String connectionTypeStr = typename + " " + subtypename;
        ((TextView) findViewById(R.id.connection_type)).setText(connectionTypeStr);
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
    public void connectionSuccessful(byte[] publicKey) {

    }

    @Override
    public void requestPermission(final MessageProto.TrustChainBlock block, final Peer peer) {

    }

    @Override
    public void updateInternalSourceAddress(String address) {
        Log.d("App-To-App Log", "Local ip: " + address);
        TextView localIp = (TextView) findViewById(R.id.local_ip_address_view);
        localIp.setText(address);
    }

    @Override
    public void updateIncomingPeers() {
        incomingPeerAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateOutgoingPeers() {
        outgoingPeerAdapter.notifyDataSetChanged();
    }
}
