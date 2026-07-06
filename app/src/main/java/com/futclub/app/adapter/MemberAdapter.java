package com.futclub.app.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.futclub.app.R;
import com.futclub.app.databinding.ItemMemberBinding;
import com.futclub.app.model.Member;

import java.util.List;

/**
 * Adapter RecyclerView untuk menampilkan daftar member komunitas
 * (mirip daftar anggota grup WhatsApp), dipakai di CommunityDetailActivity.
 */
public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<Member> members;

    public MemberAdapter(List<Member> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberBinding binding = ItemMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        holder.binding.tvMemberName.setText(member.getName());

        Glide.with(holder.binding.getRoot().getContext())
                .load(member.getPhotoUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(holder.binding.imgMemberPhoto);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        final ItemMemberBinding binding;

        MemberViewHolder(ItemMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
