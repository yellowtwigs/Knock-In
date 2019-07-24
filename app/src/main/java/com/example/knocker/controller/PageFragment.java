package com.example.knocker.controller;

import android.annotation.SuppressLint;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.knocker.R;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class PageFragment extends Fragment {

    // 1 - Create keys for our Bundle
    private LinearLayout page_fragment_Layout;
    private VideoView page_fragment_VideoView;
    private TextView page_fragment_Title;
    private Uri uri;
    private String title;

    public PageFragment(Uri uri, String title) {
        this.uri = uri;
        this.title = title;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_page, container, false);

        page_fragment_Layout = view.findViewById(R.id.fragment_page_rootview);
        page_fragment_VideoView = view.findViewById(R.id.fragment_page_content);
        page_fragment_Title = view.findViewById(R.id.fragment_page_title);

        page_fragment_VideoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(getContext());
        page_fragment_VideoView.setMediaController(mediaController);
        mediaController.setAnchorView(page_fragment_VideoView);

        page_fragment_Title.setText(title);

        page_fragment_VideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        page_fragment_VideoView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        page_fragment_VideoView.suspend();
    }
}
