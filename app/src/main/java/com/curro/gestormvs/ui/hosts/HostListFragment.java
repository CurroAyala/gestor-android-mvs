package com.curro.gestormvs.ui.hosts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.curro.gestormvs.databinding.FragmentHostListBinding;


public class HostListFragment extends Fragment {

    private FragmentHostListBinding binding;


    public HostListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHostListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}