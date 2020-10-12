package com.chenyou.noveleditor.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.chenyou.noveleditor.R;
import com.chenyou.noveleditor.adapter.BookAdapter;
import com.chenyou.noveleditor.data.BookData;
import com.chenyou.noveleditor.data.CRUD;
import com.chenyou.noveleditor.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final int BOOK_RESULT_CODE = 100;//单击返回码
    private static final int PICTURE = 200;//图库返回码
    private static final int CAMERA = 300;//相机返回码

    private Toolbar main_toolbar;//标题栏
    private ListView main_listview;//书籍列表
    private String filePath;

    private PopupWindow popupWindow;//弹出菜单
    private DisplayMetrics metrics;//获取分辨率
    private WindowManager windowManager;//窗口管理器

    private int itemposition;//长按listview的item的position

    private List<BookData> bookDataList;//存放书籍的List
    private BookAdapter bookAdapter;
    private Utils utils;
    private Context context = this;

    private int width;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();

        if (getSPrefs() == null) {
            File file = getExternalFilesDir("");
            String defaultfilepath = file.toString();
            filePath = defaultfilepath;
        } else {
            filePath = getSPrefs();
        }
        refreshBookListView();
        setToolbar();
    }

    /**
     * 获取保存路径
     *
     * @return
     */
    private String getSPrefs() {
        SharedPreferences pref = getSharedPreferences("bookpath", MODE_PRIVATE);
        String filePath = pref.getString("filepath", null);
        return filePath;
    }


    /**
     * 初始化布局
     */
    private void initview() {
        main_listview = (ListView) findViewById(R.id.main_listview);
        main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        bookDataList = new ArrayList<>();
        utils = new Utils();
        //设置适配器
        bookAdapter = new BookAdapter(MainActivity.this, bookDataList);
        main_listview.setAdapter(bookAdapter);
        main_listview.setOnItemClickListener(this);
        main_listview.setOnItemLongClickListener(this);

    }


    /**
     * 刷新书籍显示页面
     */
    private void refreshBookListView() {
        //数据库中获取所有书籍数据
        //创建表格
        CRUD op = new CRUD(context);

        //清除List中存放的书籍信息
        if (bookDataList.size() > 0) {
            bookDataList.clear();
        }
        //把从数据库表格中获取的书籍信息添加到List中
        bookDataList.addAll(op.getAllBooks());

        bookAdapter.notifyDataSetChanged();
    }

    /**
     * 设置标题栏
     */
    private void setToolbar() {
        //把默认标题去掉
        main_toolbar.setTitle("");
        //取代原本的actionbar
        setSupportActionBar(main_toolbar);
        //决定左上角的图标是否可以点击。没有向左的小图标。 true 图标可以点击  false 不可以点击。
        getSupportActionBar().setHomeButtonEnabled(true);
        main_toolbar.setNavigationIcon(R.drawable.ic_settings_black_24dp);
        main_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示菜单
                showPopUpView();
            }
        });

    }

    /**
     * 显示弹出菜单
     */
    private void showPopUpView() {
        windowManager = getWindowManager();
        metrics = new DisplayMetrics();
        // 获取屏幕大小
        windowManager.getDefaultDisplay().getMetrics(metrics);
        //获取手机分辨率
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        //获取弹出菜单布局
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.main_popupview, null);
        //加载设置popupwindow布局
        popupWindow = new PopupWindow(contentView, (int) (width * 0.5), height, true);
        popupWindow.setContentView(contentView);
        //设置弹出菜单的背景颜色
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        TextView main_pop_savepath = contentView.findViewById(R.id.main_pop_savepath);
        main_pop_savepath.setOnClickListener(this);

        //获取主页面布局
        View rootview = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
        //设置弹出菜单加载位置
        popupWindow.showAtLocation(rootview, Gravity.NO_GRAVITY, 0, 0);
    }

    /**
     * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
     * (只会在第一次初始化菜单时调用)
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 绑定toobar跟menu
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 在onCreateOptionsMenu执行后，菜单被显示前调用；如果菜单已经被创建，则在菜单显示前被调用。 同样的，
     * 返回true则显示该menu,false 则不显示; （可以通过此方法动态的改变菜单的状态，比如加载不同的菜单等）
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog.Builder builder;
        switch (item.getItemId()) {
            case R.id.menu_new://创建新书
                final EditText editText = new EditText(this);
                editText.setHint("请输入新书名字");
                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("创建新书")
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //这里trim()作用是去掉首位空格，防止不必要的错误
                                String str = editText.getText().toString().trim();
                                if (!str.equals("")) {
                                    createBook(editText);//创建新书

                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 创建新书
     *
     * @param editText
     */
    private void createBook(EditText editText) {

        //设置新书信息
        String bookName = editText.getText().toString();
        String bookDate = utils.getTime();
        String bookPath = filePath + "/" + editText.getText().toString();
        String bookIcon = null;
        String bookNewchapter = null;

        //创建新书文件夹
        File file = new File(bookPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        BookData newbookData = new BookData(bookIcon, bookName, bookNewchapter, bookDate, bookPath);


        //把新书的信息插入书单中
        CRUD op = new CRUD(context);
        op.addBookData(newbookData);

        refreshBookListView();//刷新页面
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_pop_savepath://设置存储路径
//                Toast.makeText(MainActivity.this, "设置存储路径", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, StorageActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 书籍单击事件（listview的item单击事件）
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        itemposition = position;
        BookData bookData = bookDataList.get(itemposition);
        Intent intent = new Intent(MainActivity.this, ChapterActivity.class);
        //需要修改的内容
        intent.putExtra("newchapter", bookData.getBookNewchapter());
        intent.putExtra("bookname", bookData.getBookName());
        intent.putExtra("bookpath", bookData.getBookPath());
        startActivityForResult(intent, BOOK_RESULT_CODE);
    }

    /**
     * 书籍长按事件（listview的item长按事件）
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        itemposition = position;
        main_listview.setOnCreateContextMenuListener(this);
        return false;
    }

    /**
     * 创建Listview的item长按事件弹出菜单
     *
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(1, 1000, 0, "删除书籍");

        menu.add(1, 1001, 1, "更改书名");

        menu.add(1, 1002, 2, "更换封面");

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * 弹出菜单选项
     *
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        //获取书籍信息
        final BookData bookData = bookDataList.get(itemposition);
        switch (item.getItemId()) {
            case 1000://删除书籍
                //删除保存的书籍文件夹
                utils.deleteDirectory(bookData.getBookPath());

                //把书单中的指定书籍删除
                CRUD op = new CRUD(context);
                op.removeBook(bookData);

                //刷新页面
                refreshBookListView();
                break;

            case 1001://更改书名
                final EditText editText = new EditText(this);
                editText.setHint("请输入新的书名");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("更改书名")
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //新书名字，这里trim()作用是去掉首位空格，防止不必要的错误
                                String str = editText.getText().toString().trim();

                                if (str.equals("")) {
                                    return;
                                } else {//更改书名
                                    //旧书籍文件夹目录路径
                                    String usedPath = bookData.getBookPath();
                                    //新书籍文件夹目录路径
                                    File file = new File(usedPath);
                                    //file.getParent()返回String类型
                                    String newPath = file.getParent() + "/" + editText.getText().toString();
                                    //更改书籍文件夹名称
                                    renameToNewFile(usedPath, newPath);

                                    bookData.setBookName(str);
                                    bookData.setBookDate(utils.getTime());
                                    bookData.setBookPath(newPath);


                                    //更新书单中的书籍信息
                                    CRUD op = new CRUD(context);
                                    op.updateBook(bookData);
                                }
                                //刷新页面
                                refreshBookListView();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
                break;

            case 1002://更换封面
                getBookIcon();
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 设置书籍封面图片
     */
    private void getBookIcon() {
        String[] items = new String[]{"图库", "相机"};
        new AlertDialog.Builder(this)
                .setTitle("选择来源")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0://图库
                                //启动其他应用的activity:使用隐式意图
                                Intent picture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(picture, PICTURE);
                                break;
                            case 1://相机
                                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(camera, CAMERA);
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    /**
     * 重命名书籍文件夹
     *
     * @param usedPath
     * @param newPath
     * @return
     */
    private boolean renameToNewFile(String usedPath, String newPath) {
        File srcDir = new File(usedPath);
        //就文件夹路径
        boolean isOk = srcDir.renameTo(new File(newPath));
        return isOk;
    }

    /**
     * 结果返回
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BookData bookData = bookDataList.get(itemposition);
        if (requestCode == BOOK_RESULT_CODE && resultCode == RESULT_OK && data != null) {//书籍点击
            //修改结果
            String newchapter = data.getExtras().getString("newchapter", "请创建新章节");
            bookData.setBookNewchapter(newchapter);
            bookData.setBookDate(utils.getTime());
            //更新书单中的书籍信息
            CRUD op = new CRUD(context);
            op.updateBook(bookData);
            //刷新书籍列表
            refreshBookListView();

        } else if (requestCode == CAMERA && resultCode == RESULT_OK && data != null) {//相机
            //获取图片对象
            Bundle extras = data.getExtras();
            //获取图片保存路径
            String bookicon = (String) extras.get("data");
            bookData.setBookIcon(bookicon);
            //更新书单中的书籍信息
            CRUD op = new CRUD(context);
            op.updateBook(bookData);
            //刷新书籍列表
            refreshBookListView();

        } else if (requestCode == PICTURE && resultCode == RESULT_OK && data != null) {//图库
            Uri uri = data.getData();
            if (utils.decodeUriAsBitmap(context, uri)) {
                String pathResult = utils.getPath(context, uri);
                bookData.setBookIcon(pathResult);
                System.out.println("BookIcon:" + bookData.getBookIcon());
                //更新书单中的书籍信息
                CRUD op = new CRUD(context);
                op.updateBook(bookData);
                //刷新书籍列表
                refreshBookListView();
            }
        }
    }
}
