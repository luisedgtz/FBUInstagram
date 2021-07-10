package com.example.instagram.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.instagram.Adapters.PostsGridAdapter;
import com.example.instagram.Helpers.BitmapScaler;
import com.example.instagram.Helpers.DeviceDimensionsHelper;
import com.example.instagram.LoginActivity;
import com.example.instagram.Models.Post;
import com.example.instagram.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class UserFragment extends Fragment {

    Button btnLogout;
    Button btnChangePic;
    TextView tvUser;

    public static final String TAG = "UserFragment";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private File photoFile;
    public String photoFileName = "photo.jpg";

    private RecyclerView rvPosts;
    private PostsGridAdapter mAdapter;
    private List<Post> posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        rvPosts = getView().findViewById(R.id.rvGridPosts);
        // allows for optimizations
        rvPosts.setHasFixedSize(true);

        // Define 2 column grid layout
        final GridLayoutManager layout = new GridLayoutManager(getContext(), 3);
        rvPosts.setLayoutManager(layout);

        posts = new ArrayList<>();
        mAdapter = new PostsGridAdapter(getActivity(), posts);

        rvPosts.setAdapter(mAdapter);
        getUserPosts();


        btnLogout = getView().findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                if (ParseUser.getCurrentUser() == null) {
                    goLogin();
                }
            }
        });

        btnChangePic = getView().findViewById(R.id.btnChangePic);
        btnChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });
        loadUserPicture();
    }

    public void getUserPosts() {
        ParseQuery query = ParseQuery.getQuery(Post.class);
        String objectId = ParseUser.getCurrentUser().getObjectId();
        query.whereEqualTo("userString" , objectId);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                //check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                Log.d(TAG, Integer.toString(posts.size()));
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription());
                }
                mAdapter.clear();
                mAdapter.addAll(posts);
            }
        });
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);
        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {
                //Get screenWidth for scaling
                // by this point we have the camera photo on disk
                Bitmap rawImage = rotateBitmapOrientation(photoFile.getAbsolutePath());

                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                rawImage.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

                //Update user_picture in parse for current user
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
                    @Override
                    public void done(ParseUser object, ParseException e) {
                        if (e == null) {
                            byte[] bitmapBytes = bytes.toByteArray();
                            ParseFile photo = new ParseFile("File image", bitmapBytes);
                            object.put("profile_pic", photo);
                            object.saveInBackground();
                            loadUserPicture();
                        }
                    }
                });
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void loadUserPicture(){
        ImageView ivUserPic = getView().findViewById(R.id.ivUserPic);
        tvUser = getView().findViewById(R.id.tvUser);
        String objectId = ParseUser.getCurrentUser().getObjectId();
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.include("profile_pic");
        query.whereEqualTo("objectId", objectId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                //Check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting user", e);
                    return;
                }
                ParseFile imageUser = (ParseFile) objects.get(0).getParseFile("profile_pic");
                tvUser.setText(objects.get(0).getUsername());
                int margin = 20;
                int radius = 100;
                if (imageUser != null) {
                    Log.d(TAG, "Setting photo");
                    ivUserPic.setImageResource(0);
                    Glide.with(getContext()).load(imageUser.getUrl()).circleCrop().into(ivUserPic);
                }
            }
        });
    }

    private void goLogin() {
        Intent i = new Intent(this.getContext(), LoginActivity.class);
        startActivity(i);
    }
}