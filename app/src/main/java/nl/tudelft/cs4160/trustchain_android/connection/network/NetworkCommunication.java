package nl.tudelft.cs4160.trustchain_android.connection.network;

import android.util.Log;
import nl.tudelft.cs4160.trustchain_android.Peer;
import nl.tudelft.cs4160.trustchain_android.Util.DualKey;
import nl.tudelft.cs4160.trustchain_android.connection.Communication;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationListener;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;


/**
 * Class that is responsible for WiFi connection.
 */
public class NetworkCommunication extends Communication {

    private final static String TAG = NetworkCommunication.class.getName();

    public static final int DEFAULT_PORT = 8080;

    private static Server server;

    public NetworkCommunication(TrustChainDBHelper dbHelper, DualKey kp, CommunicationListener listener) {
        super(dbHelper, kp, listener);
    }

    public void sendMessage(Peer peer, MessageProto.Message message) {
        ClientTask task = new ClientTask(
                peer.getIpAddress(),
                peer.getPort(),
                message,
                getListener());
        task.execute();
    }

    @Override
    public void start() {
        if(server == null) {
            Log.d(TAG, "Creating new server");
            server = new Server(this, getListener());
            server.start();
        } else {
            server.setListener(getListener());
        }
    }

    @Override
    public void stop() {
        server.stop();
        server = null;
    }


    @Override
    public void addNewPublicKey(Peer p) {
        getPeers().put(p.getIpAddress(), p.getPublicKey());
    }


}