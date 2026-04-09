package com.curro.gestormvs.ui.virtualMachines;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.curro.gestormvs.R;
import com.curro.gestormvs.databinding.ItemVmBinding;
import com.curro.gestormvs.domain.models.VirtualMachine;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachineAdapter extends RecyclerView.Adapter<VirtualMachineAdapter.VirtualMachineViewHolder> {

    public interface OnVirtualMachineActionListener {
        void onPower(VirtualMachine vm);
        void onReboot(VirtualMachine vm);
        void onHibernate(VirtualMachine vm);
        void onPause(VirtualMachine vm);
        void onCreateSnapshot(VirtualMachine vm);
    }

    private List<VirtualMachine> vms = new ArrayList<>();
    private final OnVirtualMachineActionListener listener;

    public VirtualMachineAdapter(OnVirtualMachineActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VirtualMachine> newVms) {
        this.vms = newVms;
        notifyDataSetChanged(); // TODO: improve this using DiffUtil
    }

    @NonNull
    @Override
    public VirtualMachineAdapter.VirtualMachineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVmBinding binding = ItemVmBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VirtualMachineViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VirtualMachineAdapter.VirtualMachineViewHolder holder, int position) {
        holder.bind(vms.get(position));
    }

    @Override
    public int getItemCount() {
        return vms.size();
    }

    public class VirtualMachineViewHolder extends RecyclerView.ViewHolder {

        private final ItemVmBinding binding;

        VirtualMachineViewHolder(ItemVmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VirtualMachine vm) {
            // binding.tvVmId.setText(vm.getId());
            binding.tvVmName.setText(vm.getName());
            binding.tvVmState.setText(vm.getState());
            applyStatusStyle(vm.getState());
            updateButtons(vm.getState());

            binding.btnPower.setOnClickListener(v -> listener.onPower(vm));
            binding.btnRestart.setOnClickListener(v -> listener.onReboot(vm));
            binding.btnSleep.setOnClickListener(v -> listener.onHibernate(vm));
            binding.btnPause.setOnClickListener(v -> listener.onPause(vm));
            binding.btnSnapshot.setOnClickListener(v -> listener.onCreateSnapshot(vm));
        }

        // Aux methods -------------------------
        private void applyStatusStyle(String status) {
            Context ctx = binding.getRoot().getContext();

            int bgColor;
            int textColor;

            switch (status.trim().toLowerCase()) {
                case "running":
                    bgColor   = ContextCompat.getColor(ctx, R.color.state_running_bg);
                    textColor = ContextCompat.getColor(ctx, R.color.state_running_text);
                    break;
                case "paused":
                case "suspended":
                case "pmsuspended":
                    bgColor   = ContextCompat.getColor(ctx, R.color.state_paused_bg);
                    textColor = ContextCompat.getColor(ctx, R.color.state_paused_text);
                    break;
                case "shut off":
                case "crashed":
                default:
                    bgColor   = ContextCompat.getColor(ctx, R.color.state_shutoff_bg);
                    textColor = ContextCompat.getColor(ctx, R.color.state_shutoff_text);
                    break;
            }

            binding.tvVmState.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            binding.tvVmState.setTextColor(textColor);
        }

        private void updateButtons(String state) {
            switch (state.trim().toLowerCase()) {
                case "running":
                    binding.btnPause.setImageResource(R.drawable.ic_pause);
                    binding.btnSleep.setImageResource(R.drawable.ic_sleep);
                    break;

                case "paused":
                    binding.btnPause.setImageResource(R.drawable.ic_play);
                    binding.btnSleep.setImageResource(R.drawable.ic_sleep);
                    break;

                case "shut off":
                default:
                    binding.btnPause.setImageResource(R.drawable.ic_pause);
                    binding.btnSleep.setImageResource(R.drawable.ic_restore);
                    break;
            }
        }

    }

}
