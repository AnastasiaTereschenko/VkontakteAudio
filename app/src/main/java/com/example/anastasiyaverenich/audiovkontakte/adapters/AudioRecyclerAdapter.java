package com.example.anastasiyaverenich.audiovkontakte.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.anastasiyaverenich.audiovkontakte.R;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.example.anastasiyaverenich.audiovkontakte.ui.OnLoadMoreListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AudioRecyclerAdapter extends RecyclerView.Adapter {
    private final Context mContext;
    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    public final int mResourceId;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private int visibleThreshold = 5;
    private AudioItemClickListener listener;
    public OnLoadMoreListener onLoadMoreListener;
    List<Audio.Track> audios;

    public AudioRecyclerAdapter(Context context, int resource, List<Audio.Track> objects,
                                RecyclerView recyclerView) {
        mContext = context;
        mResourceId = resource;
        this.audios = objects;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    // Log.e("TAG", "onScrolled " + getLoaded());
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager
                            .findLastVisibleItemPosition();
                    if (!loading && totalItemCount > 0 && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if ((onLoadMoreListener != null)) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
            /*recyclerView.setOnScrollListener(new RecyclerViewPauseOnScrollListener(ImageLoader.
                    getInstance(), pauseOnScroll, pauseOnFling));*/

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio,
                    parent, false);
            vh = new AudioViewHolder(view);
        } else if (viewType == VIEW_PROG) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_bar,
                    parent, false);
            vh = new ProgressViewHolder(view);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AudioViewHolder) {
            final Audio.Track audio = audios.get(position);
            final AudioViewHolder audioViewHolder = (AudioViewHolder) holder;
            long durationAudioSeconds = Long.valueOf(audio.getDuration());
            long balanceSeconds = durationAudioSeconds % 60;
            int balanceSecondsInt = (int) balanceSeconds;
            String durationAudioSecondsMinutes;
            if ((balanceSecondsInt >= 0) && (balanceSecondsInt <= 9)) {
                durationAudioSecondsMinutes = TimeUnit.SECONDS.toMinutes(durationAudioSeconds) + ":0" + balanceSeconds;
            } else {
                durationAudioSecondsMinutes = TimeUnit.SECONDS.toMinutes(durationAudioSeconds) + ":" + balanceSeconds;
            }
            //TextView nameAudioTextView = ((TextView) audioViewHolder.findViewById(R.id.nameAudio));
            //TextView durationAudioTextView = ((TextView) rowView.findViewById(R.id.durationAudio));
            audioViewHolder.artistAudio.setText(audio.getArtist());
            audioViewHolder.nameAudio.setText(audio.getTitle());
            audioViewHolder.durationAudio.setText(String.valueOf(durationAudioSecondsMinutes));
            audioViewHolder.audioText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.AudioItemClick(position);
                }
            });
        } else if (holder instanceof ProgressViewHolder) {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return audios.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }

    protected static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView nameAudio;
        TextView durationAudio;
        TextView artistAudio;
        RelativeLayout audioText;

        public AudioViewHolder(View itemView) {
            super(itemView);
            nameAudio = (TextView) itemView.findViewById(R.id.nameAudio);
            durationAudio = (TextView) itemView.findViewById(R.id.durationAudio);
            artistAudio = (TextView) itemView.findViewById(R.id.artistAudio);
            audioText = (RelativeLayout) itemView.findViewById(R.id.audioText);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBarOnFeed);
        }
    }

    public void setListener(AudioItemClickListener listener) {
        this.listener = listener;
    }

    public interface AudioItemClickListener {
        void AudioItemClick(int position );
    }

}
