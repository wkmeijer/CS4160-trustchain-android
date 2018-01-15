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
    private static Communication communication;
    private static TrustChainDBHelper dbHelper;

    private CommunicationSingleton(Context context, CommunicationListener communicationListener) {
    }

    public static void initContextAndListener(Context context, CommunicationListener communicationListener){
        CommunicationSingleton.context = context;
        CommunicationSingleton.dbHelper = new TrustChainDBHelper(context);
        //load keys
        KeyPair kp = Key.loadKeys(context);
        CommunicationSingleton.communication = new NetworkCommunication(context, dbHelper, kp, communicationListener);
        CommunicationSingleton.communication.start();
    }
    public static Communication getCommunication() {
        return communication;
    }

    public static TrustChainDBHelper getDbHelper() {
        return dbHelper;
    }

    public static void setCommunicationListener(CommunicationListener communicationListener) {
        communication.setCommunicationListener(communicationListener);
    }

    public static Context getContext() {
        return context;
    }
}
