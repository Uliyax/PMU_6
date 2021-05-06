package com.example.firebaselab;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder>{

    public List<Post> posts;
    private OnPostListener onPostListener;
    private Context context;

    public List getPosts() {
        return posts;
    }

    public void removePost(int position) throws ParserConfigurationException {
        posts.remove(position);

    }
    PostsAdapter(List<Post> posts, OnPostListener onPostListener) {
        this.posts = posts;
        this.onPostListener = onPostListener;
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.post_list_item;

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        PostsViewHolder viewHolder = new PostsViewHolder(view, onPostListener);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostsViewHolder holder, int position) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions((Activity) context, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 5);
        //holder.bind(position);
        Post post = posts.get(position);
        holder.listItemPostTitle.setText(post.getTitle());
        holder.listItemPostDescription.setText(post.description);
        //holder.imagePreview.setImageURI(Uri.fromFile(new File(post.imagePath)));
        //holder.imagePreview.setImageBitmap(BitmapFactory.decodeFile(post.imagePath));
        if (!post.getImageUrl().isEmpty()){

            try {
                URL url = new URL(post.getImageUrl());
                LoadImageURL liurl = new LoadImageURL();
                Bitmap bmp = (Bitmap) liurl.execute(url).get();
                holder.imagePreview.setImageBitmap(bmp);
                bmp = null;
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        if (post.getImagePath() != null) {
            if (post.getImagePath().startsWith("https")) {
                try {
                    URL url = new URL(post.getImagePath());
                    LoadImageURL liurl = new LoadImageURL();
                    Bitmap bmp = (Bitmap) liurl.execute(url).get();
                    holder.imagePreview.setImageBitmap(bmp);
                    bmp = null;
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            else
                if (!post.getImagePath().isEmpty())
                    holder.imagePreview.setImageURI(Uri.fromFile(new File(post.imagePath)));
            //holder.imagePreview.setImageBitmap(BitmapFactory.decodeFile(post.imagePath));
        }

        if (!post.songPath.isEmpty()) {
            holder.musicMarker.setVisibility(View.VISIBLE);
//            holder.imagePreview.setImageBitmap(AddPost.getSongPicture(context, Uri.fromFile(new File(post.songPath))));

            holder.imagePreview.setImageBitmap(AddPost.getSongPicture(post.songPath.replace("document", "storage").replace(":", "/")));

        }

    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    void saveData(Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("posts.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write("STRING");
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView listItemPostTitle;
        TextView listItemPostDescription;
        ImageView imagePreview;
        ImageView musicMarker;
        Uri imageUri;

        FrameLayout frameLayout;


        OnPostListener onPostListener;

        Point displaySize = MainActivity.size;

        PostsViewHolder(View itemView, OnPostListener onPostListener) {
            super(itemView);
            listItemPostTitle = itemView.findViewById(R.id.tv_post_title);
            listItemPostDescription = itemView.findViewById(R.id.tv_post_text);
            imagePreview = itemView.findViewById(R.id.iv_post_list_preview);
            musicMarker = itemView.findViewById(R.id.iv_post_list_music_marker);
            frameLayout = itemView.findViewById(R.id.fl);

            this.onPostListener = onPostListener;
            itemView.setOnClickListener(this);




            setSize(frameLayout);
            setSize(listItemPostTitle);
            setSize(listItemPostDescription);
            setSize(imagePreview);


        }

        public void bind(int position) {
            //listItemPostTitle.setText(title);
            //listItemPostText.setText(description);
            //listItemPostView.setText(String.valueOf(listIndex));
        }

        void setSize(FrameLayout fl) {
            ViewGroup.LayoutParams lp = fl.getLayoutParams();
            lp.height = displaySize.y / 5;
        }
        void setSize(ImageView iv) {
            ViewGroup.LayoutParams lp = iv.getLayoutParams();
            lp.width = displaySize.y / 5;
            lp.height = lp.width;
            iv.setLayoutParams(lp);
        }
        void setSize(TextView tv) {
            int width = displaySize.x - displaySize.y / 5 - 20;
            ViewGroup.LayoutParams lp = tv.getLayoutParams();
            lp.width = width;
            tv.setLayoutParams(lp);
        }

        @Override
        public void onClick(View v) {
            onPostListener.onPostClick(getAdapterPosition());
        }
    }

    public interface OnPostListener{
        void onPostClick(int position);
    }




    private class LoadImageURL extends AsyncTask {
        @Override
        protected Bitmap doInBackground(Object[] objects) {
            Bitmap bmp = null;
            try {
                URL url = new URL(objects[0].toString());
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e){
                e.printStackTrace();
                bmp = null;
            }
            return bmp;
        }
    }
}
