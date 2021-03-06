package com.chenyou.noveleditor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.data.BookData;
import com.chenyou.noveleditor.utils.BitmapUtils;
import com.chenyou.noveleditor.utils.Utils;

import java.util.List;

public class BookAdapter extends BaseAdapter {


    private List<BookData> bookDataList;
    private Context context;

    public BookAdapter(Context context, List<BookData> bookDataList) {
        this.bookDataList = bookDataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return bookDataList == null ? 0 : bookDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.book_item, null);
            viewHolder = new ViewHolder();

            viewHolder.bookIcon = (ImageView) convertView.findViewById(R.id.book_icon);
            viewHolder.bookName = (TextView) convertView.findViewById(R.id.book_name);
            viewHolder.bookNewchapter = (TextView) convertView.findViewById(R.id.book_newchapter);
            viewHolder.bookDate = (TextView) convertView.findViewById(R.id.book_date);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //更新书籍内容
        BookData bookData = bookDataList.get(position);
        viewHolder.bookName.setText(bookData.getBookName());
        viewHolder.bookDate.setText(bookData.getBookDate());


        if (bookData.getBookNewchapter()!=null) {
            viewHolder.bookNewchapter.setText("最新章节："+bookData.getBookNewchapter());
        } else {
            viewHolder.bookNewchapter.setText("请新建章节");
        }


        if (bookData.getBookIcon() != null) {//加载指定路径的图片
            Bitmap bitmap = BitmapFactory.decodeFile(bookData.getBookIcon());
            Glide.with(context).load(bitmap).centerCrop().into(viewHolder.bookIcon);
        } else {//默认封面
            Glide.with(context).load(R.drawable.add_pictrue).into(viewHolder.bookIcon);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView bookIcon;//封面图片
        TextView bookName;//书籍名称
        TextView bookNewchapter;//最新章节
        TextView bookDate;//更新时间
    }


}
