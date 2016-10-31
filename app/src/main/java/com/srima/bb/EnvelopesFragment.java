

package com.srima.bb;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

public class EnvelopesFragment extends Fragment
                               implements LoaderCallbacks<Cursor>,
                                          GridView.OnItemClickListener,
                                          MonitorScrollView.OnScrollListener,
                                          DeleteAdapter.Deleter,
                                          TitleFragment {
    GridView mGrid;
    EnvelopesAdapter mEnvelopes;
    DeleteAdapter mDeleteAdapter;
    TextView mTotal;
    View mTotalContainer;
    View mTotalLabel;
    MonitorScrollView mScroll;
    SharedPreferences mPrefs;

    public EnvelopesFragment() {
        setHasOptionsMenu(true);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup cont,
                                       Bundle state) {
        View retVal = inflater.inflate(R.layout.envelopesactivity, cont, false);
        mPrefs = PreferenceManager
                 .getDefaultSharedPreferences(getActivity().getBaseContext());
        mGrid = (GridView) retVal.findViewById(R.id.grid);
        getLoaderManager().initLoader(0, null, this);
        mEnvelopes = new EnvelopesAdapter(getActivity(), null);
        mDeleteAdapter = new DeleteAdapter(getActivity(), this, mEnvelopes, 0);
        mGrid.setAdapter(mDeleteAdapter);
        mGrid.setOnItemClickListener(this);
        mTotalContainer = retVal.findViewById(R.id.totalamount);
        mTotal = (TextView) mTotalContainer.findViewById(R.id.value);
        mTotalLabel = mTotalContainer.findViewById(R.id.name);
        mScroll = (MonitorScrollView) retVal.findViewById(R.id.scroll);
        mScroll.setOnScrollListener(this);
        return retVal;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Fragment frag = Fragment.instantiate(
            getActivity(),
            GraphFragment.class.getName()
        );
        getChildFragmentManager().beginTransaction()
         .replace(R.id.graph, frag)
         .commit();
    }

    @Override public String getTitle() {
        return getActivity().getString(R.string.app_name);
    }

    @Override public void onPause() {
        super.onPause();
        mDeleteAdapter.performDelete();
    }

    @Override public void performDelete(long id) {
        deleteEnvelope((int)id);
    }
    @Override public void onDelete(long id) {
        loadEnvelopesData(mEnvelopes.getCursor());
    }
    @Override public void undoDelete(long id) {
        loadEnvelopesData(mEnvelopes.getCursor());
    }

    private void deleteEnvelope(int id) {
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM envelopes WHERE _id = ?",
                       new String[] {Integer.toString(id)});
            db.execSQL("DELETE FROM log WHERE envelope = ?",
                       new String[] {Integer.toString(id)});
            db.setTransactionSuccessful();
            getActivity().getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @Override public void onScrollChanged(int pos, int oldPos) {
        mTotal.setTranslationY((pos*2)/3);
        mTotalLabel.setTranslationY((pos*2)/3);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SQLiteLoader retVal = new SQLiteLoader(
            getActivity(), new EnvelopesOpenHelper(getActivity()), "envelopes",
            new String[] {
                "name", "cents", "color", "_id"
            },
            null,
            null,
            null,
            null,
            "name"
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        loadEnvelopesData(data);
    }
    private void loadEnvelopesData(Cursor data) {
        data.moveToFirst();
        int l = data.getCount();
        long total = 0;
        boolean hasColor = false;
        for (int i = 0; i != l; ++i) {
            int id = data.getInt(data.getColumnIndexOrThrow("_id"));
            if (id != mDeleteAdapter.getDeletedId()) {
                total += data.getLong(data.getColumnIndexOrThrow("cents"));
                int color = data.getInt(data.getColumnIndexOrThrow("color"));
                if (color != 0) {
                    hasColor = true;
                }
            }
            data.moveToNext();
        }
        mTotal.setText(EditMoney.toColoredMoney(getActivity(), total));
        mEnvelopes.changeCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        mEnvelopes.changeCursor(null);
    }

    @Override public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
        openEnvelope((int)id);
    }

    private void openEnvelope(int id) {
        Bundle args = new Bundle();
        args.putInt("com.srima.bb.envelope", id);
        ((EnvelopesActivity)getActivity()).switchFragment(EnvelopeDetailsFragment.class, "EnvelopeDetailsFragment", args);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.envelopesactivity, menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newEnvelope_menuItem:
                SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                                    .getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("name", "");
                values.put("color", 0);
                long id = db.insert("envelopes", null, values);
                db.close();
                getActivity().getContentResolver().notifyChange(EnvelopesOpenHelper.URI, null);
                openEnvelope((int)id);
                return true;
            case R.id.transfer_menuItem:
                DialogFragment f = TransferFragment.newInstance();
                f.show(getFragmentManager(), "dialog");
                return true;
            case R.id.paycheck_menuItem:
                ((EnvelopesActivity)getActivity()).switchFragment(
                    PaycheckFragment.class,
                    "PaycheckFragment",
                    null
                );
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
