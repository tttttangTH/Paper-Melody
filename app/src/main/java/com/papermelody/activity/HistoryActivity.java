package com.papermelody.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.papermelody.R;
import com.papermelody.model.HistoryMusic;
import com.papermelody.model.LocalMusic;
import com.papermelody.util.ToastUtil;
import com.papermelody.widget.HistoryItemRecyclerViewAdapter;
import com.papermelody.widget.HistoryItemRecyclerViewDecoration;
import com.papermelody.widget.HistoryItemRecyclerViewLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

public class HistoryActivity extends BaseActivity {
    /**
     * 用于展示历史作品（保存在本地的作品）
     * 方法：直接在本地读取记录
     * 路径：(File)Environment.getExternalStorageDirectory
     */
    @BindView(R.id.history_item_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_history)
    Toolbar toolbar;

    private String[] musicCaption = new String[]{"MUSIC 1", "MUSIC 2", "A", "B", "HGS", "ZB", "TTH"};
    private Context context;
    private String musicExtendedName = ".m4a";
    private HistoryItemRecyclerViewAdapter adapter;
    private List<LocalMusic> localMusic = new ArrayList<>();

    private HistoryItemRecyclerViewAdapter.mOnItemClickListener onItemClickListener = new
            HistoryItemRecyclerViewAdapter.mOnItemClickListener() {
                @Override
                public void OnItemClick(LocalMusic music) {
                    Log.d("TESTTT", music.getFilename());
                    Intent intent = new Intent(context, LocalListenActivity.class);
                    intent.putExtra(PlayActivity.FILENAME, music.getFilename());
                    startActivity(intent);
                }
            };

//    private void __TEST() {
//        try {
//            for (int i = 0; i < 5; ++i) {
//                File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath(),
//                        "Music" + Integer.toString(i) + "_mode_0_j_1_cat_2_" + musicExtendedName);
//                FileOutputStream fos = new FileOutputStream(file);
//                String info = "I am a chinese!";
//                fos.write(info.getBytes());
//                fos.close();
//                Log.d("FILEE", "写入成功");
//                Thread.sleep(10);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        // ctl.setExpandedTitleMarginBottom(5);
//        __TEST();
        initToolbar();
        initRecyclerView();
        getFileDir(getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        //Log.d("FILEPATH", Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private void initToolbar() {
        toolbar.setTitle(getString(R.string.history_musics));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener((View v) -> {
            finish();
        });
    }


    private void initRecyclerView() {
        adapter = new HistoryItemRecyclerViewAdapter(musicCaption, localMusic, context);
        adapter.setOnItemClickListener(onItemClickListener);
        mRecyclerView.addItemDecoration(new HistoryItemRecyclerViewDecoration(1));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new HistoryItemRecyclerViewLayoutManager());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    public void getFileDir(String filePath) {
        try {
            File f = new File(filePath);
            Log.d("FILEE", "1");
            File[] files = f.listFiles();// 列出所有文件
            Log.d("FILEE", "2");
            if (files != null) {
                int count = files.length;// 文件个数
                for (int i = 0; i < count; i++) {
                    File file = files[i];
                    Log.d("FILEE", "3");
                    try {
                        String __name = file.getName();

                        if (__name.length() > 4 &&
                                __name.substring(__name.length() - 4, __name.length()).equals(
                                        musicExtendedName)) {
                            Log.d("FILEE", "4");
                            localMusic.add(new LocalMusic(
                                    file.getName(), file.lastModified(), file.length()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        //  ToastUtil.showShort("Read this error, but continuing");
                    }
                }
            }
            Collections.sort(localMusic, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    HistoryMusic a = (HistoryMusic) o1;
                    HistoryMusic b = (HistoryMusic) o2;
                    return Long.toString(b.getCreateTime()).compareTo(
                            Long.toString(a.getCreateTime()));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("FILEE", e.toString());
            ToastUtil.showShort("Wrong Reading");
        }
    }


    @Override
    protected int getContentViewId() {
        return R.layout.activity_history;
    }
}
