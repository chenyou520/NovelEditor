package com.chenyou.noveleditor.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CRUD {
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    /**
     * 创建书籍表格
     */
    public CRUD(Context context) {
        dbHandler = new BookDatabase(context, "bookList.db", null, 1);
    }

    /**
     * 添加书籍
     *
     * @param bookData
     * @return
     */
    public BookData addBookData(BookData bookData) {
        db = dbHandler.getWritableDatabase();

        //插入到表格中，方式1
        db.execSQL("insert into books( book_icon, book_name,book_newchapter, book_date, book_path) values(?,?,?,?,?)",
                new Object[]{bookData.getBookIcon(), bookData.getBookName(), bookData.getBookNewchapter(), bookData.getBookDate(), bookData.getBookPath()});

        Cursor cursor = db.rawQuery("select * from " + BookDatabase.TABLE_NAME + " where " + BookDatabase.BOOK_NAME + " = " + "'" + bookData.getBookName() + "'", null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        bookData.setBookId(cursor.getLong(cursor.getColumnIndex(BookDatabase.ID)));
        cursor.close();
        dbHandler.close();
        System.out.println("增加方法执行完毕！");
        return bookData;

    }

    /**
     * 查询书籍
     *
     * @param id
     * @return
     */
    public BookData getBookData(long id) {
        db = dbHandler.getWritableDatabase();
        //查询书籍
        //使用游标索引从数据库中获取书籍信息
        Cursor cursor = db.rawQuery("select * from " + BookDatabase.TABLE_NAME + " where " + BookDatabase.BOOK_NAME + " = " + "'" + id + "'", null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        //保存书籍查询内容
        BookData bookData = new BookData(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5));
        cursor.close();
        dbHandler.close();
        return bookData;
    }

    /**
     * 查询所有书籍信息
     *
     * @return
     */
    public List<BookData> getAllBooks() {
        db = dbHandler.getWritableDatabase();
        //查询书籍
        List<BookData> bookDatas = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + BookDatabase.TABLE_NAME, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                //保存书籍查询内容
                BookData bookData1 = new BookData(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5));
                bookData1.setBookId(cursor.getLong(0));
                bookDatas.add(bookData1);
            }
        }
        cursor.close();
        dbHandler.close();
        return bookDatas;
    }

    /**
     * 修改书籍
     *
     * @param bookData
     * @return
     */
    public void updateBook(BookData bookData) {
        db = dbHandler.getWritableDatabase();

        //正在更新行
        //UPDATE table_name
        //SET column1 = value1, column2 = value2...., columnN = valueN
        //WHERE [condition];
        db.execSQL("update " + BookDatabase.TABLE_NAME
                + " set "
                + BookDatabase.BOOK_ICON + "=" + "'" + bookData.getBookIcon() + "'" + ","
                + BookDatabase.BOOK_NAME + "=" + "'" + bookData.getBookName() + "'" + ","
                + BookDatabase.BOOK_NEWCHAPTER + "=" + "'" + bookData.getBookNewchapter() + "'" + ","
                + BookDatabase.BOOK_DATE + "=" + "'" + bookData.getBookDate() + "'" + ","
                + BookDatabase.BOOK_PATH + "=" + "'" + bookData.getBookPath() + "'"
                + " where " + BookDatabase.ID + " = " + "'" + bookData.getBookId() + "'");

        dbHandler.close();
    }

    /**
     * 删除书籍
     *
     * @param bookData
     */
    public void removeBook(BookData bookData) {
        db = dbHandler.getWritableDatabase();
        //根据ID值删除书籍信息
        db.execSQL("delete from " + BookDatabase.TABLE_NAME + " where " + BookDatabase.ID + " = " + "'" + bookData.getBookId() + "'");
        dbHandler.close();
    }
}
