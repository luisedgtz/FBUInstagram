package com.example.instagram.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagram.Models.Post;
import com.example.instagram.R;

import java.util.List;

public class PostsGridAdapter extends RecyclerView.Adapter<PostsGridAdapter.VH> {
    private Context mContext;
    private List<Post> mPosts;

    public PostsGridAdapter(Activity context, List<Post> posts) {
        mContext = context;
        mPosts = posts;
    }

    //Clean all elements of the recycler
    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> list) {
        mPosts.addAll(list);
        notifyDataSetChanged();
    }

    // Inflate the view based on the viewType provided.
    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_post, parent, false);
        return new VH(itemView, mContext);
    }

    // Display data at the specified position
    @Override
    public void onBindViewHolder(VH holder, int position) {
        Post post = mPosts.get(position);
        Log.d("AdapterGrid" , post.getDescription());
        Glide.with(mContext).load(post.getImage()).into(holder.ivPhoto);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    // Provide a reference to the views for each contact item
    public class VH extends RecyclerView.ViewHolder {
        final View rootView;
        final ImageView ivPhoto;

        public VH(View itemView, final Context context) {
            super(itemView);
            rootView = itemView;
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPostCard);
        }
    }
}
