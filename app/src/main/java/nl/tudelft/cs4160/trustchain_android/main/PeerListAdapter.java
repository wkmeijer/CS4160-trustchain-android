package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.PubKeyAndAddressPairStorage;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;

public class PeerListAdapter extends ArrayAdapter<PeerAppToApp> {
    private final Context context;
    private CoordinatorLayout coordinatorLayout;

    public PeerListAdapter(Context context, int resource, List<PeerAppToApp> peerConnectionList, CoordinatorLayout coordinatorLayout) {
        super(context, resource, peerConnectionList);
        this.context = context;
        this.coordinatorLayout = coordinatorLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.peer_connection_list_item, parent, false);

            holder = new ViewHolder();
            holder.mStatusIndicator = convertView.findViewById(R.id.status_indicator);
            holder.mCarrier = convertView.findViewById(R.id.carrier);
            holder.mPeerId = convertView.findViewById(R.id.peer_id);
            holder.mDestinationAddress = convertView.findViewById(R.id.destination_address);
            holder.mReceivedIndicator = convertView.findViewById(R.id.received_indicator);
            holder.mSentIndicator = convertView.findViewById(R.id.sent_indicator);
            holder.mTableLayoutConnection = convertView.findViewById(R.id.tableLayoutConnection);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PeerAppToApp peer = getItem(position);

        holder.mPeerId.setText(peer.getPeerId() == null ? "" : peer.getPeerId());
        if (peer.getNetworkOperator() != null) {
            if (peer.getConnectionType() == ConnectivityManager.TYPE_MOBILE) {
                holder.mCarrier.setText(peer.getNetworkOperator());
            } else {

                if (OverviewConnectionsActivity.CONNECTABLE_ADDRESS.equals(peer.getExternalAddress().getHostAddress())) {
                    holder.mCarrier.setText("Server");
                } else {
                    holder.mCarrier.setText(connectionTypeString(peer.getConnectionType()));
                }
            }
        } else {
            holder.mCarrier.setText("");
        }
        if (peer.hasReceivedData()) {
            if (peer.isAlive()) {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusConnected));
            } else {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusCantConnect));
            }
        } else {
            if (peer.isAlive()) {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusConnecting));
            } else {
                holder.mStatusIndicator.setTextColor(context.getResources().getColor(R.color.colorStatusCantConnect));
            }
        }

        if (peer.getExternalAddress() != null) {
            holder.mDestinationAddress.setText(String.format("%s:%d", peer.getExternalAddress().toString().substring(1), peer.getPort()));
        }

        if (System.currentTimeMillis() - peer.getLastSendTime() < 200) {
            animate(holder.mSentIndicator);
        }
        if (System.currentTimeMillis() - peer.getLastReceiveTime() < 200) {
            animate(holder.mReceivedIndicator);
        }
        setOnClickListener(holder.mTableLayoutConnection, position);

        return convertView;
    }

    private void animate(final View view) {
        view.setAlpha(1);
        view.animate().alpha(0).setDuration(500).start();
    }

    private String connectionTypeString(int connectionType) {
        switch (connectionType) {
            case ConnectivityManager.TYPE_WIFI:
                return "Wifi";
            case ConnectivityManager.TYPE_BLUETOOTH:
                return "Bluetooth";
            case ConnectivityManager.TYPE_ETHERNET:
                return "Ethernet";
            case ConnectivityManager.TYPE_MOBILE:
                return "Mobile";
            case ConnectivityManager.TYPE_MOBILE_DUN:
                return "Mobile dun";
            case ConnectivityManager.TYPE_VPN:
                return "VPN";
            default:
                return "Unknown";
        }
    }

    static class ViewHolder {
        TextView mPeerId;
        TextView mCarrier;
        TextView mDestinationAddress;
        TextView mStatusIndicator;
        TextView mReceivedIndicator;
        TextView mSentIndicator;
        TableLayout mTableLayoutConnection;
    }

    /**
     * On click peer. If it's possible to add this peer
     * to your inbox this happens, otherwise a snackbar message
     * will explain why this isn't possible.
     * @param mTableLayoutConnection
     * @param position click position
     */
    private void setOnClickListener(TableLayout mTableLayoutConnection, int position) {
        mTableLayoutConnection.setTag(position);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                PeerAppToApp peer = getItem(pos);
                if(peer.isAlive() && peer.hasReceivedData()) {
                    PublicKeyPair pubKeyPair = PubKeyAndAddressPairStorage.getPubKeyByAddress(context, peer.getAddress().toString().replace("/", ""));
                    if(pubKeyPair != null) {
                        InboxItem i = new InboxItem(peer.getPeerId(), new ArrayList<Integer>(), peer.getAddress().getHostString(), pubKeyPair, peer.getPort());
                        UserNameStorage.setNewPeerByPublicKey(context, peer.getPeerId(), pubKeyPair);
                        InboxItemStorage.addInboxItem(context, i);
                        Snackbar mySnackbar = Snackbar.make(coordinatorLayout,
                                peer.getPeerId() + " added to inbox", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();
                    } else {
                        Snackbar mySnackbar = Snackbar.make(coordinatorLayout,
                                "This peer didn't send a public key yet", Snackbar.LENGTH_SHORT);
                        mySnackbar.show();
                    }
                } else {
                    Snackbar mySnackbar = Snackbar.make(coordinatorLayout,
                            "This peer is currently not active", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
        };
        mTableLayoutConnection.setOnClickListener(onClickListener);
    }
}
