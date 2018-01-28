package nl.tudelft.cs4160.trustchain_android.GUITest;

import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.main.ConnectionExplanationActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


/**
 * Created by clintoncao on 25-1-18.
 */

@RunWith(RobolectricTestRunner.class)
public class ConnectionExplanationActivityTest {

    private ConnectionExplanationActivity conExActivity;

    @Before
    public void setUp() {
        conExActivity = Robolectric.setupActivity(ConnectionExplanationActivity.class);
    }

    @Test
    public void testTextViewIsCreated() {
        TextView headerTv = (TextView) conExActivity.findViewById(R.id.connectionInfoHeaderText);
        assertNotNull(headerTv);
    }

    @Test
    public void testListViewIsCreated() {
        ListView infoLv = (ListView) conExActivity.findViewById(R.id.connectionColorExplanationList);
        assertNotNull(infoLv);
    }

    @Test
    public void testRightAmountOfItemsAreInList() {
        ListView infoLv = (ListView) conExActivity.findViewById(R.id.connectionColorExplanationList);
        assertEquals(5, infoLv.getCount());
    }   
}
