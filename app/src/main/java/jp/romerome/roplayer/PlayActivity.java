package jp.romerome.roplayer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by roman on 2015/06/30.
 */
public class PlayActivity extends AppCompatActivity implements PlayerService.StateChangeListener{

	public static final String INTENT_KEY = "TRACK_ID";
	private Track mTrack;
	private TextView mArtistView;
	private TextView mTitleView;
	private TextView mAlbumView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_plane);
		setContentView(R.layout.activity_play);
		mTrack = RoLibrary.getCurrentTrack(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("");
		mArtistView = (TextView) toolbar.findViewById(R.id.artist);
		mTitleView = (TextView) toolbar.findViewById(R.id.title);
		mAlbumView = (TextView) toolbar.findViewById(R.id.album);

		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		PlayFragment fragment = new PlayFragment();
		fragment.setStateChangeListener(this);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.body, fragment)
				.commit();

		updateView();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case android.R.id.home:
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateView(){
		if(mTrack != null) {
			mArtistView.setText(mTrack.artist);
			mTitleView.setText(mTrack.title);
			mAlbumView.setText(mTrack.album);
		}
	}

	@Override
	public void onStateChange(int state) {

	}

	@Override
	public void onTrackChange(Track track,int no,int playlistSize) {
		mTrack = track;
		updateView();
	}

	@Override
	public void onRepeatModeChange(int repeatMode) {

	}

}
