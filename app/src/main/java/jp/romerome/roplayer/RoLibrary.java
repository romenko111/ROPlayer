package jp.romerome.roplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by roman on 2015/06/27.
 */
public class RoLibrary {

	private static final String KEY_NO = "No";
	private static final String KEY_REPEAT = "Reapeat";
	public static final String KEY_PLAYWITH = "Playwith";

	public static final int REPEAT_OFF = 0;
	public static final int REPEAT_NORMAL = 1;
	public static final int REPEAT_TRACK = 2;
	public static final int REPEAT_OFF_TRACK = 3;
	public static final int REPEAT_NEXT_ALBUM = 4;

	public static final int PLAYWITH_ALBUM = 0;
	public static final int PLAYWITH_ALBUM_ARTIST = 2;
	public static final int PLAYWITH_TRACK = 1;

	private static ArrayList<Track> mTracks;
	private static ArrayList<Artist> mArtists;
	private static ArrayList<Album> mAlbums;
	private static ArrayList<Track> mCurrentPlaylist;
	private static int mNo;
	private static int mRepeatMode;
	private static int mPlaywith;
	private static boolean check = false;

    public static void update(Context context){
        mTracks = new ArrayList<>();
		mArtists = new ArrayList<>();
		mAlbums = new ArrayList<>();
		String[] COLUMNS = {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ARTIST_ID,
				MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.TRACK,
		};
		ContentResolver resolver = context.getContentResolver();
		String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
		String[] selectionArgs = new String[]{"/storage/MicroSD/Music/%"};
		Cursor cursor = resolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				COLUMNS,
				null,
				null,
				MediaStore.Audio.Media.TITLE + " ASC"
		);
		while( cursor.moveToNext() ){
			Track track = new Track(cursor);
			mTracks.add(track);

			boolean exist = false;
			for(Artist artist : mArtists){
				exist = false;
				if(artist.id == track.artistId){
					exist = true;
					artist.tracks++;
					break;
				}
			}
			if(!exist){
				Artist artist = new Artist(track.artistId,track.artist,0,1);
				mArtists.add(artist);
			}

			for(Album album : mAlbums){
				exist = false;
				if(album.id == track.albumId){
					exist = true;
					album.tracks++;
					break;
				}
			}
			if(!exist){
				Album album = new Album(track.albumId,track.album,track.artistId,track.artist,1);
				mAlbums.add(album);
			}
		}
		cursor.close();
		for(Artist artist : mArtists){
			for(Album album : mAlbums){
				if(album.artistId == artist.id){
					artist.albums++;
				}
			}
		}
		check = true;
		mCurrentPlaylist = Database.getCurrentPlaylist(context);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		mNo = sp.getInt(KEY_NO, -1);
		mRepeatMode = sp.getInt(KEY_REPEAT,REPEAT_OFF);
		mPlaywith = sp.getInt(KEY_PLAYWITH,PLAYWITH_TRACK);
    }

	public static int getNo(Context context){
		if(!check){
			update(context);
		}
		return mNo;
	}

	public static void setNo(Context context,int no){
		mNo = no;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(KEY_NO,mNo);
		editor.commit();
	}

	public static int getRepeatMode(Context context){
		if(!check){
			update(context);
		}
		return mRepeatMode;
	}

	public static void setRepeatMode(Context context ,int repeatMode){
		mRepeatMode = repeatMode;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(KEY_REPEAT,mRepeatMode);
		editor.commit();
	}

	public static ArrayList<Track> getCurrentPlaylist(Context context){
		if(!check){
			update(context);
		}
		return mCurrentPlaylist;
	}

	public static void setCurrentPlaylist(final Context context,ArrayList<Track> playlist,int playwith){
		mCurrentPlaylist = playlist;
		mPlaywith = playwith;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(KEY_PLAYWITH,mPlaywith);
		editor.commit();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Database.setCurrentPlaylist(context,mCurrentPlaylist);
			}
		}).start();
	}

	public static void setCurrentPlaylist(Context context,long albumId,int playwith){
		setCurrentPlaylist(context, getTracksInAlbum(context, albumId), playwith);
	}

	public static int getPlaywith(Context context){
		if(!check){
			update(context);
		}
		return mPlaywith;
	}

	public static ArrayList<Artist> getArtists(Context context){
		if(!check){
			update(context);
		}
		return mArtists;
	}

	public static Track getCurrentTrack(Context context){
		if(!check){
			update(context);
		}
		if(mCurrentPlaylist.size() == 0 || mNo < 0 || mNo > mCurrentPlaylist.size()){
			return null;
		}
		return mCurrentPlaylist.get(mNo - 1);
	}

	public static Artist getArtist(Context context,long artistId){
		if(!check){
			update(context);
		}
		for(Artist artist : mArtists){
			if(artist.id == artistId){
				return artist;
			}
		}
		return null;
	}

	public static ArrayList<Track> getTracks(Context context){
		if(!check){
			update(context);
		}
		return mTracks;
	}

	public static Track getTrack(Context context,long trackId){
		if(!check){
			update(context);
		}
		for(Track track : mTracks){
			if(track.id == trackId){
				return track;
			}
		}
		return null;
	}

	public static ArrayList<Track> getTracksInArtist(Context context,Artist artist){
		if(!check){
			update(context);
		}
		return getTracksInArtist(context,artist.id);
	}

	public static ArrayList<Track> getTracksInArtist(Context context,long artistId){
		if(!check){
			update(context);
		}
		ArrayList<Track> tracks = new ArrayList<>();
		for(Track track : mTracks){
			if(track.artistId == artistId){
				tracks.add(track);
			}
		}
		return tracks;
	}

	public static ArrayList<Track> getTracksInAlbum(Context context,long albumId){
		if(!check){
			update(context);
		}
		ArrayList<Track> tracks = new ArrayList<>();
		for(Track track : mTracks){
			if(track.albumId == albumId){
				tracks.add(track);
			}
		}
		Collections.sort(tracks, new TrackNumComparator());
		return tracks;
	}

	public static ArrayList<Track> getTracksInAlbum(Context context,Album album){
		if(!check){
			update(context);
		}
		return getTracksInAlbum(context,album.id);
	}

	public static ArrayList<Album> getAlbums(Context context) {
		if(!check){
			update(context);
		}
		return mAlbums;
	}

	public static ArrayList<Album> getAlbumsInArtists(Context context,long artistId){
		if(!check){
			update(context);
		}
		ArrayList<Album> albums = new ArrayList<>();
		for(Album album : mAlbums){
			if(album.artistId == artistId){
				albums.add(album);
			}
		}
		return albums;
	}

	public static ArrayList<Album> getAlbumsInArtists(Context context,Artist artist){
		if(!check){
			update(context);
		}
		return getAlbumsInArtists(context,artist.id);
	}

	public static Album getAlbum(Context context,long albumId){
		if(!check){
			update(context);
		}
		for(Album album : mAlbums){
			if(album.id == albumId){
				return album;
			}
		}
		return null;
	}

	public static String getStringTime(long time){
		long dm = time/60000;
		long ds = (time-(dm*60000))/1000;
		return String.format("%d:%02d", dm, ds);
	}

	public static String getStringTime(Track track){
		return getStringTime(track.duration);
	}

	private static class TrackNumComparator implements Comparator<Track> {
		public int compare(Track a, Track b){
			if(a.trackNo > b.trackNo)
				return 1;
			else if(a.trackNo == b.trackNo)
				return 0;
			else
				return -1;
		}
	}
}
