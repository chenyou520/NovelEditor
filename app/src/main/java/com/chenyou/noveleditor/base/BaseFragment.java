package com.chenyou.noveleditor.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 作用：基本的Fragment
 * ChapterListFragment和DustbinFragment都继承它
 */
public abstract class BaseFragment extends Fragment {

    private Context context;//ChapterActivity
    private String title;



    public BaseFragment(Context context, String title) {
        this.context = context;
        this.title = title;
    }

    @Nullable
    @Override
    public Context getContext() {
        return context;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 当Fragment被创建的时候回调这个方法
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    /**
     * 当视图被创建的时候回调
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return initview();
    }

    /**
     * 让孩子实现自己的视图，达到自己特有的效果
     *
     * @return
     */
    public abstract View initview();

    /**
     * 当Activity被创建之后被回调
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 1.如果自页面没有数据，联网请求数据，并且绑定到initView初始化的视图上
     * 2.绑定到initView初始化的视图上
     */
    public void initData() {
    }
}
