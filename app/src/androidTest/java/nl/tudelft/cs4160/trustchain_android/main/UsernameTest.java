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
public class UsernameTest {

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityTestRule = new ActivityTestRule<>(UserConfigurationActivity.class);

    @Test
    public void usernameTest() {
        ViewInteraction appCompatEditText = onView(
allOf(withId(R.id.username),
childAtPosition(
childAtPosition(
withClassName(is("android.widget.LinearLayout")),
0),
1),
isDisplayed()));
        appCompatEditText.perform(replaceText("ssss"), closeSoftKeyboard());
        
        ViewInteraction appCompatButton = onView(
allOf(withId(R.id.confirm_button), withText("Confirm"),
childAtPosition(
childAtPosition(
withClassName(is("android.support.constraint.ConstraintLayout")),
0),
3),
isDisplayed()));
        appCompatButton.perform(click());
        
        ViewInteraction textView = onView(
allOf(withId(R.id.peer_id), withText("ssss"),
childAtPosition(
childAtPosition(
withId(R.id.overviewButtons),
2),
1),
isDisplayed()));
        textView.check(matches(withText("ssss")));
        
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
