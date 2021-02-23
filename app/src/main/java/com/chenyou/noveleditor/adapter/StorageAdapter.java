package com.chenyou.noveleditor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chenyou.noveleditor.R;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 设置文件保存位置
 */
public class StorageAdapter extends BaseAdapter implements View.OnClickListener, AdapterView.OnItemClickListener {
    private String rootPath;//根目录
    private LayoutInflater mInflater;//布局管理
    private Bitmap mIcon3;//文件夹图片
    private Bitmap mIcon4;//文件图片
    private List<File> fileList;//文件列表
    private ListView listView;//FileBrowserActivity的listView视图
    private View header;//列表头部布局（返回根目录和返回上层目录）视图
    private View layoutReturnRoot;//返回根目录
    private View layoutReturnPre;//返回上层目录
    private TextView curPathTextView;//初始化进入的目录，默认目录
    private String suffix = "";//后缀
    private String currentDirPath;//当前目录路径
    private FileSelectListener listener;//文件选择监听

    public StorageAdapter(View fileSelectListView, String rootPath, String defaultPath) {
        //获取根目录
        this.rootPath = rootPath;
        //获取layoutFileSelectList（FileBrowserActivity）的上下文
        Context context = fileSelectListView.getContext();
        //添加（调用）FileBrowserActivity的布局
        mInflater = LayoutInflater.from(context);

        //设置图片
        mIcon3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_fodler);
        mIcon4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_file);

        //显示当前路径
        curPathTextView = (TextView) fileSelectListView.findViewById(R.id.curPath);
        //列表头部布局（返回根目录、返回上层目录）视图
        header = fileSelectListView.findViewById(R.id.layoutFileListHeader);
        //返回根目录
        layoutReturnRoot = fileSelectListView.findViewById(R.id.layoutReturnRoot);
        //返回上层目录
        layoutReturnPre = fileSelectListView.findViewById(R.id.layoutReturnPre);
        layoutReturnRoot.setOnClickListener(this);
        layoutReturnPre.setOnClickListener(this);

        //判断
        if (defaultPath != null && !defaultPath.isEmpty()) {
            getFileDir(defaultPath);
        } else {
            getFileDir(rootPath);
        }

        listView = (ListView) fileSelectListView.findViewById(R.id.list);
        listView.setAdapter(this);
        listView.setOnItemClickListener(this);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.file_item, null);
            viewHolder = new ViewHolder();
            viewHolder.text_filename = (TextView) convertView.findViewById(R.id.text_filename);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        File file = fileList.get(position);
        viewHolder.text_filename.setText(file.getName());
        if (file.isDirectory()) {
            viewHolder.icon.setImageBitmap(mIcon3);
        } else {
            viewHolder.icon.setImageBitmap(mIcon4);
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView text_filename;
    }

    /**
     * 点击监听
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutReturnRoot://返回根目录
                getFileDir(rootPath);
                break;
            case R.id.layoutReturnPre://返回上层目录
                getFileDir(new File(currentDirPath).getParent());
                break;
            default:
                break;
        }
    }

    /**
     * listView的item点击监听
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        File file = fileList.get(position);
        //检查表示此抽象路径名的文件是否是一个目录。
        if (file.isDirectory()) {
            getFileDir(file.getPath());
        } else {
            //选择文件
            if (listener != null) {
                listener.onFileSelect(file);
            }
        }
    }

    /**
     * 获取所选文件路径下的所有文件，并且更新到listview中
     *
     * @param filePath
     */
    private void getFileDir(String filePath) {
        File file = new File(filePath);
        //测试pathname是否应该包含在当前File目录中，符合则返回true。
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                //是文件而且文件后缀不为空
                if (pathname.isFile() && !suffix.isEmpty()) {
                    //返回文件名和后缀
                    return pathname.getName().endsWith(suffix);
                }
                return true;
            }
        });

        //将所有文件添加到fileList中
        fileList = Arrays.asList(files);
        //按名称排序（降序）
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                //o1是文件，o2是目录，返回1（目录在前，文件在后）
                if (o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                //o1是目录，o2是文件，返回-1（目录在前，文件在后））
                if (o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                //按照文件名排序
                return o1.getName().compareTo(o2.getName());
            }
        });

        //判断头部布局是否为空
        if (header != null) {
            //如果有根目录则显示，若没有则不显示
            header.setVisibility(filePath.equals(rootPath) ? View.GONE : View.VISIBLE);
        }

        //刷新布局
        notifyDataSetChanged();

        //判断当前路径显示框是否存在，若存在则显示文件目录
        if (curPathTextView != null) {
            curPathTextView.setText(filePath);
        }
        //设置当前目录路径为文件目录
        currentDirPath = filePath;
        ////选择目录
        if (listener != null) {
            listener.onDirSelect(file);
        }
    }

    /**
     * 文件选择监听接口
     */
    public interface FileSelectListener {
        //选择文件
        void onFileSelect(File selectedFile);

        //选择目录
        void onDirSelect(File selectedDir);
    }

    public void setOnFileSelectListener(FileSelectListener listener) {
        this.listener = listener;
    }
}
