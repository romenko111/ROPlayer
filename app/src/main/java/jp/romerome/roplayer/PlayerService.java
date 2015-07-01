package jp.romerome.roplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.EventListener;

/**
 * Created by roman on 2015/07/01.
 */
public class PlayerService extends Service {

	public static final int STATE_PLAY = 0;
	public static final int STATE_PAUSE = 1;
	public static final int STATE_STOP = 2;

	private NotificationManager mNM;
	private final IBinder mBinder = new RoBinder();
	MediaPlayer mp;
	Track mTrack;
	private int mState = STATE_STOP;
	private StateChangeListener mListener;

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		// Display a notification about us starting.
		showNotification();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onDestroy() {
		mNM.cancel(R.string.app_name);
		mp.stop();
		mp.release();
		mp = null;
	}

	private void showNotification() {
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.mipmap.ic_launcher)    // アイコン
				.setTicker("Hello")                      // 通知バーに表示する簡易メッセージ
				.setWhen(System.currentTimeMillis())     // 時間
				.setContentTitle("My notification")      // 展開メッセージのタイトル
				.setContentText("Hello Notification!!")  // 展開メッセージの詳細メッセージ
				.setContentIntent(contentIntent)         // PendingIntent
				.build();
		mNM.notify(R.string.app_name,notification);
	}

	public boolean isPlaying(){
		if(mp == null)return  false;
		return mp.isPlaying();
	}

	public void play(){
		if(mp != null && !mp.isPlaying() && mState == STATE_PAUSE){
			mp.start();
			setState(STATE_PLAY);
		}
	}

	public void play(Track track){
		setNewTrack(track);
		mp.start();
		setState(STATE_PLAY);
	}

	public void pause(){
		if(mp != null && mp.isPlaying() && mState == STATE_PLAY){
			mp.pause();
			setState(STATE_PAUSE);
		}
	}

	private void setNewTrack(Track track){
		mTrack = track;
		if(mp != null){
			mp.stop();
			mp.release();
		}
		mp = new MediaPlayer();
		try {
			mp.setDataSource(mTrack.path);
			mp.prepare();
		} catch (IOException e) {
			Log.d("TEST", e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private void setState(int state){
		mState = state;
		if(mListener != null){
			mListener.onStateChange(mState);
		}
	}

	public void setOnStateChangeListener(StateChangeListener listener){
		mListener = listener;
	}

	public class RoBinder extends Binder {
		public PlayerService getService(){
			return PlayerService.this;
		}
	}

	public interface StateChangeListener extends EventListener{
		public void onStateChange(int state);
	}

}
