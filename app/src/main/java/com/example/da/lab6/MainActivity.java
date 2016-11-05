package com.example.da.lab6;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {
    private Animation imag_rotate;
    private ImageView music_img;
    private TextView status;
    private Button play;
    private Button stop;
    private Button quit;
    private MusicService musicService;
    private SeekBar seekbar;
    private TextView music_time,total_time;
    private ObjectAnimator animator;
    private int flag = 0;
    private IBinder iBinder;
    //test
    private IBinder mbinder = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicService  = new MusicService();
        findAllView();   //绑定控件
        play.setOnClickListener(new MyButton());
        stop.setOnClickListener(new MyButton());
        quit.setOnClickListener(new MyButton());
        connection();
        setAnimator();  //设置并开启动画
        //SimpleDateFormat time = new SimpleDateFormat("mm:ss");  //定义时间格式
        //total_time.setText(time.format(PlayerGetDuration_Position(103))); //将音乐长度格式化为时间并显示在total_time中
        //runnable这个线程会在handler所在线程执行,也就是UI线程
        //Log.i("connetct success", "onServiceConnected: ");
    }
    public void findAllView(){
        music_img = (ImageView) findViewById(R.id.music_img);
        status = (TextView) findViewById(R.id.statu);
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        quit = (Button) findViewById(R.id.quit);
        music_time = (TextView) findViewById(R.id.music_time);
        total_time = (TextView) findViewById(R.id.total_time);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        imag_rotate = AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotate); //加载动画,xml方式实现
    }
    public void stopRotate(){ //停止旋转动画
        music_img.clearAnimation();
    }
    class MyButton implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.play:
                    handler.post(runnable);
                    PlayerStart_Stop(101);
                    if(flag==1) {animator.pause();status.setText("Pause"); play.setText("Play");flag=0;;}
                    else{animator.resume();status.setText("Playing");play.setText("Pause");flag = 1;}
                    break;
                case R.id.stop:animator.end();PlayerStart_Stop(102); //新增
                    status.setText("Stopped");flag=0;play.setText("Play");setAnimator(); break;
                case R.id.quit: MainActivity.this.finish();unbindService(sc);System.exit(0);break;//退出程序要解绑服务
            }
        }
    }
    private void PlayerStart_Stop(int code){ //控制音乐播放或停止 101为播放 102为停止
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try{
            iBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private int PlayerGetDuration_Position(int code){ //获得音乐的时长以及当前播放时间 103 为时长 104为当前时间
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try{
            iBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return reply.readInt();
    }
    private void PlayerSeek(int position){ //设置播放器从给定时间点的音乐位置播放
        int code = 105;
        Parcel data = Parcel.obtain();
        data.writeInt(position);
        Parcel reply = Parcel.obtain();
        try{
            iBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void connection(){
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,sc, Context.BIND_AUTO_CREATE);
    }
    public ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           // musicService = ((MusicService.MyBinder)(service)).getService();
            iBinder = service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };
    public android.os.Handler handler = new android.os.Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Log.i("Running", "run: ");
            SimpleDateFormat time = new SimpleDateFormat("mm:ss");  //定义时间格式
            total_time.setText(time.format(PlayerGetDuration_Position(103)));
            music_time.setText(time.format(PlayerGetDuration_Position(104)));  //获得当前播放进度,并格式化为时间格式
            seekbar.setMax(PlayerGetDuration_Position(103));  //设置进度条最大数值
            seekbar.setProgress(PlayerGetDuration_Position(104));//设置进度条当前进度为音乐当前播放的位置
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {   //实现改变拖动条后设置当前音乐播放进度为相应时间
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        PlayerSeek(seekbar.getProgress()); //将当前音频播放位置设置为进度条的进度
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            handler.postDelayed(runnable, 100);
        }

    };
    public void setAnimator(){
        animator = new ObjectAnimator().ofFloat(music_img,"rotation",0,360);
        animator.setDuration(10000);
        animator.setRepeatCount(-1);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();  //额,先开始然后再暂停的原因是因为需要在第一次按下play按钮的时候开始动画,但之后的都需要设置为resume继续动画
        animator.pause();
    }
}
 //Mediaplayer详解:http://www.itnose.net/detail/6090452.html
/*异步任务实现更新ui*/
