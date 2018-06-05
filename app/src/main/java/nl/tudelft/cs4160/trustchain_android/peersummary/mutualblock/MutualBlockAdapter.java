package nl.tudelft.cs4160.trustchain_android.peersummary.mutualblock;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.block.TrustChainBlockHelper;
import nl.tudelft.cs4160.trustchain_android.block.ValidationResult;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainColor;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import nl.tudelft.cs4160.trustchain_android.peersummary.PeerSummaryActivity;
import nl.tudelft.cs4160.trustchain_android.storage.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.util.ByteArrayConverter;
import nl.tudelft.cs4160.trustchain_android.util.Util;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MutualBlockAdapter extends RecyclerView.Adapter<MutualBlockAdapter.ViewHolder> {

    private static final int REQUEST_STORAGE_PERMISSIONS = 1;
    private ArrayList<MutualBlockItem> mutualBlocks;
    private Context context;
    private DualSecret keyPair;
    private TrustChainDBHelper dbHelper;

    /**
     * Constructor.
     *
     * @param mutualBlocks the list of blocks that both user have in common.
     */
    public MutualBlockAdapter(Context context, ArrayList<MutualBlockItem> mutualBlocks) {
        this.mutualBlocks = mutualBlocks;
        this.context = context;
        this.keyPair = Key.loadKeys(context);
        this.dbHelper = new TrustChainDBHelper(context);
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
        MutualBlockItem mutualBlock = mutualBlocks.get(position);
        if (mutualBlock != null) {
            Button signButton = viewHolder.signButton;
            TextView blockStatTv = viewHolder.blockStatTextView;
            setOnClickListenerSignBlock(viewHolder, position);

            //if the linked public key is equal to ours, it means this block is addressed to us.
            //So if we don't have a linked block, we know we still need to sign it.
            if(Arrays.equals(mutualBlock.getBlock().getLinkPublicKey().toByteArray(), keyPair.getPublicKeyPair().toBytes()) &&
                    dbHelper.getLinkedBlock(mutualBlock.getBlock()) == null) {
                if(mutualBlock.getValidationResult() == ValidationResult.INVALID) {
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

            viewHolder.userNameTextView.setText(UserNameStorage.getUserName(context));
            viewHolder.peerNameTextView.setText(mutualBlock.getPeerName());

            if (mutualBlock.getSeqNum() == 0) {
                viewHolder.seqNumTextView.setText(context.getResources().getString(R.string.seq_unknown));
            } else {
                viewHolder.seqNumTextView.setText(context.getResources().getString(R.string.sequenceNum,
                        mutualBlock.getSeqNum()));
            }

            if (mutualBlock.getLinkSeqNum() == 0) {
                viewHolder.linkSeqNumTextView.setText(context.getResources().getString(R.string.seq_unknown));
            } else {
                signButton.setVisibility(View.GONE);
                viewHolder.linkSeqNumTextView.setText(context.getResources().getString(R.string.sequenceNum,
                        mutualBlock.getSeqNum()));
            }

            TextView transTv = viewHolder.transactionTextView;

            if (TrustChainBlockHelper.containsBinaryFile(mutualBlock)) {
                // If the block contains a file show the 'click to open' text

                transTv.setText(mutualBlock.getTransactionFormat() + " file\n" +
                                context.getString(R.string.click_to_open));
                setOpenFileClickListener(transTv, mutualBlock.getBlock());
            } else {
                transTv.setText(new String(mutualBlock.getTransaction(), UTF_8));
            }

            String myPublicKeyString = null;
            if (keyPair != null) {
                myPublicKeyString = ByteArrayConverter.bytesToHexString(keyPair.getPublicKey().toBytes());
            }
            String linkedKey = ByteArrayConverter.byteStringToString(mutualBlock.getBlock().getLinkPublicKey());
            String normalKey = ByteArrayConverter.byteStringToString(mutualBlock.getBlock().getPublicKey());

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
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermissions();
                    return;
                }

                File file = new File(android.os.Environment.getExternalStorageDirectory() + "/TrustChain/" + Util.byteArrayToHexString(block.getSignature().toByteArray()) + "." + block.getTransaction().getFormat());
                if (file.exists()) file.delete();

                byte[] bytes = block.getTransaction().getUnformatted().toByteArray();
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);

                try {
                    if (!Util.copyFile(is, file)) {
                        Snackbar.make(view, "Copying file to filesystem failed.", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(view, "Copying file to filesystem failed.", Snackbar.LENGTH_LONG).show();
                    return;
                }

                String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mimetype == null) mimetype = "text/plain"; // If no mime type is found, try to open as plain text

                Intent i = new Intent();
                i.setDataAndType(Uri.fromFile(file), mimetype);
                context.startActivity(i);
            }
        });
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions((Activity)context, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_STORAGE_PERMISSIONS);
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
                ((PeerSummaryActivity) context).requestSignPermission(mutualBlocks.get(position).getBlock());
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
