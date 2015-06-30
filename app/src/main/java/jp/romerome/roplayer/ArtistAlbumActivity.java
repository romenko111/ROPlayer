package jp.romerome.roplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class ArtistAlbumActivity extends AppCompatActivity {

	public static final String INTENT_KEY = "ARTIST_ID";
	private Artist mArtist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_toolbar);
		Intent intent = getIntent();
		mArtist = RoLibrary.getArtist(intent.getLongExtra(INTENT_KEY,-1));

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setLogo(R.mipmap.ic_launcher);
		toolbar.setTitle(mArtist.artist);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		Fragment fragment = new ArtistAlbumFragment();
		Bundle args = new Bundle();
		args.putLong(INTENT_KEY,mArtist.id);
		fragment.setArguments(args);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.body, fragment)
				.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
