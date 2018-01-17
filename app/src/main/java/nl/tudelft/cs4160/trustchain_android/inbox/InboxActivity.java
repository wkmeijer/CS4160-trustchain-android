package nl.tudelft.cs4160.trustchain_android.inbox;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.InboxItemStorage;

public class InboxActivity extends AppCompatActivity  {

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
    }

    /**
     * Update the inbox counters. A new adapter is created based on the new state of the inboxes,
     * which is then set in the recyclerview. This has to run on the main UI thread because
     * only the original thread that created a view hierarchy can touch its views.
     */
    synchronized private void getInboxItems() {
        final Context currContext = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inboxItems = new ArrayList<>();
                inboxItems = InboxItemStorage.getInboxItems(currContext);
                mAdapter = new InboxAdapter(inboxItems);
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getInboxItems();
    }
}
