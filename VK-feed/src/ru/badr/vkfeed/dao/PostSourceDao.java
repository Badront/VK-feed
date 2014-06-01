package ru.badr.vkfeed.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiUser;

/**
 * User: ABadretdinov
 * Date: 29.05.14
 * Time: 17:32
 */
public class PostSourceDao
{
	public static final String TABLE_NAME="post_sources";
	public static final String COLUMN_ID="_id";
	public static final String COLUMN_NAME="name";
	public static final String COLUMN_PHOTO_50="photo_50";
	public static final String COLUMN_PHOTO="photo";

	private static final String CREATE_QUERY=
			"create table if not exists " +
					TABLE_NAME+
					" (" +
					COLUMN_ID+" integer primary key, " +
					COLUMN_NAME+ " text default null, "+
					COLUMN_PHOTO+ " text default null, "+
					COLUMN_PHOTO_50+" text default null "+
					");";

	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_QUERY);
	}

	public long saveOrUpdate(SQLiteDatabase database, VKApiUser user){
		ContentValues values=new ContentValues();
		values.put(COLUMN_ID,user.getId());
		values.put(COLUMN_NAME,(user.first_name!=null?user.first_name:"")+" "+(user.last_name!=null?user.last_name:""));
		values.put(COLUMN_PHOTO_50,user.photo_50);
		values.put(COLUMN_PHOTO,user.photo_200);
		if(getById(database,user.getId())!=null){
			return database.update(
					TABLE_NAME,
					values,
					COLUMN_ID+"=?",
					new String[]{String.valueOf(user.getId())})>0?user.getId():0;
		}
		return database.insert(TABLE_NAME,null,values);
	}

	public long saveOrUpdate(SQLiteDatabase database,VKApiCommunity group){
        int id=-1*group.getId();
		ContentValues values=new ContentValues();
		values.put(COLUMN_ID,id);
		values.put(COLUMN_NAME,group.name);
		values.put(COLUMN_PHOTO_50,group.photo_50);
		values.put(COLUMN_PHOTO,group.photo_200);
		if(getById(database,id)!=null){
			return database.update(
					TABLE_NAME,
					values,
					COLUMN_ID+"=?",
					new String[]{String.valueOf(id)})>0?id:0;
		}
		return database.insert(TABLE_NAME,null,values);
	}

	public PostSource getById(SQLiteDatabase database,int id){
		Cursor cursor=database.query(TABLE_NAME,new String[]{
				COLUMN_ID,
				COLUMN_NAME,
				COLUMN_PHOTO,
				COLUMN_PHOTO_50},
				COLUMN_ID+"=?",new String[]{String.valueOf(id)},null,null,null);
		PostSource postSource=null;
		if(cursor.moveToFirst()){
			postSource=new PostSource();
			postSource.id=cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
			postSource.name=cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
			postSource.photo=cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO));
			postSource.photo_50=cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_50));
		}
		cursor.close();
		return postSource;
	}

	public void clear(SQLiteDatabase database){
		database.delete(TABLE_NAME,null,null);
	}
}
