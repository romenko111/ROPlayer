package jp.romerome.roplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by roman on 2015/07/03.
 */
public class Database {


	public static void setCurrentPlaylist(Context context,ArrayList<Track> playlist){
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(DatabaseHelper.TABLE_NAME, null, null);

			for (Track track : playlist) {
				ContentValues values = new ContentValues();
				values.put(DatabaseHelper.COLUMN_TRACK_ID, track.id);
				db.insert(DatabaseHelper.TABLE_NAME, null, values);
			}
			db.setTransactionSuccessful();
		}finally {
			db.endTransaction();
		}
		//db.close();
	}

	public static ArrayList<Track> getCurrentPlaylist(Context context){
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ArrayList<Track> playlist = new ArrayList<>();
		String query = "select * from " + DatabaseHelper.TABLE_NAME +";";
		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();

		for(int i=0;i < cursor.getCount();i++){
			int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TRACK_ID));
			playlist.add(RoLibrary.getTrack(context, id));
			cursor.moveToNext();
		}
		cursor.close();
		//db.close();
		return playlist;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		private static final String DATABASE_NAME = "RoLibrary.db";
		private static final int DATABASE_VERSION = 1;
		private static final String ID = "_id";

		private static final String TABLE_NAME = "CurrentPlaylist";
		private static final String COLUMN_TRACK_ID = "TrackID";
		private static DatabaseHelper instance = null;

		public static synchronized DatabaseHelper getInstance(Context context){
			if(instance == null){
				instance = new DatabaseHelper(context);
			}
			return instance;
		}

		private DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String query = "create table " + TABLE_NAME + "(" +
					ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					COLUMN_TRACK_ID + " INTEGER);";
			db.execSQL(query);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists " + TABLE_NAME);
			onCreate(db);
		}
	}

}
