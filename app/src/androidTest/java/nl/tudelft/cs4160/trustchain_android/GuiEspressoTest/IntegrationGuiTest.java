package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.main.UserConfigurationActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Created by Laurens on 12/18/2017.
 */

public class IntegrationGuiTest {
    String user = "Tester";

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityRule = new ActivityTestRule<>(
            UserConfigurationActivity.class,
            true,
            false);

    @Test
    public void logInAndConnectWithAnUser(){
        emptyUserNamePreferences();
        mActivityRule.launchActivity(new Intent());

        //Set the name of the user.
        onView(withId(R.id.username)).perform(replaceText(user));
        closeSoftKeyboard();
        onView(withId(R.id.confirm_button)).perform(click());

        //Open Inbox
        onView(withId(R.id.open_inbox_item)).perform(click());
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Clear Entire Inbox")).perform(click());
        //Go back to OverviewConnectionsActivity.
        onView(withId(R.id.userButton)).perform(click());

    }

    private void emptyUserNamePreferences(){
        // Check whether it is empty
        // If not, put null in it
        if(UserNameStorage.getUserName(getInstrumentation().getTargetContext()) != null) {
            UserNameStorage.setUserName(getInstrumentation().getTargetContext(), null);
        }
    }
}
