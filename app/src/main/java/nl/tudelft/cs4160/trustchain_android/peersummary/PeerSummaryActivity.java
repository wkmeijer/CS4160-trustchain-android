package nl.tudelft.cs4160.trustchain_android.peersummary;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.CrawlRequestListener;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.peersummary.mutualblock.MutualBlockAdapter;
import nl.tudelft.cs4160.trustchain_android.peersummary.mutualblock.MutualBlockItem;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.util.FileDialog;
import nl.tudelft.cs4160.trustchain_android.util.Util;

import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.GENESIS_SEQ;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.createBlock;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.sign;

public class PeerSummaryActivity extends AppCompatActivity implements CrawlRequestListener {
    private final static String TAG = PeerSummaryActivity.class.toString();
    private static final int REQUEST_STORAGE_PERMISSIONS = 1;
    private static final int MAX_ATTACHMENT_SIZE = 61440; //Max file attachment size in bytes, set to 60bytes leaving 5kb for other block data, as the max message size in UDP is 64KB
    private Context context;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Network network;
    private InboxItem inboxItemOtherPeer;
    private TrustChainDBHelper DBHelper;
    TextView statusText;
    EditText messageEditText;
    PeerSummaryActivity thisActivity;
    DualSecret kp;
    TrustChainDBHelper dbHelper;

