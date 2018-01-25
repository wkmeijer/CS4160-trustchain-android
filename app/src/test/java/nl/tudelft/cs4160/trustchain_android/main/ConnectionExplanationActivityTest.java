package nl.tudelft.cs4160.trustchain_android.main;

import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import nl.tudelft.cs4160.trustchain_android.BuildConfig;
import nl.tudelft.cs4160.trustchain_android.R;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


/**
 * Created by clintoncao on 25-1-18.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class ConnectionExplanationActivityTest {

    private ConnectionExplanationActivity conExActivity;

    @Before
    public void setUp() {
        conExActivity = Robolectric.setupActivity(ConnectionExplanationActivity.class);
    }

    @Test
    public void test() {
        TextView headerTv = (TextView) conExActivity.findViewById(R.id.connectionInfoHeaderText);
        ListView infoLv = (ListView) conExActivity.findViewById(R.id.connectionColorExplanationList);
        assertNotNull(headerTv);
        assertNotNull(infoLv);
        assertEquals(5, infoLv.getCount());
    }
}
