package nl.tudelft.cs4160.trustchain_android.storage.sharedpreferences;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import nl.tudelft.cs4160.trustchain_android.crypto.PublicKeyPair;
import nl.tudelft.cs4160.trustchain_android.inbox.InboxItem;

public class InboxItemStorage {
    //Inbox item key to store the objects
    final static String INBOX_ITEM_KEY = "INBOX_ITEM_KEY:";

    /**
     * Get all inbox items that are stored.
     * @param context
     * @return
     */
    public static ArrayList<InboxItem> getInboxItems(Context context) {
        InboxItem[] array = new InboxItem[0];
        try {
            array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (array != null) {
            return new ArrayList<>(Arrays.asList(array));
        }
        return new ArrayList<>();
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
        try {
            InboxItem[] array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
            if (array == null) {
                InboxItem[] inboxItems = new InboxItem[1];
                inboxItems[0] = inboxItem;
                SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, inboxItems);
            } else {
                InboxItem[] inboxItems = new InboxItem[array.length + 1];
                for (int i = 0; i < array.length; i++) {
                    inboxItems[i] = array[i];
                    if (array[i].getPeer().getPublicKeyPair() != null && Arrays.equals(array[i].getPeer().getPublicKeyPair().toBytes(), inboxItem.getPeer().getPublicKeyPair().toBytes())) {
                        return;
                    }
                }
                inboxItems[array.length] = inboxItem;
                SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, inboxItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mark blocks as read, so remove all block references for the given inbox item.
     * @param context
     * @param inboxItem
     */
    public static void markHalfBlockAsRead(Context context, InboxItem inboxItem) {
        try {
            InboxItem[] array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
            if (array == null) {
                return;
            } else {
                for (int i = 0; i < array.length; i++) {
                    if (array[i].getPeer().getPublicKeyPair().equals(inboxItem.getPeer().getPublicKeyPair())) {
                        InboxItem item = array[i];
                        item.setHalfBlocks(new ArrayList<>());
                        array[i] = item;
                        SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, array);
                        return;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add the link of a half block to the inbox item it concerns.
     * @param context
     * @param pubKeyPair
     * @param halfBlockSequenceNumber the sequence number of the block that is added.
     */
    public static void addHalfBlock(Context context, PublicKeyPair pubKeyPair, int halfBlockSequenceNumber) {
        try {
            InboxItem[] array = SharedPreferencesStorage.readSharedPreferences(context, INBOX_ITEM_KEY, InboxItem[].class);
            if (array == null) {
                return;
            } else {
                for (int i = 0; i < array.length; i++) {
                    PublicKeyPair p = pubKeyPair;
                    PublicKeyPair p2 = array[i].getPeer().getPublicKeyPair();
                    if (array[i].getPeer().getPublicKeyPair().equals(pubKeyPair)) {
                        InboxItem item = array[i];
                        item.addHalfBlocks(halfBlockSequenceNumber);
                        array[i] = item;
                        SharedPreferencesStorage.writeSharedPreferences(context, INBOX_ITEM_KEY, array);
                        return;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
