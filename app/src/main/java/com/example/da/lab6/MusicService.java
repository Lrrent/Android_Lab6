package com.example.da.lab6;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.animation.RotateAnimation;

import java.io.IOException;

public class MusicService extends Service {
    private   MediaPlayer mediaPlayer = new MediaPlayer();
    public final IBinder binder = new MyBinder();
    public class MyBinder extends Binder{
        /*使用ontransact函数可以避免mediaplayer被其他类访问的情况,即可以将这些函数设置为private*/
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch(code){
                case 101:playOrPause();break;
                case 102:stop();break;
                case 103:reply.writeInt(mediaPlayer.getDuration());break;
                case 104:reply.writeInt(mediaPlayer.getCurrentPosition());break;
                case 105:mediaPlayer.seekTo(data.readInt());break;
            }
            return super.onTransact(code, data, reply, flags);
        }

        MusicService getService() {
            return MusicService.this;
        }

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }
    public MusicService() {
        try {
            mediaPlayer.setDataSource("/data/K.Will-Melt.mp3");
            Log.i("getdata", "MusicService: ");
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (IOException e) {
            Log.d("Hint", "Can't not open the song ");
            e.printStackTrace();
        }
    }
    private void playOrPause(){
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        else mediaPlayer.start();
    }
    private void stop(){
        if (mediaPlayer != null){
            mediaPlayer.stop();
            //刚开始的写法为停止后直接prepare然后seekto(0),但是会出现点击停止按钮后依然后播放一小段才能够完全停止的状况,然后改为以下的方法
            //停止后将mediaplayer重置后再重新加载音乐文件，这样就不会出现问题,具体原因未明,但是这种方法好像也不太好,待其他解决方法
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource("/data/K.Will-Melt.mp3");
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
