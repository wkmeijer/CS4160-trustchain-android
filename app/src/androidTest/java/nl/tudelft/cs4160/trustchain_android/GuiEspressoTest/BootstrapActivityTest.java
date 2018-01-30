package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.main.BootstrapActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Laurens on 12/18/2017.
 */

public class BootstrapActivityTest {

    @Rule
    public ActivityTestRule<BootstrapActivity> mActivityRule = new ActivityTestRule<>(
            BootstrapActivity.class);

    @Test
    public void gotoOverviewConnections(){
        // Change to invalid IP
        onView(withId(R.id.bootstrap_IP)).perform(replaceText("dwdw534"));
        onView(withId(R.id.change_bootstrap)).perform(click());
        onView(withId(R.id.bootstrap_IP)).check(matches(isDisplayed()));

        // Change to valid IP
        onView(withId(R.id.bootstrap_IP)).perform(replaceText("145.94.155.32"));
        // after this button is pressed the activity is shut down (Because no OverviewConnection is active)
//        onView(withId(R.id.change_bootstrap)).perform(click());
//        onView(withId(R.id.incoming_peer_connection_list_view)).check(matches(isDisplayed()));
    }

    @Test
    public void gotoMyChain(){
        // waiting for the PR
//        // Open the ActionBar
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//        // Click on the menu item
//        onView(withText("My Chain")).perform(click());
//        // Show the chain on the screen.
//        onView(withId(R.id.blocks_list)).check(matches(isDisplayed()));
    }

}
