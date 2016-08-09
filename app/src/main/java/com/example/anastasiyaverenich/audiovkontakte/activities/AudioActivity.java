package com.example.anastasiyaverenich.audiovkontakte.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
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

import com.example.anastasiyaverenich.audiovkontakte.MusicService;
import com.example.anastasiyaverenich.audiovkontakte.MusicService.MusicBinder;
import com.example.anastasiyaverenich.audiovkontakte.R;
import com.example.anastasiyaverenich.audiovkontakte.adapters.AudioRecyclerAdapter;
import com.example.anastasiyaverenich.audiovkontakte.gsonFactories.RecipeTypeAdapterFactory;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.example.anastasiyaverenich.audiovkontakte.modules.IApiMethods;
import com.example.anastasiyaverenich.audiovkontakte.ui.OnLoadMoreListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class AudioActivity extends ActionBarActivity {
    private static final String API_URL = "https://api.vk.com";
    private int OWNER_ID = 113577371;
    private int OFFSET = 0;
    private static final int COUNT = 15;
    private static final String VERSION = "5.53";
    private static final String ACCESS_TOKEN = "cde7f3e108bd048bf2e09b0042681d35e332b4d2109b16988dcb83a6f18a4c72d4a5b03d8551a9335cf98";
    private IApiMethods methods;
    private Callback<Audio> callback;
    List<Audio.Track> listAudio = new ArrayList<>();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    public AudioRecyclerAdapter adapter;
    public MusicService musicSrv;
    public Intent playIntent;
    public boolean musicBound = false;
    public RelativeLayout audioView;
    public int currentPosition = -1;
    ImageView playOrStopSong;
    ImageView nextSong;
    ImageView prevSong;
    int pressedPosition;
    int iNext;
    int iPrev;
    SeekBar seekBar;
    Formatter mFormatter;
    StringBuilder mFormatBuilder;
    TextView endTime, currentTime;
    private Handler mHandler = new MessageHandler(this);
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        playOrStopSong = (ImageView) findViewById(R.id.play_song);
        nextSong = (ImageView) findViewById(R.id.next_song);
        prevSong = (ImageView) findViewById(R.id.prev_song);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        audioView = (RelativeLayout) findViewById(R.id.audio_view);
        endTime = (TextView) findViewById(R.id.duration_song);
        currentTime = (TextView) findViewById(R.id.current_time_song);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        //if (seekBar != null) {
        //if (seekBar instanceof SeekBar) {
        //SeekBar seeker = (SeekBar) seekBar;
        seekBar.setOnSeekBarChangeListener(mSeekListener);
        // }
        seekBar.setMax(1000);
        //}

        audioView.setVisibility(View.INVISIBLE);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initAdapter();
        if (LoginActivity.useAudioVkontakteWithSdk)
            getResponse(COUNT, OFFSET);
        else {
            loadingAudio();
        }

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
                OFFSET = OFFSET + COUNT;
                if (LoginActivity.useAudioVkontakteWithSdk)
                    getResponse(COUNT, OFFSET);
                else {
                    methods.getAudio(OWNER_ID, OFFSET, COUNT, VERSION, ACCESS_TOKEN, callback);
                }
            }
        });
        playOrStopSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicSrv.isPlaying()) {
                    playOrStopSong.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    musicSrv.pauseSong();
                } else {
                    playOrStopSong.setImageResource(R.drawable.ic_pause_black_24dp);
                    musicSrv.playBackSong();
                }
            }
        });
        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iNext == 0) {
                    iNext = pressedPosition;
                }
                iNext = iNext + 1;
                songPickedNexOrPrev(iNext);
            }
        });
        prevSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iPrev == 0) {
                    iPrev = pressedPosition;
                }
                iPrev = iPrev - 1;
                songPickedNexOrPrev(iPrev);
            }
        });

        adapter.setListener(new AudioRecyclerAdapter.AudioItemClickListener() {
                                @Override
                                public void AudioItemClick(int position) {
                                    pressedPosition = position;
                                    if (currentPosition == -1) {
                                        audioView.setVisibility(View.VISIBLE);
                                        songPicked(position);
                                    } else if (currentPosition == position) {
                                        if (musicSrv.isPlaying()) {
                                            playOrStopSong.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                                            musicSrv.pauseSong();
                                        } else {
                                            playOrStopSong.setImageResource(R.drawable.ic_pause_black_24dp);
                                            musicSrv.playBackSong();
                                        }
                                    } else {
                                        songPicked(position);
                                        playOrStopSong.setImageResource(R.drawable.ic_pause_black_24dp);

                                    }
                                    //setProgress();
                                    currentPosition = position;
                                }
                            }
        );
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (musicSrv == null) {
            return 0;
        }

        int position = musicSrv.getCurrentPosition();
        int duration = musicSrv.getDuration();
        if (seekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
            // int percent = mPlayer.getBufferPercentage();
            //seekBar.setSecondaryProgress(percent * 10);
        }

        if (endTime != null)
            endTime.setText(stringForTime(duration));
        if (currentTime != null)
            currentTime.setText(stringForTime(position));

        return position;
    }

    public void songPicked(int position) {
        musicSrv.setSong(position);
        musicSrv.playSong();
        show();

    }

    public void songPickedNexOrPrev(int position) {
        musicSrv.setSong(position);
        musicSrv.playNextOrPrevSong();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TAG", "Start");
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    /*@Override
    protected void onResume() {
        super.onResume();
        stopService(playIntent);
        musicSrv = null;
    }*/

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            Log.e("TAG", "mmm");
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(listAudio);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void initAdapter() {
        adapter = new AudioRecyclerAdapter(this, R.layout.audio, listAudio, recyclerView);
        recyclerView.setAdapter(adapter);
    }

    private void loadingAudio() {
        Gson gson = new GsonBuilder().
                registerTypeAdapterFactory(new RecipeTypeAdapterFactory()).create();
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setConverter(new GsonConverter(gson))
                .build();
        methods = restAdapter.create(IApiMethods.class);
        callback = new Callback<Audio>() {
            @Override
            public void success(Audio results, Response response) {
                Log.e("TAG", "SUCCESS " + results.response.size());
                if (listAudio.size() != 0) {
                    if ((listAudio.get(listAudio.size() - 1)) == null) {
                        listAudio.remove(listAudio.size() - 1);
                        adapter.notifyDataSetChanged();
                        //adapter.notifyItemRemoved(listAudio.size());
                    }
                }
                listAudio.addAll(results.response);
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("TAG", "ERROR ");
                retrofitError.printStackTrace();
                listAudio.remove(listAudio.size() - 1);
                adapter.notifyItemRemoved(listAudio.size());
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
            }
        };
        methods.getAudio(OWNER_ID, OFFSET, COUNT, VERSION, ACCESS_TOKEN, callback);
    }

    public void getResponse(int count, int offset) {
        VKParameters params = new VKParameters();
        params.put(VKApiConst.OFFSET, offset);
        params.put(VKApiConst.COUNT, count);
        VKRequest requestAudio = VKApi.audio().get(params);
        requestAudio.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                if (listAudio.size() != 0) {
                    if ((listAudio.get(listAudio.size() - 1)) == null) {
                        listAudio.remove(listAudio.size() - 1);
                        adapter.notifyDataSetChanged();
                        //adapter.notifyItemRemoved(listAudio.size());
                    }
                }
                for (int i = 0; i < ((VKList<VKApiAudio>) response.parsedModel).size(); i++) {
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>) response.parsedModel).get(i);
                    listAudio.add(new Audio.Track(vkApiAudio.artist, vkApiAudio.title, vkApiAudio.duration, vkApiAudio.url));
                }
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("VkDemoApp", "onError: " + error);
                listAudio.remove(listAudio.size() - 1);
                adapter.notifyItemRemoved(listAudio.size());
                adapter.notifyDataSetChanged();
                adapter.setLoaded();
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.audio_end, menu);
        getMenuInflater().inflate(R.menu.audio_shuffle, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv = null;
                System.exit(0);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "destroy player");
        stopService(playIntent);
        musicSrv = null;
    }

    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            long duration = musicSrv.getDuration();
            long newposition = (duration * progress) / 1000L;
            musicSrv.seekTo((int) newposition);
            if (currentTime != null)
                currentTime.setText(stringForTime((int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
        }
    };

    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     */
    public void show() {
        setProgress();


        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

  /*  public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
    }*/

    private static class MessageHandler extends Handler {
        private final WeakReference<AudioActivity> mView;

        MessageHandler(AudioActivity view) {
            mView = new WeakReference<AudioActivity>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            AudioActivity view = mView.get();
            if (view == null || view.musicSrv == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    msg = obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg, 1000 - (pos % 1000));
                    break;
            }
        }
    }
}
