package jp.romerome.roplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Created by roman on 2015/06/27.
 */
public class ImageGetTask extends AsyncTask<Track,Void,Bitmap> {
	private ImageView imageView;
	private String    tag;
	private MediaMetadataRetriever mmr;
	private Context mContext;

	public ImageGetTask(Context context,ImageView _image){
		super();
		imageView = _image;
		tag   =  imageView.getTag().toString();
		mmr = new MediaMetadataRetriever();
		mContext = context;
	}

	@Override
	protected Bitmap doInBackground(Track... params) {
		//Bitmap bitmap = ImageCache.getImage(params[0].albumId);
		Bitmap bitmap =null;
		if(bitmap==null){
			try {
				mmr.setDataSource(params[0].path);
				byte[] data = mmr.getEmbeddedPicture();
				if (data == null) {
					bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
				} else {
					bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				}
				float scale = Math.min((float) imageView.getWidth() / bitmap.getWidth(), (float) imageView.getHeight() / bitmap.getHeight());
				Matrix matrix = new Matrix();
				matrix.postScale(scale, scale);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				//ImageCache.setImage(params[0].albumId, bitmap);
			}
			catch (Exception e){
				bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
			}
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if(tag.equals(imageView.getTag())) imageView.setImageBitmap(result);
	}


	public static Bitmap decodeBitmap(String path, int width, int height){
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, width, height);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float)height / (float)reqHeight);
			} else {
				inSampleSize = Math.round((float)width / (float)reqWidth);
			}
		}
		return inSampleSize;
	}


}
