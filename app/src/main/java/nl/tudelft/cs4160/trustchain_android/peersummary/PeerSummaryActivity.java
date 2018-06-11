package nl.tudelft.cs4160.trustchain_android.peersummary;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.network.CrawlRequestListener;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.util.FileDialog;
import nl.tudelft.cs4160.trustchain_android.util.Util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.GENESIS_SEQ;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.containsBinaryFile;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.createBlock;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper.sign;

public class PeerSummaryActivity extends AppCompatActivity implements CrawlRequestListener {
    private final static String TAG = PeerSummaryActivity.class.toString();
    private static final int REQUEST_STORAGE_PERMISSIONS = 1;
    private static final int MAX_ATTACHMENT_SIZE = 61440; //Max file attachment size in bytes, set to 60kbytes leaving 5kb for other block data, as the max message size in UDP is 64KB
    private Context context;
    private RecyclerView mRecyclerView;
    private MutualBlockAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Network network;
    private InboxItem inboxItemOtherPeer;
    private TrustChainDBHelper DBHelper;
    TextView statusText;
    EditText messageEditText;
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
        mLayoutManager = new LinearLayoutManager(this);

        mAdapter = new MutualBlockAdapter(this, inboxItemOtherPeer.getPeerAppToApp(),
                inboxItemOtherPeer.getPublicKeyPair().toBytes());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

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
                Intent intent = new Intent(this, ChainExplorerActivity.class);
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

    public void onClickSend(View view) {
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
            transactionData = messageEditText.getText().toString().getBytes(UTF_8);
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
                String txString = containsBinaryFile(block) ?
                        getString(R.string.type_file, block.getTransaction().getFormat()) :
                        block.getTransaction().getUnformatted().toString(UTF_8);
                builder.setMessage("Do you want to sign Block[ " + txString + " ] from " + inboxItemOtherPeer.getUserName() + "?")
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
            }
        });
    }

    /**
     * Sign a received halfblock and directly send this block back to the peer.
     * @param linkedBlock
     */
    public void signAndSendHalfBlock(final MessageProto.TrustChainBlock linkedBlock) {
        DualSecret keyPair = Key.loadKeys(this);
        MessageProto.TrustChainBlock block = createBlock(null, null, //Setting format and transaction not needed, they are already contained in linkedblock
                DBHelper, keyPair.getPublicKeyPair().toBytes(),
                linkedBlock, inboxItemOtherPeer.getPublicKeyPair().toBytes());

        final MessageProto.TrustChainBlock signedBlock = sign(block, keyPair.getSigningKey());

        //insert the new signed block
        DBHelper.insertInDB(signedBlock); // See read the docs (should be signed though)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    network.sendBlockMessage(inboxItemOtherPeer.getPeerAppToApp(), signedBlock);

                    //show that the block is valid
                    mAdapter.updateValidationResult(linkedBlock, ValidationResult.VALID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Notifies the adapter that the mutualblocks set has changed. It runs on the ui thread
     * just be sure when calling it from another thread.
     */
    public void mutualBlocksChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Block received and added to the inbox.
     * If the received block should be displayed in the trustchain activity
     * it will add the block to the adapter and notify the adapter of the change.
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
            try {
                mAdapter.addBlock(block, TrustChainBlockHelper.validate(block, dbHelper).getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    /**
     * Called when the user presses the 'send document' button.
     * Opens a FileDialog to let the user select a file to send.
     * @param view
     */
    public void onClickChooseFile(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Util.requestReadStoragePermissions(this, 1);
            return;
        }

        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        FileDialog fileDialog = new FileDialog(this, mPath);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                messageEditText.setEnabled(false);
                transactionDocument = file;
                selectedFilePath.setText(file.getPath());
                if (file.length() > MAX_ATTACHMENT_SIZE) {
                    selectedFilePath.requestFocus();
                    selectedFilePath.setError("Too big (" + Util.readableSize(file.length()) + ") max is " + Util.readableSize(MAX_ATTACHMENT_SIZE));
                    sendButton.setEnabled(false);
                } else {
                    selectedFilePath.setError(null);
                    sendButton.setEnabled(true);
                    Snackbar.make(findViewById(R.id.peer_summary_layout),getString(R.string.warning_files),Snackbar.LENGTH_LONG).show();
                }
            }
        });
        fileDialog.showDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            // Permissions are denied, do nothing.
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

}
