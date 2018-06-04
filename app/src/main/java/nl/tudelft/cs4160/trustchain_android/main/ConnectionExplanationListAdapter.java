package nl.tudelft.cs4160.trustchain_android.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import nl.tudelft.cs4160.trustchain_android.R;

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
            convertView = inflater.inflate(R.layout.item_connection_explanation_list, null, true);
            TextView symbol = convertView.findViewById(R.id.colorSymbol);
            TextView symbolMeaning = convertView.findViewById(R.id.symbolMeaning);
            symbol.setText(symbolList.get(position));
            // when no color specified, use default
            if (colorList[position] != 0) {
                symbol.setTextColor(context.getResources().getColor(colorList[position]));
            }
            symbol.setTextSize(18.f);
            symbolMeaning.setText(colorExplanationText[position]);
            symbolMeaning.setTextSize(18.f);
        }
        return convertView;
    }

}
