

package com.srima.bb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.PopupMenu;
import android.widget.TextView;

public class EditTransactionFragment extends OkFragment
                                     implements TextWatcher {
    int mId;
    EditText mDescription;
    EditMoney mAmount;

    public static EditTransactionFragment newInstance(int id, String desc, long cents) {
        EditTransactionFragment retVal = new EditTransactionFragment();

        Bundle args = new Bundle();
        args.putInt("com.srima.bb.log", id);
        args.putString("com.srima.bb.log.description", desc);
        args.putLong("com.notriddle.bugdget.log.cents", cents);
        retVal.setArguments(args);

        return retVal;
    }

    @Override public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        mAmount.setOnEditorActionListener(this);
        mAmount.addTextChangedListener(this);

        Bundle args = getArguments();
        mId = args.getInt("com.srima.bb.log");
        mDescription.setText(
            args.getString("com.srima.bb.log.description")
        );
        mAmount.setCents(args.getLong("com.notriddle.bugdget.log.cents"));
    }

    @Override public View onCreateInternalView(LayoutInflater inflater,
                                               ViewGroup cont, Bundle state) {
        View retVal = inflater.inflate(R.layout.spendfragment, cont, false);
        mAmount = (EditMoney) retVal.findViewById(R.id.amount);
        mAmount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        mDescription = (EditText) retVal.findViewById(R.id.description);
        retVal.findViewById(R.id.delayed).setVisibility(View.GONE);
        retVal.findViewById(R.id.delay).setVisibility(View.GONE);
        retVal.findViewById(R.id.repeat).setVisibility(View.GONE);
        retVal.findViewById(R.id.frequency).setVisibility(View.GONE);
        return retVal;
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

    @Override public String getTitle() {
        return getActivity().getString(R.string.edit_name);
    }

    @Override public void ok() {
        SQLiteDatabase db = (new EnvelopesOpenHelper(getActivity()))
                            .getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(
                "UPDATE log SET cents = ?, description = ? WHERE _id = ?",
                new String[] {
                    Long.toString(mAmount.getCents()),
                    mDescription.getText().toString(),
                    Integer.toString(mId)
                }
            );
            EnvelopesOpenHelper.playLog(db);
            db.setTransactionSuccessful();
            getActivity().getContentResolver()
                          .notifyChange(EnvelopesOpenHelper.URI, null);
        } finally {
            db.endTransaction();
            db.close();
        }
    }
};

