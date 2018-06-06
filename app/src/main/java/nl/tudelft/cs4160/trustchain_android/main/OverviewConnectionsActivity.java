package nl.tudelft.cs4160.trustchain_android.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.funds.FundsActivity;
import nl.tudelft.cs4160.trustchain_android.funds.qr.ExportWalletQRActivity;
import nl.tudelft.cs4160.trustchain_android.funds.qr.ScanQRActivity;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.network.NetworkStatusListener;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerListener;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.camera.CameraActivity;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.BootstrapIPStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.SharedPreferencesStorage;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;

public class OverviewConnectionsActivity extends AppCompatActivity implements NetworkStatusListener, PeerListener {

    // The server ip address, this is the bootstrap phone that's always running
    public static String CONNECTABLE_ADDRESS = "130.161.211.254";

    public final static int DEFAULT_PORT = 1873;
    private final static int BUFFER_SIZE = 65536;
    public final static String VERSION_KEY = "VERSION_KEY:";
    private PeerListAdapter activePeersAdapter;
    private PeerListAdapter newPeersAdapter;
    private TrustChainDBHelper dbHelper;
    private Network network;
    private PeerHandler peerHandler;
    private String wan = "";
    private static final String TAG = "OverviewConnections";

    /**
     * Initialize views, start send and receive threads if necessary.
     * Start a thread that refreshes the peers every second.
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview_connections);
        updateVersion();
        initVariables(savedInstanceState);
        initExitButton();
        addInitialPeer();
        startListenThread();
        startSendThread();
        initPeerLists();

        Runnable refreshTask = () -> {
            while(true) {
                updatePeerLists();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(refreshTask).start();
    }

    /**
     * Initialize all local variables
     * If this activity is opened with a saved instance state
     * we load the list of peers from this saved state.
     * @param savedInstanceState
     */
    private void initVariables(Bundle savedInstanceState) {
        dbHelper = new TrustChainDBHelper(this);
        initKey();
        peerHandler = new PeerHandler(Key.loadKeys(this).getPublicKeyPair(), UserNameStorage.getUserName(this));
        network = Network.getInstance(getApplicationContext());

        if (savedInstanceState != null) {
            ArrayList<Peer> list = (ArrayList<Peer>) savedInstanceState.getSerializable("peers");
            getPeerHandler().setPeerList(list);
        }

        getPeerHandler().setPeerListener(this);
        network.setNetworkStatusListener(this);
        network.updateConnectionType((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        ((TextView) findViewById(R.id.peer_id)).setText(UserNameStorage.getUserName(this));
    }

    /**
     * If the app is launched for the first time
     * a new keyPair is created and saved locally in the storage.
     * A genesis block is also created automatically.
     */
    private void initKey() {
        DualSecret kp = Key.loadKeys(getApplicationContext());
        if (kp == null) {
            kp = Key.createAndSaveKeys(getApplicationContext());
            MessageProto.TrustChainBlock block = TrustChainBlockHelper.createGenesisBlock(kp);
            dbHelper.insertInDB(block);
        }
    }

    /**
     * Check which version the current installed app is and take appropriate actions.
     * Update the stored version to the version of the current installed app.
     */
    private void updateVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int newVersion = pInfo.versionCode;
        int storedVersion = 0;
        try {
            storedVersion = SharedPreferencesStorage.readSharedPreferences(this,VERSION_KEY,Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // The way inboxitems are stored was changed, so this storage needs to be cleared
        if(storedVersion < 10) {
            InboxItemStorage.deleteAll(this);
        }

        try {
            SharedPreferencesStorage.writeSharedPreferences(this, VERSION_KEY, newVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inflates the menu with a layout.
     * @param menu
     * @return
     */
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
     * @return true if everything was executed.- [ ] No out-of-sleep feature on Android. dead overlay.
- [ ] update on_packet() every second a screen refresh and update message-timeout values on screen.
- [ ] design and implement a fault-resilient overlay. make flawless.
- [ ] documented algorithm
- [ ] Add last send message + got last response message
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
            case R.id.import_tokens:
                startActivity(new Intent(OverviewConnectionsActivity.this, ScanQRActivity.class));
                return true;
            case R.id.export_tokens:
                startActivity(new Intent(OverviewConnectionsActivity.this, ExportWalletQRActivity.class));
                return true;
            case R.id.funds:
                startActivity(new Intent(this, FundsActivity.class));
                return true;
            case R.id.find_peer:
                Intent bootstrapActivity = new Intent(this, ChangeBootstrapActivity.class);
                startActivityForResult(bootstrapActivity, 1);
                return true;
            case R.id.passport_scan:
                Intent cameraActivity = new Intent(this, CameraActivity.class);
                startActivityForResult(cameraActivity, 1);
                return true;
            default:
                return false;
        }
    }

    /**
     * On click open inbox button open the inbox activity.
     * @param view
     */
    public void onClickOpenInbox(View view) {
        InboxActivity.peerList = peerHandler.getPeerList();
        Intent inboxActivityIntent = new Intent(this, InboxActivity.class);
        startActivity(inboxActivityIntent);
    }

    /**
     * Initialize the exit button.
     */
    private void initExitButton() {
        Button mExitButton = findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(view -> finish());
    }

    /**
     * Initialize the inboxItem lists.
     */
    private void initPeerLists() {
        ListView connectedPeerConnectionListView = findViewById(R.id.active_peers_list_view);
        ListView incomingPeerConnectionListView = findViewById(R.id.new_peers_list_view);
        CoordinatorLayout content = findViewById(R.id.content);
        activePeersAdapter = new PeerListAdapter(getApplicationContext(), R.layout.item_peer_connection_list, peerHandler.getactivePeersList(), content);
        connectedPeerConnectionListView.setAdapter(activePeersAdapter);
        newPeersAdapter = new PeerListAdapter(getApplicationContext(), R.layout.item_peer_connection_list, peerHandler.getnewPeersList(), content);
        incomingPeerConnectionListView.setAdapter(newPeersAdapter);
    }


    /**
     * This method is the callback when submitting the new bootstrap address.
     * The method is called when leaving the ChangeBootstrapActivity.
     * The filled in ip address is passed on to this method.
     * When the callback of the bootstrap activity is successful
     * set this ip address as ConnectableAddress in the preferences.
     *
     * @param requestCode
     * @param resultCode
     * @param data the data passed on by the previous activity, in this case the ip address
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("ConnectableAddress", data.getStringExtra("ConnectableAddress"));
                editor.apply();
                String newBootstrap = BootstrapIPStorage.getIP(this);
                CONNECTABLE_ADDRESS = newBootstrap;
                addInitialPeer();
            }
        }
    }

    /**
     *
     * NETWORKING STUFF
     * Contains the main send thread and methods to update the network related UI items.
     */

    /**
     * Add the bootstrap to the peerlist.
     */
    public void addInitialPeer() {
        String address = BootstrapIPStorage.getIP(this);
        CreateInetSocketAddressTask createInetSocketAddressTask = new CreateInetSocketAddressTask(this);
        try {
            if (address != null && !address.equals("")) {
                createInetSocketAddressTask.execute(address, String.valueOf(DEFAULT_PORT));
            } else {
                createInetSocketAddressTask.execute(CONNECTABLE_ADDRESS, String.valueOf(DEFAULT_PORT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Asynctask to create the inetsocketaddress since network stuff can no longer happen on the main thread in android v3 (honeycomb).
     */
    private static class CreateInetSocketAddressTask extends AsyncTask<String, Void, InetSocketAddress> {
        private WeakReference<OverviewConnectionsActivity> activityReference;

        CreateInetSocketAddressTask(OverviewConnectionsActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected InetSocketAddress doInBackground(String... params) {
            InetSocketAddress inetSocketAddress = null;
            OverviewConnectionsActivity activity = activityReference.get();
            if (activity == null) return null;

            try {
                InetAddress connectableAddress = InetAddress.getByName(params[0]);
                int port = Integer.parseInt(params[1]);
                inetSocketAddress = new InetSocketAddress(connectableAddress, port);

                activity.peerHandler.addPeer(inetSocketAddress, null,null);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            return inetSocketAddress;
        }
    }

    /**
     * Start the thread send thread responsible for sending an introduction request to 10 random peers every 5 seconds as a heartbeat timer.
     * This number is chosen arbitrarily to avoid the app sending too much packets and using too much data keeping connections open with many peers.
     */
    private void startSendThread() {
        Thread sendThread = new Thread(() -> {
            boolean snackbarVisible = false;
            View view = findViewById(android.R.id.content);
            Snackbar networkUnreachableSnackbar = Snackbar.make(view, "Network unavailable", Snackbar.LENGTH_INDEFINITE);

            // wait max one second for the CreateInetSocketAddressTask to finish, indicated by that the bootstrap is added to the
            int t = 0;
            while(peerHandler.size() == 0 && t < 100) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t++;
            }

            while(true) {
                try {
                    if (peerHandler.size() > 0) {
                        // select 10 random peers to send an introduction request to
                        int limit = 10;
                        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
                        lock.readLock().lock();
                        List<Peer> connectedPeers = new ArrayList<>(peerHandler.getPeerList());
                        lock.readLock().unlock();
                        if(connectedPeers.size() <= limit) {
                            for(Peer peer : connectedPeers){
                                network.sendIntroductionRequest(peer);
                            }
                        } else {
                            Random rand = new Random();
                            for (int i = 0; i < limit; i++) {
                                int index = rand.nextInt(connectedPeers.size());
                                network.sendIntroductionRequest(connectedPeers.get(index));
                                connectedPeers.remove(index);
                            }
                        }
                    }
                    // if the network is reachable again, remove the snackbar
                    if(snackbarVisible) {
                        networkUnreachableSnackbar.dismiss();
                        snackbarVisible = false;
                        Log.i(TAG, "Network reachable again");
                    }
                } catch (SocketException e) {
                    Log.i(TAG, "network unreachable");
                    if(!snackbarVisible) {
                        networkUnreachableSnackbar.show();
                        snackbarVisible = true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sendThread.start();
        Log.d(TAG, "Send thread started");
    }

    /**
     * Start the listen thread. The thread opens a new {@link DatagramChannel} and calls {@link Network#dataReceived(Context, ByteBuffer,
     * InetSocketAddress)} for each incoming datagram.
     */
    private void startListenThread() {
        final Context context = this;

        Thread listenThread = new Thread(() -> {
            try {
                ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                while (!Thread.interrupted()) {
                    inputBuffer.clear();
                    SocketAddress address = network.receive(inputBuffer);
                    inputBuffer.flip();
                    network.dataReceived(context, inputBuffer, (InetSocketAddress) address);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Listen thread stopped");
            }
        });
        listenThread.start();
        Log.d(TAG, "Listen thread started");
    }

    /**
     * Update wan address
     * @param message a message that was received, the destination is our wan address
     */
    public void updateWan(MessageProto.Message message) throws UnknownHostException {
        InetAddress addr = InetAddress.getByAddress(message.getDestinationAddress().toByteArray());
        int port = message.getDestinationPort();
        InetSocketAddress socketAddress = new InetSocketAddress(addr, port);

        if (peerHandler.getWanVote().vote(socketAddress)) {
            wan = peerHandler.getWanVote().getAddress().toString();
        }
        setWanvote(wan.replace("/",""));
    }

    /**
     * Set the external ip field based on the WAN vote.
     *
     * @param ip the ip address.
     */
    private void setWanvote(final String ip) {
        new Handler(getMainLooper()).post(() -> {
            TextView mWanVote = findViewById(R.id.wanvote);
            mWanVote.setText(ip);
        });
    }

    /**
     * Update the showed inboxItem lists.
     * First split into new peers and the active list
     * Then remove the peers that aren't responding for a long time.
     */
    @Override
    public void updatePeerLists() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    peerHandler.removeDeadPeers();
                    peerHandler.splitPeerList();
                    activePeersAdapter.notifyDataSetChanged();
                    newPeersAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * When the app closes destroy the network channel.
     */
    @Override
    protected void onDestroy() {
        network.closeChannel();
        super.onDestroy();
    }

    /**
     * when loading the activity from instance state add
     * the peer list as serializable.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("peers", peerHandler.getPeerList());
        super.onSaveInstanceState(outState);
    }

    /**
     * Display connectionType
     *
     * @param connectionType
     * @param typename
     * @param subtypename
     */
    @Override
    public void updateConnectionType(int connectionType, String typename, String subtypename) {
        String connectionTypeStr = typename + " " + subtypename;
        ((TextView) findViewById(R.id.connection_type)).setText(connectionTypeStr);
    }

    /**
     * Update the source address textview
     * @param address
     */
    @Override
    public void updateInternalSourceAddress(final String address) {
        Log.d(TAG, "Local ip: " + address);

        runOnUiThread(() -> {
            TextView localIp = findViewById(R.id.local_ip_address_view);
            localIp.setText(address);
        });
    }

    /**
     * Update the connected peer adapter by notifying that the data has changed.
     */
    @Override
    public void updateActivePeers() {
        activePeersAdapter.notifyDataSetChanged();
    }

    /**
     * Update the incoming peer adapter by notifying that the data has changed. Usually when a new
     * peer has been found that we are not connected to yet.
     */
    @Override
    public void updateNewPeers() {
        newPeersAdapter.notifyDataSetChanged();
    }

    /**
     * Return the peer handler object.
     * @return
     */
    @Override
    public PeerHandler getPeerHandler() {
        return peerHandler;
    }
}