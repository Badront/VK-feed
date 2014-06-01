package ru.badr.vkfeed.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.*;
import ru.badr.vkfeed.R;
import ru.badr.vkfeed.VKBeanContainer;
import ru.badr.vkfeed.adapter.NewsFeedAdapter;
import ru.badr.vkfeed.dao.PostSource;
import ru.badr.vkfeed.service.PostSourceService;
import ru.badr.vkfeed.utils.DateUtils;

import java.text.MessageFormat;
import java.util.Date;

/**
 * User: ABadretdinov
 * Date: 29.05.14
 * Time: 17:03
 */
public class NewsFeedItem extends VKActivity {
    private VKApiPost post;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_feed_info);
        options = new DisplayImageOptions.Builder()
                //.cacheInMemory(true)
                .showImageOnLoading(R.drawable.gray_element)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader = ImageLoader.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.news_feed_item);
        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey("post")) {
            post = args.getParcelable("post");
            initPost();
        }
    }

    PostSourceService service = VKBeanContainer.getInstance().getPostSourceService();

    private void initPost() {
        PostSource postSource = service.getSourceById(this,
                post.source_id != 0 ?
                        post.source_id
                        : post.from_id != 0 ?
                        post.from_id
                        : post.signer_id);

        ((TextView) findViewById(R.id.author)).setText(postSource.name);
        imageLoader.displayImage(postSource.photo_50, (ImageView) findViewById(R.id.icon), options);

        Date postDate = new Date(post.date * 1000);
        ((TextView) findViewById(R.id.time)).setText(DateUtils.getDateTimeFormat().format(postDate));

        ((TextView) findViewById(R.id.message)).setText(Html.fromHtml(post.text));

        ((TextView) findViewById(R.id.reposts)).setText(String.valueOf(post.reposts_count));
        ((TextView) findViewById(R.id.comments)).setText(String.valueOf(post.comments_count));
        ((TextView) findViewById(R.id.likes)).setText(String.valueOf(post.likes_count));

        initAttachments();
        initInnerPosts();
    }

    private MediaPlayer mediaPlayer;
    private TextView lastPlayerTV;

    private void initAttachments() {
        LinearLayout attachmentsHolder = (LinearLayout) findViewById(R.id.attachments);
        attachmentsHolder.removeAllViews();
        if (post.attachments != null && post.attachments.size() > 0) {
            attachmentsHolder.setVisibility(View.VISIBLE);
            for (VKAttachments.VKApiAttachment attachment : post.attachments) {
                View attachmentView;
                if (VKAttachments.TYPE_PHOTO.equals(attachment.getType()) || VKAttachments.TYPE_POSTED_PHOTO.equals(attachment.getType())) {
                    attachmentView = new ImageView(this);
                    VKApiPhoto photo=(VKApiPhoto) attachment;
                    //todo проверить
                    final String imageUrl=photo.photo_604!=null?photo.photo_604:photo.photo_130;
                    if(!TextUtils.isEmpty(imageUrl)){
                        imageLoader.displayImage(imageUrl, (ImageView) attachmentView, options);
                        attachmentView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(imageUrl), "image/*");
                                startActivity(intent);
                                }
                        });
                    }
                } else if (VKAttachments.TYPE_AUDIO.equals(attachment.getType())) {
                    attachmentView = getLayoutInflater().inflate(R.layout.audio_view, null, false);
                    TextView playText = (TextView) attachmentView;
                    final VKApiAudio audio = (VKApiAudio) attachment;
                    playText.setText((audio.artist != null ? audio.artist : "") + "-" + (audio.title != null ? audio.title : ""));
                    playText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            killMediaPlayer();
                            if (lastPlayerTV != null) {
                                lastPlayerTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_play, 0, 0, 0);
                                //если нажали на тот трек, что был до этого, то вырубаем просто
                                if (lastPlayerTV.equals(v)) {
                                    lastPlayerTV = null;
                                    return;
                                }
                            }
                            lastPlayerTV = (TextView) v;
                            lastPlayerTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.audio_pause, 0, 0, 0);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mediaPlayer = new MediaPlayer();
                                        mediaPlayer.setDataSource(audio.url);
                                        mediaPlayer.prepare();
                                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mp) {
                                                mediaPlayer.start();
                                            }
                                        });
                                        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                            @Override
                                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                                return true;
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    });
                } else if (VKAttachments.TYPE_VIDEO.equals(attachment.getType())) {
                    attachmentView = getLayoutInflater().inflate(R.layout.video_view, null, false);
                    ImageView imageView = (ImageView) attachmentView.findViewById(R.id.preview);
                    final VKApiVideo video = (VKApiVideo) attachment;
                    imageLoader.displayImage(video.photo_320, imageView, options);
                    attachmentView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String link = video.mp4_360 != null ? video.mp4_360 : video.mp4_240;
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            if (TextUtils.isEmpty(link)) {
                                Uri data = Uri.parse(MessageFormat.format("http://vk.com/video{0}_{1}", String.valueOf(video.owner_id), String.valueOf(video.id)));
                                intent.setData(data);
                            } else {
                                Uri data = Uri.parse(link);
                                intent.setDataAndType(data, "video/*");
                            }
                            startActivity(intent);
                        }
                    });
                } else if (VKAttachments.TYPE_POST.equals(attachment.getType())) {
                    attachmentView = getViewForInnerPost((VKApiPost) attachment);
                } else if (VKAttachments.TYPE_LINK.equals(attachment.getType())) {
                    attachmentView = new TextView(this);
                    VKApiLink link = (VKApiLink) attachment;
                    TextView textView = (TextView) attachmentView;
                    textView.setText(Html.fromHtml("<a href=\"" + link.url + "\">" + link.description + "</a>"));
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    attachmentView = new TextView(this);
                    ((TextView) attachmentView).setText(MessageFormat.format(getString(R.string.attachment_format_not_supported), attachment.getType()));
                }

                attachmentsHolder.addView(attachmentView);
            }
        } else {
            attachmentsHolder.setVisibility(View.GONE);
        }
    }

    private void initInnerPosts() {
        LinearLayout copyHistoryHolder = (LinearLayout) findViewById(R.id.copy_history_holder);
        copyHistoryHolder.removeAllViews();
        if (post.copy_history != null && post.copy_history.size() > 0) {
            copyHistoryHolder.setVisibility(View.VISIBLE);
            for (VKApiPost innerPost : post.copy_history) {
                View innerPostView = getViewForInnerPost(innerPost);

                copyHistoryHolder.addView(innerPostView);
            }
        } else {
            copyHistoryHolder.setVisibility(View.GONE);
        }
    }

    private View getViewForInnerPost(final VKApiPost innerPost) {
        View innerPostView = getLayoutInflater().inflate(R.layout.news_feed_copy_history_item, null, false);
        NewsFeedAdapter.NewsFeedHolder innerHolder = new NewsFeedAdapter.NewsFeedHolder();
        innerHolder.author = (TextView) innerPostView.findViewById(R.id.author);
        innerHolder.icon = (ImageView) innerPostView.findViewById(R.id.icon);
        innerHolder.time = (TextView) innerPostView.findViewById(R.id.time);
        innerHolder.message = (TextView) innerPostView.findViewById(R.id.message);


        PostSource postSource = service.getSourceById(this,
                innerPost.source_id != 0 ?
                        innerPost.source_id
                        : innerPost.from_id != 0 ?
                        innerPost.from_id
                        : innerPost.signer_id);
        innerHolder.author.setText(postSource.name);
        imageLoader.displayImage(postSource.photo_50, innerHolder.icon, options);

        innerHolder.message.setText(Html.fromHtml(innerPost.text));

        Date innerPostDate = new Date(innerPost.date * 1000);
        innerHolder.time.setText(DateUtils.getDateTimeFormat().format(innerPostDate));

        innerPostView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewsFeedItem.this, NewsFeedItem.class);
                intent.putExtra("post", innerPost);
                startActivity(intent);
            }
        });
        return innerPostView;
    }

    private void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        killMediaPlayer();
        super.onDestroy();
    }
}
