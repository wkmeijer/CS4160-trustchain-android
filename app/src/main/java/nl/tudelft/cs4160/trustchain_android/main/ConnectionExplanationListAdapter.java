package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.appToApp.PeerAppToApp;

/**
 * Adapter for creating the items in the color explanation screen.
 */
public class ConnectionExplanationListAdapter extends ArrayAdapter {

    private final Context context;
    private ArrayList<String> symbolList;
    private String[] colorExplanationText;
    private int[] colorList;

    public ConnectionExplanationListAdapter(Context context, int resource, ArrayList<String> symbolList, String[] colorExplanationText, int[] colorList) {
        super(context, resource, colorExplanationText);
        this.symbolList = symbolList;
        this.context = context;
        this.colorExplanationText = colorExplanationText;
        this.colorList = colorList;
    }

    /**
     * Create the view of each item in the list
     * @param position the position
     * @param convertView the view
     * @param parent the parent view
     * @return a view showing the explanation.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.connection_explanation_list_item, null, true);
            TextView symbol = (TextView) convertView.findViewById(R.id.colorSymbol);
            TextView symbolMeaning = (TextView) convertView.findViewById(R.id.symbolMeaning);
            symbol.setText(symbolList.get(position));
            symbol.setTextColor(context.getResources().getColor(colorList[position]));
            symbol.setTextSize(18.f);
            symbolMeaning.setText(colorExplanationText[position]);
            symbolMeaning.setTextSize(18.f);
        }
        return convertView;
    }

}
