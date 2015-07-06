package jp.romerome.roplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 2015/06/28.
 */
public class ListArtistAdapter extends ArrayAdapter<Artist>{

	LayoutInflater mInflater;
	MediaMetadataRetriever mmr;
	Context mContext;

	public ListArtistAdapter(Context context, List item){
		super(context, 0, item);
		mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		mmr = new MediaMetadataRetriever();
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView,ViewGroup parent){

		Artist item = getItem(position);
		ViewHolder holder;

		if(convertView==null){
			convertView = mInflater.inflate(R.layout.artist_item, null);
			holder = new ViewHolder();
			holder.tracksTextView    = (TextView)convertView.findViewById(R.id.tracks);
			holder.artistTextView   = (TextView)convertView.findViewById(R.id.artist);
			holder.albumsTextView = (TextView)convertView.findViewById(R.id.albums);
			holder.albumartView = (ImageView) convertView.findViewById(R.id.album_art);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
			holder.task.cancel(true);
		}

		holder.artistTextView.setText(item.artist);
		holder.tracksTextView.setText(String.valueOf(mContext.getString(R.string.title)+"("+item.tracks+")"));
		holder.albumsTextView.setText(String.valueOf(mContext.getString(R.string.album)+"("+item.albums+")"));
		holder.albumartView.setImageBitmap(null);

		ArrayList<Track> tracks = RoLibrary.getTracksInArtist(mContext,item);
		holder.albumartView.setTag(tracks.get(0).path);
		holder.task = new ImageGetTask(mContext, holder.albumartView);
		holder.task.execute(tracks.get(0));

		return convertView;
	}

	static class ViewHolder{
		TextView  tracksTextView;
		TextView  artistTextView;
		TextView  albumsTextView;
		ImageView albumartView;
		ImageGetTask task;
	}
}
