package nl.tudelft.cs4160.trustchain_android.inbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.Peer;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationListener;
import nl.tudelft.cs4160.trustchain_android.connection.CommunicationSingleton;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public class InboxActivity extends AppCompatActivity implements CommunicationListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<InboxItem> inboxItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mAdapter = new InboxAdapter(inboxItems);
        mRecyclerView.setAdapter(mAdapter);
        CommunicationSingleton.initContextAndListener(getApplicationContext(), this);
    }

    private void getInboxItems() {
        inboxItems = new ArrayList<>();
        inboxItems = InboxItemStorage.getInboxItems(this);
        mAdapter = new InboxAdapter(inboxItems);
        mRecyclerView.swapAdapter(mAdapter, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getInboxItems();
        CommunicationSingleton.setCommunicationListener(this);
    }

    @Override
    public void updateLog(String msg) {
        //getInboxItems();
    }

    @Override
    public void requestPermission(MessageProto.TrustChainBlock block, Peer peer) {

    }

    @Override
    public void connectionSuccessful(byte[] publicKey) {

    }
}
