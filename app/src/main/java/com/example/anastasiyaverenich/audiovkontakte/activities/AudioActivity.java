package com.example.anastasiyaverenich.audiovkontakte.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anastasiyaverenich.audiovkontakte.R;
import com.example.anastasiyaverenich.audiovkontakte.adapters.AudioRecyclerAdapter;
import com.example.anastasiyaverenich.audiovkontakte.fragments.DetailsAudioFragment;
import com.example.anastasiyaverenich.audiovkontakte.interfaces.MediaPlayerControlInterface;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.example.anastasiyaverenich.audiovkontakte.modules.MediaPlayerState;
import com.example.anastasiyaverenich.audiovkontakte.singleton.MediaPlayerControl;
import com.example.anastasiyaverenich.audiovkontakte.singleton.MediaPlayerLoader;
import com.example.anastasiyaverenich.audiovkontakte.singleton.MediaPlayerSetUi;
import com.example.anastasiyaverenich.audiovkontakte.ui.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayerControlInterface {
    public static int INIT_CURRENT_POSITION = -1;
    List<Audio.Track> listAudio = new ArrayList<>();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    public AudioRecyclerAdapter adapter;
    public RelativeLayout audioView;
    public int currentPosition = -1;
    ImageView playOrStopSong;
    SeekBar seekBar;
    Formatter formatter;
    StringBuilder formatBuilder;
    TextView nameAudio, artistAudio;
    public boolean isDestroy;
    private MediaPlayerLoader mediaPlayerLoader;
    private MediaPlayerSetUi mediaPlayerSetUi;
    private MediaPlayerControl mediaPlayerControl;
    DetailsAudioFragment detailsAudioFragment;
    int positionInFragment = -1;
    int pressedPosition;
    MediaPlayerState mediaPlayerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onCreate");
        setContentView(R.layout.activity_audio);
        mediaPlayerState = new MediaPlayerState();
        playOrStopSong = (ImageView) findViewById(R.id.play_song);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        audioView = (RelativeLayout) findViewById(R.id.audio_view);
        nameAudio = (TextView) findViewById(R.id.name_audio);
        artistAudio = (TextView) findViewById(R.id.artist_audio);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        playOrStopSong.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(MediaPlayerControl.getInstance().seekListener);
        seekBar.setMax(1000);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        initAdapter();
        mediaPlayerControl = MediaPlayerControl.getInstance();
        mediaPlayerSetUi = MediaPlayerSetUi.getInstance();
        mediaPlayerLoader = MediaPlayerLoader.getInstance();
        mediaPlayerLoader.addCallback(mediaPlayerLoaderCallback);
        if (LoginActivity.useAudioVkontakteWithSdk)
            mediaPlayerLoader.getResponse(15, 0);
        else {
            mediaPlayerLoader.loadingAudio();
        }
        mediaPlayerControl.subscribeToUpdatesFromMediaPlayerControl(this);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        listAudio.add(null);
                        adapter.notifyDataSetChanged();
                    }
                };
                handler.post(r);
                mediaPlayerLoader.loadMore();
