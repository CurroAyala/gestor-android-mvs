package com.curro.gestormvs.ui.virtualMachines;

import android.app.Service;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.curro.gestormvs.databinding.FragmentVirtualMachineListBinding;
import com.curro.gestormvs.domain.models.Host;
import com.curro.gestormvs.domain.models.VirtualMachine;
import com.curro.gestormvs.ui.ServiceLocator;
import com.curro.gestormvs.ui.virtualMachines.viewModels.VirtualMachineListViewModel;
import com.curro.gestormvs.ui.virtualMachines.viewModels.VirtualMachineViewModelFactory;


public class VirtualMachineListFragment extends Fragment {

    private FragmentVirtualMachineListBinding binding;
    private VirtualMachineListViewModel viewModel;
    private VirtualMachineAdapter adapter;


    public VirtualMachineListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVirtualMachineListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        long hostId = -1;

        if (getArguments() != null) {
            hostId = VirtualMachineListFragmentArgs.fromBundle(getArguments()).getHostId();
        }

        setupViewModel(hostId);
        setupRecyclerView();
    }

    private void setupViewModel(long hostId) {
        VirtualMachineViewModelFactory factory = new VirtualMachineViewModelFactory(
                ServiceLocator.provideHostRepository(requireContext()),
                ServiceLocator.provideSshRepository(),
                ServiceLocator.provideCheckVMStateUseCase()
        );
        viewModel = new ViewModelProvider(this, factory).get(VirtualMachineListViewModel.class);

        // Decide if it is necessary to show the password dialog
        viewModel.getHostData().observe(getViewLifecycleOwner(), host -> {
            if (host == null) return;

            binding.tvHostTitle.setText(host.getName());
            if (host.getPassword() != null && !host.getPassword().isEmpty()) {
                viewModel.connectAndLoadVMs(host);
            } else {
                showPasswordDialog(host);
            }
        });

        viewModel.getVms().observe(getViewLifecycleOwner(), vms -> {
            adapter.submitList(vms);

            boolean isEmpty = vms == null || vms.isEmpty();
            binding.recyclerVms.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error == null) return;
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            // Navigation.findNavController(requireView()).navigateUp();
        });

        viewModel.loadHost(hostId);
    }

    private void setupRecyclerView() {
        adapter = new VirtualMachineAdapter(new VirtualMachineAdapter.OnVirtualMachineActionListener() {
            @Override
            public void onPower(VirtualMachine vm) {
                viewModel.powerVM(vm);
            }

            @Override
            public void onReboot(VirtualMachine vm) {
                viewModel.rebootVM(vm);
            }

            @Override
            public void onHibernate(VirtualMachine vm) { viewModel.hibernate(vm); }

            @Override
            public void onPause(VirtualMachine vm) { viewModel.pauseVM(vm); }

            @Override
            public void onCreateSnapshot(VirtualMachine vm) { showSnapshotDialog(vm); }

            @Override
            public void onListSnapshots(VirtualMachine vm) {
                VirtualMachineListFragmentDirections.ActionVirtualMachineListFragmentToSnapshotListFragment action =
                        VirtualMachineListFragmentDirections.actionVirtualMachineListFragmentToSnapshotListFragment(vm);

                Navigation.findNavController(requireView()).navigate(action);
            }
        });

        binding.recyclerVms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerVms.setAdapter(adapter);
    }

    private void showPasswordDialog(Host host) {
        PasswordDialogFragment dialog = PasswordDialogFragment.newInstance(host.getName());

        dialog.setOnPasswordSubmitListener(new PasswordDialogFragment.OnPasswordSubmitListener() {
            @Override
            public void onPasswordSubmitted(String password) {
                host.setPassword(password);
                viewModel.connectAndLoadVMs(host);
            }

            @Override
            public void onCancelled() {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        dialog.show(getChildFragmentManager(), "password_dialog");
    }

    private void showSnapshotDialog(VirtualMachine vm) {
        SnapshotNameDialogFragment dialog = SnapshotNameDialogFragment.newInstance(vm.getName());

        dialog.setOnNameSubmitListener(new SnapshotNameDialogFragment.OnNameSubmitListener() {
            @Override
            public void onNameSubmitted(String snapshotName) {
                viewModel.createSnapshot(vm, snapshotName);
            }

            @Override
            public void onCancelled() {
                // Nothing to do here
            }
        });

        dialog.show(getChildFragmentManager(), "snapshot_dialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //viewModel.disconnect();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        viewModel.disconnect();
    }

}