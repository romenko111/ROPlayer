package jp.romerome.roplayer;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by roman on 2015/06/27.
 */
public class ImageCache {
	private static HashMap<Long,Bitmap> cache = new HashMap<Long,Bitmap>();

	public static Bitmap getImage(Long key) {
		if (cache.containsKey(key)) {
			return cache.get(key);
		}
		return null;
	}

	public static void setImage(Long key, Bitmap image) {
		cache.put(key, image);
	}
}
