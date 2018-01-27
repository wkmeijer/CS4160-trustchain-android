package nl.tudelft.cs4160.trustchain_android.GUITest;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;

import nl.tudelft.cs4160.trustchain_android.BuildConfig;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.main.UserConfigurationActivity;

import static junit.framework.Assert.assertTrue;


/**
 * Created by Laurens on 12/18/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class OverviewConnectionsActivityTest {

    private UserConfigurationActivity userConActivity;
    private OverviewConnectionsActivity overviewConnectionsActivity;

    @Before
    public void initializeUser() {
        userConActivity = Robolectric.setupActivity(UserConfigurationActivity.class);
        // Enter the username
        EditText userNameInput = (EditText) userConActivity.findViewById(R.id.username);
        userNameInput.setText("New User");

        // Press the login button
        Button confirmButton = (Button) userConActivity.findViewById(R.id.confirm_button);
        confirmButton.callOnClick();

        // TODO:
        // fix this
        //overviewConnectionsActivity = Robolectric.setupActivity(OverviewConnectionsActivity.class);
    }

    @Test
    public void goToInfoActivity() {
        MenuItem menuItem = new RoboMenuItem(R.id.connection_explanation_menu);
        overviewConnectionsActivity.onOptionsItemSelected(menuItem);
        ShadowActivity shadowActivity = Shadows.shadowOf(overviewConnectionsActivity);
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void gotoChainExplorerActivity() {
        MenuItem menuItem = new RoboMenuItem(R.id.chain_menu);
        overviewConnectionsActivity.onOptionsItemSelected(menuItem);
        ShadowActivity shadowActivity = Shadows.shadowOf(overviewConnectionsActivity);
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void goToChangeBootstrapActivity() {
        MenuItem menuItem = new RoboMenuItem(R.id.find_peer);
        overviewConnectionsActivity.onOptionsItemSelected(menuItem);
        ShadowActivity shadowActivity = Shadows.shadowOf(overviewConnectionsActivity);
        assertTrue(shadowActivity.isFinishing());
    }
}
