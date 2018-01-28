package nl.tudelft.cs4160.trustchain_android.GUITest;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowIntent;

import java.util.List;
import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.SharedPreferences.UserNameStorage;
import nl.tudelft.cs4160.trustchain_android.main.ConnectionExplanationActivity;
import nl.tudelft.cs4160.trustchain_android.main.OverviewConnectionsActivity;
import nl.tudelft.cs4160.trustchain_android.main.UserConfigurationActivity;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;


/**
 * Created by Laurens on 12/18/2017.
 */
@RunWith(RobolectricTestRunner.class)
public class OverviewConnectionsActivityTest {

    private UserConfigurationActivity userConActivity;
    private OverviewConnectionsActivity overviewConnectionsActivity;

    @Before
    public void initializeUser() {
        userConActivity = Robolectric.setupActivity(UserConfigurationActivity.class);
        // Enter the username
        EditText userNameInput = (EditText) userConActivity.findViewById(R.id.username);
        userNameInput.setText("New User");

        // Press the login button
        Button confirmButton = (Button) userConActivity.findViewById(R.id.confirm_button);
        confirmButton.callOnClick();

        // TODO:
        // fix this
        overviewConnectionsActivity = Robolectric.setupActivity(OverviewConnectionsActivity.class);
    }

    @Test
    public void tableLayoutIsCreated() {
        TableLayout tableLayoutForButtons = (TableLayout) overviewConnectionsActivity.findViewById(R.id.overviewButtons);
        assertNotNull(tableLayoutForButtons);
    }

    @Test
    public void localIpTextViewIsCreated() {
        TextView localIPTV = (TextView) overviewConnectionsActivity.findViewById(R.id.local_ip_address_view);
        assertNotNull(localIPTV);
    }

    @Test
    public void wanVoteTextViewIsCreated() {
        TextView wanVoteTV = (TextView) overviewConnectionsActivity.findViewById(R.id.wanvote);
        assertNotNull(wanVoteTV);
    }

    @Test
    public void peerIDTextViewIsCreated() {
        TextView peerIDTV = (TextView) overviewConnectionsActivity.findViewById(R.id.peer_id);
        assertNotNull(peerIDTV);
    }

    @Test
    public void connectionTypeTextViewIsCreated() {
        TextView connectionTypeTV = (TextView) overviewConnectionsActivity.findViewById(R.id.connection_type);
        assertNotNull(connectionTypeTV);
    }

    @Test
    public void exitButtonIsCreated() {
        Button exitButton = (Button) overviewConnectionsActivity.findViewById(R.id.exit_button);
        assertNotNull(exitButton);
    }

    @Test
    public void listViewsCreated() {
        ListView incomingPeerLV = (ListView) overviewConnectionsActivity.findViewById(R.id.incoming_peer_connection_list_view);
        ListView outgoingPeerLV = (ListView) overviewConnectionsActivity.findViewById(R.id.outgoing_peer_connection_list_view);
        assertNotNull(incomingPeerLV);
        assertNotNull(outgoingPeerLV);
    }

    @Test
    public void openInboxButtonIsCreated() {
        Button openInboxButton = (Button) overviewConnectionsActivity.findViewById(R.id.open_inbox_item);
        assertNotNull(openInboxButton);
    }
}