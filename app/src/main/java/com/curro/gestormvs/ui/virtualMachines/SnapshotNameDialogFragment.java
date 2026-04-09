package com.curro.gestormvs.ui.virtualMachines;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.curro.gestormvs.R;
import com.curro.gestormvs.databinding.FragmentSnapshotNameDialogBinding;


public class SnapshotNameDialogFragment extends DialogFragment {

    public interface OnNameSubmitListener {
        void onNameSubmitted(String name);
        void onCancelled();
    }

    private static final String ARG_VM_NAME = "vm_name";

    private FragmentSnapshotNameDialogBinding binding;
    private OnNameSubmitListener listener;


    public static SnapshotNameDialogFragment newInstance(String vmName) {
        SnapshotNameDialogFragment fragment = new SnapshotNameDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VM_NAME, vmName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnNameSubmitListener(OnNameSubmitListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSnapshotNameDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        String vmName = getArguments() != null ? getArguments().getString(ARG_VM_NAME, "") : "";
        binding.tvDialogSnapshotName.setText(vmName);

        binding.btnDialogConfirm.setOnClickListener(v -> {
            String name = binding.etDialogName.getText().toString().trim();
            if (name.isEmpty()) {
                binding.etDialogName.setError("Name cannot be empty");
                return;
            }
            if (listener != null) listener.onNameSubmitted(name);
            dismiss();
        });

        binding.btnDialogCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelled();
            }
            dismiss();
        });

        // Handle the "Done" action in the keyboard
        binding.etDialogName.setOnEditorActionListener((v, actionId, event) -> {
            binding.btnDialogConfirm.performClick();
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}