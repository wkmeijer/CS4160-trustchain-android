package nl.tudelft.cs4160.trustchain_android.testConnection;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;

/**
 * Created by rico on 28-2-18.
 */

public class TestConnection {


    // A queue of Runnables
    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();

    // Instantiates the queue of Runnables as a LinkedBlockingQueue
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    // Creates a thread pool manager
    private ThreadPoolExecutor mDecodeThreadPool = new ThreadPoolExecutor(
            500,       // Initial pool size
            500,       // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mDecodeWorkQueue);

    private final static int END_PORT =65535;
    private final static int START_PORT = 1000;
    private final static int NUM_PORTS = END_PORT - START_PORT;

    public TestConnection() {
        Log.e("TEST", "Opening from port " + START_PORT + " till " + END_PORT  + " channels");
        openSockets();
    }

    private void openSockets() {
        for(int i=0; i < NUM_PORTS; i++) {
            mDecodeThreadPool.execute(new ListenThread(START_PORT + i));
        }
    }

    private static class ListenThread implements Runnable{

        private DatagramSocket socket;
        private int port;
        private ListenThread(int port) {
            this.port = port;
        }

        public void run() {
            try {
                socket = new DatagramSocket(port);
                socket.setSoTimeout(2000);
                Log.e("TEST", "trying to receive on port" + port);
                byte[] buf = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                socket.receive(dp);
                Log.e("TEST", "received data from port " + port + "\n" +
                        "from: " + dp.getAddress());

            } catch (IOException e) {
                System.err.println("Error in port " + port);
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(socket != null)
                    socket.close();
            }

        }

    }



}
