package cn.lixiaoqian.LocationUtil;
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

    public void Play(Context ctt, String name) throws IOException {
        Log.d("Unity", "播放原生本地音乐");
        AssetFileDescriptor  fileDescriptor = ctt.getAssets().openFd(name);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
        fileDescriptor.getStartOffset(),
        fileDescriptor.getLength());
        mediaPlayer.prepare();
        mediaPlayer.start();
        Log.d("Unity", "开始播放原生本地音乐");
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("Unity", "结束播放原生本地音乐");
                UnityPlayer.UnitySendMessage("AudioManager", "MobilePreparedCallBack","");
            }
        });
    }

   public void PlayNetWork(String url) throws IOException {
        Log.d("Unity", "播放网络音乐");
        mediaPlayer.reset();
        mediaPlayer.setDataSource(url);//设置播放的数据源。
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);//是否循环播放
        mediaPlayer.prepareAsync();//网络视频，异步
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
           @Override
           public void onPrepared(MediaPlayer mp) {
           mediaPlayer.start();
           }
       });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           @Override
           public void onCompletion(MediaPlayer mp) {
               Log.d("Unity", "结束播放网络音乐");
               UnityPlayer.UnitySendMessage("AudioManager", "MobilePreparedCallBack","");
           }
       });
    }

    public void Pause()
    {
        mediaPlayer.pause();
    }

    public void Stop()
    {
        mediaPlayer.stop();
    }
}

