package com.example.knocker.controller;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.knocker.R;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class TutorialPageFragment extends Fragment {

    // 1 - Create keys for our Bundle
    private AppCompatImageView page_fragment_Images;

    private Drawable drawable;
    private String title;

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
