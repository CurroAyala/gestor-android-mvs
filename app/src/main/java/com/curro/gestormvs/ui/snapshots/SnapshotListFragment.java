package com.curro.gestormvs.ui.snapshots;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.curro.gestormvs.databinding.FragmentSnapshotListBinding;
import com.curro.gestormvs.domain.models.Snapshot;
import com.curro.gestormvs.domain.models.VirtualMachine;
import com.curro.gestormvs.ui.ServiceLocator;
import com.curro.gestormvs.ui.snapshots.viewModels.SnapshotListViewModel;
import com.curro.gestormvs.ui.snapshots.viewModels.SnapshotViewModelFactory;


public class SnapshotListFragment extends Fragment {

    private FragmentSnapshotListBinding binding;
    private SnapshotListViewModel viewModel;
    private SnapshotAdapter adapter;

    private VirtualMachine currentVm;


    public SnapshotListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSnapshotListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentVm = SnapshotListFragmentArgs.fromBundle(getArguments()).getVirtualMachine();
        }

        setupViewModel(currentVm);
        setupRecyclerView();
    }

    private void setupViewModel(VirtualMachine vm) {
        SnapshotViewModelFactory factory = new SnapshotViewModelFactory(
                ServiceLocator.provideSshRepository()
        );
        viewModel = new ViewModelProvider(this, factory).get(SnapshotListViewModel.class);

        viewModel.getSnapshots().observe(getViewLifecycleOwner(), snapshots -> {
            adapter.submitList(snapshots);

            boolean isEmpty = snapshots == null || snapshots.isEmpty();
            binding.rvSnapshots.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error == null) return;
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });

        if (vm != null) {
            viewModel.loadSnapshots(vm);
        }
    }

    private void setupRecyclerView() {
        adapter = new SnapshotAdapter(new SnapshotAdapter.OnSnapshotActionListener() {
            @Override
            public void onRevert(Snapshot snapshot) { viewModel.revertSnapshot(currentVm, snapshot); }

            @Override
            public void onDelete(Snapshot snapshot) { viewModel.deleteSnapshot(currentVm, snapshot); }
        });

        binding.rvSnapshots.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSnapshots.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }

}