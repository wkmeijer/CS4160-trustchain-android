package nl.tudelft.cs4160.trustchain_android.connection.network;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import nl.tudelft.cs4160.trustchain_android.Peer;
import nl.tudelft.cs4160.trustchain_android.connection.Communication;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationListener;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;


/**
 * Class is package private to prevent another activity from accessing it and breaking everything
 */
class Server {
    ServerSocket serverSocket;

    String responseLog = "";

    private Communication communication;
    private CommunicationListener listener;

    private SocketServerThread socketServerThread;

    public Server(Communication communication, CommunicationListener listener) {
        this.communication = communication;
        this.listener = listener;
    }

    public void setListener(CommunicationListener listener) {
        this.listener = listener;
        this.listener.updateLog("Server is waiting for messages...");
    }

    /**
     * Starts the socketServer thread which will listen for incoming messages.
     */
    public void start() {
        socketServerThread = new SocketServerThread();
        Thread thread = new Thread(socketServerThread );
        thread.start();

    }

    public void stop() {
        socketServerThread.stop();
    }

    private class SocketServerThread implements Runnable {
        static final int SocketServerPORT = 8080;

        private boolean running = true;

        /**
         * Starts the serverSocket, in the while loop it starts listening for messages.
         * serverSocket.accept() blocks until a message is received.
         */
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(SocketServerPORT);

                if (listener != null) {
                    listener.updateLog("Server is waiting for messages...");
                }

                while (running) {
                    Socket socket = serverSocket.accept();

                    // We have received a message, this could be either a crawl request or a halfblock
                    MessageProto.Message message = MessageProto.Message.parseFrom(socket.getInputStream());
                    Peer peer = new Peer(null, socket.getInetAddress().getHostAddress(), socket.getPort());
                    communication.receivedMessage(message, peer);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void stop() {
            running = false;
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
                responseLog += "Something wrong! " + e.toString() + "\n";
            } finally {

                if (listener != null) {
                    listener.updateLog("\n  Server: " + responseLog);
                }
            }
        }
    }
}