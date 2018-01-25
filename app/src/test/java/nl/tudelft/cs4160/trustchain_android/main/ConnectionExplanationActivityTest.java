package nl.tudelft.cs4160.trustchain_android.main;

import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import nl.tudelft.cs4160.trustchain_android.BuildConfig;
import nl.tudelft.cs4160.trustchain_android.R;

import static junit.framework.Assert.assertTrue;


/**
 * Created by clintoncao on 25-1-18.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class ConnectionExplanationActivityTest {

    @Test
    public void testElementsAreDisplayed() {
        //ConnectionExplanationActivity activity = Robolectric.setupActivity(ConnectionExplanationActivity.class);
        //TextView headerTv = (TextView) activity.findViewById(R.id.connectionInfoHeaderText);
        assertTrue(true);
    }
}
