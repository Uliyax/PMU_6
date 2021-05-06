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
        }
        else {
            fDatabase = FirebaseDatabase.getInstance().getReference();
            fDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        //postsAdapter = new PostsAdapter(posts, this);

    }





}