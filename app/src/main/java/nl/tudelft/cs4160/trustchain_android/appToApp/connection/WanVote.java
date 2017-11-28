package nl.tudelft.cs4160.trustchain_android.appToApp.connection;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * The WanVote counts address votes and return the majority vote, or when equal the last vote.
 * <p/>
 * Created by jaap on 6/1/16.
 */
public class WanVote {
    private final static int MAX_SIZE = 3;
    LinkedList<InetSocketAddress> votes;
    private InetSocketAddress majorityAddress;

    public WanVote() {
        votes = new LinkedList<>();
        majorityAddress = null;
    }

    public boolean vote(InetSocketAddress address) {
        InetSocketAddress originalAddress = majorityAddress;
        votes.add(address);
        if (votes.size() > MAX_SIZE)
            votes.removeFirst();
        calculateAddress();
        if (!majorityAddress.equals(originalAddress)) {
            return true;
        }
        return false;
    }

    private void calculateAddress() {
        HashMap<InetSocketAddress, Integer> map = new HashMap<>();
        for (InetSocketAddress vote : votes) {
            if (map.containsKey(vote)) {
                map.put(vote, map.get(vote) + 1);
            } else {
                map.put(vote, 1);
            }
        }
        int max = 0;
        majorityAddress = null;
        for (Map.Entry<InetSocketAddress, Integer> entry : map.entrySet()) {
            if (entry.getValue() >= max) {
                max = entry.getValue();
                majorityAddress = entry.getKey();
            }
        }
    }

    public InetSocketAddress getAddress() {
        return majorityAddress;
    }

}
