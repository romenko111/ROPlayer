package jp.romerome.roplayer;

/**
 * Created by roman on 2015/06/28.
 */
public class Artist {

	public long id;
	public String artist;
	public int albums;
	public int tracks;

	public Artist(long id,String artist,int albums,int tracks){
		this.id = id;
		this.artist = artist;
		this.albums = albums;
		this.tracks = tracks;
	}


}
