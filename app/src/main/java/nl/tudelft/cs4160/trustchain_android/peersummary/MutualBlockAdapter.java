package nl.tudelft.cs4160.trustchain_android.peersummary;

import android.content.Context;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainColor;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.util.OpenFileClickListener;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MutualBlockAdapter extends RecyclerView.Adapter<MutualBlockAdapter.ViewHolder> {

    private ArrayList<MessageProto.TrustChainBlock> mutualBlocks = new ArrayList<>();
    private ArrayList<Integer> validationResults = new ArrayList<>();
    private Context context;
    private DualSecret keyPair;
    private PublicKeyPair peerPublicKey;
    private TrustChainDBHelper dbHelper;

    private String myPeerName, peerName;
    private PeerSummaryActivity activity;


    /**
     * Constructor.
     **/
    public MutualBlockAdapter(PeerSummaryActivity activity, Peer peer) {
        this.context = activity.getApplicationContext();
        this.keyPair = Key.loadKeys(context);
        this.dbHelper = new TrustChainDBHelper(context);
        this.myPeerName = UserNameStorage.getUserName(context);
        this.peerName = peer.getName();
        this.activity = activity;
        this.peerPublicKey = peer.getPublicKeyPair();

        loadMutualBlocks();
    }

    /**
     * Load the mutual blocks async and update when a new mutual block has been found.
     */
    private void loadMutualBlocks() {
        Runnable loadMutualBlocks = new Runnable() {
            @Override
            public void run() {
                DualSecret keyPair = Key.loadKeys(activity);
                PublicKeyPair myPublicKey = keyPair.getPublicKeyPair();
                for (MessageProto.TrustChainBlock block : dbHelper.getBlocks(keyPair.getPublicKeyPair().toBytes(), true)) {
                    byte[] linkedPublicKey = block.getLinkPublicKey().toByteArray();
                    byte[] publicKey = block.getPublicKey().toByteArray();
                    if (Arrays.equals(linkedPublicKey,myPublicKey.toBytes()) && Arrays.equals(publicKey,peerPublicKey.toBytes())) {
                        int validationResultStatus;
                        try {
                            validationResultStatus = TrustChainBlockHelper.validate(block, dbHelper).getStatus();
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        addBlock(block, validationResultStatus);
                    }

                }
            }
        };
        //start handler thread which starts the above runnable
        HandlerThread ht = new HandlerThread("loadBlocks");
        ht.start();
        Handler handler= new Handler(ht.getLooper());
        handler.post(loadMutualBlocks);
        ht.quitSafely();
    }

    /**
     * Add a block to the mutual blocks with the corresponding validation result.
     * @param block The mutual block.
     * @param validationResult Validation result of this block.
     */
    public void addBlock(MessageProto.TrustChainBlock block, int validationResult) {
        if(mutualBlocks.contains(block)) return;
        mutualBlocks.add(block);
        validationResults.add(validationResult);
        activity.mutualBlocksChanged();
    }

    /**
     * Update the validation result of a mutual block.
     * @param block The mutual block.
     * @param validationResult The new validation result.
     */
    public void updateValidationResult(MessageProto.TrustChainBlock block, int validationResult) {
        for(int i=0; i<mutualBlocks.size(); i++) {
            if(block.equals(mutualBlocks.get(i))) {
                validationResults.set(i,validationResult);
                activity.mutualBlocksChanged();
                return;
            }
        }
    }



    /**
     * Create a holder where item will be stored in the view.
     *
     * @param parent   the parent item.
     * @param viewType the type of the view.
     * @return a viewholder containing an item.
     */
    @Override
    public MutualBlockAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mutualblock, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    /**
     * Populating data of an item in the holder.
     *
     * @param viewHolder the view holder
     * @param position   the position of the item.
     */
    @Override
    public void onBindViewHolder(MutualBlockAdapter.ViewHolder viewHolder, int position) {
        MessageProto.TrustChainBlock mutualBlock = mutualBlocks.get(position);
        int validationResult = validationResults.get(position);
        if (mutualBlock != null) {
            Button signButton = viewHolder.signButton;
            TextView blockStatTv = viewHolder.blockStatTextView;
            setOnClickListenerSignBlock(viewHolder, position);

            //if the linked public key is equal to ours, it means this block is addressed to us.
            //So if we don't have a linked block, we know we still need to sign it.
            if(Arrays.equals(mutualBlock.getLinkPublicKey().toByteArray(), keyPair.getPublicKeyPair().toBytes()) &&
                    dbHelper.getLinkedBlock(mutualBlock) == null) {
                if(validationResult == ValidationResult.INVALID) {
                    blockStatTv.setText(context.getResources().getString(R.string.invalid_block));
                    signButton.setVisibility(View.GONE);
                } else {
                    blockStatTv.setText(context.getResources().getString(R.string.need_sign));
                    signButton.setVisibility(View.VISIBLE);
                }
            } else {
                blockStatTv.setText(context.getResources().getString(R.string.valid_block));
                blockStatTv.setBackgroundColor(context.getResources().getColor(R.color.validBlock));
                signButton.setVisibility(View.GONE);
            }

            viewHolder.userNameTextView.setText(myPeerName);
            viewHolder.peerNameTextView.setText(peerName);

            if (mutualBlock.getSequenceNumber() == 0) {
                viewHolder.seqNumTextView.setText(context.getResources().getString(R.string.seq_unknown));
            } else {
                viewHolder.seqNumTextView.setText(context.getResources().getString(R.string.sequenceNum,
                        mutualBlock.getSequenceNumber()));
            }

            if (mutualBlock.getLinkSequenceNumber() == 0) {
                viewHolder.linkSeqNumTextView.setText(context.getResources().getString(R.string.seq_unknown));
            } else {
                signButton.setVisibility(View.GONE);
                viewHolder.linkSeqNumTextView.setText(context.getResources().getString(R.string.sequenceNum,
                        mutualBlock.getSequenceNumber()));
            }

            TextView transTv = viewHolder.transactionTextView;

            if (TrustChainBlockHelper.containsBinaryFile(mutualBlock)) {
                // If the block contains a file show the 'click to open' text
                transTv.setText(context.getString(R.string.click_to_open_file, mutualBlock.getTransaction().getFormat()));
                setOpenFileClickListener(transTv, mutualBlock);
            } else {
                transTv.setText(new String(mutualBlock.getTransaction().getUnformatted().toByteArray(), UTF_8));
            }

            String myPublicKeyString = null;
            if (keyPair != null) {
                myPublicKeyString = ByteArrayConverter.bytesToHexString(keyPair.getPublicKey().toBytes());
            }
            String linkedKey = ByteArrayConverter.byteStringToString(mutualBlock.getLinkPublicKey());
            String normalKey = ByteArrayConverter.byteStringToString(mutualBlock.getPublicKey());

            if (normalKey.equals(myPublicKeyString)) {
                viewHolder.own_chain_indicator.setBackgroundColor(ChainColor.getMyColor(context));
            } else {
                viewHolder.own_chain_indicator.setBackgroundColor(ChainColor.getColor(context, normalKey));
            }
            if (linkedKey.equals(myPublicKeyString)){
                viewHolder.link_chain_indicator_mutualBlock.setBackgroundColor(ChainColor.getMyColor(context));
            }else{
                viewHolder.link_chain_indicator_mutualBlock.setBackgroundColor(ChainColor.getColor(context, linkedKey));
            }
        }
    }

    /**
     * Takes a view and a TrustChainBlock, attaches a click listener to the view that extracts the
     * file from the given block and opens it using an intent.
     * @param view View to attach the listener to
     * @param block TrustChainBlock that contains a file
     */
    private void setOpenFileClickListener(View view, final MessageProto.TrustChainBlock block) {
        view.setOnClickListener(new OpenFileClickListener(activity, block));
    }


    /**
     * Define the listener on the button for the unsigned blocks and invoke the method of signing blocks
     *
     * @param holder The viewholder for this adapter.
     */
    private void setOnClickListenerSignBlock(final MutualBlockAdapter.ViewHolder holder, final int position) {
        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.requestSignPermission(mutualBlocks.get(position));
            }
        };
        holder.signButton.setOnClickListener(mOnClickListener);
    }





    @Override
    public int getItemCount() {
        return mutualBlocks.size();
    }

    /**
     * Define what needs to be shown in the view holder.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView blockStatTextView;
        TextView userNameTextView;
        TextView peerNameTextView;
        TextView seqNumTextView;
        TextView linkSeqNumTextView;
        TextView transactionTextView;
        Button signButton;
        LinearLayout link_chain_indicator_mutualBlock;
        LinearLayout own_chain_indicator;

        /**
         * Constructor.
         *
         * @param itemView the view of the item.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            own_chain_indicator = itemView.findViewById(R.id.own_chain_indicator);
            link_chain_indicator_mutualBlock = itemView.findViewById(R.id.link_chain_indicator_mutualBlock);
            blockStatTextView = itemView.findViewById(R.id.blockStatus);
            userNameTextView = itemView.findViewById(R.id.userMutualBlock);
            peerNameTextView = itemView.findViewById(R.id.peerMutualBlock);
            seqNumTextView = itemView.findViewById(R.id.sequenceNumberMutualBlock);
            linkSeqNumTextView = itemView.findViewById(R.id.linkSeqNumMutualBlock);
            transactionTextView = itemView.findViewById(R.id.transactionMutualBlock);
            signButton = itemView.findViewById(R.id.sign_button);
        }
    }

}
