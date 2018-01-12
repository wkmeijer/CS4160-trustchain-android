package nl.tudelft.cs4160.trustchain_android.Network;

import nl.tudelft.cs4160.trustchain_android.connection.CommunicationListener;

/**
 * Created by michiel on 12-1-2018.
 */

public interface NetworkCommunicationListener extends CommunicationListener {
    void updateInternalSourceAddress(String address);
}
