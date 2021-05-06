package com.example.firebaselab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements PostsAdapter.OnPostListener{

    public static boolean sqlite = true;
    public static boolean admin = false;


    private RecyclerView postsList;
    private PostsAdapter postsAdapter;
    private Fragment fragment;
    FloatingActionButton fab;

    public static SQLiteDatabase db;
    public static DatabaseReference fDatabase;

    public static Point size;

    public List<Post> posts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = getApplicationContext();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        size = getDisplaySize();
        posts = new ArrayList<>();

        postsList = findViewById(R.id.rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        postsList.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        if (intent.hasExtra("database")) {
            String database = intent.getStringExtra("database");
            if (database.equals("firebase"))
                sqlite = false;
            else
                sqlite = true;
        }



        if (sqlite) {

            db = new PostsSQLite.PostsReaderDbHelper(context).getWritableDatabase();
            try {
                loadPostsFromSQlite();
            } catch (MalformedURLException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            if (intent.hasExtra("update")) {
                try {
                    loadPostsFromSQlite();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (intent.hasExtra("position")) {
                int pos = intent.getIntExtra("position", -1);
                for(Post p : posts) {
                    Log.d("post before:" , p.title);
                }
                Log.d("size before:", posts.size() + "");
                posts.remove(pos);
                db.execSQL("DELETE FROM posts");
                Log.d("size after:", posts.size() + "");
                Collections.reverse(posts);
                for(Post p : posts) {
                    ContentValues values = new ContentValues();
                    values.put(PostsSQLite.PostsEntry.COLUMN_NAME_TITLE, p.title);
                    if (p.getDescription() != null)
                        values.put(PostsSQLite.PostsEntry.COLUMN_NAME_DESCRIPTION, p.description);
                    if (p.getImagePath() != null)
                        values.put(PostsSQLite.PostsEntry.COLUMN_NAME_IMAGE_PATH, p.getImagePath());
                    long newRowId = db.insert(PostsSQLite.PostsEntry.TABLE_NAME, null, values);
                }
                posts.clear();
                try {
                    loadPostsFromSQlite();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            fDatabase = FirebaseDatabase.getInstance().getReference();
            fDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loadPostsFromFirebase();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



            loadPostsFromFirebase();


            if (intent.hasExtra("position")) {
                String t_d_ip = intent.getStringExtra("t_d_ip");
                fDatabase.child("Posts");

                Query query = fDatabase.child("Posts").orderByChild("t_d_ip").equalTo(t_d_ip);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot numberSnapshot : dataSnapshot.getChildren()) {
                            numberSnapshot.getRef().removeValue();
                            posts.clear();
                            loadPostsFromFirebase();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            else {
                loadPostsFromFirebase();
            }

        }


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragment = new AddPost();
                fragmentTransaction.add(R.id.host_activity, fragment);
                fragmentTransaction.commit();
                fab.setVisibility(View.INVISIBLE);
            }
        });

        //postsAdapter = new PostsAdapter(posts, this);

    }


    public Point getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    public void addPost(Post post){
        posts.add(0, post);
        postsAdapter.notifyItemInserted(0);
        postsList.smoothScrollToPosition(0);
    }

    @Override
    public void onPostClick(int position) {
        Post post = posts.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putString("title", post.title);
        bundle.putString("description", post.description);
        if (post.getImagePath() != null)
            bundle.putString("imagePath", post.imagePath);
        if (post.getSongPath() != null)
            bundle.putString("songPath", post.songPath);
        if (!post.getImageUrl().isEmpty()) {
            bundle.putString("imageUrl", post.imageUrl);
        }
        ShowPost postFragment = new ShowPost();
        postFragment.setArguments(bundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.host_activity, postFragment);
        fragmentTransaction.commit();
        fab.setVisibility(View.INVISIBLE);
    }

    void reloadPostsFromSQLite() {
        posts.clear();
        String[] projection = {
                PostsSQLite.PostsEntry.COLUMN_NAME_TITLE,
                PostsSQLite.PostsEntry.COLUMN_NAME_DESCRIPTION,
                PostsSQLite.PostsEntry.COLUMN_NAME_IMAGE_PATH
        };

        Cursor cursor = db.query(PostsSQLite.PostsEntry.TABLE_NAME, projection, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Post p = new Post();
            p.title = cursor.getString(0);
            p.description = cursor.getString(1);
            if (cursor.getString(2) != null) {
                p.setImagePath(cursor.getString(2));
            }
            posts.add(p);
        }
        cursor.close();
    }
    void loadPostsFromSQlite() throws MalformedURLException, ExecutionException, InterruptedException {
        List<Post> postsFromSQL = new ArrayList<>();
        String[] projection = {
                PostsSQLite.PostsEntry.COLUMN_NAME_TITLE,
                PostsSQLite.PostsEntry.COLUMN_NAME_DESCRIPTION,
                PostsSQLite.PostsEntry.COLUMN_NAME_IMAGE_PATH
        };

        Cursor cursor = db.query(PostsSQLite.PostsEntry.TABLE_NAME, projection, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Post p = new Post();
            p.title = cursor.getString(0);
            p.description = cursor.getString(1);
            if (cursor.getString(2) != null) {
                p.setImagePath(cursor.getString(2));
            }
            postsFromSQL.add(0, p);
        }
        cursor.close();
        if (postsFromSQL.isEmpty()) {
            GetRSS getRSS = new GetRSS();
            getRSS.execute().get();
            List<Post> newPosts = getRSS.rssPosts;

            for (Post p : newPosts) {
                ContentValues values = new ContentValues();
                values.put(PostsSQLite.PostsEntry.COLUMN_NAME_TITLE, p.title);
                if (!p.description.isEmpty())
                    values.put(PostsSQLite.PostsEntry.COLUMN_NAME_DESCRIPTION, p.description);
                if (!p.getImageUrl().isEmpty())
                    values.put(PostsSQLite.PostsEntry.COLUMN_NAME_IMAGE_PATH, p.getImageUrl());
                long newRowId = db.insert(PostsSQLite.PostsEntry.TABLE_NAME, null, values);
                postsFromSQL.add(0, p);
            }

        }
        posts = postsFromSQL;
        postsAdapter = new PostsAdapter(posts, MainActivity.this);
        postsList.setAdapter(postsAdapter);
    }

    void initFireDatabase() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                posts.add(0, post);
                postsAdapter = new PostsAdapter(posts, MainActivity.this);
                postsList.setAdapter(postsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                return;
            }
        };
        fDatabase.addValueEventListener(postListener);
    }


    void loadPostsFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Post> postsFromDb = collectPosts((Map<String,Object>) dataSnapshot.getValue());


                posts = postsFromDb;
                postsAdapter = new PostsAdapter(posts, MainActivity.this);
                postsList.setAdapter(postsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    List collectPosts(Map<String, Object> postsFromDb) {
        List<Post> ps = new ArrayList<>();
        if (postsFromDb != null) {
            for (Map.Entry<String, Object> entry : postsFromDb.entrySet()) {
                Post p = new Post();

                Map singlePost = (Map) entry.getValue();
                String title = singlePost.get("title").toString();
                String description = null;
                String imagePath = null;
                if (singlePost.get("description") != null)
                    description = singlePost.get("description").toString();
                if (singlePost.get("imagePath") != null)
                    imagePath = singlePost.get("imagePath").toString();
                p.title = title;
                p.description = description;
                p.imagePath = imagePath;
                p.setNumber(singlePost.get("number").toString());
                //posts.add(p);
                ps.add(p);
            }
        }
        Collections.sort(ps);
        Collections.reverse(ps);
        return ps;
    }

}
class GetRSS extends AsyncTask {
    URL url;
    List<Post> rssPosts = new ArrayList<>();
    protected Object doInBackground(Object[] objects) {
        try {
            loadRSSfeed();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
    void loadRSSfeed() throws MalformedURLException {
        URL url = new URL("https://lenta.ru/rss");
        int counter = 0;
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(url.openConnection().getInputStream(), "UTF-8");
            boolean insideItem = false;
            Post p = new Post();
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        p = new Post();
                        insideItem = true;
                    }
                    else if (xpp.getName().equalsIgnoreCase("title")) {
                        if (insideItem) {
                            String title_= xpp.nextText();
                            p.setTitle(title_);
                        }
                    }
                    else if (xpp.getName().equalsIgnoreCase("description")) {
                        if (insideItem) {
                            String description_ = xpp.nextText();
                            p.setDescription(description_);
                        }
                    }
                    else if (xpp.getName().equalsIgnoreCase("enclosure")){
                        if (insideItem) {
                            String imgUrl = xpp.getAttributeValue(null, "url");
                            p.setImageUrl(imgUrl);
                        }
                    }
                }
                else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                    insideItem = false;
                    counter++;
                    rssPosts.add(0, p);
                    if (counter > 13)
                        return;
                }
                eventType = xpp.next();

            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }





}