package nl.tudelft.cs4160.trustchain_android.funds;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

import static nl.tudelft.cs4160.trustchain_android.Util.Util.readableSize;

public class FundsAdapter extends ArrayAdapter<MessageProto.TrustChainBlock> {
    public FundsAdapter(@NonNull Context context) {
        super(context, R.layout.item_transaction);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String transactionString;

        View view;
        if (convertView == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, parent, false);
        else view = convertView;

        MessageProto.TrustChainBlock block = this.getItem(position);

        transactionString = block.getTransaction().toStringUtf8();
        System.out.println("Found " + transactionString);

        long up = 0;
        long down = 0;

        try {
            JSONObject object = new JSONObject(transactionString); // TODO refactor to some kind of factory
            up = object.getLong("up");
            down = object.getLong("down");
        } catch (JSONException e) {
            //Log.e("FundsAdapter", "Skipped incorrect transaction block.", e);
        }

        TextView upAmount = view.findViewById(R.id.funds_item_up_amount);
        upAmount.setText(readableSize(up));


        TextView downAmount = view.findViewById(R.id.funds_item_down_amount);
        downAmount.setText(readableSize(down));

        return view;
    }
}
