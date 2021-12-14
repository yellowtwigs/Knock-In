package com.yellowtwigs.knockin.controller;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class TutorialViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Drawable> listOfTuto;
    private Boolean fromMainActivity;

    public TutorialViewPagerAdapter(FragmentManager fm, ArrayList<Drawable> listOfTuto, Boolean fromMainActivity) {
        super(fm);
        this.listOfTuto = listOfTuto;
        this.fromMainActivity = fromMainActivity;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return listOfTuto.size();
    }


    /**
     * Return the Fragment associated with a specified position.
     */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (fromMainActivity) {
            return new TutorialPageFragment(listOfTuto.get(position), fromMainActivity, position);
        } else {
            return new TutorialPageFragment(listOfTuto.get(position));
        }
    }
}