package com.parthiv.sunshine.app;

/**
 * Created by Parthiv on 19/08/2016.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceActivity;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers_preference,target);
    }

    @Override
        protected boolean isValidFragment(String fragmentName) {
        return PrefFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}