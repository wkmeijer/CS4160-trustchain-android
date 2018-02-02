package nl.tudelft.cs4160.trustchain_android.main;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ViewChainTest {
    private int gottaRunThisMethod = emptyUserNamePreferences();

    private int emptyUserNamePreferences(){
        // Check whether it is empty
        // If not, put null in it
        if(UserNameStorage.getUserName(getInstrumentation().getTargetContext()) != null) {
            UserNameStorage.setUserName(getInstrumentation().getTargetContext(), null);
        }

        return 1;
    }

    @Rule
    public ActivityTestRule<UserConfigurationActivity> mActivityTestRule = new ActivityTestRule<>(UserConfigurationActivity.class);

    @Test
    public void viewChainTest() throws  InterruptedException {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.username),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("ddfgh"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.confirm_button), withText("Confirm"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.constraint.ConstraintLayout")),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        Thread.sleep(5000);

        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.title), withText("My Chain"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        DataInteraction linearLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.blocks_list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)))
                .atPosition(0);
        linearLayout.perform(click());

        ViewInteraction appCompatTextView2 = onView(
                allOf(withId(R.id.pub_key), withText("308201313081EA06072A8648CE3D02013081DE020101302B06072A8648CE3D010102207FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFED304404202AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA984914A14404207B425ED097B425ED097B425ED097B425ED097B425ED097B4260B5E9C7710C8640441042AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD245A20AE19A1B8A086B4E01EDD2C7748D14C923D4D7E6D7C61B229E9C5A27ECED3D902201000000000000000000000000000000014DEF9DEA2F79CD65812631A5CF5D3ED020108034200042175169D36B4B5C7070EED2BC48F0624D4D002C7DAD31FC087A01CEAFA77837666EDC8904DBE5DF65284EC98340A993F9ED55729236A433D9BE26C0E8A609C18"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.expanded_item),
                                        0),
                                1),
                        isDisplayed()));
        appCompatTextView2.perform(click());

        DataInteraction linearLayout2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.blocks_list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)))
                .atPosition(0);
        linearLayout2.perform(click());

        ViewInteraction linearLayout3 = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.top_item),
                                childAtPosition(
                                        withId(R.id.blocks_list),
                                        0)),
                        0),
                        isDisplayed()));
        linearLayout3.check(matches(isDisplayed()));

        ViewInteraction tableLayout = onView(
                allOf(withId(R.id.expanded_item),
                        childAtPosition(
                                allOf(withId(R.id.top_item),
                                        childAtPosition(
                                                withId(R.id.blocks_list),
                                                0)),
                                1),
                        isDisplayed()));
        tableLayout.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.link_pub_key), withText("00"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.expanded_item),
                                        1),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("00")));

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
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
