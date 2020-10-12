package com.chenyou.noveleditor.data;

/**
 * 保存书籍信息的类，并且有Parcelable序列化，可用于Intent对象传输
 */
public class BookData {
    private String bookIcon;//书籍封面图片的路径
    private String bookName;//书籍名称
    private String bookNewchapter;//最新章节名称
    private String bookDate;//日期
    private String bookPath;//书籍保存路径
    private long bookId;//用于表格保存书籍的id

    public BookData() {

    }

    public BookData(String bookIcon, String bookName, String bookNewchapter, String bookDate, String bookPath) {
        this.bookIcon = bookIcon;
        this.bookName = bookName;
        this.bookNewchapter = bookNewchapter;
        this.bookDate = bookDate;
        this.bookPath = bookPath;
    }

    public String getBookIcon() {
        return bookIcon;
    }

    public void setBookIcon(String bookIcon) {
        this.bookIcon = bookIcon;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookNewchapter() {
        return bookNewchapter;
    }

    public void setBookNewchapter(String bookNewchapter) {
        this.bookNewchapter = bookNewchapter;
    }

    public String getBookDate() {
        return bookDate;
    }

    public void setBookDate(String bookDate) {
        this.bookDate = bookDate;
    }

    public String getBookPath() {
        return bookPath;
    }

    public void setBookPath(String bookPath) {
        this.bookPath = bookPath;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

}
