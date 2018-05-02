package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;

import nl.tudelft.cs4160.trustchain_android.main.ChangeBootstrapActivity;


/**
 * Created by Laurens on 12/18/2017.
 */

public class BootstrapActivityTest {

    @Rule
    public ActivityTestRule<ChangeBootstrapActivity> mActivityRule = new ActivityTestRule<>(
            ChangeBootstrapActivity.class);
//
//    @Test
//    public void gotoOverviewConnections() throws InterruptedException{
//        mActivityRule.launchActivity(new Intent());
//        Thread.sleep(1000);
//        // Change to invalid IP
//        onView(withId(R.id.bootstrap_IP)).perform(replaceText("dwdw534"));
//        closeSoftKeyboard();
//        Thread.sleep(1000);
//        onView(withId(R.id.change_bootstrap)).perform(click());
//        onView(withId(R.id.bootstrap_IP)).check(matches(isDisplayed()));
//
//        // Change to valid IP
//        Thread.sleep(1000);
//        onView(withId(R.id.bootstrap_IP)).perform(replaceText("145.94.155.32"));
//        // after this button is pressed the activity is shut down (Because no OverviewConnection is active)
//        onView(withId(R.id.change_bootstrap)).perform(click());
//        closeSoftKeyboard();
//    }

}
