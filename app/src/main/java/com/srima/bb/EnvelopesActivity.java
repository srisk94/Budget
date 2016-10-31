

package com.srima.bb;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class EnvelopesActivity extends LockedActivity
                               implements OkFragment.OnDismissListener,
                                          ListView.OnItemClickListener,
                                          ColorFragment.OnColorChangeListener {

    ListView mNavDrawer;
    NavAdapter mNavAdapter;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mNavToggle;
    ColorDrawable mActionBarColor;
    View mCustomActionBarView;
    int mColor;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity);
        setupActionBarBackground();
        setupDrawer();

        if (state == null) {
            final Intent i = getIntent();
            final Bundle args = i.getExtras();
            final Uri data = i.getData();
            final String fragmentName = data != null
                ? data.getHost()
                : EnvelopesFragment.class.getName();
            try {
                topFragment(Class.forName(fragmentName), FragmentTransaction.TRANSIT_FRAGMENT_FADE, args);
            } catch (Exception e) {
                throw new Error(e);
            }
        } else {
            configureFragment(
                getFragmentManager().findFragmentById(R.id.content_frame)
            );
        }
    }

    private void setupActionBarBackground() {
        mColor = 0xFFEEEEEE;
        mActionBarColor = new ColorDrawable(mColor);
        getActionBar().setBackgroundDrawable(mActionBarColor);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
             mActionBarColor.setCallback(new Drawable.Callback() {
                @Override public void invalidateDrawable(Drawable who) {
                    getActionBar().setBackgroundDrawable(who);
                }
                @Override public void scheduleDrawable(Drawable who, Runnable what, long when) {}
                @Override public void unscheduleDrawable(Drawable who, Runnable what) {}
             });
        }
    }

    private void setupDrawer() {
        mNavDrawer = (ListView) findViewById(R.id.left_drawer);
        mNavAdapter = new NavAdapter(this);
        mNavDrawer.setAdapter(mNavAdapter);
        mNavDrawer.setOnItemClickListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavToggle = new ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            R.drawable.ic_drawer,
            R.string.drawerOpen_button,
            R.string.drawerClose_button
        ) {
            @Override public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
                ActionBar ab = getActionBar();
                ab.setTitle(getTitle());
                invalidateOptionsMenu();
                if (mCustomActionBarView != null) {
                    ab.setCustomView(mCustomActionBarView);
                    ab.setDisplayShowTitleEnabled(false);
                    ab.setDisplayShowCustomEnabled(true);
                }
            }
            @Override public void onDrawerSlide(View v, float off) {
                super.onDrawerSlide(v, off);
                int allIn = mColor;
                int allOut = 0xFFEEEEEE;
                int result = Color.rgb(
                    (int)(Color.red(allIn)*(1-off) + Color.red(allOut)*off),
                    (int)(Color.green(allIn)*(1-off) + Color.green(allOut)*off),
                    (int)(Color.blue(allIn)*(1-off) + Color.blue(allOut)*off)
                );
                mActionBarColor.setColor(result);
            }
            @Override public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
                ActionBar ab = getActionBar();
                ab.setTitle(getString(R.string.app_name));
                invalidateOptionsMenu();
                if (mCustomActionBarView != null) {
                    ab.setDisplayShowTitleEnabled(true);
                    ab.setDisplayShowCustomEnabled(false);
                    ab.setCustomView(null);
                }
            }
        };
        mDrawerLayout.setDrawerListener(mNavToggle);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override public void onPostCreate(Bundle state) {
        super.onPostCreate(state);
        mNavToggle.syncState();
    }

    @Override public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        mNavToggle.onConfigurationChanged(config);
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        mDrawerLayout.closeDrawers();
        topFragment(mNavAdapter.getItem(pos), FragmentTransaction.TRANSIT_FRAGMENT_FADE, null);
    }

    @Override public void onResume() {
        super.onResume();
        (new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... o) {
                EnvelopesOpenHelper.playLog(EnvelopesActivity.this);
                return null;
            }
            protected void onPostExecute(Object o) {
                // do nothing.
            }
        }).execute();
    }

    @Override public void onDismiss() {
        onBackPressed();
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mNavDrawer);
        int l = menu.size();
        for (int i = 0; i != l; ++i) {
            menu.getItem(i).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void topFragment(Class<?> cls, int transition, Bundle args) {
        Fragment frag = Fragment.instantiate(
            this,
            cls.getName(),
            args
        );
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction()
         .replace(R.id.content_frame, frag)
         .setTransition(transition)
         .commit();
        fragmentManager.executePendingTransactions();
        configureFragment(frag);
    }

    public void switchFragment(Class<?> cls, String name, Bundle args) {
        Fragment frag = Fragment.instantiate(
            this,
            cls.getName(),
            args
        );
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
         .replace(R.id.content_frame, frag)
         .addToBackStack(name)
         .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
         .commit();
        fragmentManager.executePendingTransactions();
        configureFragment(frag);
    }

    @Override public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if (mDrawerLayout.isDrawerOpen(mNavDrawer)) {
            mDrawerLayout.closeDrawers();
        } else if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate();
            configureFragment(
                fragmentManager.findFragmentById(R.id.content_frame)
            );
        } else {
            super.onBackPressed();
        }
    }

    private void configureFragment(Fragment frag) {
        ActionBar ab = getActionBar();
        if (frag instanceof TitleFragment) {
            TitleFragment tFrag = (TitleFragment)frag;
            setTitle(tFrag.getTitle());
            ab.setTitle(getTitle());
            boolean isTopLevel = false;
            for (int i = 0; i != mNavAdapter.getCount(); ++i) {
                if (mNavAdapter.getItem(i) == frag.getClass()) {
                    mNavDrawer.setItemChecked(i, true);
                    isTopLevel = true;
                    break;
                } else {
                    mNavDrawer.setItemChecked(i, false);
                }
            }
            mNavToggle.setDrawerIndicatorEnabled(isTopLevel);
        } else {
            throw new Error("Top-level fragment must be a TitleFragment");
        }
        if (frag instanceof DialogFragment) {
            DialogFragment dFrag = (DialogFragment)frag;
            dFrag.setShowsDialog(false);
        }
        if (frag instanceof ColorFragment) {
            ColorFragment cFrag = (ColorFragment)frag;
            onColorChange(cFrag.getColor());
        } else {
            onColorChange(0);
        }
        if (frag instanceof CustomActionBarFragment) {
            CustomActionBarFragment cFrag = (CustomActionBarFragment)frag;
            mCustomActionBarView = cFrag.onCreateActionBarView(getLayoutInflater());
            ab.setCustomView(mCustomActionBarView);
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayShowCustomEnabled(true);
        } else {
            mCustomActionBarView = null;
            ab.setDisplayShowTitleEnabled(true);
            ab.setDisplayShowCustomEnabled(false);
            ab.setCustomView(null);
        }
    }

    @Override public void onColorChange(int color) {
        mColor = color == 0 ? 0xFFEEEEEE : color;
        mActionBarColor.setColor(mColor);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (mNavToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                topFragment(EnvelopesFragment.class, FragmentTransaction.TRANSIT_FRAGMENT_CLOSE, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
