package nl.tudelft.cs4160.trustchain_android.network;

import java.io.IOException;
import java.net.UnknownHostException;

import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.network.peer.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public interface NetworkCommunicationListener {
    void updateInternalSourceAddress(String address);
    void updatePeerLists();
    void updateWan(MessageProto.Message message) throws UnknownHostException;
    void updateConnectionType(int connectionType, String typename, String subtypename);
    void handleIntroductionRequest(Peer peer, MessageProto.IntroductionRequest request) throws IOException;
    void handleIntroductionResponse(Peer peer, MessageProto.IntroductionResponse response) throws Exception;
    void handlePunctureRequest(Peer peer, MessageProto.PunctureRequest request) throws IOException;
    void handleReceivedBlock(Peer peer, MessageProto.TrustChainBlock block) throws IOException;
    void handleCrawlRequest(Peer peer, MessageProto.CrawlRequest request) throws IOException;
    void handlePuncture(Peer peer, MessageProto.Puncture puncture) throws IOException;
    PeerHandler getPeerHandler();
}
