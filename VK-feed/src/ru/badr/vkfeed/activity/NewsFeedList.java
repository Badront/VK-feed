package ru.badr.vkfeed.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.*;
import com.vk.sdk.api.model.VKApiNews;
import com.vk.sdk.api.model.VKApiPost;
import ru.badr.vkfeed.Constants;
import ru.badr.vkfeed.R;
import ru.badr.vkfeed.VKBeanContainer;
import ru.badr.vkfeed.adapter.NewsFeedAdapter;
import ru.badr.vkfeed.service.PostSourceService;

/**
 * User: Histler
 * Date: 28.05.14
 */
public class NewsFeedList extends VKActivity implements PullToRefreshBase.OnLastItemVisibleListener
{
    private String nextFrom =null;
    private String userId;
    private PullToRefreshListView listView;
    private NewsFeedAdapter adapter;
	private PostSourceService postSourceService=VKBeanContainer.getInstance().getPostSourceService();

    private MenuItem refreshing;
    private MenuItem refresh;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_feed,menu);
        refreshing=menu.findItem(R.id.refreshing);
        refresh=menu.findItem(R.id.refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                VKSdk.logout();
                VKAccessToken.removeTokenAtKey(this, Constants.VK_ACCESS_TOKEN);
                startActivity(new Intent(this,LoginScreen.class));
                finish();
                return true;
            case R.id.refresh:
                loadNews(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_feed);
		ActionBar actionBar=getSupportActionBar();
		actionBar.setTitle(R.string.news_feed);
        listView= (PullToRefreshListView) findViewById(R.id.listView);
        VKAccessToken accessToken= VKAccessToken.tokenFromSharedPreferences(this, Constants.VK_ACCESS_TOKEN);
        userId=accessToken.userId;
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNews(true);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //в pullToRefresh id возвращается всегда на 1 меньше, чем нужно.
				VKApiPost post=adapter.getItem(position-1);
				Intent intent=new Intent(NewsFeedList.this,NewsFeedItem.class);
				intent.putExtra("post",post);
				startActivity(intent);
            }
        });
        adapter=new NewsFeedAdapter(this);
        listView.setAdapter(adapter);
		listView.setOnLastItemVisibleListener(this);
        loadNews(false);
    }

    private void loadNews(final boolean fromRefresh) {
        if(refreshing!=null&&refresh!=null){
            AnimationDrawable animationDrawable= (AnimationDrawable) refreshing.getIcon();
            refreshing.setVisible(true);
            animationDrawable.start();
            refresh.setVisible(false);
        }

        VKParameters vkParameters=VKParameters.from(VKApiConst.OWNER_ID,userId);

        if(!fromRefresh&&!TextUtils.isEmpty(nextFrom)){
            vkParameters.put(VKApiConst.START_FROM, nextFrom);
        }
        if(fromRefresh&&adapter!=null&&adapter.getCount()>0){
            VKApiPost post=adapter.getItem(0);
            //todo проверить
            vkParameters.put(VKApiConst.START_TIME,post.date+1);
        }
        VKRequest request= VKApi.newsfeed().get(vkParameters);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                if(refreshing!=null&&refresh!=null){
                    AnimationDrawable animationDrawable= (AnimationDrawable) refreshing.getIcon();
                    animationDrawable.stop();
                    refreshing.setVisible(false);
                    refresh.setVisible(true);
                }
                if(response.parsedModel!=null){
                    VKApiNews vkNews= (VKApiNews) response.parsedModel;
                    if(!fromRefresh){
						if(!TextUtils.isEmpty(vkNews.next_from)){
							nextFrom =vkNews.next_from;
						}
					}
					if(vkNews.groups!=null){
						postSourceService.saveSource(NewsFeedList.this,vkNews.groups);
						adapter.addGroups(vkNews.groups);
					}
					if(vkNews.profiles!=null){
						postSourceService.saveSource(NewsFeedList.this,vkNews.profiles);
						adapter.addProfiles(vkNews.profiles);
					}
                    adapter.addNews(fromRefresh,vkNews.items);
                }
				if(fromRefresh){
                	listView.onRefreshComplete();
				}
				if(alreadyLoading){
					alreadyLoading=false;
				}
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                if(refreshing!=null&&refresh!=null){
                    AnimationDrawable animationDrawable= (AnimationDrawable) refreshing.getIcon();
                    animationDrawable.stop();
                    refreshing.setVisible(false);
                    refresh.setVisible(true);
                }
                listView.onRefreshComplete();
				if(alreadyLoading){
					alreadyLoading=false;
				}
            }
        });
    }

    private boolean doubleBackToExitPressedOnce=false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					postSourceService.clear(NewsFeedList.this);
				}
			}).start();
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.back_toast), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;

            }
        }, 2000);
    }
	private boolean alreadyLoading=false;
	@Override
	public void onLastItemVisible()
	{
		if(!alreadyLoading){
			alreadyLoading=true;
			loadNews(false);
		}
	}
}
