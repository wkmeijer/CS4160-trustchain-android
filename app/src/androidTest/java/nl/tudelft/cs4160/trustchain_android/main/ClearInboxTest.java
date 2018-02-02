package nl.tudelft.cs4160.trustchain_android.main;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import nl.tudelft.cs4160.trustchain_android.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ClearInboxTest {

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityTestRule = new ActivityTestRule<>(UserConfigurationActivity.class);

    @Test
    public void clearInboxTest() {
        ViewInteraction appCompatEditText = onView(
allOf(withId(R.id.username),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
0),
1),
isDisplayed()));
        appCompatEditText.perform(replaceText("sdgs"), closeSoftKeyboard());
        
        ViewInteraction appCompatButton = onView(
allOf(withId(R.id.confirm_button), withText("Confirm"),
childAtPosition(
childAtPosition(
withClassName(is("android.support.constraint.ConstraintLayout")),
0),
3),
isDisplayed()));
        appCompatButton.perform(click());
        
        ViewInteraction appCompatButton2 = onView(
allOf(withId(R.id.open_inbox_item), withText("Open Inbox"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
3),
0),
isDisplayed()));
        appCompatButton2.perform(click());
        
        ViewInteraction appCompatButton3 = onView(
allOf(withId(R.id.userButton), withText("+ Find new users"),
childAtPosition(
allOf(withId(R.id.wrapperLinearLayout),
childAtPosition(
withId(R.id.my_recycler_view),
0)),
0),
isDisplayed()));
        appCompatButton3.perform(click());
        
        ViewInteraction tableLayout = onView(
allOf(withId(R.id.tableLayoutConnection),
childAtPosition(
withParent(withId(R.id.incoming_peer_connection_list_view)),
1),
isDisplayed()));
        tableLayout.perform(click());
        
        ViewInteraction appCompatButton4 = onView(
allOf(withId(R.id.open_inbox_item), withText("Open Inbox"),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
3),
0),
isDisplayed()));
        appCompatButton4.perform(click());
        
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        
        ViewInteraction appCompatTextView = onView(
allOf(withId(R.id.title), withText("Clear Entire Inbox"),
childAtPosition(
childAtPosition(
withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
0),
0),
isDisplayed()));
        appCompatTextView.perform(click());
        
        }

        private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup)parent).getChildAt(position));
            }
        };
    }
    }
