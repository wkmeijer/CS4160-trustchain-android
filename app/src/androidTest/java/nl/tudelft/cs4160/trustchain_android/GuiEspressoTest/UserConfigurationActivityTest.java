package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.rule.ActivityTestRule;
import android.text.LoginFilter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.main.UserConfigurationActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;


/**
 * Created by Laurens on 12/18/2017.
 */

public class UserConfigurationActivityTest {
    private String user = "New User";

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityRule = new ActivityTestRule<>(
            UserConfigurationActivity.class,
            true,
            false);

    @Test
    public void makeNewUsername(){
        emptyUserNamePreferences();
        mActivityRule.launchActivity(new Intent());

        //enter the username
        onView(withId(R.id.username)).perform(replaceText(user));
        // press the login button
        onView(withId(R.id.confirm_button)).perform(click());
        // look whether the ID is correctly displayed in the OverviewConnections window.
        onView(allOf(withId(R.id.peer_id), withText(user))).check(matches(isDisplayed()));
    }

    @Test
    public void usernameAlreadyStored(){
        setUsernameInPref();
        mActivityRule.launchActivity(new Intent());

        // look whether the ID is correctly displayed in the OverviewConnections window.
        onView(allOf(withId(R.id.peer_id), withText(user))).check(matches(isDisplayed()));
    }

    private void emptyUserNamePreferences(){
        // Check whether it is empty
        // If not, put null in it
        if(UserNameStorage.getUserName(getInstrumentation().getTargetContext()) != null) {
            UserNameStorage.setUserName(getInstrumentation().getTargetContext(), null);
        }
    }

    private void setUsernameInPref(){
        // Set SharedPreferences data
        UserNameStorage.setUserName(getInstrumentation().getTargetContext(), user);
    }
}
