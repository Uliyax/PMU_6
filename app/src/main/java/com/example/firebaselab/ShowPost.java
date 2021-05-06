package com.example.firebaselab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.example.firebaselab.MainActivity.db;
import static com.example.firebaselab.MainActivity.fDatabase;

public class ShowPost extends Fragment {
    MediaPlayer mediaPlayer;
    boolean called = false;

    String title_;
    String description_;
    String imagePath_;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_post, container, false);
        final TextView title = view.findViewById(R.id.tv_title);
        final TextView description = view.findViewById(R.id.tv_description);
        final ImageView bitmap = view.findViewById(R.id.iv_show_post_image);
        final Button removePostButton = view.findViewById(R.id.b_remove_post);
        final Button showOnMapButton = view.findViewById(R.id.b_show_on_map);
        final Button saveDescription = view.findViewById(R.id.b_save_description);
        final EditText editDescription = view.findViewById(R.id.et_edit_description);
        ImageView musicMarker = view.findViewById(R.id.iv_music_marker_show_post);
        final int position = this.getArguments().getInt("position");


            removePostButton.setVisibility(View.GONE);
            description.setClickable(false);




        title.setText(this.getArguments().getString("title"));
        title_ = this.getArguments().getString("title");
        if (this.getArguments().containsKey("description")) {
            description_ = this.getArguments().getString("description");
            if (description_ == null || description_.isEmpty())
                description_ = "null";
            description.setText(this.getArguments().getString("description"));
        }
        if (this.getArguments().containsKey("imageUrl")) {
            try {
                LoadImageURL liurl = new LoadImageURL();
                Bitmap b = (Bitmap) liurl.execute(this.getArguments().getString("imageUrl")).get();
                bitmap.setImageBitmap(b);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.getArguments().containsKey("imagePath")) {
            if (this.getArguments().getString("imagePath").startsWith("https")) {
                try {
                    LoadImageURL liurl = new LoadImageURL();
                    Bitmap b = (Bitmap) liurl.execute(this.getArguments().getString("imagePath")).get();
                    bitmap.setImageBitmap(b);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                imagePath_ = this.getArguments().getString("imagePath");
                if (imagePath_ == null || imagePath_.isEmpty())
                    imagePath_ = "null";
                bitmap.setImageURI(Uri.fromFile(new File(this.getArguments().getString("imagePath"))));
            }
        }
        if (this.getArguments().containsKey("songPath")) {
            //Uri songUri = Uri.parse(this.getArguments().getString("songUri"));
            String path = this.getArguments().getString("songPath");
            musicMarker.setVisibility(View.VISIBLE);
            //Bitmap bitmap1 = getSongPicture(getContext(), songUri);
            Bitmap bitmap1 = AddPost.getSongPicture(path);
            bitmap.setImageBitmap(bitmap1);

            try {
                attachSongToImageView(bitmap, path);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        resizePicture(bitmap);


            description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    description.setVisibility(View.GONE);
                    editDescription.setVisibility(View.VISIBLE);
                    editDescription.setText(description.getText().toString());
                    saveDescription.setVisibility(View.VISIBLE);
                    bitmap.setVisibility(View.GONE);
                }
            });

        saveDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editDescription.setVisibility(View.GONE);
                description.setVisibility(View.VISIBLE);
                description.setText(editDescription.getText().toString());
                saveDescription.setVisibility(View.GONE);
                bitmap.setVisibility(View.VISIBLE);
                if (!MainActivity.sqlite) {
                    String t_d_ip = title_ + "_" + description_ + "_" + imagePath_;
                    Query query = fDatabase.child("Posts").orderByChild("t_d_ip").equalTo(t_d_ip);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot descriptionSnapshot : dataSnapshot.getChildren()) {
                                descriptionSnapshot.getRef().child("description").setValue(editDescription.getText().toString());
                                descriptionSnapshot.getRef().child("t_d_ip").setValue(title_ + "_" + editDescription.getText().toString() + "_" + imagePath_);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    db.execSQL("UPDATE posts SET description = '" + editDescription.getText().toString() + "' WHERE " +
                            "description = '" + description_ + "' AND title = '" + title_ +"'");
                    startActivity(new Intent(getContext(), MainActivity.class).putExtra("update", "true"));
                }

            }
        });



        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().getSupportFragmentManager().beginTransaction().hide(ShowPost.this).commit();
                getActivity().findViewById(R.id.fab).setVisibility(View.VISIBLE);
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer = null;
                }

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        removePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                Log.d("ShowPost", "position: " + position);
                intent.putExtra("position", position);
                intent.putExtra("title", title.getText().toString());
                intent.putExtra("description", description.getText().toString());
                intent.putExtra("t_d_ip", title_ + "_" + description_ + "_" + imagePath_);
                startActivity(intent);
            }
        });
        showOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MapsActivity.class));
            }
        });
        return view;
    }
    private void attachSongToImageView(ImageView iv, Uri songUri) throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
    private void attachSongToImageView(ImageView iv, String songPath) throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(songPath.replace("document", "storage").replace(":", "/"));
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
    private Bitmap getSongPicture(Context context, Uri songUri) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(context, songUri);
        Bitmap bitmap;
        byte[] data = metadataRetriever.getEmbeddedPicture();
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }


    public void resizePicture(ImageView iv) {
        if (iv.getDrawable() != null) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point displaySize = new Point();
            display.getRealSize(displaySize);
            int imageWidth = iv.getDrawable().getIntrinsicWidth();
            float multiplier = (float) displaySize.x / imageWidth;
            ViewGroup.LayoutParams layoutParams = iv.getLayoutParams();
            layoutParams.width = displaySize.x;
            layoutParams.height = (int) (iv.getDrawable().getIntrinsicHeight() * multiplier);
            iv.setLayoutParams(layoutParams);
        }
    }
    public void removeFromXml(int position) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        FileInputStream fis = getContext().openFileInput("Posts.xml");

        List<InputStream> streams = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()),
                fis,
                new ByteArrayInputStream("</root>".getBytes()));
        InputStream cntr = new SequenceInputStream(Collections.enumeration(streams));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc;
        if (!called) {
            doc = db.parse(cntr);
            called = true;
        }
        else {
            doc = db.parse(fis);
        }
        int l = doc.getElementsByTagName("post").getLength();
        Element element = (Element) doc.getElementsByTagName("post").item(l - position - 1);
        element.getParentNode().removeChild(element);


        NodeList nodes = doc.getElementsByTagName("post");
        Document doc1 = db.newDocument();


        doc.normalize();
        prettyPrint(doc);
        overwriteXmlFile(doc);
    }

    public final void prettyPrint(Document xml) throws TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
    }
    public void overwriteXmlFile(Document doc) throws FileNotFoundException, TransformerException {
        File file = new File(getContext().getFilesDir(), "Posts.xml");
        StreamResult result = new StreamResult(new PrintWriter(new FileOutputStream(file, false)));
        DOMSource source = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }
    public final void prettyPrintWithourRoot() throws TransformerException, IOException, ParserConfigurationException, SAXException {
        FileInputStream fis = getContext().openFileInput("Posts.xml");

        List<InputStream> streams = Arrays.asList(new ByteArrayInputStream("<root>".getBytes()),
                fis,
                new ByteArrayInputStream("</root>".getBytes()));
        InputStream cntr = new SequenceInputStream(Collections.enumeration(streams));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc;
        doc = db.parse(cntr);

        overwriteXmlFile(doc);

        doc.normalize();
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(out));
    }


    private static class LoadImageURL extends AsyncTask {
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
