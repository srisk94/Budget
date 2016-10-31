/*
 * This file is a part of Budget with Envelopes.
 * Copyright 2013 Michael Howell <michael@notriddle.com>
 *
 * Budget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Budget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Budget. If not, see <http://www.gnu.org/licenses/>.
 */

package com.srima.bb;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

public class Util {
    public static Bundle packSparseIntArray(SparseIntArray a) {
        Bundle b = new Bundle();
        for (int i = 0; i != a.size(); ++i) {
            b.putInt(Integer.toString(a.keyAt(i)), a.valueAt(i));
        }
        return b;
    }
    public static SparseIntArray unpackSparseIntArray(Bundle b) {
        SparseIntArray a = new SparseIntArray();
        Set<String> k = b.keySet();
        Iterator<String> i = k.iterator();
        while (i.hasNext()) {
            String key = i.next();
            a.put(Integer.parseInt(key), b.getInt(key));
        }
        return a;
    }
    public static Bundle packSparseLongArray(SparseArray<Long> a) {
        Bundle b = new Bundle();
        for (int i = 0; i != a.size(); ++i) {
            b.putLong(Integer.toString(a.keyAt(i)), a.valueAt(i));
        }
        return b;
    }
    public static SparseArray<Long> unpackSparseLongArray(Bundle b) {
        SparseArray<Long> a = new SparseArray<Long>();
        Set<String> k = b.keySet();
        Iterator<String> i = k.iterator();
        while (i.hasNext()) {
            String key = i.next();
            a.put(Integer.parseInt(key), b.getLong(key));
        }
        return a;
    }
    static public int numberOf(SparseBooleanArray items, boolean value) {
                if (items == null) return 0;
        int retVal = 0;
        for (int i = 0; i != items.size(); ++i) {
            if (items.get(items.keyAt(i)) == value)
                retVal += 1;
        }
        return retVal;
    }
    static public void pump(InputStream src, OutputStream dest) throws IOException {
        byte[] buf = new byte[2048];
        int len;
        while ((len = src.read(buf)) > 0) {
            dest.write(buf, 0, len);
        }
    }
};
