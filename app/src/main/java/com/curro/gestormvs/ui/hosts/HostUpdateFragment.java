package com.curro.gestormvs.ui.hosts;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.curro.gestormvs.databinding.FragmentHostUpdateBinding;

import com.curro.gestormvs.ui.ServiceLocator;
import com.curro.gestormvs.ui.hosts.viewModels.HostUpdateViewModel;
import com.curro.gestormvs.ui.hosts.viewModels.HostViewModelFactory;


public class HostUpdateFragment extends Fragment {

    private FragmentHostUpdateBinding binding;
    private HostUpdateViewModel viewModel;


    public HostUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHostUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        long hostId = -1;

        if (getArguments() != null) {
            hostId = HostUpdateFragmentArgs.fromBundle(getArguments()).getHostId();
        }

        setupViewModel(hostId);
        setupListeners(hostId);
    }

    private void setupViewModel(long hostId) {
        HostViewModelFactory factory = new HostViewModelFactory(
                ServiceLocator.provideHostRepository(requireContext())
        );
        viewModel = new ViewModelProvider(this, factory).get(HostUpdateViewModel.class);

        viewModel.hostUpdated.observe(getViewLifecycleOwner(), updated -> {
            if (updated) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        );

        viewModel.getHostData().observe(getViewLifecycleOwner(), host -> {
            if (host != null) {
                binding.etName.setText(host.getName());
                binding.etIp.setText(host.getIp());
                binding.etPort.setText(String.valueOf(host.getPort()));
                binding.etUser.setText(host.getUser());


                if (host.getPassword() != null) {
                    binding.etPassword.setText("*****");
                }
            }
        });
        viewModel.loadHost(hostId);
    }

    private void setupListeners(long hostId) {
        binding.btnSave.setOnClickListener(v -> {
            String name     = binding.etName.getText().toString();
            String ip       = binding.etIp.getText().toString();
            String port     = binding.etPort.getText().toString();
            String user     = binding.etUser.getText().toString();
            String password = binding.etPassword.getText().toString();

            viewModel.saveExistingHost(hostId, name, user, ip, port, password);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}