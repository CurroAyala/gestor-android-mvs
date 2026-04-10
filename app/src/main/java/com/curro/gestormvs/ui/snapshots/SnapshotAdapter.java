package com.curro.gestormvs.ui.snapshots;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.curro.gestormvs.databinding.ItemSnapshotBinding;
import com.curro.gestormvs.domain.models.Snapshot;

import java.util.ArrayList;
import java.util.List;

public class SnapshotAdapter extends RecyclerView.Adapter<SnapshotAdapter.SnapshotViewHolder> {

    public interface OnSnapshotActionListener {
        void onRevert(Snapshot snapshot);
        void onDelete(Snapshot snapshot);
    }

    private List<Snapshot> snapshots = new ArrayList<>();
    private final OnSnapshotActionListener listener;

    public SnapshotAdapter(OnSnapshotActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Snapshot> newSnapshots) {
        this.snapshots = newSnapshots;
        notifyDataSetChanged(); // TODO: improve this using DiffUtil
    }

    @NonNull
    @Override
    public SnapshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSnapshotBinding binding = ItemSnapshotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SnapshotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SnapshotViewHolder holder, int position) {
        holder.bind(snapshots.get(position));
    }

    @Override
    public int getItemCount() { return snapshots.size(); }

    public class SnapshotViewHolder extends RecyclerView.ViewHolder {
        private final ItemSnapshotBinding binding;

        SnapshotViewHolder(ItemSnapshotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Snapshot snapshot) {
            binding.tvSnapshotName.setText(snapshot.getName());
            binding.tvSnapshotDate.setText(snapshot.getDescription());
            binding.btnRevert.setOnClickListener(v -> listener.onRevert(snapshot));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(snapshot));
        }
    }

}
