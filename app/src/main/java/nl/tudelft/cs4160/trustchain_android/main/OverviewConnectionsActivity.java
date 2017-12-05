package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerList;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.WanVote;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionResponse;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.PunctureRequest;
import nl.tudelft.cs4160.trustchain_android.bencode.BencodeReadException;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;

public class OverviewConnectionsActivity extends AppCompatActivity {

    public final static String CONNECTABLE_ADDRESS = "130.161.211.254";
    final static int UNKNOWN_PEER_LIMIT = 20;
    final static String HASH_ID = "hash_id";
    final static int DEFAULT_PORT = 1873;
    final static int KNOWN_PEER_LIMIT = 10;
    private static final int BUFFER_SIZE = 2048;
    private TextView mWanVote;
    private Button mExitButton;
    private PeerListAdapter incomingPeerAdapter;
    private PeerListAdapter outgoingPeerAdapter;
    private DatagramChannel channel;

    private PeerList peerList;
    private List<PeerAppToApp> incomingList = new ArrayList<>();
    private List<PeerAppToApp> outgoingList = new ArrayList<>();
    private String hashId;
    private String networkOperator;
    private WanVote wanVote;
    private int connectionType;
    private ByteBuffer outBuffer;
    private InetSocketAddress internalSourceAddress;

    private Thread sendThread;
    private Thread listenThread;

    private boolean willExit = false;

    /**
     * Initialize views, start send and receive threads if necessary.
     *
     * @param savedInstanceState saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        initVariables(savedInstanceState);
        initExitButton();
        openChannel();
        updateConnectionType();
        addInitialPeer();
        startListenThread();
        startSendThread();
        showLocalIpAddress();
        initPeerLists();
        if (savedInstanceState != null) {
            updatePeerLists();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chain_menu:
                Intent chainExplorerActivity = new Intent(this, ChainExplorerActivity.class);
                startActivity(chainExplorerActivity);
                return true;
            case R.id.details_menu:
                Intent detailActivity = new Intent(this, DetailActivity.class);
                startActivity(detailActivity);
                return true;
            case R.id.switch_menu:
                Intent trustChainActivity = new Intent(this, TrustChainActivity.class);
                startActivity(trustChainActivity);
                return true;
            default:
                return true;
        }
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
        TelephonyManager telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        networkOperator = telephonyManager.getNetworkOperatorName();
        if (savedInstanceState != null) {
            peerList = new PeerList((ArrayList<PeerAppToApp>) savedInstanceState.getSerializable("peers"));
        } else {
            peerList = new PeerList();
        }
        hashId = getId();
        ((TextView) findViewById(R.id.peer_id)).setText(hashId.toString().substring(0, 4));
        wanVote = new WanVote();
        outBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        mWanVote = (TextView) findViewById(R.id.wanvote);
    }


    private void initExitButton() {
        mExitButton = (Button) findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                willExit = true;
                finish();
            }
        });
    }

    /**
     * Initialize the peerAppToApp lists.
     */
    private void initPeerLists() {
        ListView incomingPeerConnectionListView = (ListView) findViewById(R.id.incoming_peer_connection_list_view);
        ListView outgoingPeerConnectionListView = (ListView) findViewById(R.id.outgoing_peer_connection_list_view);
        incomingPeerAdapter = new PeerListAdapter(getApplicationContext(), R.layout.peer_connection_list_item, incomingList, PeerAppToApp.INCOMING);
        incomingPeerConnectionListView.setAdapter(incomingPeerAdapter);
        outgoingPeerAdapter = new PeerListAdapter(getApplicationContext(), R.layout.peer_connection_list_item, outgoingList, PeerAppToApp.OUTGOING);
        outgoingPeerConnectionListView.setAdapter(outgoingPeerAdapter);
    }

