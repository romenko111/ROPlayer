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

public class ListAlbumAdapter extends ArrayAdapter<Album>{

	LayoutInflater mInflater;
	MediaMetadataRetriever mmr;
	Context mContext;

	public ListAlbumAdapter(Context context, List item){
		super(context, 0, item);
		mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		mmr = new MediaMetadataRetriever();
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView,ViewGroup parent){

		Album item = getItem(position);
		ViewHolder holder;

		if(convertView==null){
			convertView = mInflater.inflate(R.layout.album_item, null);
			holder = new ViewHolder();
			holder.tracksTextView    = (TextView)convertView.findViewById(R.id.tracks);
			holder.artistTextView   = (TextView)convertView.findViewById(R.id.artist);
			holder.albumTextView = (TextView)convertView.findViewById(R.id.album);
			holder.albumartView = (ImageView) convertView.findViewById(R.id.album_art);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
			holder.task.cancel(true);
		}

		holder.artistTextView.setText(item.artist);
		holder.tracksTextView.setText(String.valueOf(mContext.getString(R.string.title)+"("+item.tracks+")"));
		holder.albumTextView.setText(item.album);
		holder.albumartView.setImageBitmap(null);

		ArrayList<Track> tracks = RoLibrary.getTracksInAlbum(mContext,item);
		holder.albumartView.setTag(tracks.get(0).path);
		holder.task = new ImageGetTask(mContext, holder.albumartView);
		holder.task.execute(tracks.get(0));

		return convertView;
	}

	static class ViewHolder{
		TextView  tracksTextView;
		TextView  artistTextView;
		TextView  albumTextView;
		ImageView albumartView;
		ImageGetTask task;
	}
}
