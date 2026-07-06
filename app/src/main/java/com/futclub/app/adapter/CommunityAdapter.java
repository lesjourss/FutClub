package com.futclub.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.futclub.app.R;
import com.futclub.app.databinding.ItemCommunityBinding;
import com.futclub.app.model.Community;

import java.util.List;

/**
 * Adapter untuk menampilkan daftar komunitas di RecyclerView (Home screen).
 * Ini adalah RecyclerView WAJIB sesuai syarat UAS untuk "menampilkan daftar data secara dinamis".
 */
public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    public interface OnCommunityClickListener {
        void onClick(Community community);
    }

    private final List<Community> communities;
    private final OnCommunityClickListener listener;

    public CommunityAdapter(List<Community> communities, OnCommunityClickListener listener) {
        this.communities = communities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommunityBinding binding = ItemCommunityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CommunityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        holder.bind(communities.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    static class CommunityViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommunityBinding binding;

        CommunityViewHolder(ItemCommunityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Community community, OnCommunityClickListener listener) {
            binding.tvCommunityName.setText(community.getName());
            binding.tvCategoryBadge.setText(community.getCategoryName());
            binding.tvMemberCount.setText(community.getMemberCount() + " anggota");

            Glide.with(binding.getRoot().getContext())
                    .load(community.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.imgCommunityPhoto);

            binding.getRoot().setOnClickListener(v -> listener.onClick(community));
        }
    }
}
