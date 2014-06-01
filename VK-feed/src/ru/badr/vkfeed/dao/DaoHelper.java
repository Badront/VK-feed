package ru.badr.vkfeed.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ru.badr.vkfeed.VKBeanContainer;

/**
 * User: ABadretdinov
 * Date: 29.05.14
 * Time: 17:29
 */
public class DaoHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "vk_feed.db";
	private static final int DATABASE_VERSION = 1;

	public DaoHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		PostSourceDao dao= VKBeanContainer.getInstance().getPostSourceDao();
		dao.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		//todo do nothing
	}
}
