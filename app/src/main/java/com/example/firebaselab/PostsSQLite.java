package com.example.firebaselab;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class PostsSQLite {
    private PostsSQLite() { }

    public static class PostsEntry implements BaseColumns {
        public static final String TABLE_NAME = "posts";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_IMAGE_PATH = "imagePath";
        public static final String COLUMN_NAME_USER_ID = "userId";

        public static final String TABLE2_NAME = "users";
        public static final String COLUMN_NAME_LOGIN = "login";
    }
    private static final String SQL_CREATE_POSTS =
            "CREATE TABLE " + PostsEntry.TABLE_NAME + " (" +
                    PostsEntry._ID + " INTEGER PRIMARY KEY," +
                    PostsEntry.COLUMN_NAME_TITLE + " TEXT," +
                    PostsEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    PostsEntry.COLUMN_NAME_IMAGE_PATH + " TEXT," +
                    PostsEntry.COLUMN_NAME_USER_ID + " INTEGER," +
                    "FOREIGN KEY (" + PostsEntry.COLUMN_NAME_USER_ID + ") " +
                    "REFERENCES " + PostsEntry.TABLE2_NAME +"(" +
                    PostsEntry.COLUMN_NAME_USER_ID + "))";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + PostsEntry.TABLE2_NAME + " (" +
                    PostsEntry._ID + " INTEGER PRIMARY KEY," +
                    PostsEntry.COLUMN_NAME_LOGIN + " TEXT UNIQUE)";

    private static final String SQL_DELETE_POSTS =
            "DROP TABLE IF EXISTS " + PostsEntry.TABLE_NAME;

    private static final String SQL_DELETE_USERS =
            "DROP TABLE IF EXISTS " + PostsEntry.TABLE2_NAME;

    public static class PostsReaderDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "PostsReader.db";

        public PostsReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_USERS);
            db.execSQL(SQL_CREATE_POSTS);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_POSTS);
            db.execSQL(SQL_DELETE_USERS);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

}
