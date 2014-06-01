package ru.badr.vkfeed.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiCommunityFull;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;
import ru.badr.vkfeed.VKBeanContainer;
import ru.badr.vkfeed.dao.DaoHelper;
import ru.badr.vkfeed.dao.PostSource;
import ru.badr.vkfeed.dao.PostSourceDao;

/**
 * User: ABadretdinov
 * Date: 29.05.14
 * Time: 18:18
 */
public class PostSourceService
{
	private PostSourceDao postSourceDao;
	public void initialize(){
		postSourceDao= VKBeanContainer.getInstance().getPostSourceDao();
	}
	public void saveSource(Context context,VKUsersArray users){
		DaoHelper dataBaseHolder = new DaoHelper(context);
		SQLiteDatabase writableDatabase = dataBaseHolder.getWritableDatabase();
		try {
			for(VKApiUserFull user:users){
				postSourceDao.saveOrUpdate(writableDatabase,user);
			}
		} finally {
			writableDatabase.close();
		}
	}
	public void saveSource(Context context,VKApiCommunityArray groups){
		DaoHelper dataBaseHolder = new DaoHelper(context);
		SQLiteDatabase writableDatabase = dataBaseHolder.getWritableDatabase();
		try {
			for(VKApiCommunityFull group:groups){
				postSourceDao.saveOrUpdate(writableDatabase,group);
			}
		} finally {
			writableDatabase.close();
		}
	}
	public PostSource getSourceById(Context context,int id){
		DaoHelper dataBaseHolder = new DaoHelper(context);
		SQLiteDatabase readableDatabase = dataBaseHolder.getReadableDatabase();
		try {
			return postSourceDao.getById(readableDatabase,id);
		} finally {
			readableDatabase.close();
		}
	}
	public void clear(Context context){
		DaoHelper dataBaseHolder = new DaoHelper(context);
		SQLiteDatabase writableDatabase = dataBaseHolder.getWritableDatabase();
		try {
			postSourceDao.clear(writableDatabase);
		} finally {
			writableDatabase.close();
		}
	}
}
