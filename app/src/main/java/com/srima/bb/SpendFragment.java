

package com.srima.bb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.lang.Exception;

public class SpendFragment extends OkFragment
                           implements LoaderCallbacks<Cursor>,
                                      TextWatcher,
                                      AdapterView.OnItemClickListener,
                                      DatePicker.OnDateChangedListener,
                                      CompoundButton.OnCheckedChangeListener {
    public static final boolean EARN = false;
    public static final boolean SPEND = true;

    int mId;
    AutoCompleteTextView mDescription;
    EditMoney mAmount;
    SimpleLogAdapter mLogAdapter;
    EnvelopesOpenHelper mHelper;
    boolean mNegative;
    DatePicker mDelay;
    CheckBox mDelayed;
    CheckBox mRepeat;
    Spinner mFrequency;
    ArrayAdapter<CharSequence> mFrequencyAdapter;

    public static SpendFragment newInstance(int id, boolean negative) {
        SpendFragment retVal = new SpendFragment();

        Bundle args = new Bundle();
        args.putInt("com.srima.bb.envelope", id);
        args.putBoolean("com.srima.bb.negative", negative);
        retVal.setArguments(args);

        return retVal;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);

        mHelper = new EnvelopesOpenHelper(getActivity());
        Bundle args = getArguments();
        mId = args.getInt("com.srima.bb.envelope");
        mNegative = args.getBoolean("com.srima.bb.negative");
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public AlertDialog onCreateDialog(Bundle state) {
        AlertDialog retVal = super.onCreateDialog(state);
        retVal
        .getWindow()
         .setSoftInputMode(WindowManager.LayoutParams
                                         .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return retVal;
    }

    @Override public View onCreateInternalView(LayoutInflater inflater,
                                               ViewGroup cont, Bundle state) {
        View retVal = inflater.inflate(R.layout.spendfragment, cont, false);
        mAmount = (EditMoney) retVal.findViewById(R.id.amount);
        mDescription = (AutoCompleteTextView) retVal.findViewById(R.id.description);
        mDescription.requestFocus();
        mLogAdapter = new SimpleLogAdapter(getActivity(), null);
        mLogAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                String s = mNegative ? "<" : ">";
                constraint = (constraint == null) ? "" : constraint;
                Cursor retVal = mHelper.getReadableDatabase().query(
                    "log",
                    new String[] { "description", "cents", "time", "_id" },
                    "envelope = ? AND cents "+s+" 0 AND UPPER(description) LIKE ?",
                    new String[] {
                        Integer.toString(mId),
                        constraint.toString()
                                   .toUpperCase(Locale.getDefault())+"%"
                    },
                    null, null, "time * -1"
                );
                retVal.setNotificationUri(getActivity().getContentResolver(),
                                          EnvelopesOpenHelper.URI);
                return retVal;
            }
        });
        mDescription.setAdapter(mLogAdapter);
        mDescription.setOnItemClickListener(this);
        mAmount.setOnEditorActionListener(this);
        mAmount.addTextChangedListener(this);

        mRepeat = (CheckBox) retVal.findViewById(R.id.repeat);
        mRepeat.setOnCheckedChangeListener(this);
        mFrequency = (Spinner) retVal.findViewById(R.id.frequency);
        mFrequencyAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
	                 R.array.frequency_array, R.layout.spinner_item);
        mFrequencyAdapter.setDropDownViewResource(R.layout.spinner_item);
        mFrequency.setAdapter(mFrequencyAdapter);

        mDelayed = (CheckBox) retVal.findViewById(R.id.delayed);
        mDelayed.setOnCheckedChangeListener(this);
        mDelay = (DatePicker) retVal.findViewById(R.id.delay);
        GregorianCalendar tomorrow = new GregorianCalendar();
        tomorrow.add(GregorianCalendar.DAY_OF_MONTH, 1);
        mDelay.init(tomorrow.get(tomorrow.YEAR), tomorrow.get(tomorrow.MONTH), tomorrow.get(tomorrow.DAY_OF_MONTH), this);

        if (state == null) {
            Bundle args = getArguments();
            mDescription.setText(args.getString("com.srima.bb.description", ""));
            mAmount.setCents(args.getLong("com.srima.bb.cents", 0));
            mDelayed.setChecked(args.getBoolean("com.srima.bb.delayed", false));
            mRepeat.setChecked(args.getBoolean("com.srima.bb.repeat", false));
            mFrequency.setSelection(mFrequencyAdapter.getPosition(args.getString("com.srima.bb.frequency", "Daily")));
        }
        onCheckedChanged(mDelayed, mDelayed.isChecked());
        onCheckedChanged(mRepeat, mRepeat.isChecked());

        return retVal;
    }

    @Override protected void writeArgs(Bundle args) {
        args.putString("com.srima.bb.description",
                       mDescription.getText().toString());
        args.putLong("com.srima.bb.cents", mAmount.getCents());
        args.putBoolean("com.srima.bb.delayed",
                        mDelayed.isChecked());
        args.putBoolean("com.srima.bb.repeat",
                        mRepeat.isChecked());
        args.putString("com.srima.bb.frequency",
                       ((TextView)mFrequency.getSelectedView()).getText().toString());
    }

    @Override public void onCheckedChanged(CompoundButton b, boolean checked) {
	    if (b.getId() == R.id.repeat){
		    mFrequency.setEnabled(checked);
	    } else {
		    if (getActivity().getResources()
		        .getDimensionPixelOffset(R.dimen.tabletBool) == 0
		        && getShowsDialog() && checked) {
			    changeToActivity();
		    } else {
			    mDelay.setVisibility(checked ? View.VISIBLE : View.GONE);
		    }
	    }
    }

    @Override public void onDateChanged(DatePicker view, int year, int month,
                                        int day) {
        // Do nothing.
    }

    @Override public void onItemClick(AdapterView a, View v, int pos, long id) {
        Cursor c = mLogAdapter.getCursor();
        int oldPos = c.getPosition();
        c.moveToPosition(pos);
        mDescription.setText(c.getString(
            c.getColumnIndexOrThrow("description")
        ));
        if (mAmount.getCents() == 0) {
            mAmount.setCents((mNegative?-1:1) * c.getLong(c.getColumnIndexOrThrow("cents")));
        }
        c.moveToPosition(oldPos);
    }

    @Override public boolean isOk() {
        return mAmount != null && mAmount.getCents() != 0;
    }

    @Override public void beforeTextChanged(CharSequence s, int start,
                                            int count, int after) {
        // Do nothing.
    }
    @Override public void onTextChanged(CharSequence s, int start,
                                        int before, int count) {
        // Do nothing.
    }
    @Override public void afterTextChanged(Editable s) {
        refreshOkButton();
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String s = mNegative ? "<" : ">";
        SQLiteLoader retVal = new SQLiteLoader(
            getActivity(),
            mHelper,
            "log",
            new String[] { "description", "cents", "time", "_id" },
            "envelope = ? AND cents "+s+" 0",
            new String[] {Integer.toString(mId)},
            null,
            null,
            "time * -1"
        );
        retVal.setNotificationUri(EnvelopesOpenHelper.URI);
        return retVal;
    }

    @Override public void onLoadFinished(Loader<Cursor> ldr, Cursor data) {
        mLogAdapter.changeCursor(data);
    }

    @Override public void onLoaderReset(Loader<Cursor> ldr) {
        //dismiss();
    }

    @Override public String getTitle() {
        return getActivity().getString(mNegative?R.string.spend_name:R.string.earn_name);
    }

    @Override public void ok() {
	    String frequency = mRepeat.isChecked() ? ((TextView)mFrequency.getSelectedView()).getText().toString() : null;
        if (mDelayed.isChecked()) {
            long time = new GregorianCalendar(
                mDelay.getYear(),
                mDelay.getMonth(),
                mDelay.getDayOfMonth()
            ).getTimeInMillis();
            EnvelopesOpenHelper.depositeDelayed(
                getActivity(), mId,
                (mNegative?-1:1)*mAmount.getCents(),
                mDescription.getText().toString(),
                frequency,
                time
            );
        } else {
            EnvelopesOpenHelper.deposite(getActivity(), mId,
                                         (mNegative?-1:1)*mAmount.getCents(),
                                         mDescription.getText().toString(),
                                         frequency );
        }
    }
};

