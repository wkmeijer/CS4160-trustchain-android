package nl.tudelft.cs4160.trustchain_android.chainExplorer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.protobuf.ByteString;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.color.ChainColor;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationSingleton;
import nl.tudelft.cs4160.trustchain_android.main.ChainExplorerInfoActivity;
import nl.tudelft.cs4160.trustchain_android.main.TrustChainActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.Peer.bytesToHex;
import static nl.tudelft.cs4160.trustchain_android.block.TrustChainBlock.pubKeyToString;

public class ChainExplorerAdapter extends BaseAdapter {
    static final String TAG = "ChainExplorerAdapter";

    Context context;
    List<MessageProto.TrustChainBlock> blocksList;
    HashMap<ByteString, String> peerList = new HashMap<>();

    public ChainExplorerAdapter(Context context, List<MessageProto.TrustChainBlock> blocksList, byte[] myPubKey) {
        this.context = context;
        this.blocksList = blocksList;
        // put my public key in the peerList
        peerList.put(ByteString.copyFrom(myPubKey), "me");
        peerList.put(TrustChainBlock.EMPTY_PK, "unknown");
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
     * Puts the data from a TrustChainBlock object into the item textview.
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
        try {
            ValidationResult result = TrustChainBlock.validate(block, CommunicationSingleton.getDbHelper());
            for(String error : result.getErrors()){
                Log.d("testt", error);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Check if we already know the peer, otherwise add it to the peerList
        ByteString pubKeyByteStr = block.getPublicKey();
        ByteString linkPubKeyByteStr = block.getLinkPublicKey();
        String peerAlias;
        String linkPeerAlias;

        if (peerList.containsKey(pubKeyByteStr)) {
            peerAlias = peerList.get(pubKeyByteStr);
        } else {
            peerAlias = "peer" + (peerList.size() - 1);
            peerList.put(pubKeyByteStr, peerAlias);
        }

        if (peerList.containsKey(linkPubKeyByteStr)) {
            linkPeerAlias = peerList.get(linkPubKeyByteStr);
        } else {
            linkPeerAlias = "peer" + (peerList.size() - 1);
            peerList.put(linkPubKeyByteStr, linkPeerAlias);
        }

        // Check if the sequence numbers are 0, which would mean that they are unknown
        String seqNumStr;
        String linkSeqNumStr;
        if (block.getSequenceNumber() == 0) {
            seqNumStr = "unknown";
        } else {
            seqNumStr = "seq: " + String.valueOf(block.getSequenceNumber());
        }

        if (block.getLinkSequenceNumber() == 0) {
            linkSeqNumStr = "unknown";
        } else {
            linkSeqNumStr = "seq: " + String.valueOf(block.getLinkSequenceNumber());
        }

        // collapsed view
        TextView peer = (TextView) convertView.findViewById(R.id.peer);
        TextView seqNum = (TextView) convertView.findViewById(R.id.sequence_number);
        TextView linkPeer = (TextView) convertView.findViewById(R.id.link_peer);
        TextView linkSeqNum = (TextView) convertView.findViewById(R.id.link_sequence_number);
        TextView transaction = (TextView) convertView.findViewById(R.id.transaction);
        View ownChainIndicator = convertView.findViewById(R.id.own_chain_indicator);
        View linkChainIndicator = convertView.findViewById(R.id.link_chain_indicator);

        // For the collapsed view, set the public keys to the aliases we gave them.
        peer.setText(peerAlias);
        seqNum.setText(seqNumStr);
        linkPeer.setText(linkPeerAlias);
        linkSeqNum.setText(linkSeqNumStr);
        transaction.setText(block.getTransaction().toStringUtf8());

        // expanded view
        TextView pubKey = (TextView) convertView.findViewById(R.id.pub_key);
        setOnClickListener(pubKey);
        TextView linkPubKey = (TextView) convertView.findViewById(R.id.link_pub_key);
        setOnClickListener(linkPubKey);
        TextView prevHash = (TextView) convertView.findViewById(R.id.prev_hash);
        TextView signature = (TextView) convertView.findViewById(R.id.signature);
        TextView expTransaction = (TextView) convertView.findViewById(R.id.expanded_transaction);

        pubKey.setText(bytesToHex(pubKeyByteStr.toByteArray()));
        linkPubKey.setText(bytesToHex(linkPubKeyByteStr.toByteArray()));
        prevHash.setText(bytesToHex(block.getPreviousHash().toByteArray()));
        signature.setText(bytesToHex(block.getSignature().toByteArray()));
        expTransaction.setText(block.getTransaction().toStringUtf8());

        if (peerAlias.equals("me")) {
            ownChainIndicator.setBackgroundColor(ChainColor.getMyColor(context));
        }else{
            ownChainIndicator.setBackgroundColor(ChainColor.getColor(context,bytesToHex(pubKeyByteStr.toByteArray())));
        }
        if (linkPeerAlias.equals("me")) {
            linkChainIndicator.setBackgroundColor(ChainColor.getMyColor(context));
        }else{
            linkChainIndicator.setBackgroundColor(ChainColor.getColor(context,bytesToHex(pubKeyByteStr.toByteArray())));
        }
        return convertView;
    }

    public void setOnClickListener(View view) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                Intent intent = new Intent(context, ChainExplorerActivity.class);
                intent.putExtra("publicKey", hexStringToByteArray(tv.getText().toString()));
                context.startActivity(intent);
            }
        };
        view.setOnClickListener(onClickListener);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
