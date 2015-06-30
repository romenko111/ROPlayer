package jp.romerome.roplayer;

/**
 * Created by roman on 2015/06/27.
 */
public class Album {

	public long             id;
	public String           album;
	public long artistId;
	public String           artist;
	public int              tracks;

	public Album(long id,String album,long artistId,String artist,int tracks){
		this.id = id;
		this.album = album;
		this.artist = artist;
		this.tracks = tracks;
		this.artistId = artistId;
	}

}
