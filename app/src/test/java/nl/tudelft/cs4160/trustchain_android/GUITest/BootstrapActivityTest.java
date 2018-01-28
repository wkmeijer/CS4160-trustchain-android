package nl.tudelft.cs4160.trustchain_android.GUITest;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.maven.model.Contributor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import nl.tudelft.cs4160.trustchain_android.BuildConfig;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.main.BootstrapActivity;
import nl.tudelft.cs4160.trustchain_android.main.ConnectionExplanationActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


/**
 * Created by clintoncao on 25-1-18.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class BootstrapActivityTest {

    private BootstrapActivity bootstrapAct;

    @Before
    public void setUp() {
        bootstrapAct = Robolectric.setupActivity(BootstrapActivity.class);
    }

    @Test
    public void testEditTextIsCreated() {
        EditText inputField = (EditText) bootstrapAct.findViewById(R.id.bootstrap_IP);
        assertNotNull(inputField);
    }

    @Test
    public void testButtonIsCreated() {
        Button confirmButton = (Button) bootstrapAct.findViewById(R.id.change_bootstrap);
        assertNotNull(confirmButton);
    }
}
