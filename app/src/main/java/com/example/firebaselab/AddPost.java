package com.example.firebaselab;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlSerializer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.firebaselab.MainActivity.db;
import static com.example.firebaselab.MainActivity.fDatabase;
import static com.example.firebaselab.MainActivity.sqlite;


public class AddPost extends Fragment {

    private ImageView previewImage;
    private ImageView musicMarker;
    private Bitmap bitmapImage;
    private Uri songUri = null;
    private Uri imageUri = null;
    private String imagePath;
    private String songPath;
    private MediaPlayer mediaPlayer;
//    private Button removeMedia;

    long maxId = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);


        final Context context = getActivity();

        final EditText title = view.findViewById(R.id.et_set_title);
        final EditText description = view.findViewById(R.id.et_set_description);

        previewImage = view.findViewById(R.id.iv_post_image_preview);
        musicMarker = view.findViewById(R.id.iv_music_marker_new_post);



        final Button selectMedia = view.findViewById(R.id.b_select_media);
//        removeMedia = view.findViewById(R.id.b_remove_media);
        Button createPost = view.findViewById(R.id.b_create_post);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        selectMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                removeMedia.setVisibility(View.INVISIBLE);
                musicMarker.setVisibility(View.INVISIBLE);
//                if (songUri != null)
//                    removeMedia.callOnClick();
                selectMedia(context);
            }
        });
//        removeMedia.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bitmapImage = null;
//                songUri = null;
//                previewImage.setImageResource(0);
//                mediaPlayer.reset();
//                musicMarker.setVisibility(View.INVISIBLE);
//                removeMedia.setVisibility(View.INVISIBLE);
//            }
//        });

        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (title.length() > 0) {
                    Post post = new Post(title.getText().toString(), description.getText().toString(), songUri, imagePath);

                    if (MainActivity.sqlite) {
                        ContentValues values = new ContentValues();
                        values.put(PostsSQLite.PostsEntry.COLUMN_NAME_TITLE, post.title);
                        if (!post.description.isEmpty())
                            values.put(PostsSQLite.PostsEntry.COLUMN_NAME_DESCRIPTION, post.description);
                        if (post.getImagePath() != null)
                            values.put(PostsSQLite.PostsEntry.COLUMN_NAME_IMAGE_PATH, post.getImagePath());
                        long newRowId = db.insert(PostsSQLite.PostsEntry.TABLE_NAME, null, values);
                    }
                    else {
                        DatabaseReference reff;
                        reff = fDatabase.child("Posts");
                        post.setNumber(String.valueOf(maxId  + 1));
                        post.setT_d_ip();
                        fDatabase.child("Posts").child("post" + String.valueOf(maxId + 1)).setValue(post);
//                        fDatabase.child("Posts").setValue(post);
                    }


//                    FirebaseDatabase.getInstance().getReference().push().setValue(post);


                    ((MainActivity) Objects.requireNonNull(getActivity())).addPost(post);
                    getActivity().getSupportFragmentManager().beginTransaction().hide(AddPost.this).commit();
                    getActivity().findViewById(R.id.fab).setVisibility(View.VISIBLE);
                    mediaPlayer.reset();
                }
                else {
                    title.setError("Пустое поле");
                }
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().getSupportFragmentManager().beginTransaction().hide(AddPost.this).commit();
                getActivity().findViewById(R.id.fab).setVisibility(View.VISIBLE);
                mediaPlayer.reset();

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        if (!sqlite) {
            fDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        maxId = (dataSnapshot.child("Posts").getChildrenCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        return view;
    }

    private void selectMedia(Context context) {
        final CharSequence[] options = {"Chose from Audios", "Chose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select media");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equals("Chose from Audios")) {
                    Intent selectAudio = new Intent(Intent.ACTION_GET_CONTENT);
                    selectAudio.setType("audio/*");
                    startActivityForResult(selectAudio, 1);
                }
                else if (options[which].equals("Chose from Gallery")) {
                    Intent selectPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(selectPicture, 2);
                }
                else  {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RESULT_CANCELED) {
            previewImage.setImageResource(0);
            switch (requestCode) {
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        songUri = data.getData();
                        bitmapImage = getSongPicture(getContext(), songUri);
                        songPath = songUri.getPath();

                        previewImage.setImageBitmap(bitmapImage);
                    }
                    break;
                case 2:
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            imageUri = data.getData();

                            Cursor cursor = getContext().getContentResolver().query(imageUri, null, null, null, null);
                            cursor.moveToFirst();
                            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                            cursor.close();


                            bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            previewImage.setImageBitmap(bitmapImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
//            if (previewImage.getDrawable() != null)
//                removeMedia.setVisibility(View.VISIBLE);
            resizePicture(previewImage);
            if (songUri != null) {
                try {
                    attachSongToImageView(previewImage, songUri);
                    musicMarker.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static Bitmap getSongPicture(Context context, Uri songUri) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(context, songUri);
        Bitmap bitmap;
        byte[] data = metadataRetriever.getEmbeddedPicture();
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }
    static Bitmap getSongPicture(String songPath) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        songPath = songPath.replace("document", "storage").replace(":", "/");

        metadataRetriever.setDataSource(songPath);
        Bitmap bitmap;
        byte[] data = metadataRetriever.getEmbeddedPicture();
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }
    private void attachSongToImageView(ImageView iv, Uri songUri) throws IOException {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(getActivity(), songUri);
        mediaPlayer.prepare();
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                else
                    mediaPlayer.pause();
            }
        });
    }
    private void resizePicture(ImageView iv) {
        if (iv.getDrawable() != null) {
            Point displaySize = getDisplaySize();
            int imageWidth = iv.getDrawable().getIntrinsicWidth();
            float multiplier = (float) displaySize.x / imageWidth;
            ViewGroup.LayoutParams layoutParams = iv.getLayoutParams();
            layoutParams.width = displaySize.x;
            layoutParams.height = (int) (iv.getDrawable().getIntrinsicHeight() * multiplier);
            iv.setLayoutParams(layoutParams);
        }
    }
    private Point getDisplaySize() {
        Display display = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void writeToXml(String title, String description, String imageUri, String songUri) {
        try {
            FileOutputStream fileos = getContext().openFileOutput("Posts.xml", Context.MODE_APPEND);
            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);

            xmlSerializer.startTag(null, "post");
            xmlSerializer.startTag(null, "title");
            xmlSerializer.text(title);
            xmlSerializer.endTag(null, "title");


            if (!description.isEmpty()) {
                xmlSerializer.startTag(null, "description");
                xmlSerializer.text(description);
                xmlSerializer.endTag(null, "description");
            }


            if (imageUri != null) {
                xmlSerializer.startTag(null, "imageUri");
                xmlSerializer.text(imageUri);
                xmlSerializer.endTag(null, "imageUri");
            }


            if (songUri != null) {
                xmlSerializer.startTag(null, "songUri");
                xmlSerializer.text(songUri);
                xmlSerializer.endTag(null, "songUri");
            }


            xmlSerializer.endTag(null, "post");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();
            fileos.write(dataWrite.getBytes());
            fileos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