    private File transactionDocument;
    private TextView selectedFilePath;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peer_overview);
        this.context = this;
        DBHelper = new TrustChainDBHelper(this);
        inboxItemOtherPeer = (InboxItem) getIntent().getSerializableExtra("inboxItem");
        InboxItemStorage.markHalfBlockAsRead(this, inboxItemOtherPeer);
        initVariables();
        initializeMutualBlockRecycleView();
        requestChain();
    }

    /**
     * Sets the layout for the menu, after it is created.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trustchain_menu, menu);
        return true;
    }

    /**
     * Initializes the menu in the upper right corner.
     *
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chain_menu:
                Intent chainExplorerActivity = new Intent(this, ChainExplorerActivity.class);
                startActivity(chainExplorerActivity);
                return true;
            case R.id.close:
                if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                    ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                            .clearApplicationUserData();
                } else {
                    Toast.makeText(getApplicationContext(), "Requires at least API 19 (KitKat)", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * Initialization of all variables all textfields are set
     * such as local and external ip
     */
    private void initVariables() {
        thisActivity = this;
        statusText = findViewById(R.id.status);
        statusText.setMovementMethod(new ScrollingMovementMethod());

        messageEditText = findViewById(R.id.message_edit_text);
        mRecyclerView = findViewById(R.id.mutualBlocksRecyclerView);
        selectedFilePath = findViewById(R.id.selected_path);
        sendButton = findViewById(R.id.send_button);

        dbHelper = new TrustChainDBHelper(this);
        network = Network.getInstance(getApplicationContext());
    }

    /**
     * Initialize the recycle view that will show the mutual blocks of the user and the other peer.
     */
    private void initializeMutualBlockRecycleView() {
        FindMutualBlocksTask findMutualBlocksTask = new FindMutualBlocksTask(this);
        findMutualBlocksTask.execute();
    }

    /**
     * Request the chain of the other peer.
     * This is done by sending a crawl request to the peer.
     * If this peer receives the crawl request the peer will send his/her chain of blocks back.
     */
    public void requestChain() {
        network = Network.getInstance(getApplicationContext());
        network.setMutualBlockListener(this);
        network.updateConnectionType((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

        int sq = -5;
        MessageProto.TrustChainBlock block = dbHelper.getBlock(inboxItemOtherPeer.getPublicKeyPair().toBytes(), dbHelper.getMaxSeqNum(inboxItemOtherPeer.getPublicKeyPair().toBytes()));
        if (block != null) {
            sq = block.getSequenceNumber();
        } else {
            sq = GENESIS_SEQ;
        }

        final MessageProto.CrawlRequest crawlRequest =
                MessageProto.CrawlRequest.newBuilder()
                        .setPublicKey(ByteString.copyFrom(getMyPublicKey()))
                        .setRequestedSequenceNumber(sq)
                        .setLimit(100).build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("BCrawlTest", "Sent crawl request");
                    network.sendCrawlRequest(inboxItemOtherPeer.getPeerAppToApp(), crawlRequest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Load all blocks which contain the peer's public key.
     * The peer's public key is either in the communication if Trustchain blocks have been exchanged,
     * or will else likely be in the PubkeyAndAddress storage.
     *
     * @param view
     */
    public void onClickViewChain(View view) {
        // Try to instantiate public key.
        if (this.inboxItemOtherPeer.getPublicKeyPair() != null) {
            byte[] publicKey = this.inboxItemOtherPeer.getPublicKeyPair().toBytes();
            if (publicKey != null) {
                Intent intent = new Intent(context, ChainExplorerActivity.class);
                intent.putExtra(ChainExplorerActivity.BUNDLE_EXTRAS_PUBLIC_KEY , publicKey);
                startActivity(intent);
            }
        }
    }

    /**
     * Listener for the connection button.
     * On click a block is created and send to a inboxItemOtherPeer.
     * When we encounter an unknown inboxItemOtherPeer, send a crawl request to that inboxItemOtherPeer in order to get its
     * public key.
     * Also, when we want to send a block always send our last 5 blocks to the inboxItemOtherPeer so the block
     * request won't be rejected due to NO_INFO error.
     * <p>
     * This is code to simulate dispersy, note that this does not work properly with a busy network,
     * because the time delay between sending information to the inboxItemOtherPeer and sending the actual
     * to-be-signed block could cause gaps.
     * <p>
     * Also note that whatever goes wrong we will never get a valid full block, so the integrity of
     * the network is not compromised due to not using dispersy.
     */

    public void onClickSend(View view) throws UnsupportedEncodingException {
        byte[] publicKey = Key.loadKeys(this).getPublicKeyPair().toBytes();
        byte[] transactionData;
        String format = "";
        if (transactionDocument != null) {
            int size = (int) transactionDocument.length();
            format = transactionDocument.getName().substring(transactionDocument.getName().lastIndexOf('.') + 1);

            transactionData = new byte[size];

            BufferedInputStream inputstream;
            try {
                inputstream = new BufferedInputStream(new FileInputStream(transactionDocument));
                inputstream.read(transactionData, 0, size);
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.myCoordinatorLayout), e.getMessage(), Snackbar.LENGTH_LONG).show();
                return;
            }
        } else {
            transactionData = messageEditText.getText().toString().getBytes("UTF-8");
        }

        final MessageProto.TrustChainBlock block = createBlock(transactionData, format, DBHelper, publicKey, null, inboxItemOtherPeer.getPublicKeyPair().toBytes());
        final MessageProto.TrustChainBlock signedBlock = TrustChainBlockHelper.sign(block, Key.loadKeys(getApplicationContext()).getSigningKey());
        Log.d(TAG, "Signed block is " + signedBlock.toByteArray().length + " bytes");
        messageEditText.setText("");
        messageEditText.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        // insert the half block in your own chain
        new TrustChainDBHelper(this).insertInDB(signedBlock);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    network.sendBlockMessage(inboxItemOtherPeer.getPeerAppToApp(), signedBlock);
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout),"Half block send!", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(findViewById(R.id.myCoordinatorLayout),e.getMessage(), Snackbar.LENGTH_LONG);
                }
            }
        }).start();
    }

    /**
     * Called when pressing the sign button in a mutual block.
     * This method signs the half block when agreed with the pop-up.
     * @param block
     */
    public void requestSignPermission(final MessageProto.TrustChainBlock block) {
        //just to be sure run it on the ui thread
        //this is not necessary when this function is called from a AsyncTask
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(context);
                }
                try {
                    builder.setMessage("Do you want to sign Block[ " + block.getTransaction().getUnformatted().toString("UTF-8") + " ] from " + inboxItemOtherPeer.getUserName() + "?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    signAndSendHalfBlock(block);
                                }
                            })
                            .setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // do nothing?
                                }
                            });
                    builder.create();
                    builder.show();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * sign a received halfblock and directly send this block back to the peer.
     * @param linkedBlock
     */
    public void signAndSendHalfBlock(MessageProto.TrustChainBlock linkedBlock) {
        DualSecret keyPair = Key.loadKeys(this);
        MessageProto.TrustChainBlock block = createBlock(null, null, DBHelper,
                keyPair.getPublicKeyPair().toBytes(),
                linkedBlock, inboxItemOtherPeer.getPublicKeyPair().toBytes());

        final MessageProto.TrustChainBlock signedBlock = sign(block, keyPair.getSigningKey());

        //todo again we could do validation?
        DBHelper.insertInDB(signedBlock); // See read the docs (should be signed though)

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    network.sendBlockMessage(inboxItemOtherPeer.getPeerAppToApp(), signedBlock);

                    // update the mutualblocks list
                    initializeMutualBlockRecycleView();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Block received and added to the inbox.
     * If the received block should be displayed in the trustchain activity
     * the recycle adapter is reloaded. This makes sure new blocks show up
     * real-time.
     * @param block the received block
     */
    @Override
    public void blockAdded(MessageProto.TrustChainBlock block) {
        DualSecret keyPair = Key.loadKeys(this);
        byte[] myPublicKey = keyPair.getPublicKeyPair().toBytes();
        byte[] peerPublicKey = this.inboxItemOtherPeer.getPublicKeyPair().toBytes();
        byte[] publicKey = block.getPublicKey().toByteArray();
        byte[] linkedPublicKey = block.getLinkPublicKey().toByteArray();
        if (Arrays.equals(myPublicKey,linkedPublicKey) && Arrays.equals(peerPublicKey, publicKey)) {
            initializeMutualBlockRecycleView();
        }
    }

    /**
     * Get the public key of the current app user.
     * @return
     */
    public byte[] getMyPublicKey() {
        if (kp == null) {
            kp = Key.loadKeys(this);
        }
        return kp.getPublicKeyPair().toBytes();
    }

    public void onClickChooseFile(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissions();
            return;
        }

        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                transactionDocument = file;
                selectedFilePath.setText(file.getPath());
                if (file.length() > MAX_ATTACHMENT_SIZE) {
                    selectedFilePath.requestFocus();
                    selectedFilePath.setError("Too big (" + Util.readableSize(file.length()) + ")");
                    sendButton.setEnabled(false);
                } else {
                    selectedFilePath.setError(null);
                    sendButton.setEnabled(true);
                }
            }
        });
        fileDialog.showDialog();
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            finish();
                        } else {
                            onClickChooseFile(null);
                        }
                        break;
                    default:
                        Log.w(TAG, "Callback for unknown permission: " + permissions[i]);
                        break;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Asynctask to find blocks that both the user and the other peer have in common.
     * This method is called when the activity is started.
     */
    private static class FindMutualBlocksTask extends AsyncTask<Void, Void, ArrayList<MutualBlockItem>> {
        private WeakReference<PeerSummaryActivity> activityReference;

        FindMutualBlocksTask(PeerSummaryActivity context) {
            activityReference = new WeakReference<>(context);
        }

        protected ArrayList<MutualBlockItem> doInBackground(Void... params) {
            PeerSummaryActivity activity = activityReference.get();
            if (activity == null) return null;

            ArrayList<MutualBlockItem> mutualBlocks = new ArrayList<>();
            DualSecret keyPair = Key.loadKeys(activity);
            byte[] myPublicKey = keyPair.getPublicKeyPair().toBytes();
            byte[] peerPublicKey = activity.inboxItemOtherPeer.getPublicKeyPair().toBytes();


            for (MessageProto.TrustChainBlock block : activity.DBHelper.getBlocks(keyPair.getPublicKeyPair().toBytes(), true)) {
                byte[] linkedPublicKey = block.getLinkPublicKey().toByteArray();
                byte[] publicKey = block.getPublicKey().toByteArray();
                if (Arrays.equals(linkedPublicKey,myPublicKey) && Arrays.equals(publicKey,peerPublicKey)) {
                    int validationResultStatus;
                    try {
                        validationResultStatus = TrustChainBlockHelper.validate(block, activity.DBHelper).getStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    mutualBlocks.add(new MutualBlockItem(
                            activity.inboxItemOtherPeer.getUserName(), block, validationResultStatus));
                }
            }
            return mutualBlocks;
        }

        /**
         * Use the produced blocklist to update the UI.
         *
         * @param mutualBlockList
         */
        protected void onPostExecute(ArrayList<MutualBlockItem> mutualBlockList) {
            PeerSummaryActivity activity = activityReference.get();
            if (activity == null) return;

            activity.mLayoutManager = new LinearLayoutManager(activity);
            activity.mAdapter = new MutualBlockAdapter(activity, mutualBlockList);
            activity.mRecyclerView.setLayoutManager(activity.mLayoutManager);
            activity.mRecyclerView.setAdapter(activity.mAdapter);
        }
    }
}
