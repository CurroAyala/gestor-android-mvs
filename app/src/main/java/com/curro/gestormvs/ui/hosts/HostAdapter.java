package com.curro.gestormvs.ui.hosts;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.curro.gestormvs.databinding.ItemHostBinding;
import com.curro.gestormvs.domain.models.Host;

import java.util.ArrayList;
import java.util.List;

public class HostAdapter extends RecyclerView.Adapter<HostAdapter.HostViewHolder> {

    public interface OnHostActionListener {
        void onEdit(Host host);
        void onDelete(Host host);
    }

    private List<Host> hosts = new ArrayList<>();
    private final OnHostActionListener listener;

    public HostAdapter(OnHostActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Host> newHosts) {
        this.hosts = newHosts;
        notifyDataSetChanged(); // TODO: improve this using DiffUtil
    }

    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHostBinding binding = ItemHostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new HostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        holder.bind(hosts.get(position));
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    public class HostViewHolder extends RecyclerView.ViewHolder {

        private final ItemHostBinding binding;

        HostViewHolder(ItemHostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Host host) {
            binding.tvHostName.setText(host.getName());
            binding.tvHostIp.setText(host.getIp());

            binding.btnEdit.setOnClickListener(v -> listener.onEdit(host));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(host));
        }
    }
}
