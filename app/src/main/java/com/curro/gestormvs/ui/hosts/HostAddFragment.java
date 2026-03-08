package com.curro.gestormvs.ui.hosts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.curro.gestormvs.databinding.FragmentHostAddBinding;


public class HostAddFragment extends Fragment {

    private FragmentHostAddBinding binding;


    public HostAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHostAddBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }
}