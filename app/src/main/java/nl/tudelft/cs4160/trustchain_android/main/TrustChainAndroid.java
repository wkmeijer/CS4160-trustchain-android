package nl.tudelft.cs4160.trustchain_android.main;

import android.app.Application;
import android.content.Context;

/**
 * Used by the exceptionhandler to recover the app after a crash.
 * Can be removed when app is stable
 */
public class TrustChainAndroid extends Application {
    public static TrustChainAndroid instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
    public static TrustChainAndroid getInstance() {
        return instance;
    }
}