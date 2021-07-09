package com.example.instagram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagram.Models.Post;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.Date;

public class PostDetailsActivity extends AppCompatActivity {

    Post post;
    TextView tvUsername;
    ImageView ivPostImage;
    TextView tvCreatedAt;
    TextView tvDescriptionDetails;

    public static final String TAG = "PostDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        tvUsername = findViewById(R.id.tvUserNameDetails);
        ivPostImage = findViewById(R.id.ivImageDetails);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        tvDescriptionDetails = findViewById(R.id.tvDescriptionDetails);

        tvUsername.setText(post.getUser().getUsername());
        //Get createdAt date and converting to relative time string
        Date createdAt = post.getCreatedAt();
        String timeAgo = Post.calculateTimeAgo(createdAt);
        tvCreatedAt.setText(timeAgo);
        tvDescriptionDetails.setText(post.getDescription());
        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivPostImage);
        }
    }
}