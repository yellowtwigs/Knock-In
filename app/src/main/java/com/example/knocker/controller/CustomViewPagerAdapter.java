package com.example.knocker.controller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class CustomViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Uri> listOfTuto;
    private ArrayList<String> listOfTitle;

    public CustomViewPagerAdapter(FragmentManager fm, ArrayList<Uri> listOfTuto, ArrayList<String> listOfTitle) {
        super(fm);
        this.listOfTuto = listOfTuto;
        this.listOfTitle = listOfTitle;
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
        return new PageFragment(listOfTuto.get(position), listOfTitle.get(position));
    }
}