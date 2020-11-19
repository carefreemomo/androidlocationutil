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
    public String CurUrl="";
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
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if(listener!=null)
                {
                    listener.OnProgress(percent);
                    UnityPlayer.UnitySendMessage("AudioManager", "MobileAudioProgress", Integer.toString(percent));
                }
            }
        });
    }

    public void PlayNetWork(final int index,final String url) throws IOException {
        Log.d(TAG, "播放网络音乐");
        CurIndex = index;
        if(url != CurUrl) {
            CurUrl = url;
        }
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
                CurUrl="";
                CurIndex=0;
                Log.d(TAG, "结束播放网络音乐");
                UnityPlayer.UnitySendMessage("AudioManager", "MobileAudioEnd", "");
                if (listener != null) {
                    listener.OnEndListener();
                }
            }
        });

        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    while (true) {
//                        Log.i(TAG, "run: "+mediaPlayer.getCurrentPosition());
//                        Log.i(TAG, "run: "+mediaPlayer.getDuration());
                        Thread.sleep(500);// 线程暂停10秒，单位毫秒
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            float percent = (float)(mediaPlayer.getCurrentPosition()) / (float)(mediaPlayer.getDuration()) * 100;
//                            Log.i(TAG, "run2: "+percent);
                            UnityPlayer.UnitySendMessage("AudioManager", "MobileAudioProgress", Float.toString(percent));
                            if (listener != null) {
                                listener.OnProgress(percent);
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

        public void Pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            CurIndex=mediaPlayer.getCurrentPosition();
        }
    }

    public void Continue()
    {
        if(GetPlayStatus(CurUrl)==2)
        {
            mediaPlayer.seekTo(CurIndex);
            mediaPlayer.start();
        }
    }

    public void Stop() {
        if(mediaPlayer==null)
        {
            Log.i(TAG, "Stop1: ");
            return;
        }
        Log.i(TAG, "Stop2: "+mediaPlayer.isPlaying());
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            CurUrl="";
            CurIndex=0;
        }
    }

    public void SetVolume(float value)
    {
        if(mediaPlayer!=null)
        {
            mediaPlayer.setVolume(value,value);
        }
    }

    public int GetPlayStatus(String url) {
        //未播放
        int playing =0;
        //播放
        if(mediaPlayer.isPlaying()&&CurUrl.equals(url))
        {
            playing=1;
        }
        //暂停
        else if( !mediaPlayer.isPlaying()&&CurUrl.equals(url)&&CurIndex!=0)
        {
            playing =2;
        }
        return playing;
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
        void OnProgress(float progress);
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

