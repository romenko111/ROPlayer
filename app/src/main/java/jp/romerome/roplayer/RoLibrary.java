package jp.romerome.roplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by roman on 2015/06/27.
 */
public class RoLibrary {

	private Context context;
	private static ArrayList<Track> mTracks;
	private static ArrayList<Artist> mArtists;
	private static ArrayList<Album> mAlbums;
	private static Track mCurrentTrack;

	public RoLibrary(Context context){
		this.context = context;
	}

    public static void update(Context context){
        mTracks = new ArrayList<>();
		mArtists = new ArrayList<>();
		mAlbums = new ArrayList<>();

		updateTracks(context);
    }

	private static void updateTracks(Context context){
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
	}

	public static ArrayList<Artist> getArtists(){
		return mArtists;
	}

	public static Artist getArtist(long artistId){
		for(Artist artist : mArtists){
			if(artist.id == artistId){
				return artist;
			}
		}
		return null;
	}

	public static ArrayList<Track> getTracks(){
		return mTracks;
	}

	public static Track getTrack(long trackId){
		for(Track track : mTracks){
			if(track.id == trackId){
				return track;
			}
		}
		return null;
	}

	public static ArrayList<Track> getTracksInArtist(Artist artist){
		return getTracksInArtist(artist.id);
	}

	public static ArrayList<Track> getTracksInArtist(long artistId){
		ArrayList<Track> tracks = new ArrayList<>();
		for(Track track : mTracks){
			if(track.artistId == artistId){
				tracks.add(track);
			}
		}
		return tracks;
	}

	public static ArrayList<Track> getTracksInAlbum(long albumId){
		ArrayList<Track> tracks = new ArrayList<>();
		for(Track track : mTracks){
			if(track.albumId == albumId){
				tracks.add(track);
			}
		}
		Collections.sort(tracks, new TrackNumComparator());
		return tracks;
	}

	public static ArrayList<Track> getTracksInAlbum(Album album){
		return getTracksInAlbum(album.id);
	}

	public static ArrayList<Album> getAlbums() {
		return mAlbums;
	}

	public static ArrayList<Album> getAlbumsInArtists(long artistId){
		ArrayList<Album> albums = new ArrayList<>();
		for(Album album : mAlbums){
			if(album.artistId == artistId){
				albums.add(album);
			}
		}
		return albums;
	}

	public static ArrayList<Album> getAlbumsInArtists(Artist artist){
		return getAlbumsInArtists(artist.id);
	}

	public static Album getAlbum(long albumId){
		for(Album album : mAlbums){
			if(album.id == albumId){
				return album;
			}
		}
		return null;
	}

	public static String getDuration(long duration){
		long dm = duration/60000;
		long ds = (duration-(dm*60000))/1000;
		return String.format("%d:%02d", dm, ds);
	}

	public static String getDuration(Track track){
		return getDuration(track.duration);
	}

	public static Track getCurrentTrack(){
		return mCurrentTrack;
	}

	public static void setCurrentTrack(Track track){
		mCurrentTrack = track;
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
