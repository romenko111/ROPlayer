package jp.romerome.roplayer;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by roman on 2015/06/27.
 */
public class Track {

    public long     id;             //�R���e���g�v���o�C�_�ɓo�^���ꂽID
    public long     albumId;        //�������g���b�N�̃A���o����ID
    public long     artistId;       //�������g���b�N�̃A�[�e�B�X�g��ID
    public String   path;           //���f�[�^��PATH
    public String   title;          //�g���b�N�^�C�g��
    public String   album;          //�A���o���^�C�g��
    public String   artist;         //�A�[�e�B�X�g��
    public Uri      uri;            // URI
    public long     duration;       // �Đ�����(�~���b)
    public int      trackNo;        // �A���o���̃g���b�N�i���o

    public  Track(Cursor cursor)
    {
        id              = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
        path            = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        title           = cursor.getString( cursor.getColumnIndex( MediaStore.Audio.Media.TITLE ));
		artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
		album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
        albumId         = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        artistId        = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
        duration        = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        trackNo         = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)) % 100;
        uri             = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

}
