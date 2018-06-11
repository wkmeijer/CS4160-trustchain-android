package nl.tudelft.cs4160.trustchain_android.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;

public class ConnectionExplanationActivity extends AppCompatActivity {

    private ArrayList<String> symbolList;
    private String[] explanationText;
    private int[] colorList = {R.color.colorStatusConnected, R.color.colorStatusConnecting, R.color.colorStatusCantConnect, R.color.colorReceived, R.color.colorSent, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSymbolList();
        createExplanationTextList();
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

        symbolList.add(getString(R.string.last_received));
        symbolList.add(getString(R.string.last_sent));
    }

    /**
     * Create a list of strings of explanation texts
     */
    private void createExplanationTextList() {
        List<Integer> ids = new ArrayList<>();
        ids.add(R.string.connected);
        ids.add(R.string.connecting);
        ids.add(R.string.cannot_connect);
        ids.add(R.string.received_packet);
        ids.add(R.string.sent_packet);
        ids.add(R.string.time_since_received);
        ids.add(R.string.time_since_sent);

        explanationText = new String[ids.size()];
        for(int i=0; i<ids.size(); i++) {
            explanationText[i] = getString(ids.get(i));
        }
    }
}
