package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.main.BootstrapActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.closeSoftKeyboard;
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
    public void gotoOverviewConnections() throws InterruptedException{
//        mActivityRule.launchActivity(new Intent());
//        // Change to invalid IP
//        onView(withId(R.id.bootstrap_IP)).perform(replaceText("dwdw534"));
//        closeSoftKeyboard();
//        Thread.sleep(2500);
//        onView(withId(R.id.change_bootstrap)).perform(click());
//        onView(withId(R.id.bootstrap_IP)).check(matches(isDisplayed()));
//
//        // Change to valid IP
//        onView(withId(R.id.bootstrap_IP)).perform(replaceText("145.94.155.32"));
//        // after this button is pressed the activity is shut down (Because no OverviewConnection is active)
//        onView(withId(R.id.change_bootstrap)).perform(click());
//        closeSoftKeyboard();
    }

}
