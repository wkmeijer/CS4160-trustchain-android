package nl.tudelft.cs4160.trustchain_android.qr.exception;

public abstract class QRWalletImportException extends Exception {
    public QRWalletImportException() {
        super();
    }

    public QRWalletImportException(String message) {
        super(message);
    }

    public QRWalletImportException(Exception cause) {
        super(cause);
    }
}
