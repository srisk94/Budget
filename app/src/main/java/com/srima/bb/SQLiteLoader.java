

package com.srima.bb;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.*;
import android.net.Uri;

public class SQLiteLoader extends AsyncTaskLoader<Cursor> {
    Context mCntx;
    SQLiteDatabase mDatabase;
    String mTable;
    String[] mColumns;
    String mSelection;
    String[] mSelectionArgs;
    String mGroupBy;
    String mHaving;
    String mOrderBy;
    String mLimit;
    Cursor mResults;
    String mRawQuery;
    ContentObserver mObserver;
    Uri mNotificationUri;

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper, String table, 
                        String[] columns, String selection, String[] args, 
                        String groupBy, String having, String orderBy,
                        String limit) {
        super(cntx);
        mCntx = cntx;
        mDatabase = helper.getReadableDatabase();
        mTable = table;
        mColumns = columns;
        mSelection = selection;
        mSelectionArgs = args;
        mGroupBy = groupBy;
        mHaving = having;
        mOrderBy = orderBy;
        mLimit = limit;
        mObserver = new ForceLoadContentObserver();
    }

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper,
                        String rawQuery) {
        super(cntx);
        mCntx = cntx;
        mDatabase = helper.getReadableDatabase();
        mRawQuery = rawQuery;
        mObserver = new ForceLoadContentObserver();
    }

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper, String table, 
                        String[] columns, String selection, String[] args, 
                        String groupBy, String having, String orderBy) {
        this(cntx, helper, table, columns, selection, args, groupBy, having, orderBy, null);
    }

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper, String table, 
                        String[] columns, String selection, String[] args) {
        this(cntx, helper, table, columns, selection, args, null, null, null, null);
    }

    public SQLiteLoader(Context cntx, SQLiteOpenHelper helper, String table, 
                        String[] columns) {
        this(cntx, helper, table, columns, null, null, null, null, null, null);
    }

    public void setNotificationUri(Uri uri) {
        mNotificationUri = uri;
        if (mResults != null) {
            mResults.setNotificationUri(mCntx.getContentResolver(), uri);
        }
    }

    @Override public synchronized Cursor loadInBackground() {
        if (mResults != null) {
            mResults.unregisterContentObserver(mObserver);
        }
        mResults = mRawQuery == null
                   ? mDatabase.query(
                       mTable, mColumns, mSelection, mSelectionArgs,
                       mGroupBy, mHaving, mOrderBy, mLimit
                   )
                   : mDatabase.rawQuery(
                       mRawQuery, null
                   );
        mResults.registerContentObserver(mObserver);
        if (mNotificationUri != null) {
            mResults.setNotificationUri(mCntx.getContentResolver(), mNotificationUri);
        }
        return mResults;
    }

    @Override public synchronized void onStartLoading() {
        forceLoad();
    }

    @Override public synchronized void abandon() {
        super.abandon();
        mResults.unregisterContentObserver(mObserver);
        mResults = null;
        mDatabase.close();
    }
}

