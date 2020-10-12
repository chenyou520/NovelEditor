package com.chenyou.noveleditor.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class BookDatabase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "books";//用书籍名称作为表名
    public static final String BOOK_ICON = "book_icon";//书籍封面图片的路径
    public static final String BOOK_NAME = "book_name";//用书籍名称作为表名
    public static final String BOOK_NEWCHAPTER = "book_newchapter";//最新章节名称
    public static final String BOOK_DATE = "book_date";//日期
    public static final String BOOK_PATH = "book_path";//书籍保存路径
    public static final String ID = "_id";//表格的id,主键的字符必须子在前面加上_
    private Context context;

    /**
     * 方式1
     * 创建表格
     */
    public static final String CREATE_BOOK = "CREATE TABLE " + TABLE_NAME + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + BOOK_ICON + " TEXT,"
            + BOOK_NAME + " TEXT,"
            + BOOK_NEWCHAPTER + " TEXT,"
            + BOOK_DATE + " TEXT,"
            + BOOK_PATH + " TEXT )";


    public BookDatabase(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
