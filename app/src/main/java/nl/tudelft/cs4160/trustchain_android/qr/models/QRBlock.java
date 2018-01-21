package nl.tudelft.cs4160.trustchain_android.qr.models;

import com.squareup.moshi.Json;

public class QRBlock {
    @Json(name = "block_hash")
    public String blockHashBase64;

    @Json(name = "sequence_number")
    public int sequenceNumber;
}
