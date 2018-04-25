package nl.tudelft.cs4160.trustchain_android.inbox;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.network.peer.Peer;
import nl.tudelft.cs4160.trustchain_android.peersummary.PeerSummaryActivity;
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {
    private ArrayList<InboxItem> mDataset;
    private ArrayList<Peer> peerList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolderItem extends ViewHolder {
        // each data item is just a string in this case
        public TextView mUserNameTextView;
        public RelativeLayout mCounterRelativeLayout;
        public TextView mAddressTextView;
        public TextView mStatusTextView;

        public ViewHolderItem(LinearLayout v) {
            super(v);
            mUserNameTextView = v.findViewById(R.id.userNameTextView);
            mCounterRelativeLayout = v.findViewById(R.id.counterRelativeLayout);
            mAddressTextView = v.findViewById(R.id.addressTextView);
            mStatusTextView = v.findViewById(R.id.status_indicator);
        }
    }

    public static class ViewHolderAddPeer extends ViewHolder {
        // each data item is just a string in this case
        public Button mUserButton;

        public ViewHolderAddPeer(LinearLayout v) {
            super(v);
            mUserButton = v.findViewById(R.id.userButton);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout mWrapperLinearLayout;

        public ViewHolder(LinearLayout v) {
            super(v);
            mWrapperLinearLayout = v.findViewById(R.id.wrapperLinearLayout);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public InboxAdapter(ArrayList<InboxItem> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        switch (viewType) {
            case 0:
                // create a new view
                LinearLayout v0 = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_inbox, parent, false);
                return new ViewHolderItem(v0);
            case 1:
                // create a new view
                LinearLayout v1 = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_inbox_add_peer, parent, false);
                return new ViewHolderAddPeer(v1);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataset.size() == position) {
            return 1;
        }
        return 0;
    }

    // Replace the contents of a view (invoked by the layout manager)
    // This updates the connection status color of each peer.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (position == mDataset.size()) {
            setOnClickListenerNewUser((ViewHolderAddPeer)holder);
        } else {
            InboxItem inboxItem = mDataset.get(position);
            if (inboxItem != null) {
                ViewHolderItem h = (ViewHolderItem) holder;
                setOnClickListenerInboxItem(holder, position);
                h.mUserNameTextView.setText(inboxItem.getUserName());
                if (inboxItem.getAmountUnread() > 0) {
                    h.mCounterRelativeLayout.setVisibility(View.VISIBLE);
                } else {
                    h.mCounterRelativeLayout.setVisibility(View.GONE);
                }
                h.mAddressTextView.setText(inboxItem.getAddress() + ":" + inboxItem.getPort());

                h.mStatusTextView.setTextColor(h.mAddressTextView.getContext().getResources().getColor(R.color.colorStatusCantConnect));
                String name = inboxItem.getUserName();
                for (Peer curr : peerList) {
                    if (curr != null && curr.getPeerId() != null && curr.getPeerId().equals(name)) {
                        if (curr.isAlive()) {
                            h.mStatusTextView.setTextColor(h.mAddressTextView.getContext().getResources().getColor(R.color.colorStatusConnected));
                        }
                    }
                }
            }
        }
    }

    /**
     * Go to the overview connections activity.
     * Because this is always the previous activity on the navigation stack
     * we can do this with a finish().
     * @param holder
     */
    private void setOnClickListenerNewUser(final ViewHolderAddPeer holder) {
        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InboxActivity i = (InboxActivity) holder.mWrapperLinearLayout.getContext();
                i.finish();
            }
        };
        holder.mUserButton.setOnClickListener(mOnClickListener);
    }

    /**
     * On click inbox item go to trustchainactivity
     * and pass the selected item on as extra.
     * @param holder
     * @param position
     */
    private void setOnClickListenerInboxItem(final ViewHolder holder, final int position) {
        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.mWrapperLinearLayout.getContext(), PeerSummaryActivity.class);
                InboxItem inboxItem = mDataset.get(position);
                intent.putExtra("inboxItem", inboxItem);
                holder.mWrapperLinearLayout.getContext().startActivity(intent);
            }
        };
        holder.mWrapperLinearLayout.setOnClickListener(mOnClickListener);
    }

    public void setPeerList(ArrayList<Peer> peerList) {
        this.peerList = peerList;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size() + 1;
    }
}
