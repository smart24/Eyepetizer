package com.ethanhua.eyepetizer.module.video;import android.app.Activity;import android.content.Context;import android.content.Intent;import android.content.res.Configuration;import android.databinding.DataBindingUtil;import android.net.Uri;import android.os.Build;import android.os.Bundle;import android.support.annotation.Nullable;import android.view.View;import com.danikula.videocache.HttpProxyCacheServer;import com.ethanhua.eyepetizer.BaseActivity;import com.ethanhua.eyepetizer.R;import com.ethanhua.eyepetizer.databinding.ActivityVideoDetailsBinding;import com.ethanhua.eyepetizer.di.components.DaggerActivityComponent;import com.ethanhua.eyepetizer.module.video.viewadapter.VideoDetailsHeaderVB;import com.ethanhua.eyepetizer.module.video.viewadapter.VideoSmallCardVB;import com.ethanhua.eyepetizer.module.video.viewmodel.VideoBaseVM;import com.ethanhua.eyepetizer.module.video.viewmodel.VideoDetailsVM;import javax.inject.Inject;import static android.support.v4.app.ActivityOptionsCompat.makeSceneTransitionAnimation;/** * Created by ethanhua on 2017/9/27. */public class VideoDetailsActivity extends BaseActivity {    private static final String TRANSITION_SHARE_ELEMENT_NAME = "share";    private static final String PARAMS_VIDEO = "params_video";    private ActivityVideoDetailsBinding binding;    @Inject    public HttpProxyCacheServer httpProxyCacheServer;    @Inject    public VideoDetailsVM videoDetailsVM;    public static void actionStart(Context context, VideoBaseVM videoBaseVM) {        Intent intent = new Intent(context, VideoDetailsActivity.class);        intent.putExtra(PARAMS_VIDEO, videoBaseVM);        context.startActivity(intent);    }    public static void animActionStart(View view, VideoBaseVM videoBaseVM) {        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {            Intent intent = new Intent(view.getContext(), VideoDetailsActivity.class);            intent.putExtra(PARAMS_VIDEO, videoBaseVM);            view.getContext().startActivity(intent, makeSceneTransitionAnimation(                    (Activity) view.getContext(),                    view,                    TRANSITION_SHARE_ELEMENT_NAME)                    .toBundle());        } else {            actionStart(view.getContext(), videoBaseVM);        }    }    @Override    protected void onCreate(@Nullable Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        DaggerActivityComponent.builder()                .appComponent(getAppComponent())                .activityModule(getActivityModule())                .build()                .inject(this);        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_details);        initView();        bindViewModel(getIntent());    }    private void initView() {        binding.videoView.setMediaControllerView(binding.mediaControllerView);        getLifecycle().addObserver(binding.videoView);        videoDetailsVM.getAdapter()                .register(VideoBaseVM.class)                .to(new VideoDetailsHeaderVB(view -> {                    if (view.getId() == R.id.tv_reply) {                        startCommentsFragment();                    }                }), new VideoSmallCardVB())                .withLinker(videoBaseVM -> videoBaseVM.id == videoDetailsVM.baseVM.id ? 0 : 1);    }    @Override    protected void onNewIntent(Intent intent) {        super.onNewIntent(intent);        videoDetailsVM.record(binding.videoView.getCurrentPosition());        bindViewModel(intent);    }    private void bindViewModel(Intent intent) {        // get input data        VideoBaseVM videoBaseVM = intent.getParcelableExtra(PARAMS_VIDEO);        videoDetailsVM.baseVM = videoBaseVM;        // add video cache        String mCacheUrl = httpProxyCacheServer.getProxyUrl(videoBaseVM.uri.get().toString());        videoBaseVM.uri.set(Uri.parse(mCacheUrl));        // bind data to view        binding.setVm(videoDetailsVM);        // load data        videoDetailsVM.refresh();    }    private void startCommentsFragment() {        VideoCommentsFragment fragment = VideoCommentsFragment.newInstance(                videoDetailsVM.baseVM.id,                videoDetailsVM.baseVM.blurredUrl.get());        getSupportFragmentManager().beginTransaction()                .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out)                .add(R.id.layout_container, fragment)                .addToBackStack(null)                .show(fragment)                .commitAllowingStateLoss();    }    @Override    protected void onDestroy() {        super.onDestroy();        videoDetailsVM.record(binding.videoView.getCurrentPosition());        videoDetailsVM.clean();    }    @Override    public void onConfigurationChanged(Configuration newConfig) {        super.onConfigurationChanged(newConfig);        binding.videoView.onConfigurationChanged(newConfig);    }}