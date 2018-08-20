package com.d.music.module.media.controler;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

/**
 * MediaPlayerManager
 * Created by D on 2017/9/11.
 */
public class MediaPlayerManager {
    private volatile static MediaPlayerManager manager;

    private MediaPlayer mediaPlayer;
    private String source;

    private MediaPlayerManager() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setScreenOnWhilePlaying(true);
    }

    public static MediaPlayerManager getIns() {
        if (manager == null) {
            synchronized (MediaPlayerManager.class) {
                if (manager == null) {
                    manager = new MediaPlayerManager();
                }
            }
        }
        return manager;
    }

    @UiThread
    public void play(final String url, final OnMediaPlayerListener l) {
        play(this, url, l);
    }

    @UiThread
    public void play(final Activity activity, final String url, final OnMediaPlayerListener l) {
        play(activity, url, l);
    }

    @UiThread
    public void play(final Object obj, final String url, final OnMediaPlayerListener listener) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        source = url;
        if (listener != null) {
            listener.onLoading(mediaPlayer, url);
        }
        final WeakReference<Object> weakRef = new WeakReference<>(obj);
        try {
            mediaPlayer.reset(); // 重置
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (isDestroy(url, weakRef)) {
                        return;
                    }
                    mediaPlayer.start();
                    if (listener != null) {
                        listener.onPrepared(mediaPlayer, url);
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (isDestroy(url, weakRef)) {
                        return false;
                    }
                    if (listener != null) {
                        listener.onError(mediaPlayer, url);
                    }
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (isDestroy(url, weakRef)) {
                        return;
                    }
                    if (listener != null) {
                        listener.onCompletion(mediaPlayer, url);
                    }
                }
            });
            mediaPlayer.prepareAsync();
        } catch (Throwable e) {
            e.printStackTrace();
            mediaPlayer.reset(); // 重置
            if (listener != null) {
                listener.onError(mediaPlayer, url);
            }
        }
    }

    private boolean isDestroy(String url, WeakReference<Object> weakRef) {
        return !TextUtils.equals(url, source) || mediaPlayer == null
                || weakRef == null || weakRef.get() == null
                || weakRef.get() instanceof Activity && ((Activity) weakRef.get()).isFinishing();
    }

    public String getUrl() {
        return source;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void seekTo(int msec) {
        int duration = mediaPlayer.getDuration(); // 毫秒
        if (msec >= 0 && msec <= duration) {
            mediaPlayer.seekTo(msec);
        }
    }

    private boolean isPrepared() {
        return true;
    }

    public void start() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        mediaPlayer.stop();
    }


    @NonNull
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getCurrentPosition() {
        if (!mediaPlayer.isPlaying()) {
            return 0;
        }
        int position = mediaPlayer.getCurrentPosition();
        position = Math.max(position, 0);
        return position;
    }

    public int getDuration() {
        if (!mediaPlayer.isPlaying()) {
            return 0;
        }
        int duration = mediaPlayer.getDuration();
        duration = Math.max(duration, 0);
        return duration;
    }

    public void reset() {
        source = "";
        mediaPlayer.reset(); // 重置
    }

    private void release() {
        source = "";
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public interface OnMediaPlayerListener {
        void onLoading(MediaPlayer mp, String url);

        void onPrepared(MediaPlayer mp, String url);

        void onError(MediaPlayer mp, String url);

        void onCompletion(MediaPlayer mp, String url);

        void onCancel(MediaPlayer mp);
    }
}