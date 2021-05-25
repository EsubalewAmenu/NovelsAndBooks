package com.herma.apps.novelsandbooks.usefull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bookmarks.db";
    public static final String CONTACTS_TABLE_NAME = "bookmarks";
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE `bookmarks` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT, `realId` INTEGER, `blogposts_count` INTEGER, `writerName` TEXT, `blogwriter_id` INTEGER, `categoryName` TEXT, `chapterName` TEXT, `content` TEXT )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS bookmarks");
        onCreate(db);
    }

    public boolean addBookmark (int realId, int blogposts_count, String writerName, int blogwriter_id, String categoryName, String chapterName, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("realId", realId);
        contentValues.put("blogposts_count", blogposts_count);
        contentValues.put("writerName", writerName);
        contentValues.put("blogwriter_id", blogwriter_id);
        contentValues.put("categoryName", categoryName);
        contentValues.put("chapterName", chapterName);
        contentValues.put("content", content);
        db.insert("bookmarks", null, contentValues);
        return true;
    }

    public Cursor getBookmark(int realId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from bookmarks where realId="+realId+"", null );
        return res;
    }
    public boolean isBookmarked(int realId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from bookmarks where realId="+realId+"", null );
        return res.moveToFirst();
    }

    public Integer removeBookmark (int realId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("bookmarks",
                "realId = ? ",
                new String[] { Integer.toString(realId) });
    }

    public ArrayList<Object> getAllBookmarks() {
        ArrayList<Object> array_list = new ArrayList<Object>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from bookmarks", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            PostItem postItem = new PostItem();
            postItem.setId(res.getInt(res.getColumnIndex("realId")));
            postItem.setRealId(res.getInt(res.getColumnIndex("realId")));
            postItem.setBlogposts_count(res.getInt(res.getColumnIndex("blogposts_count")));
            postItem.setBlogwriter_name(res.getString(res.getColumnIndex("writerName")));
            postItem.setBlogwriter_id(res.getInt(res.getColumnIndex("blogwriter_id")));
            postItem.setCategoryName(res.getString(res.getColumnIndex("categoryName")));
            postItem.setChapterName(res.getString(res.getColumnIndex("chapterName")));
            postItem.setContent(res.getString(res.getColumnIndex("content")));

            array_list.add(postItem);
            res.moveToNext();
        }
        return array_list;
    }
}