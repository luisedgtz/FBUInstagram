package com.example.instagram;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
    ImageView ivUser;
    TextView tvDescriptionDetails;

    public static final String TAG = "PostDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        //Setting custom action bar
        ActionBar actionBar = getSupportActionBar();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(v);

        post = (Post) Parcels.unwrap(getIntent().getParcelableExtra(Post.class.getSimpleName()));
        tvUsername = findViewById(R.id.tvUserNameDetails);
        ivPostImage = findViewById(R.id.ivImageDetails);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        ivUser = findViewById(R.id.ivProfilePicDetails);
        tvDescriptionDetails = findViewById(R.id.tvDescriptionDetails);

        tvUsername.setText(post.getUser().getUsername());
        //Get createdAt date and converting to relative time string
        Date createdAt = post.getCreatedAt();
        String timeAgo = Post.calculateTimeAgo(createdAt);
        tvCreatedAt.setText(timeAgo);
        tvDescriptionDetails.setText(post.getDescription());
        ParseFile image = post.getImage();
        ParseFile userImage = post.getUser().getParseFile("profile_pic");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivPostImage);
        }

        if (userImage != null) {
            Glide.with(this).load(userImage.getUrl()).circleCrop().into(ivUser);
        }
    }
}