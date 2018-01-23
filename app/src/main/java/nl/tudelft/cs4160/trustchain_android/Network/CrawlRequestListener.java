package nl.tudelft.cs4160.trustchain_android.Network;

import java.io.IOException;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * Created by michiel on 12-1-2018.
 */

public interface CrawlRequestListener {
    void handleCrawlRequestBlockMessageRequest(PeerAppToApp peer, BlockMessage message) throws IOException, MessageException;
    void blockAdded(BlockMessage block);
}
