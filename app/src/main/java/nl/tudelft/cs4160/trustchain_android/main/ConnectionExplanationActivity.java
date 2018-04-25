package nl.tudelft.cs4160.trustchain_android.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.R;

public class ConnectionExplanationActivity extends AppCompatActivity {

    private ArrayList<String> symbolList;
    private String[] explanationText = {"Connected with peer", "Connecting with peer", "Cannot connect with peer", "Received a packet from peer", "Sent a packet to peer"};
    private int[] colorList = {R.color.colorStatusConnected, R.color.colorStatusConnecting, R.color.colorStatusCantConnect, R.color.colorReceived, R.color.colorSent};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSymbolList();
        setContentView(R.layout.activity_connection_explanation);
        createConnectionExplanationList();
    }


    /**
     * Create the items that provides the explanation of the colors.
     */
    private void createConnectionExplanationList() {
        TextView connectionInfoHeaderText = findViewById(R.id.connectionInfoHeaderText);
        connectionInfoHeaderText.setTextSize(18.f);
        ListView connectionExplanationListView = findViewById(R.id.connectionColorExplanationList);
        ConnectionExplanationListAdapter connectionExplanationListAdapter =
                new ConnectionExplanationListAdapter
                        (
                            getApplicationContext(),
                            R.layout.item_connection_explanation_list,
                            symbolList,
                            explanationText,
                            colorList
                        );

        connectionExplanationListView.setAdapter(connectionExplanationListAdapter);
    }

    /**
     * Create the list of symbols for the list view.
     */
    private void createSymbolList() {
        symbolList = new ArrayList<String>();
        for (int i = 0; i < 3; i++) {
            String symbol = this.getString(R.string.circle_symbol);
            symbolList.add(symbol);
        }

        for (int i = 0; i < 2; i++) {
            String symbol = this.getString(R.string.indicator_symbol);
            symbolList.add(symbol);
        }
    }
}
