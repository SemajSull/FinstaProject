package com.example.finstatest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final List<Post> posts;
    private final OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onLikeClick(Post post, int position);
        void onCommentClick(Post post, int position);
    }

    public PostAdapter(List<Post> posts, OnPostInteractionListener listener) {
        this.posts = posts;
        this.listener = listener;
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
        holder.tvTimestamp.setText(getTimeAgo(post.getCreatedAt()));

        // Update like button state
        holder.btnLike.setImageResource(post.isLiked() ? 
            R.drawable.ic_like_filled : R.drawable.ic_like);

        // Join comments into one string
        StringBuilder sb = new StringBuilder();
        for (String c : post.getComments()) {
            sb.append(c).append("\n");
        }
        holder.tvComments.setText(sb.toString().trim());

        // Load image (Glide)
        Glide.with(holder.itemView.getContext())
                .load(post.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.ivPostImage);

        Glide.with(holder.itemView.getContext())
                .load(post.getPfpUrl())
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(holder.ivPfp);

        // Set click listeners
        holder.btnLike.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLikeClick(post, holder.getAdapterPosition());
            }
        });

        holder.btnComment.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(post, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    private String getTimeAgo(Date date) {
        long now = System.currentTimeMillis();
        long diff = now - date.getTime();

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + "m ago";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + "h ago";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + "d ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
            return sdf.format(date);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvLikes, tvCaption, tvComments, tvTimestamp;
        ImageView ivPostImage, ivPfp;
        ImageButton btnLike, btnComment;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPfp = itemView.findViewById(R.id.ivUserPfp);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
        }
    }
}
