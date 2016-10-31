

package com.srima.bb;

import android.app.Activity;
import android.os.Bundle;

public abstract class LockedActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
       // check();
	}

    @Override public void onResume() {
        super.onResume();
        //check();
    }

   /** private void check() {
        if (!PinActivity.ensureUnlocked(this)) {
            finish();
        }
    }*/
}
