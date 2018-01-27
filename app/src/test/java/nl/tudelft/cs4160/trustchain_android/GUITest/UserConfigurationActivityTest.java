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

    @Test
    public void makeNewUsername(){
        // Empty any previously stored usernames
        userConActivity = Robolectric.setupActivity(UserConfigurationActivity.class);
        emptyUserNamePreferences();

        // Enter the username
        EditText userNameInput = (EditText) userConActivity.findViewById(R.id.username);
        userNameInput.setText(user);

        // Press the login button
        Button confirmButton = (Button) userConActivity.findViewById(R.id.confirm_button);
        confirmButton.callOnClick();

        // Check if the overview connection activity is called
        Intent expectedIntent = new Intent(userConActivity, OverviewConnectionsActivity.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @Test
    public void usernameAlreadyStored(){
        // Set a stored username in advance
        userConActivity = Robolectric.setupActivity(UserConfigurationActivity.class);
        setUsernameInPref();
        userConActivity = Robolectric.setupActivity(UserConfigurationActivity.class);

        // The first activity should directly show the overview connections activity since there was already a username stored.
        Intent expectedIntent = new Intent(userConActivity, OverviewConnectionsActivity.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
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
