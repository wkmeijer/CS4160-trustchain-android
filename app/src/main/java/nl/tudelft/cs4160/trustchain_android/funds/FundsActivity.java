package nl.tudelft.cs4160.trustchain_android.funds;


import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.crypto.Key;
import nl.tudelft.cs4160.trustchain_android.crypto.DualSecret;
import nl.tudelft.cs4160.trustchain_android.database.TrustChainDBHelper;
import nl.tudelft.cs4160.trustchain_android.message.MessageProto;
import static nl.tudelft.cs4160.trustchain_android.Util.Util.readableSize;

public class FundsActivity extends AppCompatActivity {

    ListView transactionListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funds);
        TrustChainDBHelper helper = new TrustChainDBHelper(this);

        DualSecret ownKeyPair = Key.loadKeys(this);
        byte[] myPublicKey = ownKeyPair.getPublicKeyPair().toBytes();
        transactionListView = findViewById(R.id.transaction_listview);
        FundsAdapter adapter = new FundsAdapter(this);

        List<MessageProto.TrustChainBlock> blocks =  helper.getBlocks(myPublicKey, false);
        Collections.reverse(blocks);

        adapter.addAll(blocks);
        transactionListView.setAdapter(adapter);

        long total_up = 0;
        long total_down = 0;

        try {

            MessageProto.TrustChainBlock latestBlock = helper.getLatestBlock(myPublicKey);
            String transactionString = latestBlock.getTransaction().toStringUtf8();
            Log.i("FundsActivity", transactionString);
            JSONObject object = new JSONObject(transactionString); // TODO refactor to some kind of factory
            total_up = object.getLong("total_up");
            total_down = object.getLong("total_down");
        } catch (Exception e) {

        }
        long max = Math.max(total_down,total_up);

        float total_up_fraction = (float)total_up / max * 100;
        float total_down_fraction = (float) total_down /max * 100 ;

        ProgressBar upload_bar = (ProgressBar) findViewById(R.id.upload_bar);
        ObjectAnimator animation = ObjectAnimator.ofInt (upload_bar, "progress", 0, (int)total_up_fraction); // see this max value coming back here, we animale towards that value
        animation.setDuration (1000); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();

        ProgressBar download_bar = (ProgressBar) findViewById(R.id.download_bar);
        ObjectAnimator download_bar_animation = ObjectAnimator.ofInt (download_bar, "progress", 0, (int)total_down_fraction); // see this max value coming back here, we animale towards that value
        download_bar_animation.setDuration (1000); //in milliseconds
        download_bar_animation.setInterpolator (new DecelerateInterpolator());
        download_bar_animation.start ();

        TextView upAmount = findViewById(R.id.up_and_down_label);
        upAmount.setText("Up: "+readableSize((long)total_up)
                        +"\nDown: "+readableSize((long)total_down));

        RatingBar reputation_rating = findViewById(R.id.reputation_rating);

        /*
            Random scale used for rating people's willingness to upload
            [0..4]
            0 stars = up/down < 1
            1 star = up/down == 1
            2 stars = up/down == 3
            3 stars = up/down == 5
            4 stars = up/down == 10
         */
        float rating = (float)total_up/total_down;
        float stars = 0;
        if (rating > 10 ) {
            stars = 4;
        } else if ( rating > 5) {
            stars = 3 + ( rating - 5 ) / 5;
        } else if (rating > 3 ) {
            stars = 2 + (rating - 3) / 2;
        } else if ( rating > 1 ) {
            stars = 1 + (rating - 1 ) / 2;
        } else {
            stars = rating ;
        }
        reputation_rating.setRating(stars);
    }
}



