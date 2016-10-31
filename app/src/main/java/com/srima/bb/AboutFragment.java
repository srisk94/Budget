

package com.srima.bb;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AboutFragment extends Fragment
                           implements
                                      TitleFragment {

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup cont,
                                       Bundle state) {
        View retVal = inflater.inflate(R.layout.aboutactivity, cont, false);

        TextView txt = (TextView) retVal.findViewById(R.id.aboutText);



        return retVal;
    }



    @Override public String getTitle() {
        return getActivity().getString(R.string.about_name);
    }
}
