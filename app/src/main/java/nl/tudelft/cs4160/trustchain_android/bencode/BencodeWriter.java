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
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

/**
 * A BencodeWriter writes Bencoded values into an {@link OutputStream}.
 *
 * @since 0.1.0
 */
public final class BencodeWriter implements Closeable, Flushable {
    private OutputStream out;

    /**
     * Creates a new <code>BencodeWriter</code> out of <code>out</code>.
     *
     * @since 0.1.0
     * @param out the <code>OutputStream</code> to write to
     */
    public BencodeWriter(OutputStream out) {
        this.out = out;
    }

    /**
     * Closes the underlying <code>OutputStream</code>.
     *
     * @exception IOException if an IO exception occurs when closing
     */
    public void close() throws IOException {
        out.close();
    }

    /**
     * Flushes the underlying <code>OutputStream</code>.
     *
     * @exception IOException if an IO exception occurs when flushing
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Writes a bencoded string to the <code>OutputStream</code>.
     *
     * @exception NullPointerException if <code>s</code> is <code>null</code>
     * @exception IOException if an IO exception occurs when writing
     */
    public void writeString(String s) throws IOException {
        byte[] bs = s.getBytes("UTF-8");
        byte[] bsLen = new Integer(bs.length).toString().getBytes("UTF-8");
        out.write(bsLen);
        out.write(':');
        out.write(bs);
    }

    /**
     * Writes a bencoded long to the <code>OutputStream</code>.
     *
     * @exception IOException if an IO exception occurs when writing
     */
    public void writeLong(long l) throws IOException {
        byte[] bs = new Long(l).toString().getBytes("UTF-8");
        out.write('i');
        out.write(bs);
        out.write('e');
    }

    /**
     * Writes a bencoded list to the <code>OutputStream</code>. The values in
     * the list must themselves be bencodeable.
     *
     * @exception IllegalArgumentException if an underlying value is not bencodable
     * @exception NullPointerException if <code>list</code> is <code>null</code>
     * @exception IOException if an IO exception occurs when writing
     */
    public void writeList(List<Object> list) throws IOException {
        out.write('l');
        for (Object elem : list) {
            write(elem);
        }
        out.write('e');
    }

    /**
     * Writes a bencoded map to the <code>OutputStream</code>. The values in
     * <code>map</code> must be bencodeable.
     *
     * @exception IllegalArgumentException if an underlying value is not bencodable
     * @exception NullPointerException if <code>map</code> is <code>null</code>,
     * or if any of the underlying key/values are null
     * @exception IOException if an IO exception occurs when writing
     */
    public void writeDict(Map<String, Object> map) throws IOException {
        out.write('d');
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            writeString(entry.getKey());
            write(entry.getValue());
        }
        out.write('e');
    }



    /**
     * Writes a bencoded value to the <code>OutputStream</code>. The value must
     * be bencodable.
     *
     * @exception IllegalArgumentException if <code>o</code> is not bencodable
     * @exception NullPointerException if <code>o</code> or any underlying
     * values in <code>o</code> are null
     * @exception IOException if an IO exception occurs when writing
     */
    public void write(Object o) throws IOException {
        if (o instanceof Long) {
            writeLong((Long) o);
        }
        else if (o instanceof Integer) {
            writeLong(((Integer) o).longValue());
        }
        else if (o instanceof String) {
            writeString((String) o);
        }
        // Do not support smaller types, at least not for now
        else if (o instanceof List) {
            writeList((List<Object>) o);
        }
        else if (o instanceof Map) {
            writeDict((Map<String,Object>) o);
        }
        else {
            String msg = String.format("Value must either be integer, string, list or map, was %s",
                                       o.getClass().getName());
            throw new IllegalArgumentException(msg);
        }
    }
}
