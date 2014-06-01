package ru.badr.vkfeed.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.*;
import ru.badr.vkfeed.R;
import ru.badr.vkfeed.utils.DateUtils;

import java.util.Date;

/**
 * User: Histler
 * Date: 29.05.14
 */
public class NewsFeedAdapter extends BaseAdapter {
    private VKPostArray posts;
	private VKApiCommunityArray groups;
	private VKUsersArray profiles;
    private LayoutInflater inflater;
	private DisplayImageOptions options;
	private ImageLoader imageLoader;
    public NewsFeedAdapter(Context context) {
        inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.posts=new VKPostArray();
		this.groups=new VKApiCommunityArray();
		this.profiles=new VKUsersArray();
		options = new DisplayImageOptions.Builder()
				//.cacheInMemory(true)
				.showImageOnLoading(R.drawable.gray_element)
				.cacheOnDisc(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		imageLoader=ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public VKApiPost getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return posts.get(position).id;
    }

	public void addGroups(VKApiCommunityArray groups){
		if(groups!=null){
			for(VKApiCommunityFull group:groups){
				if(!this.groups.contains(group)){
					this.groups.add(group);
				}
			}
		}
	}

	public void addProfiles(VKUsersArray profiles){
		if(profiles!=null){
			for(VKApiUserFull user:profiles){
				if(!this.profiles.contains(user)){
					this.profiles.add(user);
				}
			}
		}
	}

	public void addNews(/*int position*/boolean fromStart, VKPostArray newPosts){
        if(newPosts!=null){
			if(fromStart){
				/*int postsSize=newPosts.size();
				for(int i=postsSize-1;i>=0;i--){
					VKApiPost post=newPosts.get(i);
					if(!posts.contains(post)){
						posts.add(0,post);
					}
				}*/
                posts.addAll(0,newPosts);
            }else {
				/*for(VKApiPost post:newPosts){
					if(!posts.contains(post)){
						posts.add(post);
					}
				}*/
                posts.addAll(newPosts);
            }
            notifyDataSetChanged();
        }
    }

	public static class NewsFeedHolder{
		public ImageView icon;
		public TextView author;
		public TextView time;
		public TextView message;

		TextView comments;
		TextView reposts;
		TextView likes;
		LinearLayout copyHistory;
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
		NewsFeedHolder holder;
		if(vi==null){
			holder=new NewsFeedHolder();
			vi=inflater.inflate(R.layout.news_feed_row,null,false);
			holder.icon= (ImageView)vi.findViewById(R.id.icon);
			holder.author= (TextView)vi.findViewById(R.id.author);
			holder.time= (TextView)vi.findViewById(R.id.time);
			holder.message= (TextView)vi.findViewById(R.id.message);

			holder.comments= (TextView)vi.findViewById(R.id.comments);
			holder.reposts= (TextView)vi.findViewById(R.id.reposts);
			holder.likes= (TextView)vi.findViewById(R.id.likes);
			holder.copyHistory= (LinearLayout)vi.findViewById(R.id.copy_history_holder);
			vi.setTag(holder);
		}
		else {
			holder= (NewsFeedHolder)vi.getTag();
		}
		VKApiPost post=getItem(position);
		int source=post.source_id;
		setAuthorToView(holder, source);

		holder.message.setText(Html.fromHtml(post.text));

		Date postDate=new Date(post.date*1000);
		holder.time.setText(DateUtils.getDateTimeFormat().format(postDate));

		holder.comments.setText(String.valueOf(post.comments_count));
		holder.reposts.setText(String.valueOf(post.reposts_count));
		holder.likes.setText(String.valueOf(post.likes_count));
		holder.copyHistory.removeAllViews();
		//добавляем в пост посты, которые в репосте
		if(post.copy_history!=null&&post.copy_history.size()>0){
			holder.copyHistory.setVisibility(View.VISIBLE);
			for(VKApiPost innerPost:post.copy_history){
				View innerPostView=inflater.inflate(R.layout.news_feed_copy_history_item,null,false);
				NewsFeedHolder innerHolder=new NewsFeedHolder();
				innerHolder.author= (TextView)innerPostView.findViewById(R.id.author);
				innerHolder.icon= (ImageView)innerPostView.findViewById(R.id.icon);
				innerHolder.time= (TextView)innerPostView.findViewById(R.id.time);
				innerHolder.message=(TextView)innerPostView.findViewById(R.id.message);

				int innerSource=innerPost.from_id;
				setAuthorToView(innerHolder,innerSource);

				innerHolder.message.setText(Html.fromHtml(innerPost.text));

				Date innerPostDate=new Date(innerPost.date*1000);
				innerHolder.time.setText(DateUtils.getDateTimeFormat().format(innerPostDate));

				holder.copyHistory.addView(innerPostView);
			}
		}
		else {
			holder.copyHistory.setVisibility(View.GONE);
		}
        return vi;
    }

	private void setAuthorToView(NewsFeedHolder holder, int source)
	{
		if(source<0){
			VKApiCommunityFull community=groups.getById(source*-1);
			if(community!=null){
				holder.author.setText(community.name);
				imageLoader.displayImage(community.photo_50,holder.icon,options);
			}
			else {
				holder.author.setText("");
				holder.icon.setImageResource(R.drawable.gray_element);
			}
		}
		else {
			VKApiUserFull user=profiles.getById(source);
			if(user!=null){
				holder.author.setText(
						(user.first_name!=null?user.first_name:"")+
								" "+
								(user.last_name!=null?user.last_name:""));
				imageLoader.displayImage(user.photo_50, holder.icon, options);
			}
			else {
				holder.author.setText("");
				holder.icon.setImageResource(R.drawable.gray_element);
			}
		}
	}
}