    /**
     * Add the intial hard-coded connectable peerAppToApp to the peerAppToApp list.
     */
    private void addInitialPeer() {
        try {
            addPeer(null, new InetSocketAddress(InetAddress.getByName(CONNECTABLE_ADDRESS), DEFAULT_PORT), PeerAppToApp.OUTGOING);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    /**
     * Request and display the current connection type.
     */
    private void updateConnectionType() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            connectionType = cm.getActiveNetworkInfo().getType();
        } catch (Exception e) {
            showToast("Can't connect: no active network");
            return;
        }
        ((TextView) findViewById(R.id.connection_type))
                .setText(cm.getActiveNetworkInfo().getTypeName() + " " + cm.getActiveNetworkInfo().getSubtypeName());
    }

    /**
     * Retrieve the local peerAppToApp id from storage.
     *
     * @return the peerAppToApp id.
     */
    private String getId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String id = preferences.getString(HASH_ID, null);
        if (id == null) {
            System.out.println("Generating new ID");
            id = generateHash();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(HASH_ID, id);
            editor.apply();
        }
        return id;
    }

    /**
     * Generate a new hash to be used as peerAppToApp id.
     *
     * @return the generated hash.
     */
    private String generateHash() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Start the thread send thread responsible for sending a {@link IntroductionRequest} to a random peerAppToApp every 5 seconds.
     */
    private void startSendThread() {
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        if (peerList.size() > 0) {
                            PeerAppToApp peer = getEligiblePeer(null);
                            if (peer != null) {
                                sendIntroductionRequest(peer);
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
                System.out.println("Send thread stopped");
            }
        });
        sendThread.start();
        System.out.println("Send thread started");
    }

    /**
     * Send an introduction request.
     *
     * @param peer the destination.
     * @throws IOException
     */
    private void sendIntroductionRequest(PeerAppToApp peer) throws IOException {
        IntroductionRequest request = new IntroductionRequest(hashId, peer.getAddress(), connectionType, networkOperator);
        sendMesssage(request, peer);
    }

    /**
     * Send a puncture request.
     *
     * @param peer         the destination.
     * @param puncturePeer the peerAppToApp to puncture.
     * @throws IOException
     */
    private void sendPunctureRequest(PeerAppToApp peer, PeerAppToApp puncturePeer) throws IOException {
        PunctureRequest request = new PunctureRequest(hashId, peer.getAddress(), internalSourceAddress, puncturePeer);
        sendMesssage(request, peer);
    }

    /**
     * Send a puncture.
     *
     * @param peer the destination.
     * @throws IOException
     */
    private void sendPuncture(PeerAppToApp peer) throws IOException {
        Puncture puncture = new Puncture(hashId, peer.getAddress(), internalSourceAddress);
        sendMesssage(puncture, peer);
    }

    /**
     * Send an introduction response.
     *
     * @param peer    the destination.
     * @param invitee the invitee to which the destination peerAppToApp will send a puncture request.
     * @throws IOException
     */
    private void sendIntroductionResponse(PeerAppToApp peer, PeerAppToApp invitee) throws IOException {
        List<PeerAppToApp> pexPeers = new ArrayList<>();
        for (PeerAppToApp p : peerList.getList()) {
            if (p.hasReceivedData() && p.getPeerId() != null && p.isAlive())
                pexPeers.add(p);
        }
        IntroductionResponse response = new IntroductionResponse(hashId, internalSourceAddress, peer
                .getAddress(), invitee, connectionType, pexPeers, networkOperator);
        sendMesssage(response, peer);
    }

    /**
     * Send a message to given peerAppToApp.
     *
     * @param message the message to send.
     * @param peer    the destination peerAppToApp.
     * @throws IOException
     */
    private synchronized void sendMesssage(Message message, PeerAppToApp peer) throws IOException {
        System.out.println("Sending " + message);
        outBuffer.clear();
        message.writeToByteBuffer(outBuffer);
        outBuffer.flip();
        channel.send(outBuffer, peer.getAddress());
        peer.sentData();
        updatePeerLists();
    }

    /**
     * Pick a random eligible peerAppToApp/invitee for sending an introduction request to.
     *
     * @param excludePeer peerAppToApp to which the invitee is sent.
     * @return the eligible peerAppToApp if any, else null.
     */
    private PeerAppToApp getEligiblePeer(PeerAppToApp excludePeer) {
        List<PeerAppToApp> eligiblePeers = new ArrayList<>();
        for (PeerAppToApp p : peerList.getList()) {
            if (p.isAlive() && !p.equals(excludePeer)) {
                eligiblePeers.add(p);
            }
        }
        if (eligiblePeers.size() == 0) {
            System.out.println("No elegible peers!");
            return null;
        }
        Random random = new Random();
        return eligiblePeers.get(random.nextInt(eligiblePeers.size()));
    }

    /**
     * Start the listen thread. The thread opens a new {@link DatagramChannel} and calls {@link OverviewConnectionsActivity#dataReceived(ByteBuffer,
     * InetSocketAddress)} for each incoming datagram.
     */
    private void startListenThread() {
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    while (!Thread.interrupted()) {
                        inputBuffer.clear();
                        SocketAddress address = channel.receive(inputBuffer);
                        inputBuffer.flip();
                        dataReceived(inputBuffer, (InetSocketAddress) address);
                    }
                } catch (IOException e) {
                    System.out.println("Listen thread stopped");
                }
            }
        });
        listenThread.start();
        System.out.println("Listen thread started");
    }

    /**
     * Resolve a peerAppToApp id or address to a peerAppToApp, else create a new one.
     *
     * @param id       the peerAppToApp's unique id.
     * @param address  the peerAppToApp's address.
     * @param incoming boolean indicator whether the peerAppToApp is incoming.
     * @return the resolved or create peerAppToApp.
     */
    private PeerAppToApp getOrMakePeer(String id, InetSocketAddress address, boolean incoming) {
        if (id != null) {
            for (PeerAppToApp peer : peerList.getList()) {
                if (id.equals(peer.getPeerId())) {
                    if (!address.equals(peer.getAddress())) {
                        System.out.println("Peer address differs from known address");
                        peer.setAddress(address);
                        peerList.removeDuplicates();
                    }
                    return peer;
                }
            }
        }
        for (PeerAppToApp peer : peerList.getList()) {
            if (peer.getAddress().equals(address)) {
                if (id != null) peer.setPeerId(id);
                return peer;
            }
        }
        return addPeer(id, address, incoming);
    }


    /**
     * Handle incoming data.
     *
     * @param data    the data {@link ByteBuffer}.
     * @param address the incoming address.
     */
    private void dataReceived(ByteBuffer data, InetSocketAddress address) {
        try {
            Message message = Message.createFromByteBuffer(data);
            System.out.println("Received " + message);
            String id = message.getPeerId();
            if (wanVote.vote(message.getDestination())) {
                System.out.println("Address changed to " + wanVote.getAddress());
                showLocalIpAddress();
            }
            setWanvote(wanVote.getAddress().toString());
            PeerAppToApp peer = getOrMakePeer(id, address, PeerAppToApp.INCOMING);
            if (peer == null) return;
            peer.received(data);
            switch (message.getType()) {
                case Message.INTRODUCTION_REQUEST:
                    handlIntroductionRequest(peer, (IntroductionRequest) message);
                    break;
                case Message.INTRODUCTION_RESPONSE:
                    handleIntroductionResponse(peer, (IntroductionResponse) message);
                    break;
                case Message.PUNCTURE:
                    handlePuncture(peer, (Puncture) message);
                    break;
                case Message.PUNCTURE_REQUEST:
                    handlePunctureRequest(peer, (PunctureRequest) message);
                    break;
            }
            updatePeerLists();
        } catch (BencodeReadException | IOException | MessageException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle an introduction request. Send a puncture request to the included invitee.
     *
     * @param peer    the origin peerAppToApp.
     * @param message the message.
     * @throws IOException
     */
    private void handlIntroductionRequest(PeerAppToApp peer, IntroductionRequest message) throws IOException {
        peer.setNetworkOperator(message.getNetworkOperator());
        peer.setConnectionType((int) message.getConnectionType());
        if (peerList.size() > 1) {
            PeerAppToApp invitee = getEligiblePeer(peer);
            if (invitee != null) {
                sendIntroductionResponse(peer, invitee);
                sendPunctureRequest(invitee, peer);
                System.out.println("Introducing " + invitee.getAddress() + " to " + peer.getAddress());
            }
        } else {
            System.out.println("Peerlist too small, can't handle introduction request");
            sendIntroductionResponse(peer, null);
        }
    }

    /**
     * Handle an introduction response. Parse incoming PEX peers.
     *
     * @param peer    the origin peerAppToApp.
     * @param message the message.
     */
    private void handleIntroductionResponse(PeerAppToApp peer, IntroductionResponse message) {
        peer.setConnectionType((int) message.getConnectionType());
        peer.setNetworkOperator(message.getNetworkOperator());
        List<PeerAppToApp> pex = message.getPex();
        for (PeerAppToApp pexPeer : pex) {
            if (hashId.equals(pexPeer.getPeerId())) continue;
            getOrMakePeer(pexPeer.getPeerId(), pexPeer.getAddress(), PeerAppToApp.OUTGOING);
        }
    }

    /**
     * Handle a puncture. Does nothing because the only purpose of a puncture is to punch a hole in the NAT.
     *
     * @param peer    the origin peerAppToApp.
     * @param message the message.
     * @throws IOException
     */
    private void handlePuncture(PeerAppToApp peer, Puncture message) throws IOException {
    }

    /**
     * Handle a puncture request. Sends a puncture to the puncture peerAppToApp included in the message.
     *
     * @param peer    the origin peerAppToApp.
     * @param message the message.
     * @throws IOException
     * @throws MessageException
     */
    private void handlePunctureRequest(PeerAppToApp peer, PunctureRequest message) throws IOException, MessageException {
        if (!peerList.peerExistsInList(message.getPuncturePeer())) {
            sendPuncture(message.getPuncturePeer());
        }
    }

    /**
     * Show the local IP address.
     */
    private void showLocalIpAddress() {
        new AsyncTask<Void, Void, InetAddress>() {

            @Override
            protected InetAddress doInBackground(Void... params) {
                try {
                    for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = (NetworkInterface) en.nextElement();
                        for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress;
                            }
                        }
                    }
                } catch (SocketException ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(InetAddress inetAddress) {
                super.onPostExecute(inetAddress);
                internalSourceAddress = new InetSocketAddress(inetAddress, DEFAULT_PORT);
                System.out.println("Local ip: " + inetAddress);
                TextView localIp = (TextView) findViewById(R.id.local_ip_address_view);
                localIp.setText(inetAddress.toString());
            }
        }.execute();
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
     * Show a toast.
     *
     * @param toast the text to show.
     */
    private void showToast(final String toast) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add a peerAppToApp to the peerAppToApp list.
     *
     * @param peerId   the peerAppToApp's id.
     * @param address  the peerAppToApp's address.
     * @param incoming whether the peerAppToApp is an incoming peerAppToApp.
     * @return the added peerAppToApp.
     */
    private synchronized PeerAppToApp addPeer(String peerId, InetSocketAddress address, boolean incoming) {
        if (hashId.equals(peerId)) {
            System.out.println("Not adding self");
            PeerAppToApp self = null;
            for (PeerAppToApp p : peerList.getList()) {
                if (p.getAddress().equals(wanVote.getAddress()))
                    self = p;
            }
            if (self != null) {
                peerList.getList().remove(self);
                System.out.println("Removed self");
            }
            return null;
        }
        if (wanVote.getAddress() != null && wanVote.getAddress().equals(address)) {
            System.out.println("Not adding peerAppToApp with same address as wanVote");
            return null;
        }
        for (PeerAppToApp peer : peerList.getList()) {
            if (peer.getPeerId() != null && peer.getPeerId().equals(peerId)) return peer;
            if (peer.getAddress().equals(address)) return peer;
        }
        final PeerAppToApp peer = new PeerAppToApp(peerId, address);
        if (incoming) {
            showToast("New incoming peerAppToApp from " + peer.getAddress());
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                peerList.add(peer);
                trimPeers();
                splitPeerList();
                incomingPeerAdapter.notifyDataSetChanged();
                outgoingPeerAdapter.notifyDataSetChanged();
                System.out.println("Added " + peer);
            }
        });
        return peer;
    }

    /**
     * Deletes the oldest peers based on constant limits {@value KNOWN_PEER_LIMIT} and {@value UNKNOWN_PEER_LIMIT}.
     */
    private void trimPeers() {
        limitKnownPeers(KNOWN_PEER_LIMIT);
        limitUnknownPeers(UNKNOWN_PEER_LIMIT);
    }

    /**
     * Limit the amount of known peers by deleting the oldest peers.
     *
     * @param limit the limit.
     */
    private void limitKnownPeers(int limit) {
        if (peerList.size() < limit) return;
        int knownPeers = 0;
        PeerAppToApp oldestPeer = null;
        long oldestDate = System.currentTimeMillis();
        for (PeerAppToApp peer : peerList.getList()) {
            if (peer.hasReceivedData()) {
                knownPeers++;
                if (peer.getCreationTime() < oldestDate) {
                    oldestDate = peer.getCreationTime();
                    oldestPeer = peer;
                }
            }
        }
        if (knownPeers > limit) {
            peerList.remove(oldestPeer);
        }
        if (knownPeers - 1 > limit) {
            limitKnownPeers(limit);
        }
    }

    /**
     * Limit the amount of known peers by deleting the oldest peers.
     *
     * @param limit the limit.
     */
    private void limitUnknownPeers(int limit) {
        if (peerList.size() < limit) return;
        int unknownPeers = 0;
        PeerAppToApp oldestPeer = null;
        long oldestDate = System.currentTimeMillis();
        for (PeerAppToApp peer : peerList.getList()) {
            if (!peer.hasReceivedData()) {
                unknownPeers++;
                if (peer.getCreationTime() < oldestDate) {
                    oldestDate = peer.getCreationTime();
                    oldestPeer = peer;
                }
            }
        }
        if (unknownPeers > limit) {
            peerList.remove(oldestPeer);
        }
        if (unknownPeers - 1 > limit) {
            limitKnownPeers(limit);
        }
    }

    /**
     * Update the showed peerAppToApp lists.
     */
    private void updatePeerLists() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                splitPeerList();
                incomingPeerAdapter.notifyDataSetChanged();
                outgoingPeerAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Split the peerAppToApp list between incoming and outgoing peers.
     */
    private void splitPeerList() {
        List<PeerAppToApp> newIncoming = new ArrayList<>();
        List<PeerAppToApp> newOutgoing = new ArrayList<>();
        for (PeerAppToApp peer : peerList.getList()) {
            if (peer.hasReceivedData()) {
                newIncoming.add(peer);
            } else {
                newOutgoing.add(peer);
            }
        }
        if (!newIncoming.equals(incomingList)) {
            incomingList.clear();
            incomingList.addAll(newIncoming);
        }
        if (!newOutgoing.equals(outgoingList)) {
            outgoingList.clear();
            outgoingList.addAll(newOutgoing);
        }
    }

    /**
     * Check whether an ip address is valid.
     *
     * @param s the text to check for validity.
     * @return the validity.
     */
    private boolean isValidIp(String s) {
        return Patterns.IP_ADDRESS.matcher(s).matches();
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
        if (!willExit)
            showToast("App will continue in background.");
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("peers", peerList.getList());

        super.onSaveInstanceState(outState);
    }
}
