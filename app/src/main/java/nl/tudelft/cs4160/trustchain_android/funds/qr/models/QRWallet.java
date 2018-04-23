package nl.tudelft.cs4160.trustchain_android.funds.qr.models;

import com.squareup.moshi.Json;

public class QRWallet {
    @Json(name = "private_key")
    public String privateKeyBase64;

    public QRTransaction transaction;

    public QRBlock block;
}
