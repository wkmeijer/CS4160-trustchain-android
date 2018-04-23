package nl.tudelft.cs4160.trustchain_android.chainExplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public class ChainExplorerAdapter extends BaseAdapter {
    static final String TAG = "ChainExplorerAdapter";

    private final static String PEER_NAME_UNKNOWN = "unknown";

    private Context context;
    private List<MessageProto.TrustChainBlock> blocksList;
    private HashMap<ByteString, String> peerList = new HashMap<>();

    private byte[] chainPubKey;
    private byte[] myPubKey;

    public ChainExplorerAdapter(Context context, List<MessageProto.TrustChainBlock> blocksList, byte[] myPubKey,
                                byte[] chainPubKey) {
        this.context = context;
        this.blocksList = blocksList;
        this.chainPubKey = chainPubKey;
        this.myPubKey = myPubKey;
        // put my public key in the peerList

        peerList.put(ByteString.copyFrom(myPubKey), "me");
        if(!Arrays.equals(myPubKey, chainPubKey))
            peerList.put(ByteString.copyFrom(chainPubKey), retrievePeerName(chainPubKey));
        peerList.put(TrustChainBlockHelper.EMPTY_PK, "Genesis");
    }

    private String retrievePeerName(byte[] key) {
        String name = UserNameStorage.getPeerByPublicKey(context, new PublicKeyPair(key));
        if(name == null) {
            return PEER_NAME_UNKNOWN;
        }
        return name;
    }

    @Override
    public int getCount() {
        return blocksList.size();
    }

    @Override
    public Object getItem(int position) {
        return blocksList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Puts the data from a TrustChainBlockHelper object into the item textview.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageProto.TrustChainBlock block = (MessageProto.TrustChainBlock) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_trustchainblock,
                    parent, false);
        }
        // Check if we already know the peer, otherwise add it to the peerList
        ByteString pubKeyByteStr = block.getPublicKey();
        ByteString linkPubKeyByteStr = block.getLinkPublicKey();
        String peerAlias = getPeerAlias(pubKeyByteStr);
        String linkPeerAlias = getPeerAlias(linkPubKeyByteStr);

        // Check if the sequence numbers are 0, which would mean that they are unknown
        String seqNumStr;
        String linkSeqNumStr;
        if (block.getSequenceNumber() == 0) {
            seqNumStr = "Genesis Block";
        } else {
            seqNumStr = "seq: " + String.valueOf(block.getSequenceNumber());
        }

        if (block.getLinkSequenceNumber() == 0) {
            linkSeqNumStr = "";
        } else {
            linkSeqNumStr = "seq: " + String.valueOf(block.getLinkSequenceNumber());
        }

        // collapsed view
        TextView peer = convertView.findViewById(R.id.peer);
        TextView seqNum = convertView.findViewById(R.id.sequence_number);
        TextView linkPeer = convertView.findViewById(R.id.link_peer);
        TextView linkSeqNum = convertView.findViewById(R.id.link_sequence_number);
        TextView transaction = convertView.findViewById(R.id.transaction);
        View ownChainIndicator = convertView.findViewById(R.id.own_chain_indicator);
        View linkChainIndicator = convertView.findViewById(R.id.link_chain_indicator);

        // For the collapsed view, set the public keys to the aliases we gave them.
        peer.setText(peerAlias);
        seqNum.setText(seqNumStr);
        linkPeer.setText(linkPeerAlias);
        linkSeqNum.setText(linkSeqNumStr);
        transaction.setText(block.getTransaction().getUnformatted().toStringUtf8());

        // expanded view
        TextView pubKey = convertView.findViewById(R.id.pub_key);
        setOnClickListener(pubKey);
        TextView linkPubKey = convertView.findViewById(R.id.link_pub_key);
        setOnClickListener(linkPubKey);
        TextView prevHash = convertView.findViewById(R.id.prev_hash);
        TextView signature = convertView.findViewById(R.id.signature);
        TextView expTransaction = convertView.findViewById(R.id.expanded_transaction);

        pubKey.setText(ByteArrayConverter.bytesToHexString(pubKeyByteStr.toByteArray()));
        linkPubKey.setText(ByteArrayConverter.bytesToHexString(linkPubKeyByteStr.toByteArray()));
        prevHash.setText(ByteArrayConverter.bytesToHexString(block.getPreviousHash().toByteArray()));

        signature.setText(ByteArrayConverter.bytesToHexString(block.getSignature().toByteArray()));
        expTransaction.setText(block.getTransaction().getUnformatted().toStringUtf8());

        if (peerAlias.equals("me")) {
            ownChainIndicator.setBackgroundColor(ChainColor.getMyColor(context));
        }else{
            ownChainIndicator.setBackgroundColor(ChainColor.getColor(context,ByteArrayConverter.bytesToHexString(pubKeyByteStr.toByteArray())));
        }
        if (linkPeerAlias.equals("me")) {
            linkChainIndicator.setBackgroundColor(ChainColor.getMyColor(context));
        }else{
            linkChainIndicator.setBackgroundColor(ChainColor.getColor(context,ByteArrayConverter.bytesToHexString(pubKeyByteStr.toByteArray())));
        }
        return convertView;
    }

    private String getPeerAlias(ByteString key) {
        if (peerList.containsKey(key)) {
            return peerList.get(key);
        }
        String peerAlias = checkUserNameStorage(key.toByteArray());
        peerList.put(key, peerAlias);
        return peerAlias;
    }

    private String checkUserNameStorage(byte[] pubKey) {
        String name = UserNameStorage.getPeerByPublicKey(context, new PublicKeyPair(pubKey));
        if(name == null) {
            return "peer " + (peerList.size()-1);
        }
        return name;
    }

    private void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * On click chain explorer activity.
     * @param view
     */
    public void setOnClickListener(View view) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                String pubKey = tv.getText().toString();
                if(pubKey.equals("00")) {
                    showToast("00 is not a valid public key");
                    return;
                } else if(ByteArrayConverter.bytesToHexString(chainPubKey).equals(pubKey)) {
                    showToast("Already showing this public key");
                    return;
                }

                Intent intent = new Intent(context, ChainExplorerActivity.class);
                intent.putExtra(ChainExplorerActivity.BUNDLE_EXTRAS_PUBLIC_KEY,
                        ByteArrayConverter.hexStringToByteArray(tv.getText().toString()));
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        };
        view.setOnClickListener(onClickListener);
    }
}