
package com.srima.bb;

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class AllTransactionsFragment extends Fragment
                                     implements LoaderCallbacks<Cursor>,
                                                AdapterView.OnItemClickListener,
                                                TitleFragment {
    LogAdapter mAdapter;
    ListView mListView;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup cont,
                                       Bundle state) {
        View retVal = inflater.inflate(R.layout.alltransactionsactivity, cont, false);
        mListView = (ListView) retVal.findViewById(android.R.id.list);
        mAdapter = new LogAdapter(getActivity(), null);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);
        return retVal;
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String time = Long.toString(System.currentTimeMillis()-5184000000l);
        SQLiteLoader retVal = new SQLiteLoader(
            getActivity(),
            new EnvelopesOpenHelper(getActivity()),
            "SELECT e.name AS envelope, e.color AS color, l.description AS description, l.cents AS cents, l.time AS time, l._id AS _id, e._id AS envelope_id FROM log AS l LEFT JOIN envelopes AS e ON (e._id = l.envelope) ORDER BY l.time * -1"
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        // Do nothing.
    }

    @Override public void onItemClick(AdapterView a, View v, int pos, long id) {
        Cursor csr = mAdapter.getCursor();
        csr.moveToPosition(pos);
        int envelopeId = csr.getInt(csr.getColumnIndexOrThrow("envelope_id"));

        Bundle args = new Bundle();
        args.putInt("com.srima.bb.envelope", envelopeId);
        args.putInt("com.srima.bb.transaction", (int)id);
        ((EnvelopesActivity)getActivity()).switchFragment(
            EnvelopeDetailsFragment.class,
            "EnvelopeDetailsFragment",
            args
        );
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.allTransactions_name);
    }
};

