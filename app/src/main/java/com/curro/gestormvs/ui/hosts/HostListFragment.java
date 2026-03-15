package com.curro.gestormvs.ui.hosts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.curro.gestormvs.databinding.FragmentHostListBinding;
import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.ui.ServiceLocator;
import com.curro.gestormvs.ui.hosts.viewModels.HostListViewModel;
import com.curro.gestormvs.ui.hosts.viewModels.HostViewModelFactory;


public class HostListFragment extends Fragment {

    private FragmentHostListBinding binding;
    private HostListViewModel viewModel;
    private HostAdapter adapter;


    public HostListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHostListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        setupRecyclerView();
        setupViewModel();
    }

    private void setupRecyclerView() {
        adapter = new HostAdapter(new HostAdapter.OnHostActionListener() {
            @Override
            public void onEdit(Host host) {
                // TODO: edit action
            }

            @Override
            public void onDelete(Host host) {
                // TODO: delete action
            }
        });

        binding.recyclerHosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHosts.setAdapter(adapter);
    }

    private void setupViewModel() {
        HostViewModelFactory factory = new HostViewModelFactory(ServiceLocator.provideHostRepository(requireContext()));
        viewModel = new ViewModelProvider(this, factory).get(HostListViewModel.class);

        viewModel.getHosts().observe(getViewLifecycleOwner(), hosts -> {
            adapter.submitList(hosts);

            boolean isEmpty = hosts == null || hosts.isEmpty();
            binding.recyclerHosts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        );

        viewModel.loadHosts();
    }

}