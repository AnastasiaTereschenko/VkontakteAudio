package com.example.anastasiyaverenich.audiovkontakte.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anastasiyaverenich.audiovkontakte.MediaPlayerControlInterface;
import com.example.anastasiyaverenich.audiovkontakte.R;
import com.example.anastasiyaverenich.audiovkontakte.adapters.AudioRecyclerAdapter;
import com.example.anastasiyaverenich.audiovkontakte.application.MediaPlayerControl;
import com.example.anastasiyaverenich.audiovkontakte.application.MediaPlayerLoader;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.example.anastasiyaverenich.audiovkontakte.ui.OnLoadMoreListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayerControlInterface {
    List<Audio.Track> listAudio = new ArrayList<>();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    public AudioRecyclerAdapter adapter;
    public RelativeLayout audioView;
    public int currentPosition = -1;
    ImageView playOrStopSong;
    int pressedPosition;
    SeekBar seekBar;
    Formatter mFormatter;
    StringBuilder mFormatBuilder;
    TextView nameAudio, artistAudio;
    public boolean isEnd;
    int iPos = 0;
    int currentPercent = 0;
    int position;
    int duration;
    Message msg;
    public Intent playIntent;
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public boolean isDestroy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        playOrStopSong = (ImageView) findViewById(R.id.play_song);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        audioView = (RelativeLayout) findViewById(R.id.audio_view);
        nameAudio = (TextView) findViewById(R.id.name_audio);
        artistAudio = (TextView) findViewById(R.id.artist_audio);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        playOrStopSong.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(MediaPlayerControl.getInstance().seekListener);
        seekBar.setMax(1000);
        hideOrShowAudioView(View.INVISIBLE);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        initAdapter();
        if (LoginActivity.useAudioVkontakteWithSdk)
            MediaPlayerLoader.getInstance().getResponse(15, 0, listAudio, adapter);
        else {
            MediaPlayerLoader.getInstance().loadingAudio(listAudio, adapter);
        }
        MediaPlayerControl.getInstance().subscribeToUpdatesFromSingleton(this);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                MediaPlayerLoader.getInstance().loadMore(listAudio, adapter);
            }
        });
        audioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.linear_layout, new AudioFragment())
                        .commit();
            }
        });
        adapter.setListener(new AudioRecyclerAdapter.AudioItemClickListener() {
                                @Override
                                public void AudioItemClick(int position) {
                                    setNameAndArtistAudio(position);
                                    pressedPosition = position;
                                    if (currentPosition == position) {
                                        MediaPlayerControl.getInstance().isToggleMediaPlayer();
                                    } else {
                                        hideOrShowAudioView(View.VISIBLE);
                                        AudioRecyclerAdapter.isPlayingAudio(true, false);
                                        MediaPlayerControl.getInstance().songPicked(listAudio, position);
                                        playOrStopSong.setImageResource(R.drawable.ic_pause_black_24dp);
                                    }
                                    adapter.notifyDataSetChanged();
                                    currentPosition = position;
                                }
                            }
        );
    }

    private void hideOrShowAudioView(int invisible) {
        audioView.setVisibility(invisible);
    }

    public void setNameAndArtistAudio(int position) {
        String artistAudioPos = listAudio.get(position).getArtist();
        String nameAudioPos = listAudio.get(position).getTitle();
        artistAudio.setText(artistAudioPos);
        nameAudio.setText(nameAudioPos);
    }

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

    @Override
    public void onBackPressed() {
        Log.d("TAG", "onBackPressed");
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        int isOpenLogoutFragment = 2;
        i.putExtra("date", isOpenLogoutFragment);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "Destroy");
        //stopService(playIntent);
        isDestroy = true;
        MediaPlayerControl.getInstance().isDestroyActivity(isDestroy);
        AudioRecyclerAdapter.isPlayingAudio(false, true);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        MediaPlayerControl.getInstance().doUnbindService();
    }

    @Override
    public void onClick(View v) {
        MediaPlayerControl.getInstance().isToggleMediaPlayer();
    }

    public void initAdapter() {
        adapter = new AudioRecyclerAdapter(this, R.layout.audio, listAudio, recyclerView);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateUiByDismissNotification() {
        Log.d("onReceive", "updateUi");
        hideOrShowAudioView(View.INVISIBLE);
        AudioRecyclerAdapter.isPlayingAudio(false, true);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateUiByToggle(boolean isPlaying) {
        playOrStopSong.setImageResource(!isPlaying ? R.drawable.ic_play_arrow_black_24dp : R.drawable.ic_pause_black_24dp);
        AudioRecyclerAdapter.isPlayingAudio(isPlaying, false);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void playNextAudioByComplete(boolean isEnd, int isNextOrPrev) {
        if (isEnd) {
            if (iPos == 0) {
                iPos = pressedPosition;
            }
            if (isNextOrPrev == 1) {
                iPos = iPos + 1;
            } else {
                iPos = iPos - 1;
            }
            AudioRecyclerAdapter.setCurrentPosition(iPos);
            updateUiByToggle(true);
            setNameAndArtistAudio(iPos);
            //adapter.notifyDataSetChanged();
            MediaPlayerControl.getInstance().songPicked(listAudio, iPos);
        }
    }

    @Override
    public void setSecondProgress(int percent) {
        if (percent > currentPercent) {
            int perc = percent * 10;
            seekBar.setSecondaryProgress(perc);
        }
        currentPercent = percent;
    }

    @Override
    public int setProgress(int position, int duration) {
        if (seekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
        }
        return position;
    }

    public static class AudioFragment extends android.support.v4.app.Fragment {

        public AudioFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.fragment_audio, container, false);
            return v;
        }
    }
}




