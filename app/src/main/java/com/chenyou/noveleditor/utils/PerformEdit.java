package com.chenyou.noveleditor.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.util.Stack;

/**
 * 撤销和恢复撤销
 */
public class PerformEdit {
    //操作序号(一次编辑可能对应多个操作，如替换文字，就是删除+插入)
    int index;
    //撤销栈
    Stack<Action> history = new Stack<>();
    //恢复栈
    Stack<Action> historyBack = new Stack<>();

    private Editable editable;//editview的内容
    private EditText editText;
    //自动操作标志，防止重复回调,导致无限撤销
    private boolean flag = false;

    private boolean undoflag;//撤销是否可执行标志
    private boolean redoflag;//撤销是否可执行标志

    public boolean isUndoflag() {
        return undoflag;
    }

    public void setUndoflag(boolean undoflag) {
        this.undoflag = undoflag;
    }

    public boolean isRedoflag() {
        return redoflag;
    }

    public void setRedoflag(boolean redoflag) {
        this.redoflag = redoflag;
    }

    /**
     * 传进来一个用于操作的EditText控件
     *
     * @param editText
     */
    public PerformEdit(@NonNull EditText editText) {
        CheckNull(editText, "EditText不能为空");
        this.editable = editText.getText();
        this.editText = editText;
        setUndoflag(true);
        setRedoflag(false);
        editText.addTextChangedListener(new Watcher());
    }

    protected void onEditableChanged(Editable s) {

    }

    protected void onTextChanged(Editable s) {

    }

    /**
     * 清理记录
     */
    public final void clearHistory() {
        history.clear();
        historyBack.clear();
    }

    /**
     * 撤销
     */
    public final void undo() {
        if (history.empty()) {
            return;
        }
        setRedoflag(true);
        //锁定操作
        flag = true;
        //返回栈顶的值 ；会把栈顶的值删除
        Action action = history.pop();
        historyBack.push(action);//是将action入栈
        if (action.isAdd) {
            //撤销添加
            editable.delete(action.startCursor, action.startCursor + action.actionTarget.length());
            editText.setSelection(action.startCursor, action.startCursor);
        } else {
            //撤销删除
            editable.insert(action.startCursor, action.actionTarget);
            if (action.endCursor == action.startCursor) {
                editText.setSelection(action.startCursor + action.actionTarget.length());
            } else {
                editText.setSelection(action.startCursor, action.endCursor);
            }
        }
        //释放操作
        flag = false;
        //执行完后进行判空设置setUndoflag标志，如果在undo()开头设置setUndoflag(false)，需要多点击一次图片才能切换
        if (history.empty()) {
            setUndoflag(false);
        }
        //判断是否是下一个动作是否和本动作是同一个操作，直到不同为止
        //判断history栈中是否为空,操作序号是否相同
        if (!history.empty() && history.peek().index == action.index) {
            undo();
        }
    }

    /**
     * 恢复
     */
    public final void redo() {
        if (historyBack.empty()) {
            return;
        }
        setUndoflag(true);
        flag = true;
        Action action = historyBack.pop();
        history.push(action);
        if (action.isAdd) {
            //恢复添加
            editable.insert(action.startCursor, action.actionTarget);
            if (action.endCursor == action.startCursor) {
                editText.setSelection(action.startCursor + action.actionTarget.length());
            } else {
                editText.setSelection(action.startCursor, action.endCursor);
            }
        } else {
            //恢复删除
            editable.delete(action.startCursor, action.startCursor + action.actionTarget.length());
            editText.setSelection(action.startCursor, action.startCursor);
        }
        flag = false;
        //执行完后进行判空设置setRedoflag标志，如果在undo()开头设置setRedoflag，需要多点击一次图片才能切换
        if (historyBack.empty()) {
            setRedoflag(false);
        }
        if (!historyBack.empty() && historyBack.peek().index == action.index) {
            redo();
        }
    }

    /**
     * 首次设置文本
     *
     * @param text
     */
    public final void setDefultText(CharSequence text) {
        clearHistory();
        flag = true;
        editable.replace(0, editable.length(), text);
        flag = false;
    }


    /**
     * EditText内容的变化监听
     */
    private class Watcher implements TextWatcher {
        /**
         * Before text changed.
         *
         * @param s     the s
         * @param start the start 起始光标
         * @param count the endCursor 选择数量
         * @param after the after 替换增加的文字数
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (flag) {
                return;
            }
            int end = start + count;
            if (end > start && end <= s.length()) {
                //返回一个以start开始和end结束的子序列
                CharSequence charSequence = s.subSequence(start, end);
                //删除了文字
                if (charSequence.length() > 0) {
                    Action action = new Action(charSequence, start, false);
                    if (count > 1) {
                        //如果一次超过一个字符，说名用户选择了，然后替换或者删除操作
                        action.setSelectCount(count);
                    } else if (count == 1 && count == after) {
                        //一个字符替换
                        action.setSelectCount(count);
                    }
                    //还有一种情况:选择一个字符,然后删除(暂时没有考虑这种情况)

                    //将action放入栈中
                    history.push(action);
                    historyBack.clear();
                    action.setIndex(++index);
                    setRedoflag(false);
                }
            }
        }

        /**
         * @param s      the s
         * @param start  the start 起始光标
         * @param before the before 选择数量
         * @param count  the endCursor 添加的数量
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (flag) {
                return;
            }
            //变化的光标位置长度
            int end = start + count;
            //内容有变化
            if (end > start) {
                CharSequence charSequence = s.subSequence(start, end);
                //添加文字
                if (charSequence.length() > 0) {
                    Action action = new Action(charSequence, start, true);
                    history.push(action);
                    historyBack.clear();
                    if (before > 0) {
                        //文字替换（先删除再增加），删除和增加是同一个操作，所以不需要增加序号
                        action.setIndex(index);
                    } else {
                        action.setIndex(++index);
                        setRedoflag(false);
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (flag) {
                return;
            }
            if (s != editable) {
                editable = s;
                onEditableChanged(s);
            }
            PerformEdit.this.onTextChanged(s);
        }
    }

    /**
     * 保存改变的字符的状态的类
     */
    private class Action {
        /**
         * 改变字符
         */
        CharSequence actionTarget;
        /**
         * 光标位置
         */
        int startCursor;
        int endCursor;
        /**
         * 标志增加操作
         */
        boolean isAdd;
        /**
         * 操作序号
         */
        int index;

        /**
         * 初始化数据
         *
         * @param actionTarget
         * @param startCursor
         * @param isAdd
         */
        public Action(CharSequence actionTarget, int startCursor, boolean isAdd) {
            this.actionTarget = actionTarget;
            this.startCursor = startCursor;
            this.endCursor = startCursor;
            this.isAdd = isAdd;
        }

        public void setSelectCount(int count) {
            this.endCursor = endCursor + count;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    /**
     * 检查是否为空
     *
     * @param o
     * @param message
     */
    private static void CheckNull(Object o, String message) {
        if (o == null) {
            throw new IllegalStateException(message);
        }
    }
}
