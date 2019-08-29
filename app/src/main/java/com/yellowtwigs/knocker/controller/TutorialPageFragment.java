package com.yellowtwigs.knocker.controller;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.yellowtwigs.knocker.R;

import org.jetbrains.annotations.NotNull;

public class TutorialPageFragment extends Fragment {

    // 1 - Create keys for our Bundle
    private AppCompatImageView page_fragment_Images;

    private Drawable drawable;

    public TutorialPageFragment(Drawable drawable) {
        this.drawable = drawable;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_page, container, false);

//        page_fragment_Layout = view.findViewById(R.id.fragment_page_rootview);
        page_fragment_Images = view.findViewById(R.id.fragment_page_content);

        page_fragment_Images.setBackgroundDrawable(drawable);

        return view;
    }
}