//                if (listAudio.size() != 0) {
//                    if ((listAudio.get(listAudio.size() - 1)) == null) {
//                        listAudio.remove(listAudio.size() - 1);
//                        //adapter.notifyDataSetChanged();
//                        //adapter.notifyItemRemoved(listAudio.size());
//                    }
//                }
            }
        });
        audioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positionInFragment = mediaPlayerControl.getCurrentPosition();
                detailsAudioFragment = new DetailsAudioFragment(positionInFragment, listAudio, adapter);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.linear_layout, detailsAudioFragment)
                        .commit();
            }
        });
        adapter.setListener(new AudioRecyclerAdapter.AudioItemClickListener() {
                                @Override
                                public void AudioItemClick(int position) {
                                    pressedPosition = position;
                                    hideOrShowAudioView(View.VISIBLE);
                                    mediaPlayerControl.updatePosition(position);
                                    updateUiByToggle(true);
                                    adapter.setCurrentPosition(position);
                                    mediaPlayerControl.setAndPlayAudio();
                                    setNameAndArtistAudio(listAudio, position);
                                }
                            }
        );
        if (mediaPlayerState.getIsSetAudio()) {
            if (!mediaPlayerState.getIsPlaying()) {
                adapter.isPlayingAudio(false, false);
                playOrStopSong.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                Log.d("positionOfSong", String.valueOf(mediaPlayerControl.getCurrentPosition()));
            }
            hideOrShowAudioView(View.VISIBLE);
            setNameAndArtistAudio(mediaPlayerLoader.listAudio,
                    mediaPlayerState.getPosition());
            setProgress(mediaPlayerState.getPositionSeekBar(), mediaPlayerControl.duration);
        } else {
            hideOrShowAudioView(View.INVISIBLE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TAG", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TAG", "onResume");
    }

    public void hideOrShowAudioView(int invisible) {
        audioView.setVisibility(invisible);
    }

    private MediaPlayerLoader.MediaPlayerLoaderCallback mediaPlayerLoaderCallback
            = new MediaPlayerLoader.MediaPlayerLoaderCallback() {
        @Override
        public void onSuccess(List<Audio.Track> audioList) {
            Log.d("AudioActivity", "OnSuccess");
            if (audioList.size() > listAudio.size()) {
                listAudio.clear();
                listAudio.addAll(audioList);
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
                adapter.isLastLoaded = false;
            } else {
                adapter.isLastLoaded = true;
                recyclerView.setPadding(0, 0, 0, 60);
            }
        }

        @Override
        public void onError() {
            //listAudio.remove(listAudio.size() - 1);

        }
    };

    public void startLoginActivity() {
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        int isOpenLogoutFragment = 1;
        i.putExtra("date", isOpenLogoutFragment);
        setResult(RESULT_OK, i);
        /*NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        MediaPlayerControl.getInstance().doUnbindService();*/
        finish();
        //startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.audio_shuffle, menu);
        getMenuInflater().inflate(R.menu.audio_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                return true;
            case R.id.action_logout:
                startLoginActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean canGoBack() {
        if (positionInFragment == -1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("TAG  ", "onBackPressed");
        if (detailsAudioFragment != null && canGoBack()) {
            //recyclerView.setAdapter(adapter);
            positionInFragment = -1;
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(detailsAudioFragment)
                    .commit();
        } else {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            int isOpenLogoutFragment = 2;
            i.putExtra("date", isOpenLogoutFragment);
            setResult(RESULT_OK, i);
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy");
        //stopService(playIntent);
        //isDestroy = true;
        mediaPlayerLoader.removeCallback(mediaPlayerLoaderCallback);
        //
            /*mediaPlayerControl.unsubscribeToUpdatesFromMediaPlayerControl(this);
            adapter.isPlayingAudio(false, true);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
            mediaPlayerControl.updateCurrentPosition(INIT_CURRENT_POSITION);
            MediaPlayerControl.getInstance().doUnbindService();
            mediaPlayerLoader.removeCallback(mediaPlayerLoaderCallback);*/
    }

    @Override
    public void onClick(View v) {
        MediaPlayerControl.getInstance().isToggleMediaPlayer();
    }

    public void initAdapter() {
        Log.d("initAdapter", "true");
        adapter = new AudioRecyclerAdapter(this, R.layout.audio, listAudio, recyclerView);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateUiByDismissNotification() {
        Log.d("onReceive", "updateUi");
        hideOrShowAudioView(View.INVISIBLE);
        adapter.isPlayingAudio(false, true);
        adapter.notifyDataSetChanged();
        /*mediaPlayerControl.unsubscribeToUpdatesFromMediaPlayerControl(this);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        mediaPlayerControl.updateCurrentPosition(INIT_CURRENT_POSITION);*/
    }

    @Override
    public void updateUiByToggle(boolean isPlaying) {
        playOrStopSong.setImageResource(!isPlaying ? R.drawable.ic_play_arrow_black_24dp : R.drawable.ic_pause_black_24dp);
        adapter.isPlayingAudio(isPlaying, false);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateUiByComplete(boolean isEnd, int isNextOrPrev) {
        if (mediaPlayerControl.getCurrentPosition() + 1 >= listAudio.size()) {
            mediaPlayerControl.updatePosition(mediaPlayerControl.INIT_POSITION);
        }
        int position = mediaPlayerControl.getCurrentPosition();
        Log.d("TAG", String.valueOf(position));
        setNameAndArtistAudio(listAudio, position);
        adapter.setCurrentPosition(position);
        //updateUiByToggle(true);
    }

    @Override
    public void setProgress(int position, int duration) {
        mediaPlayerSetUi.setProgress(seekBar, position, duration);
    }

    @Override
    public void setSecondProgress(int percent) {
        mediaPlayerSetUi.setSecondProgress(seekBar, percent);
    }

    @Override
    public void setNameAndArtistAudio(List<Audio.Track> listAudio, int position) {
        mediaPlayerSetUi.setNameAndArtistAudio(listAudio, position, nameAudio, artistAudio);
    }
}





