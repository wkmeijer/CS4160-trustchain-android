/*
 * Copyright (c) 2016 Jean Niklas L'orange. All rights reserved.
 *
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file LICENSE at the root of this distribution.
 *
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 *
 * You must not remove this notice, or any other, from this software.
 */
package nl.tudelft.cs4160.trustchain_android.bencode;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A BencodeReader reads Bencoded values from an {@link InputStream}.
 *
 * @since 0.1.0
 */
public final class BencodeReader implements Closeable {
    private PushbackInputStream input;

    /**
     * Creates a new <code>BencodeReader</code> out of <code>input</code>. The
     * <code>BencodeReader</code> will only read the exact value(s) one
     * requests, and will not buffer values.
     *
     * @since 0.1.0
     * @param input the <code>InputStream</code> to read from
     */
    public BencodeReader(InputStream input) {
        this.input = new PushbackInputStream(input, 1);
    }

    /**
     * Closes the underlying <code>InputStream</code>.
     *
     * @exception IOException if an IO exception occurs when closing
     */
    public void close() throws IOException {
        input.close();
    }

    private int forceRead() throws IOException {
        int val = input.read();
        if (val == -1) {
            throw new EOFException();
        }
        return val;
    }

    private int peek() throws IOException {
        int val = input.read();
        if (val == -1) {
            throw new EOFException();
        }
        input.unread(val);
        return val;
    }

    /**
     * Reads a bencoded long from the <code>InputStream</code>.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     * @exception BencodeReadException if the value read is not a properly bencoded long
     */
    public long readLong() throws IOException, BencodeReadException {
        int initial = forceRead();
        if (initial != 'i') {
            throw new BencodeReadException("Bencoded integer must start with 'i', not '%c'",
                                           initial);
        }
        long val = 0;
        boolean negative = false, readDigit = false;
        while (true) {
            int cur = forceRead();
            if (cur == '-' && !negative && !readDigit) {
                negative = true;
            }
            else if ('0' <= cur && cur <= '9') {
                readDigit = true;
                val *= 10;
                val += cur - '0';
            }
            else if (cur == 'e') {
                if (readDigit) {
                    return negative ? -val : val;
                } else {
                    throw new BencodeReadException("Bencoded integer must contain at least one digit");
                }
            }
            else {
                throw new BencodeReadException("Unexpected character '%c' when reading bencoded long",
                                               cur);
            }
        }
    }

    // len is a positive ascii base-10 encoded integer, immediately followed by
    // a colon
    private int readLen() throws IOException, BencodeReadException {
        boolean readDigit = false;
        int val = 0;
        while (true) {
            int cur = forceRead();
            if ('0' <= cur && cur <= '9') {
                readDigit = true;
                val *= 10;
                val += cur - '0';
            }
            else if (cur == ':') {
                if (readDigit) {
                    return val;
                } else {
                    throw new BencodeReadException("Bencode-length must contain at least one digit");
                }
            }
            else {
                throw new BencodeReadException("Unexpected character '%c' when reading bencode-length of string",
                                               cur);
            }
        }
    }

    /**
     * Reads a bencoded <code>String</code> from the <code>InputStream</code>.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     * @exception BencodeReadException if the value read is not a properly bencoded String
     */
    public String readString() throws IOException, BencodeReadException {
        int len = readLen();
        // now read until we have the entire thing
        byte[] bs = new byte[len];
        if (len == 0) { // edge case where last value is an empty string
            return "";
        }
        int off = input.read(bs);
        if (off == -1) {
            throw new EOFException();
        }
        while (off != len) {
            int more = input.read(bs, off, len - off);
            if (more == -1) {
                throw new EOFException();
            }
            off += more;
        }
        return new String(bs, "UTF-8");
    }

    /**
     * Reads a bencoded <code>List</code> from the <code>InputStream</code>. The
     * <code>List</code> may contain lists and maps itself.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     * @exception BencodeReadException if the value read is not a properly bencoded List
     */
    public List<Object> readList() throws IOException, BencodeReadException {
        int initial = forceRead();
        if (initial != 'l') {
            throw new BencodeReadException("Bencoded list must start with 'l', not '%c'",
                                           initial);
        }
        ArrayList<Object> al = new ArrayList<Object>();
        while (peek() != 'e') {
            Object val = read();
            if (val == null) {
                throw new EOFException();
            }
            al.add(val);
        }
        forceRead(); // remove 'e' that we peeked
        return al;
    }

    /**
     * Reads a bencoded <code>Map</code> (dict in the specification) from the
     * <code>InputStream</code>. The <code>Map</code> may contain lists and maps
     * itself.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     * @exception BencodeReadException if the value read is not a properly bencoded Map
     */
    public Map<String, Object> readDict() throws IOException, BencodeReadException {
        int initial = forceRead();
        if (initial != 'd') {
            throw new BencodeReadException("Bencoded dict must start with 'd', not '%c'",
                                           initial);
        }
        HashMap<String, Object> hm = new HashMap<String, Object>();
        while (peek() != 'e') {
            String key = readString();
            Object val = read();
            if (val == null) {
                throw new EOFException();
            }
            hm.put(key, val);
        }
        forceRead(); // read 'e' that we peeked
        return hm;
    }

    /**
     * Reads a bencoded value from the <code>InputStream</code>. If the stream
     * is empty, <code>null</code> is returned instead of an error.
     *
     * @since 0.1.0
     * @exception IOException if an IO exception occurs when reading
     * @exception EOFException if the stream ended unexpectedly
     * @exception BencodeReadException if the value read is not a properly bencoded value
     */
    public Object read() throws IOException, BencodeReadException {
        int t = input.read();
        if (t == -1) {
            return null;
        }
        input.unread(t);
        switch (t) {
        case 'i':
            return readLong();
        case 'l':
            return readList();
        case 'd':
            return readDict();
        default:
            return readString();
        }
    }
}
