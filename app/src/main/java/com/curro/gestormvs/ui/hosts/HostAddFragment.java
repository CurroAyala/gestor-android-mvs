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

import com.curro.gestormvs.databinding.FragmentHostAddBinding;
import com.curro.gestormvs.ui.ServiceLocator;
import com.curro.gestormvs.ui.hosts.viewModels.HostCreateViewModel;
import com.curro.gestormvs.ui.hosts.viewModels.HostViewModelFactory;


public class HostAddFragment extends Fragment {

    private FragmentHostAddBinding binding;
    private HostCreateViewModel viewModel;


    public HostAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHostAddBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        setupViewModel();
        setupListeners();
    }

    private void setupViewModel() {
        HostViewModelFactory factory = new HostViewModelFactory(
                ServiceLocator.provideHostRepository(requireContext())
        );
        viewModel = new ViewModelProvider(this, factory).get(HostCreateViewModel.class);

        viewModel.hostSaved.observe(getViewLifecycleOwner(), saved -> {
            if (saved) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        );
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> {
            String name     = binding.etName.getText().toString();
            String ip       = binding.etIp.getText().toString();
            String port     = binding.etPort.getText().toString();
            String user     = binding.etUser.getText().toString();
            String password = binding.etPassword.getText().toString();

            viewModel.saveNewHost(name, user, ip, port, password);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}