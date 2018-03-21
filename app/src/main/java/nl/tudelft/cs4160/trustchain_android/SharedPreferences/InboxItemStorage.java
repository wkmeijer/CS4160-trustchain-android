package nl.tudelft.cs4160.trustchain_android.SharedPreferences;

import android.content.Context;

import org.libsodium.jni.keys.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;

public class InboxItemStorage {
    //Inbox item key to store the objects
    private final static String INBOX_ITEM_KEY = "INBOX_ITEM_KEY:";

    /**
     * Get all inbox items that are stored.
     * @param context
     * @return
     */
    public static ArrayList<InboxItem> getInboxItems(Context context) {
        InboxItem[] array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
        if (array != null) {
            return new ArrayList<InboxItem>(Arrays.asList(array));
        }
        return new ArrayList<InboxItem>();
    }

    /**
     * Clear the entire storage of inbox items.
     * @param context
     */
    public static void deleteAll(Context context) {
        SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, null);
    }

    /**
     * Add inbox item to the storage.
     * @param context
     * @param inboxItem
     */
    public static void addInboxItem(Context context, InboxItem inboxItem) {
        InboxItem[] array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
        if (array == null) {
            InboxItem[] inboxItems = new InboxItem[1];
            inboxItems[0] = inboxItem;
            SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, inboxItems);
        } else {
            InboxItem[] inboxItems = new InboxItem[array.length + 1];
            for (int i = 0; i < array.length; i++) {
                inboxItems[i] = array[i];
                if (array[i].getPublicKey() != null && array[i].getPublicKey().equals(inboxItem.getPublicKey())) {
                    return;
                }
            }
            inboxItems[array.length] = inboxItem;
            SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, inboxItems);
        }
    }

    /**
     * Mark blocks as read, so remove all block references for the given inbox item.
     * @param context
     * @param inboxItem
     */
    public static void markHalfBlockAsRead(Context context, InboxItem inboxItem) {
        InboxItem[] array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
        if (array == null) {
            return;
        } else {
            for (int i = 0; i < array.length; i++) {
                if (array[i].getPublicKey().equals(inboxItem.getPublicKey())) {
                    InboxItem item = array[i];
                    item.setHalfBlocks(new ArrayList<Integer>());
                    array[i] = item;
                    SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, array);
                    return;
                }
            }
        }
    }

    /**
     * Add the link of a half block to the inbox item it concerns.
     * @param context
     * @param pubKey
     * @param halfBlockSequenceNumber the sequence number of the block that is added.
     */
    public static void addHalfBlock(Context context, PublicKey pubKey, int halfBlockSequenceNumber) {
        InboxItem[] array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
        if (array == null) {
            return;
        } else {
            for (int i = 0; i < array.length; i++) {
                PublicKey p = pubKey;
                PublicKey p2 = array[i].getPublicKey();
                if (array[i].getPublicKey().equals(pubKey)) {
                    InboxItem item = array[i];
                    item.addHalfBlocks(halfBlockSequenceNumber);
                    array[i] = item;
                    SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, array);
                    return;
                }
            }
        }
    }
}
