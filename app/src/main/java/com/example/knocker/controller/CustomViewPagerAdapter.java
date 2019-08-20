package com.example.knocker.controller;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class CustomViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Drawable> listOfTuto;

    public CustomViewPagerAdapter(FragmentManager fm, ArrayList<Drawable> listOfTuto) {
        super(fm);
        this.listOfTuto = listOfTuto;
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
     *
     * @param position
     */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return new TutorialPageFragment(listOfTuto.get(position));
    }
}