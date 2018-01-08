package nl.tudelft.cs4160.trustchain_android.connection;

import android.content.Context;

import java.security.KeyPair;

import nl.tudelft.cs4160.trustchain_android.Util.Key;
import nl.tudelft.cs4160.trustchain_android.connection.network.NetworkCommunication;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;

/**
 * Created by timbu on 08/01/2018.
 */

public class CommunicationSingleton {
    private static CommunicationSingleton instance = null;
    private static Context context;
    public Communication communication;
    public TrustChainDBHelper dbHelper;

    private CommunicationSingleton(Context context, CommunicationListener communicationListener) {
        this.context = context;
        dbHelper = new TrustChainDBHelper(context);
        //load keys
        KeyPair kp = Key.loadKeys(context);
        communication = new NetworkCommunication(dbHelper, kp, communicationListener);
        communication.start();
    }

    public static CommunicationSingleton getInstance(Context context, CommunicationListener communicationListener) {
        if (instance == null) {
            instance = new CommunicationSingleton(context, communicationListener);
        }
        return instance;
    }

    public void setCommunicationListener(CommunicationListener communicationListener) {
        communication.setCommunicationListener(communicationListener);
    }

    public static Context getContext() {
        return context;
    }
}
