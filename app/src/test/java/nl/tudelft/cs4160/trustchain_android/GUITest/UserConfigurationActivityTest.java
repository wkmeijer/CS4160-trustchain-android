package nl.tudelft.cs4160.trustchain_android.GUITest;

import android.content.Context;
import android.content.Intent;
import android.text.LoginFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import nl.tudelft.cs4160.trustchain_android.BuildConfig;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.main.ConnectionExplanationActivity;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.main.UserConfigurationActivity;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.allOf;


/**
 * Created by Laurens on 12/18/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class UserConfigurationActivityTest {
    private String user = "New User";

    private UserConfigurationActivity userConActivity;

    @Before
    public void setUp() {
        userConActivity = Robolectric.setupActivity(UserConfigurationActivity.class);
    }

    @Test
    public void makeNewUsername(){
        emptyUserNamePreferences();

        //enter the username
        EditText userNameInput = (EditText) userConActivity.findViewById(R.id.username);
        userNameInput.setText(user);

        // press the login button
        Button confirmButton = (Button) userConActivity.findViewById(R.id.confirm_button);
        confirmButton.callOnClick();

        Intent expectedIntent = new Intent(userConActivity, OverviewConnectionsActivity.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());

        // look whether the ID is correctly displayed in the OverviewConnections window.
        OverviewConnectionsActivity ovCoActivity = Robolectric.buildActivity(OverviewConnectionsActivity.class, actual).create().get();
        TextView peerIdView = (TextView) ovCoActivity.findViewById(R.id.peer_id);
        assertEquals(user, peerIdView.getText());
    }

    @Test
    public void usernameAlreadyStored(){
        setUsernameInPref();

        // look whether the ID is correctly displayed in the OverviewConnections window.
        OverviewConnectionsActivity ovCoActivity = Robolectric.setupActivity(OverviewConnectionsActivity.class);
        TextView peerIdView = (TextView) ovCoActivity.findViewById(R.id.peer_id);
        assertEquals(user, peerIdView.getText());
    }

    private void emptyUserNamePreferences(){
        // Check whether it is empty
        // If not, put null in it
        if(UserNameStorage.getUserName(userConActivity) != null) {
            UserNameStorage.setUserName(userConActivity, null);
        }
    }

    private void setUsernameInPref(){
        // Set SharedPreferences data
        UserNameStorage.setUserName(userConActivity, user);
    }
}
