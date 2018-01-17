package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;

/**
 * Created by clint on 12-1-2018.
 */

public class MutualBlockAdapter extends RecyclerView.Adapter<MutualBlockAdapter.ViewHolder> {

    private ArrayList<MutualBlockItem> mutualBlocks;
    private Context context;

    /**
     * Constructor.
     * @param mutualBlocks the list of blocks that both user have in common.
     */
    public MutualBlockAdapter(Context context, ArrayList<MutualBlockItem> mutualBlocks) {
        this.mutualBlocks = mutualBlocks;
        this.context = context;
    }

    /**
     * Create a holder where item will be stored in the view.
     * @param parent the parent item.
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
     * @param viewHolder the view holder
     * @param position the position of the item.
     */
    @Override
    public void onBindViewHolder(MutualBlockAdapter.ViewHolder viewHolder, int position) {
        MutualBlockItem mutualBlockItem = mutualBlocks.get(position);
        if (mutualBlockItem != null) {
            if (mutualBlockItem.getBlockStatus() == "Signed"){
                TextView blockStatTv = viewHolder.blockStatTextView;
                blockStatTv.setBackgroundColor(0xFF00FF00); // set background color green
                blockStatTv.setText(mutualBlockItem.getBlockStatus());
                TextView signButton = viewHolder.signButton;
                signButton.setVisibility(View.GONE);
            } else {
                TextView blockStatTv = viewHolder.blockStatTextView;
                blockStatTv.setText(mutualBlockItem.getBlockStatus());
            }
            TextView userNameTv = viewHolder.userNameTextView;
            userNameTv.setText(UserNameStorage.getUserName(context));
            TextView peerNameTv = viewHolder.peerNameTextView;
            peerNameTv.setText(mutualBlockItem.getPeerName());
            TextView seqNumTv = viewHolder.seqNumTextView;
            seqNumTv.setText(Integer.toString(mutualBlockItem.getSeqNum()));
            TextView linkSeqNumTv = viewHolder.linkSeqNumTextView;
            linkSeqNumTv.setText(Integer.toString(mutualBlockItem.getLinkSeqNum()));
            TextView transTv = viewHolder.transactionTextView;
            transTv.setText(mutualBlockItem.getTransaction());
        }
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

        /**
         * Constructor.
         * @param itemView the view of the item.
         */
        public ViewHolder(View itemView) {
            super(itemView);
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
