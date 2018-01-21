package nl.tudelft.cs4160.trustchain_android.chainExplorer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.Util.DualKey;
import nl.tudelft.cs4160.trustchain_android.Util.Key;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.main.ChainExplorerInfoActivity;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static android.view.Gravity.CENTER;
import static nl.tudelft.cs4160.trustchain_android.Peer.bytesToHex;


public class ChainExplorerActivity extends AppCompatActivity {
    TrustChainDBHelper dbHelper;
    ChainExplorerAdapter adapter;
    ListView blocksList;

    static final String TAG = "ChainExplorerActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain_explorer);
        blocksList = findViewById(R.id.blocks_list);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(GridLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, CENTER));
        progressBar.setIndeterminate(true);
        blocksList.setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = findViewById(android.R.id.content);
        root.addView(progressBar);

        init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chainexplorer_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                Intent chainExplorerInfoActivity = new Intent(this, ChainExplorerInfoActivity.class);
                startActivity(chainExplorerInfoActivity);
                return true;
            default:
                return true;
        }
    }


    private void init() {
        dbHelper = new TrustChainDBHelper(this);
        DualKey kp = Key.loadKeys(getApplicationContext());
        byte[] publicKey;
        if (getIntent().hasExtra("publicKey")) {
            publicKey = getIntent().getByteArrayExtra("publicKey");
        } else {
            publicKey = kp.getPublicKeyPair().toBytes();

        }
        try {
            adapter = new ChainExplorerAdapter(this, dbHelper.getAllBlocks(), kp.getPublicKeyPair().toBytes());
            blocksList.setAdapter(adapter);
            List<MessageProto.TrustChainBlock> blocks = dbHelper.getBlocks(publicKey);
            if(blocks.size() > 0) {
                this.setTitle(bytesToHex(blocks.get(0).getPublicKey().toByteArray()));
                adapter = new ChainExplorerAdapter(this, blocks, kp.getPublicKeyPair().toBytes());
                blocksList.setAdapter(adapter);
            }else{
                // ToDo display empty chain
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        blocksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout expandedItem = view.findViewById(R.id.expanded_item);
                ImageView expandArrow = view.findViewById(R.id.expand_arrow);

                // Expand the item when it is clicked
                if (expandedItem.getVisibility() == View.GONE) {
                    expandedItem.setVisibility(View.VISIBLE);
                    Log.v(TAG, "Item height: " + expandedItem.getHeight());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        expandArrow.setImageDrawable(getDrawable(R.drawable.ic_expand_less_black_24dp));
                    } else {
                        expandArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
                    }
                } else {
                    expandedItem.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        expandArrow.setImageDrawable(getDrawable(R.drawable.ic_expand_more_black_24dp));
                    } else {
                        expandArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp));
                    }
                }
            }
        });
    }

}
