package nl.tudelft.cs4160.trustchain_android.GuiEspressoTest;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.chainExplorer.ChainExplorerActivity;

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

public class ChainExplorerActivityTest {

    @Rule
    public ActivityTestRule<ChainExplorerActivity> mActivityRule = new ActivityTestRule<>(
            ChainExplorerActivity.class);

    @Test
    public void gotoInfoAndCheckListIsCreated(){
        mActivityRule.launchActivity(new Intent());
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Info")).perform(click());
        Espresso.pressBack();
        onView(withText("unknown")).perform(click());
        onView(withId(R.id.signature)).check(matches(isDisplayed()));
    }

}
