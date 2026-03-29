package com.curro.gestormvs.ui.virtualMachines;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.curro.gestormvs.R;
import com.curro.gestormvs.databinding.FragmentHostUpdateBinding;
import com.curro.gestormvs.databinding.FragmentPasswordDialogBinding;


public class PasswordDialogFragment extends DialogFragment {

    public interface OnPasswordSubmitListener {
        void onPasswordSubmitted(String password);
        void onCancelled();
    }

    private static final String ARG_HOST_NAME = "host_name";

    private FragmentPasswordDialogBinding binding;
    private OnPasswordSubmitListener listener;


    public static PasswordDialogFragment newInstance(String hostName) {
        PasswordDialogFragment fragment = new PasswordDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOST_NAME, hostName);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPasswordSubmitListener(OnPasswordSubmitListener listener) {
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
        binding = FragmentPasswordDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupDialog();
        setupListeners();
    }

    private void setupDialog() {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Dialog width
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        String hostName = getArguments() != null
                ? getArguments().getString(ARG_HOST_NAME, "")
                : "";

        binding.tvDialogHostName.setText(
                getString(R.string.dialog_password_host_msg, hostName)
        );
    }

    private void setupListeners() {
        binding.btnDialogConfirm.setOnClickListener(v -> {
            String password = binding.etDialogPassword.getText().toString();

            if (password.isEmpty()) {
                binding.etDialogPassword.setError(
                        getString(R.string.error_password_empty)
                );
                return;
            }

            if (listener != null) {
                listener.onPasswordSubmitted(password);
            }
            dismiss();
        });

        binding.btnDialogCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelled();
            }
            dismiss();
        });

        binding.etDialogPassword.setOnEditorActionListener((v, actionId, event) -> {
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