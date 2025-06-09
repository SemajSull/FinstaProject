package com.example.finstatest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final List<Post> posts;

    public PostAdapter(List<Post> posts) {
        this.posts = posts;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.tvUsername.setText(post.getUsername());
        holder.tvLikes.setText(post.getLikesCount() + " likes");
        holder.tvCaption.setText(post.getCaption());

        // Join comments into one string
        StringBuilder sb = new StringBuilder();
        for (String c : post.getComments()) {
            sb.append(c).append("\n");
        }
        holder.tvComments.setText(sb.toString().trim());

        // Load image (Glide)
        Glide.with(holder.itemView.getContext())
                .load(post.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)   // optional placeholder
                .into(holder.ivPostImage);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvLikes, tvCaption, tvComments;
        ImageView ivPostImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername  = itemView.findViewById(R.id.tvUsername);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvLikes     = itemView.findViewById(R.id.tvLikes);
            tvCaption   = itemView.findViewById(R.id.tvCaption);
            tvComments  = itemView.findViewById(R.id.tvComments);
        }
    }
}
