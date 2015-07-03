package jp.romerome.roplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;

/**
 * Created by roman on 2015/07/01.
 */
public class PlayerService extends Service {

	public static final int STATE_PLAY = 0;
	public static final int STATE_PAUSE = 1;
	public static final int STATE_STOP = 2;
	public static final String ACTION_PLAY = "Ro_playpause";
	public static final String ACTION_NEXT = "Ro_next";
	public static final String ACTION_PREVIOUS = "Ro_previous";
	public static final String ACTION_NEW_PLAY = "Ro_newplay";

	private NotificationManager mNM;
	private final IBinder mBinder = new RoBinder();
	private MediaPlayer mp;
	private int mState = STATE_STOP;
	private ArrayList<StateChangeListener> mListeners;
	private ArrayList<Track> mCurrentPlaylist;
	private int mCurrentNo;

	@Override
	public void onCreate() {
		PlayerBroadcasrReceiver receiver = new PlayerBroadcasrReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_NEW_PLAY);
		filter.addAction(ACTION_NEXT);
		filter.addAction(ACTION_PLAY);
		filter.addAction(ACTION_PREVIOUS);
		registerReceiver(receiver,filter);

		mListeners = new ArrayList<>();
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mCurrentPlaylist = RoLibrary.getCurrentPlaylist(this);
		mCurrentNo = RoLibrary.getNo(this);
		if(mCurrentPlaylist.size() > 0 && mCurrentNo > 0 && mCurrentNo <= mCurrentPlaylist.size()) {
			setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1));
			setState(STATE_PAUSE);
		}
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

	private void showNotification(Track track) {
		Intent notificationIntent = new Intent(this, PlayActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContentTitle(track.title);
		builder.setContentText(track.album);
		builder.setSubText(track.artist);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(contentIntent);

		startForeground(R.string.app_name, builder.build());
	}

	public void playpause(){
		if(mp != null){
			if(!mp.isPlaying()) {
				play();
			} else {
				pause();
			}
		}
	}

	private void play(){
		if(mState == STATE_PAUSE){
			mp.start();
			setState(STATE_PLAY);
		}
		else if(mState == STATE_STOP){
			play(mCurrentPlaylist.get(mCurrentNo - 1));
		}
	}

	private void pause(){
		if(mState == STATE_PLAY){
			mp.pause();
			setState(STATE_PAUSE);
		}
	}

	public void seekTo(int time){
		if(mp != null){
			mp.seekTo(time);
		}
	}

	public void newPlay(){
		mCurrentPlaylist = RoLibrary.getCurrentPlaylist(this);
		mCurrentNo = RoLibrary.getNo(this);
		play(mCurrentPlaylist.get(mCurrentNo - 1));
	}

	public void next(){
		mCurrentNo++;
		if(mCurrentNo > mCurrentPlaylist.size()){
			mCurrentNo = 1;
			RoLibrary.setNo(this, mCurrentNo);
			setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1));
			setState(STATE_PAUSE);
		}
		else{
			RoLibrary.setNo(this, mCurrentNo);
			switch (mState){
				case STATE_PAUSE:
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1));
					break;

				case STATE_PLAY:
					play(mCurrentPlaylist.get(mCurrentNo - 1));
					break;
			}
		}
	}

	public void previous(){
		if(getElpsedTime() > 3000){
			seekTo(0);
		} else {
			mCurrentNo--;
			if (mCurrentNo < 1) {
				mCurrentNo = 1;
			}
			RoLibrary.setNo(this, mCurrentNo);
			switch (mState) {
				case STATE_PAUSE:
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1));
					break;

				case STATE_PLAY:
					play(mCurrentPlaylist.get(mCurrentNo - 1));
					break;
			}
		}
	}

	private void play(Track track){
		setNewTrack(track);
		mp.start();
		setState(STATE_PLAY);
	}

	public int getState(){
		return mState;
	}

	public int getElpsedTime(){
		if(mp == null){
			return -1;
		}
		return mp.getCurrentPosition();
	}

	private void setNewTrack(Track track){
		if(mp != null){
			mp.stop();
			mp.release();
		}
		mp = new MediaPlayer();
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				next();
			}
		});
		try {
			mp.setDataSource(track.path);
			mp.prepare();
		} catch (IOException e) {
			Log.d("TEST", e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
		showNotification(track);
		onTrackChange(track);
	}

	private void setState(int state){
		if(mState != state) {
			mState = state;
			for(StateChangeListener listener : mListeners){
				listener.onStateChange(mState);
			}
		}
	}

	private void onTrackChange(Track track){
		for(StateChangeListener listener : mListeners){
			listener.onTrackChange(track,mCurrentNo,mCurrentPlaylist.size());
		}
	}

	public void addStateChangeListener(StateChangeListener listener){
		mListeners.add(listener);
	}

	public class RoBinder extends Binder {
		public PlayerService getService(){
			return PlayerService.this;
		}
	}

	public interface StateChangeListener extends EventListener{
		public void onStateChange(int state);

		public void onTrackChange(Track track,int no,int playlistSize);
	}

	public class PlayerBroadcasrReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action != null){
				switch (action){
					case ACTION_NEW_PLAY:
						newPlay();
						break;

					case ACTION_PLAY:
						playpause();
						break;

					case ACTION_NEXT:
						next();
						break;

					case ACTION_PREVIOUS:
						previous();
						break;
				}
			}
		}
	}

}
