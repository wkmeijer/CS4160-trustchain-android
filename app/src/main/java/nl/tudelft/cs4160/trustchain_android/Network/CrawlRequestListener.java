package nl.tudelft.cs4160.trustchain_android.Network;

import java.io.IOException;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;

public interface CrawlRequestListener {
    void handleCrawlRequestBlockMessageRequest(PeerAppToApp peer, BlockMessage message) throws IOException, MessageException;
    void blockAdded(BlockMessage block);
}
