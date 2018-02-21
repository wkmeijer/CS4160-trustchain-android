package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.Util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.color.ChainColor;

/**
 * Created by clint on 12-1-2018.
 */

public class MutualBlockAdapter extends RecyclerView.Adapter<MutualBlockAdapter.ViewHolder> {

    private ArrayList<MutualBlockItem> mutualBlocks;
    private Context context;

    /**
     * Constructor.
     *
     * @param mutualBlocks the list of blocks that both user have in common.
     */
    public MutualBlockAdapter(Context context, ArrayList<MutualBlockItem> mutualBlocks) {
        this.mutualBlocks = mutualBlocks;
        this.context = context;
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
        MutualBlockItem mutualBlockItem = mutualBlocks.get(position);
        if (mutualBlockItem != null) {
            Button signButton = viewHolder.signButton;
            String blockStatus = mutualBlockItem.getBlockStatus();
            TextView blockStatTv = viewHolder.blockStatTextView;
            setOnClickListenerSignBlock(viewHolder, position);
            if (blockStatus.substring(blockStatus.lastIndexOf(':') + 1).equals(" Half block awaiting signing")) {
                blockStatTv.setText(mutualBlockItem.getBlockStatus());
            } else if (blockStatus.substring(blockStatus.lastIndexOf(':') + 1).equals(" Full block not yet connected in chain")) {
                blockStatTv.setText(mutualBlockItem.getBlockStatus());
                signButton.setVisibility(View.GONE);
            } else if (blockStatus.substring(blockStatus.lastIndexOf(':') + 1).equals(" Valid block")) {
                blockStatTv.setText(mutualBlockItem.getBlockStatus());
                blockStatTv.setBackgroundColor(0xFF00FF00); // set background color green
                signButton.setVisibility(View.GONE);
            } else {
                // Partial previous or invalid block IGNORE
                // TODO should partial previous really be ignored?
            }
            viewHolder.userNameTextView.setText(UserNameStorage.getUserName(context));
            viewHolder.peerNameTextView.setText(mutualBlockItem.getPeerName());

            if (mutualBlockItem.getSeqNum() == 0) {
                viewHolder.seqNumTextView.setText("unknown");
            } else {
                viewHolder.seqNumTextView.setText("seq: " + String.valueOf(mutualBlockItem.getSeqNum()));
            }

            if (mutualBlockItem.getLinkSeqNum() == 0) {
                viewHolder.linkSeqNumTextView.setText("unknown");
            } else {
                signButton.setVisibility(View.GONE);
                viewHolder.linkSeqNumTextView.setText("seq: " + String.valueOf(mutualBlockItem.getLinkSeqNum()));
            }

            TextView transTv = viewHolder.transactionTextView;
            transTv.setText(mutualBlockItem.getTransaction());


            DualSecret keyPair = Key.loadKeys(context);
            String myPublicKeyString = ByteArrayConverter.bytesToHexString(keyPair.getPublicKeyPair().toBytes());
            String linkedKey = ByteArrayConverter.byteStringToString(mutualBlockItem.getBlock().getLinkPublicKey());
            String normalKey = ByteArrayConverter.byteStringToString(mutualBlockItem.getBlock().getPublicKey());

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
     * Define the listener on the button for the unsigned blocks and invoke the method of signing blocks
     *
     * @param holder The viewholder for this adapter.
     */
    private void setOnClickListenerSignBlock(final MutualBlockAdapter.ViewHolder holder, final int position) {
        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TrustChainActivity) context).requestPermission(mutualBlocks.get(position).getBlock());
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
            own_chain_indicator = (LinearLayout) itemView.findViewById(R.id.own_chain_indicator);
            link_chain_indicator_mutualBlock = (LinearLayout) itemView.findViewById(R.id.link_chain_indicator_mutualBlock);
            blockStatTextView = (TextView) itemView.findViewById(R.id.blockStatus);
            userNameTextView = (TextView) itemView.findViewById(R.id.userMutualBlock);
            peerNameTextView = (TextView) itemView.findViewById(R.id.peerMutualBlock);
            seqNumTextView = (TextView) itemView.findViewById(R.id.sequenceNumberMutualBlock);
            linkSeqNumTextView = (TextView) itemView.findViewById(R.id.linkSeqNumMutualBlock);
            transactionTextView = (TextView) itemView.findViewById(R.id.transactionMutualBlock);
            signButton = (Button) itemView.findViewById(R.id.sign_button);
        }
    }

}
