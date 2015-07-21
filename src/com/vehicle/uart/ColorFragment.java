/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class ColorFragment extends Fragment 
{
    /**
     * Empty constructor as per the {@link Fragment} docs
     */
    public ColorFragment() 
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        final int color = getArguments().getInt("color");
        final FrameLayout root = new FrameLayout(getActivity());
        root.setBackgroundColor(color);
        return root;
    }
}
