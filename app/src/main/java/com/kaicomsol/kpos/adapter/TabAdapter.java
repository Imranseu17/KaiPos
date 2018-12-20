package com.kaicomsol.kpos.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.kaicomsol.kpos.fragment.ErrorFragment;
import com.kaicomsol.kpos.fragment.HistoryFragment;
import com.kaicomsol.kpos.fragment.PropertiesFragment;


/**
 * Created by wolfsoft3 on 24/7/18.
 */

public class TabAdapter extends FragmentPagerAdapter {

    int count;

    public TabAdapter(FragmentManager fm, int count) {
        super(fm);
        this.count = count;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                PropertiesFragment properties = new PropertiesFragment();
                return properties;
            case 1:
                HistoryFragment histroty = new HistoryFragment();
                return histroty;
            case 2:
                ErrorFragment error = new ErrorFragment();
                return error;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return count;
    }
}
