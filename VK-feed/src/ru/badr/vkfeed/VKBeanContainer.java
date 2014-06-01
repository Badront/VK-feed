package ru.badr.vkfeed;

import ru.badr.vkfeed.dao.PostSourceDao;
import ru.badr.vkfeed.service.PostSourceService;

/**
 * User: ABadretdinov
 * Date: 29.05.14
 * Time: 17:30
 */
public class VKBeanContainer
{
	private static final Object MONITOR = new Object();
	private static VKBeanContainer instance = null;

	public static VKBeanContainer getInstance(){
		if(instance!=null){
			return instance;
		}
		synchronized (MONITOR){
			if(instance==null){
				instance=new VKBeanContainer();
			}
		}
		return instance;
	}

	private PostSourceDao postSourceDao;
	private PostSourceService postSourceService;

	private VKBeanContainer(){
		postSourceDao=new PostSourceDao();
		postSourceService=new PostSourceService();
	}

	public void initialize(){
		postSourceService.initialize();
	}

	public PostSourceDao getPostSourceDao(){
		return postSourceDao;
	}

	public PostSourceService getPostSourceService()
	{
		return postSourceService;
	}
}
