package de.doerte.veranstaltungstools.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import de.doerte.veranstaltungstools.ChatFragment;
import de.doerte.veranstaltungstools.R;
import de.doerte.veranstaltungstools.StartFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;

    private boolean micPerm, camPerm;

    public SectionsPagerAdapter(Context context, FragmentManager fm, boolean micPermissionAccepted, boolean camPermissionAccepted) {
        super(fm);
        mContext = context;
        this.camPerm = camPermissionAccepted;
        this.micPerm = micPermissionAccepted;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = PlaceholderFragment.newInstance(0);
                break;
            case 1:
                fragment = StartFragment.newInstance(micPerm);
                break;
            case 2:
                fragment = ChatFragment.newInstance(camPerm);
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}