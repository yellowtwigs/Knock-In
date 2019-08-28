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

import pl.droidsonroids.gif.GifImageView;

public class TutorialPageFragment extends Fragment {

    private Drawable drawable;
    private int position;
    private Boolean fromMainActivity = false;

    TutorialPageFragment(Drawable drawable) {
        this.drawable = drawable;
    }

    TutorialPageFragment(Drawable drawable, Boolean fromMainActivity, int position) {
        this.drawable = drawable;
        this.position = position;
        this.fromMainActivity = fromMainActivity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_page_tutorial, container, false);

        AppCompatImageView page_fragment_Images = view.findViewById(R.id.fragment_page_tutorial_content);
        GifImageView page_fragment_Gif = view.findViewById(R.id.fragment_page_tutorial_gif);

        page_fragment_Images.setBackgroundDrawable(drawable);

        if (fromMainActivity) {
            if (position == 0) {
                page_fragment_Gif.setVisibility(View.VISIBLE);
                page_fragment_Images.setVisibility(View.GONE);
            } else {
                page_fragment_Gif.setVisibility(View.GONE);
                page_fragment_Images.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }
}
