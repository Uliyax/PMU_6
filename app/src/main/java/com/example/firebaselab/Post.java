package com.example.firebaselab;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;


public class Post implements Comparator<Post>, Comparable<Post> {
    public String title = "";
    public String description = "";


    public String songPath = "";

    public String imagePath = "";

    public String number = "";

    public String t_d_ip = "";

    public String imageUrl = "";

    public Post() {}
    public Post(String title, String description, Uri songUri, Uri imageUri) {
        this.title = title;
        this.description = description;
        if (songUri != null)
            this.songPath = songUri.getPath();
        if (imageUri != null)
            this.imagePath = imageUri.getPath();
    }

    public Post(String title, String description, Uri songUri, String imagePath) {
        this.title = title;
        this.description = description;
        if (songUri != null)
            this.songPath = songUri.getPath();
        if (imagePath != null)
            this.imagePath = imagePath;
    }

    public Post(String title, String description) {
        this.title = title;
        this.description = description;
    }
    public Post(String title) {
        this.title = title;
    }

    void save(Context context) throws IOException {
        String filename = "myfile";
        String fileContents = "Hello World!";
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(this.description.getBytes());
        }
    }
    void open(Context context) throws FileNotFoundException {
        FileInputStream fis = context.openFileInput("myfile");
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            String contents = stringBuilder.toString();
        }
    }


    public String getTitle() {
        if (title.isEmpty())
            return null;
        return title;
    }
    public String getDescription() {
        if (description == null || description.isEmpty())
            return null;
        return description;
    }
    public String getSongPath() {
        if (songPath.isEmpty())
            return null;
        return songPath;
    }
    public String getImagePath() {
        if (imagePath == null || imagePath.isEmpty())
            return null;
        return imagePath;
    }
    public String getNumber() {
        return number;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String url) {
        this.imageUrl = url;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    void setTitle(String title) {
        this.title = title;
    }
    void setDescription(String description) {
        this.description = description;
    }
    void setSongPath(String songPath) {
        this.songPath = songPath;
    }
    void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    void setT_d_ip() {
        this.t_d_ip = this.getTitle() + "_" + this.getDescription() + "_" + this.getImagePath();
    }
    String getT_d_ip() {
        return this.t_d_ip;
    }

    public String toString() {
        return "Title: " + this.title + '\n' +
                "Description: " + this.description + '\n' +
                "ImageUri: " + this.getImagePath() + '\n' +
                "SongUri: " + this.getSongPath()  + '.';
    }

    public int compareTo(Post p) {
        return this.number.compareTo(p.number);
    }
    public int compare(Post p, Post p1) {
        return Integer.parseInt(p.number) - Integer.parseInt(p1.number);
    }
}
