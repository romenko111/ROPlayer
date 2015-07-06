package jp.romerome.roplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;

/**
 * Created by roman on 2015/07/01.
 */
public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {

	public static final int STATE_PLAY = 0;
	public static final int STATE_PAUSE = 1;
	public static final int STATE_STOP = 2;
	public static final String ACTION_PLAY = "Ro_playpause";
	public static final String ACTION_NEXT = "Ro_next";
	public static final String ACTION_PREVIOUS = "Ro_previous";
	public static final String ACTION_NEW_PLAY = "Ro_newplay";
	public static final String ACTION_REPEAT_MODE = "Ro_repeatmode";

	private NotificationManager mNM;
	private final IBinder mBinder = new RoBinder();
	private MediaPlayer mp;
	private int mState = STATE_STOP;
	private ArrayList<StateChangeListener> mListeners;
	private ArrayList<Track> mCurrentPlaylist;
	private int mCurrentNo;
	private RemoteViews mBigViews;
	private RemoteViews mSmallViews;
	private Notification mNotify;
	private int mRepeatMode = RoLibrary.REPEAT_OFF;
	private PlayerBroadcastReceiver mReceiver;

	@Override
	public void onCreate() {
		mReceiver = new PlayerBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_NEW_PLAY);
		filter.addAction(ACTION_NEXT);
		filter.addAction(ACTION_PLAY);
		filter.addAction(ACTION_PREVIOUS);
		filter.addAction(ACTION_REPEAT_MODE);
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(mReceiver, filter);

		mListeners = new ArrayList<>();
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mCurrentPlaylist = RoLibrary.getCurrentPlaylist(this);
		mCurrentNo = RoLibrary.getNo(this);
		mRepeatMode = RoLibrary.getRepeatMode(this);
		if(mCurrentPlaylist.size() > 0 && mCurrentNo > 0 && mCurrentNo <= mCurrentPlaylist.size()) {
			showNotification(mCurrentPlaylist.get(mCurrentNo - 1));
			setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),false);
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
		unregisterReceiver(mReceiver);
	}

	private void showNotification(Track track) {
		mBigViews = new RemoteViews(getPackageName(),R.layout.notify_big);
		mSmallViews = new RemoteViews(getPackageName(),R.layout.notify_small);
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		Bitmap bitmap;
		try {
			mmr.setDataSource(track.path);
			byte[] data = mmr.getEmbeddedPicture();
			if (data == null) {
				bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
			} else {
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			}
		} catch (Exception e) {
			bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
		}
		mBigViews.setImageViewBitmap(R.id.album_art, bitmap);
		mSmallViews.setImageViewBitmap(R.id.album_art, bitmap);

		mBigViews.setTextViewText(R.id.title, track.title);
		mBigViews.setTextViewText(R.id.artist, track.artist);
		mSmallViews.setTextViewText(R.id.title, track.title);
		mSmallViews.setTextViewText(R.id.artist, track.artist);

		Intent intent = new Intent(ACTION_PLAY);
		PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,0);
		mBigViews.setOnClickPendingIntent(R.id.btn_play, pi);
		mSmallViews.setOnClickPendingIntent(R.id.btn_play, pi);
		switch (mState){
			case STATE_PAUSE:
				mBigViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.play_small);
				mSmallViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.play_small);
				break;

			case STATE_PLAY:
				mBigViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.pause_small);
				mSmallViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.pause_small);
				break;
		}

		intent = new Intent(ACTION_NEXT);
		pi = PendingIntent.getBroadcast(this,0,intent,0);
		mBigViews.setOnClickPendingIntent(R.id.btn_next, pi);
		mSmallViews.setOnClickPendingIntent(R.id.btn_next, pi);

		intent = new Intent(ACTION_PREVIOUS);
		pi = PendingIntent.getBroadcast(this,0,intent,0);
		mBigViews.setOnClickPendingIntent(R.id.btn_previous, pi);
		mSmallViews.setOnClickPendingIntent(R.id.btn_previous, pi);

		intent = new Intent(ACTION_REPEAT_MODE);
		pi = PendingIntent.getBroadcast(this,0,intent,0);
		mBigViews.setOnClickPendingIntent(R.id.btn_repeat,pi);

		Intent notificationIntent = new Intent(this, PlayActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setContent(mSmallViews);
		builder.setWhen(System.currentTimeMillis());
		builder.setContentIntent(contentIntent);
		mNotify = builder.build();
		mNotify.bigContentView = mBigViews;
		startForeground(R.string.app_name, mNotify);
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
			setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),true);
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
		setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),true);
	}

	public void next(){
		mCurrentNo++;
		if(mCurrentNo > mCurrentPlaylist.size()) {
			mCurrentNo = 1;
			int repeatMode = RoLibrary.getRepeatMode(this);
			int playwith = RoLibrary.getPlaywith(this);
			if(repeatMode == RoLibrary.REPEAT_NEXT_ALBUM){
				ArrayList<Album> albums;
				Track track;
				int index;
				ArrayList<Track> tracks;
				if(playwith == RoLibrary.PLAYWITH_ALBUM){
					albums = RoLibrary.getAlbums(this);
					track = RoLibrary.getCurrentTrack(this);
					index = albums.indexOf(RoLibrary.getAlbum(this, track.albumId));
					index++;
					if(index > albums.size() - 1){
						index = 0;
					}
					tracks = RoLibrary.getTracksInAlbum(this,albums.get(index));
					RoLibrary.setCurrentPlaylist(this,tracks,playwith);
					mCurrentPlaylist = tracks;
				}
				else if(playwith == RoLibrary.PLAYWITH_ALBUM_ARTIST){
					track = RoLibrary.getCurrentTrack(this);
					albums = RoLibrary.getAlbumsInArtists(this,track.artistId);
					index = albums.indexOf(RoLibrary.getAlbum(this, track.albumId));
					index++;
					if(index > albums.size() - 1){
						index = 0;
					}
					tracks = RoLibrary.getTracksInAlbum(this, albums.get(index));
					RoLibrary.setCurrentPlaylist(this,tracks,playwith);
					mCurrentPlaylist = tracks;
				}
			}
		}
		RoLibrary.setNo(this, mCurrentNo);
			switch (mState){
				case STATE_PAUSE:
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),false);
					break;

				case STATE_PLAY:
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),true);
					break;
			}
	}

	public void previous(){
		if(getElpsedTime() > 3000){
			seekTo(0);
		} else {
			mCurrentNo--;
			if(mCurrentNo < 1) {
				mCurrentNo = mCurrentPlaylist.size();
				int repeatMode = RoLibrary.getRepeatMode(this);
				int playwith = RoLibrary.getPlaywith(this);
				if(repeatMode == RoLibrary.REPEAT_NEXT_ALBUM){
					ArrayList<Album> albums;
					Track track;
					int index;
					ArrayList<Track> tracks;
					if(playwith == RoLibrary.PLAYWITH_ALBUM){
						albums = RoLibrary.getAlbums(this);
						track = RoLibrary.getCurrentTrack(this);
						index = albums.indexOf(RoLibrary.getAlbum(this, track.albumId));
						index--;
						if(index < 0){
							index = albums.size() - 1;
						}
						tracks = RoLibrary.getTracksInAlbum(this,albums.get(index));
						RoLibrary.setCurrentPlaylist(this,tracks,playwith);
						mCurrentPlaylist = tracks;
						mCurrentNo = mCurrentPlaylist.size();
					}
					else if(playwith == RoLibrary.PLAYWITH_ALBUM_ARTIST){
						track = RoLibrary.getCurrentTrack(this);
						albums = RoLibrary.getAlbumsInArtists(this,track.artistId);
						index = albums.indexOf(RoLibrary.getAlbum(this, track.albumId));
						index--;
						if(index < 0){
							index = albums.size() - 1;
						}
						tracks = RoLibrary.getTracksInAlbum(this, albums.get(index));
						RoLibrary.setCurrentPlaylist(this,tracks,playwith);
						mCurrentPlaylist = tracks;
						mCurrentNo = mCurrentPlaylist.size();
					}
				}
			}
			RoLibrary.setNo(this, mCurrentNo);
			switch (mState){
				case STATE_PAUSE:
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),false);
					break;

				case STATE_PLAY:
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),true);
					break;
			}
		}
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

	private void setRepeatMode(int repeatMode){
		if(mRepeatMode != repeatMode) {
			mRepeatMode = repeatMode;
			RoLibrary.setRepeatMode(this, mRepeatMode);
			for(StateChangeListener listener : mListeners){
				listener.onRepeatModeChange(mRepeatMode);
			}

			switch (mRepeatMode){
				case RoLibrary.REPEAT_OFF:
					mBigViews.setInt(R.id.btn_repeat, "setBackgroundResource", R.drawable.repeat_off_small);
					break;

				case RoLibrary.REPEAT_NORMAL:
					mBigViews.setInt(R.id.btn_repeat, "setBackgroundResource", R.drawable.repeat_normal_small);
					break;

				case RoLibrary.REPEAT_TRACK:
					mBigViews.setInt(R.id.btn_repeat, "setBackgroundResource", R.drawable.repeat_track_small);
					break;

				case RoLibrary.REPEAT_OFF_TRACK:
					mBigViews.setInt(R.id.btn_repeat, "setBackgroundResource", R.drawable.repeat_off_track_small);
					break;

				case RoLibrary.REPEAT_NEXT_ALBUM:
					mBigViews.setInt(R.id.btn_repeat, "setBackgroundResource", R.drawable.repeat_next_album_small);
					break;
			}
			startForeground(R.string.app_name,mNotify);
		}
	}

	public void toggleRepeatMode(){
		int repeatMode = mRepeatMode;
		repeatMode++;
		repeatMode %= 5;
		setRepeatMode(repeatMode);
	}

	public int getRepeatMode(){
		return mRepeatMode;
	}

	private void setNewTrack(Track track, final boolean isPlay){
		if(mp != null){
			mp.stop();
			mp.release();
		}
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				if(isPlay){
					mp.start();
					setState(STATE_PLAY);
				}else{
					setState(STATE_PAUSE);
				}
			}
		});
		try {
			mp.setDataSource(track.path);
			mp.prepareAsync();
		} catch (IOException e) {
			Log.d("TEST", e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		}
		onTrackChange(track);
	}

	private void setState(int state){
		if(mState != state) {
			mState = state;
			switch (mState){
				case STATE_PAUSE:
					mBigViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.play_small);
					mSmallViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.play_small);
					break;

				case STATE_PLAY:
					mBigViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.pause_small);
					mSmallViews.setInt(R.id.btn_play, "setBackgroundResource", R.drawable.pause_small);
					break;
			}
			startForeground(R.string.app_name,mNotify);
			for(StateChangeListener listener : mListeners){
				listener.onStateChange(mState);
			}
		}
	}

	private void onTrackChange(Track track){
		for(StateChangeListener listener : mListeners){
			listener.onTrackChange(track,mCurrentNo,mCurrentPlaylist.size());
		}
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		Bitmap bitmap;
		try {
			mmr.setDataSource(track.path);
			byte[] data = mmr.getEmbeddedPicture();
			if (data == null) {
				bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
			} else {
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			}
		} catch (Exception e) {
			bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
		}
		mBigViews.setImageViewBitmap(R.id.album_art, bitmap);
		mBigViews.setTextViewText(R.id.title, track.title);
		mBigViews.setTextViewText(R.id.artist, track.artist);
		mSmallViews.setImageViewBitmap(R.id.album_art, bitmap);
		mSmallViews.setTextViewText(R.id.title, track.title);
		mSmallViews.setTextViewText(R.id.artist, track.artist);
		startForeground(R.string.app_name,mNotify);
	}

	public void addStateChangeListener(StateChangeListener listener){
		mListeners.add(listener);
	}

	public void removeStateChangeListener(StateChangeListener listener){
		if(mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		switch (RoLibrary.getRepeatMode(this)){
			case RoLibrary.REPEAT_OFF_TRACK:
				mCurrentNo++;
				if(mCurrentNo > mCurrentPlaylist.size()){
					mCurrentNo = 1;
				}
				RoLibrary.setNo(this, mCurrentNo);
				setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),false);
				break;

			case RoLibrary.REPEAT_TRACK:
				this.mp.seekTo(0);
				this.mp.start();
				break;

			case RoLibrary.REPEAT_OFF:
				mCurrentNo++;
				if(mCurrentNo > mCurrentPlaylist.size()){
					mCurrentNo = 1;
					RoLibrary.setNo(this, mCurrentNo);
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),false);
				}else{
					RoLibrary.setNo(this, mCurrentNo);
					setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),true);
				}
				break;

			case RoLibrary.REPEAT_NORMAL:
				mCurrentNo++;
				if(mCurrentNo > mCurrentPlaylist.size()) {
					mCurrentNo = 1;
				}
				RoLibrary.setNo(this, mCurrentNo);
				setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),true);
				break;

			case RoLibrary.REPEAT_NEXT_ALBUM:
				mCurrentNo++;
				if(mCurrentNo > mCurrentPlaylist.size()) {
					mCurrentNo = 1;
					int repeatMode = RoLibrary.getRepeatMode(this);
					int playwith = RoLibrary.getPlaywith(this);
					if(repeatMode == RoLibrary.REPEAT_NEXT_ALBUM){
						ArrayList<Album> albums;
						Track track;
						int index;
						ArrayList<Track> tracks;
						if(playwith == RoLibrary.PLAYWITH_ALBUM){
							albums = RoLibrary.getAlbums(this);
							track = RoLibrary.getCurrentTrack(this);
							index = albums.indexOf(RoLibrary.getAlbum(this, track.albumId));
							index++;
							if(index > albums.size() - 1){
								index = 0;
							}
							tracks = RoLibrary.getTracksInAlbum(this,albums.get(index));
							RoLibrary.setCurrentPlaylist(this,tracks,playwith);
							mCurrentPlaylist = tracks;
						}
						else if(playwith == RoLibrary.PLAYWITH_ALBUM_ARTIST){
							track = RoLibrary.getCurrentTrack(this);
							albums = RoLibrary.getAlbumsInArtists(this,track.artistId);
							index = albums.indexOf(RoLibrary.getAlbum(this, track.albumId));
							index++;
							if(index > albums.size() - 1){
								index = 0;
							}
							tracks = RoLibrary.getTracksInAlbum(this, albums.get(index));
							RoLibrary.setCurrentPlaylist(this,tracks,playwith);
							mCurrentPlaylist = tracks;
						}
					}
				}
				RoLibrary.setNo(this, mCurrentNo);
				setNewTrack(mCurrentPlaylist.get(mCurrentNo - 1),true);
				break;
		}
	}

	public class RoBinder extends Binder {
		public PlayerService getService(){
			return PlayerService.this;
		}
	}

	public interface StateChangeListener extends EventListener{
		public void onStateChange(int state);

		public void onTrackChange(Track track,int no,int playlistSize);

		public void onRepeatModeChange(int repeatMode);
	}

	public class PlayerBroadcastReceiver extends BroadcastReceiver{

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

					case ACTION_REPEAT_MODE:
						toggleRepeatMode();
						break;

					case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
						pause();
						break;
				}
			}
		}
	}

}
