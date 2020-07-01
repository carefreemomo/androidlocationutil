package cn.lixiaoqian.Util;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import com.unity3d.player.UnityPlayer;
import java.io.IOException;

public class AudioHelper extends Activity {

    public MediaPlayer mediaPlayer = new MediaPlayer();
    private String headUrl = "";
    private static final String TAG = "Unity";
    private Context context;
    public int CurIndex;
    public String CurUrl;
    public void AudioContext(Context ctt)
    {
        context= ctt;
    }

    public void Play(final int index, String name) throws IOException {
        CurUrl=name;
        Log.d("Unity", "播放原生本地音乐");
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(name);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                fileDescriptor.getStartOffset(),
                fileDescriptor.getLength());
        mediaPlayer.prepare();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                mediaPlayer.seekTo(index);
                mediaPlayer.start();
                if (listener != null) {
                    listener.OnStartListener();
                }
            }
        });

        Log.d("Unity", "开始播放原生本地音乐");
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("Unity", "结束播放原生本地音乐");
                UnityPlayer.UnitySendMessage("AudioManager", "MobileAudioEnd", "");
                if (listener != null) {
                    listener.OnEndListener();
                }
            }
        });
    }

    public void PlayNetWork(final int index,final String url) throws IOException {
        Log.d(TAG, "播放网络音乐");
        CurUrl=url;
        mediaPlayer.reset();
        mediaPlayer.setDataSource(headUrl + url);//设置播放的数据源。
        Log.d(TAG, "PlayNetWork: " + headUrl + "|" + url);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);//是否循环播放
        mediaPlayer.prepareAsync();//网络视频，异步
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.seekTo(index);
                mediaPlayer.start();
                if (listener != null) {
                    listener.OnStartListener();
                }
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "结束播放网络音乐");
                UnityPlayer.UnitySendMessage("AudioManager", "MobileAudioEnd", "");
                if (listener != null) {
                    listener.OnEndListener();
                }
            }
        });
    }

    public void Pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            CurIndex=mediaPlayer.getCurrentPosition();
        }
    }

    public void Continue()
    {
        if(!mediaPlayer.isPlaying())
        {
            try {
                PlayNetWork(CurIndex,CurUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Stop()
    {
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }

    public void SetVolume(float value)
    {
        if(mediaPlayer!=null)
        {
            mediaPlayer.setVolume(value,value);
        }
    }

    public void InitUrl(String url) {
        headUrl=url;
    }

    /**
     * 定义一个接口
     */
    public interface onListener{
        void OnStartListener();
        void OnEndListener();
    }
    /**
     *定义一个变量储存数据
     */
    private onListener listener;
    /**
     *提供公共的方法,并且初始化接口类型的数据
     */
    public void setListener( onListener listener){
        this.listener = listener;
    }
}

