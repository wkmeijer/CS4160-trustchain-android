package nl.tudelft.cs4160.trustchain_android.Network;

import java.io.IOException;
import java.net.UnknownHostException;

import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerHandler;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.BlockMessage;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.CrawlRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionRequest;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.IntroductionResponse;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Message;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.MessageException;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.Puncture;
import nl.tudelft.cs4160.trustchain_android.appToApp.connection.messages.PunctureRequest;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public interface NetworkCommunicationListener {
    void updateInternalSourceAddress(String address);
    void updatePeerLists();
    void updateWan(MessageProto.Message message) throws MessageException, UnknownHostException;
    void updateConnectionType(int connectionType, String typename, String subtypename);
    void handleIntroductionRequest(PeerAppToApp peer, MessageProto.IntroductionRequest request) throws IOException;
    void handleIntroductionResponse(PeerAppToApp peer, MessageProto.IntroductionResponse response) throws Exception;
    void handlePunctureRequest(PeerAppToApp peer, MessageProto.PunctureRequest request) throws IOException, MessageException;
    void handleReceivedBlock(PeerAppToApp peer, MessageProto.TrustChainBlock block) throws IOException, MessageException;
    void handleCrawlRequest(PeerAppToApp peer, MessageProto.CrawlRequest request) throws IOException, MessageException;
    void handlePuncture(PeerAppToApp peer, MessageProto.Puncture puncture) throws IOException;
    PeerHandler getPeerHandler();
}
