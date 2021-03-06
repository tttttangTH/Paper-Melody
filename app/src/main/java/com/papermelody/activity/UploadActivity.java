package com.papermelody.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.papermelody.R;
import com.papermelody.model.response.HttpResponse;
import com.papermelody.model.response.UploadResponse;
import com.papermelody.util.App;
import com.papermelody.util.NetworkFailureHandler;
import com.papermelody.util.RetrofitClient;
import com.papermelody.util.SocialSystemAPI;
import com.papermelody.util.StorageUtil;
import com.papermelody.util.ToastUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UploadActivity extends BaseActivity {

    @BindView(R.id.edit_music_title)
    EditText editMusicTitle;
    @BindView(R.id.edit_music_des)
    EditText editMusicDes;
    @BindView(R.id.img_upload)
    ImageView imgUpload;
    @BindView(R.id.fab_upload_confirm)
    FloatingActionButton fabConfirm;
    @BindView(R.id.toolbar_upload)
    Toolbar toolbarUpload;

    public static final int LOAD_PIC = 0;

    private String link = null;
    private SocialSystemAPI api;
    private String cacheName = null;  // 缓存的文件的名称
    private String name = null;
    private String author = "";
    private Integer authorId = -1;
    private String authorAvatarName = "";
    private Date date = null;
    private String filePath = null;
    private String imgName = "";
    private String fileName = "";
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        fileName = intent.getStringExtra(PlayActivity.FILENAME);

        api = RetrofitClient.getSocialSystemAPI();

        initView();
    }

    private void initView() {
        setSupportActionBar(toolbarUpload);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarUpload.setNavigationOnClickListener((View v) -> {
            confirmQuit();
        });
        Log.i("nib", "view initialized");
        imgUpload.setOnClickListener((View v) -> {
            chooseImg();
            Log.i("nib", "img clicked");
        });
        fabConfirm.setOnClickListener((View v) -> {
            Log.i("nib", "fab clicked");
            uploadConfirm();
        });
    }

    private void chooseImg() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra("crop", true);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, LOAD_PIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOAD_PIC:
                if (data != null) {
                    Uri selectedImage = data.getData();
                    String imgPath = StorageUtil.imageGetPath(this, selectedImage);
                    Picasso.with(this).load(selectedImage).into(imgUpload);
                    imgUpload.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    filePath = imgPath;
                }
                break;
        }
    }

    private void uploadConfirm() {
        try {
            author = App.getUser().getNickname();
            authorId = App.getUser().getUserID();
            authorAvatarName = App.getUser().getAvatarName();
        } catch (NullPointerException e) {
            author = "AnonymousUser";
            authorId = -1;
            authorAvatarName = "";
            ToastUtil.showShort("没有登录，即将匿名上传");
        }
        uploadImg();
    }

    private void uploadImg() {
        if (editMusicDes.getText().length() > 64) {
            ToastUtil.showShort("描述不能超过64个字");
            return;
        }
        File file;
        if (filePath != null) {
            file = new File(filePath);
        } else {
            ToastUtil.showShort("文件路径为空");
            return;
        }
//        Log.d("TESTPATH", filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

        ToastUtil.showLong("上传中");
        addSubscription(api.uploadImg(body)
                .flatMap(NetworkFailureHandler.httpFailureFilter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> ((UploadResponse) response).getFileName())
                .subscribe(
                        imgName -> {
                            this.imgName = imgName;

                            Log.i("nib", getCacheDir().getAbsolutePath() + "/" + fileName);
                            uploadMusicFile(getCacheDir().getAbsolutePath() + "/" + fileName);

                        }, NetworkFailureHandler.uploadErrorHandler
                ));
    }

    private void uploadMusicFile(String filePath) {
        File file = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        addSubscription(api.uploadMusicFile(body)
                .flatMap(NetworkFailureHandler.httpFailureFilter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> ((UploadResponse) response).getFileName())
                .subscribe(
                        musicName -> {
                            uploadMusicInfo(imgName, musicName);
                        }, NetworkFailureHandler.uploadErrorHandler
                ));
    }

    private void uploadMusicInfo(String imgName, String musicName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        String date = sdf.format(new Date());
        addSubscription(api.uploadMusic(editMusicTitle.getText().toString(), author,
                authorId, authorAvatarName, date, musicName, imgName, editMusicDes.getText().toString())
                .flatMap(NetworkFailureHandler.httpFailureFilter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> ((HttpResponse) response).getError())
                .subscribe(
                        errorCode -> {
                            if (errorCode == 0) {
                                Log.i("nib", "errCode==0");
                                ToastUtil.showShort(R.string.upload_success);
                                PlayListenActivity.uploadSucess();
                                finish();
                            } else {
                                Log.i("nib", "errCode!=0");
                                ToastUtil.showShort(R.string.upload_failed);
                            }
                        }, NetworkFailureHandler.uploadErrorHandler
                ));
    }

    @Override
    public void onBackPressed() {
        // 再次点击退出
        Log.i("nib", "back pressed");
        confirmQuit();
    }

    private void confirmQuit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime > 2000) {
            ToastUtil.showShort(R.string.confirm_quit_upload);
            lastClickTime = currentTime;
        } else {
            finish();
        }
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_upload;
    }
}
