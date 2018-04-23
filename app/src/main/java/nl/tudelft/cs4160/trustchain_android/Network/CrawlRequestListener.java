package nl.tudelft.cs4160.trustchain_android.Network;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public interface CrawlRequestListener {
//    void handleCrawlRequestBlockMessageRequest(PeerAppToApp peer, BlockMessage message) throws IOException, MessageException;
    void blockAdded(MessageProto.TrustChainBlock block);
}
