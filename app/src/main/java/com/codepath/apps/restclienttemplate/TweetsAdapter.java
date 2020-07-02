package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

import static android.app.Activity.RESULT_OK;
import static androidx.core.app.ActivityCompat.startActivityForResult;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;
    TwitterClient client;

    public static final String REPLY_ID = "reply_id";
    public static final int REQUEST_CODE = 15;

    public TweetsAdapter(Context context, List<Tweet> tweets, TwitterClient client) {
        this.context = context;
        this.tweets = tweets;
        this.client = client;
    }

    // inflate layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // bind values based on position
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data
        Tweet tweet = tweets.get(position);

        // bind tweet with view holder
        holder.bind(tweet);

    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear(); // modify rather than re-assign
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> tweetList) {
        tweets.addAll(tweetList);
        notifyDataSetChanged();
    }

    // pass in context and list of tweets

    // for each row, inflate layout

    // bind values based on position of element

    // define view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvTime;
        ImageView ivMedia;
        ImageButton ibHeart;
        TextView tvLikes;
        ImageButton ibReply;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ibHeart = itemView.findViewById(R.id.ibHeart);
            tvLikes = itemView.findViewById(R.id.tvLikes);

            ibHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    final Tweet tweet = tweets.get(position);
                    final int count = Integer.valueOf(tvLikes.getText().toString());

                    client.isLiked(tweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            JSONObject jsonObject = json.jsonObject;
                            try {
                                Boolean isLiked = jsonObject.getBoolean("favorited");
                                if (!isLiked) {
                                    ibHeart.setImageResource(R.drawable.ic_vector_heart_red);
                                    client.sendLike(tweet.id, new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                                            Log.i("TweetsAdapter", "onSuccess");
                                            tvLikes.setText(String.valueOf(count + 1));
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                            Log.e("TweetsAdapter", "onFailure send", throwable);
                                        }
                                    });
                                } else {
                                    ibHeart.setImageResource(R.drawable.ic_vector_heart);
                                    client.destroyLike(tweet.id, new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                                            Log.i("TweetsAdapter", "onSuccess");
                                            ibHeart.setImageResource(R.drawable.ic_vector_heart);
                                            tvLikes.setText(String.valueOf(count - 1));
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                            Log.e("TweetsAdapter", "onFailure destroy", throwable);
                                        }
                                    });
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                        }
                    });

                }
            });



        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.handle);
            tvTime.setText(Tweet.getRelativeTimeAgo(tweet.time));
            Glide.with(context).load(tweet.user.publicImageUrl).circleCrop().into(ivProfileImage);
            if (tweet.media != null) {
                int radius = 20;
                Glide.with(context).load(tweet.media).override(1024, 1024).transform(new RoundedCorners(radius)).into(ivMedia);
            }
            tvLikes.setText(String.valueOf(tweet.user.likes));

            client.isLiked(tweet.id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    JSONObject jsonObject = json.jsonObject;
                    try {
                        Boolean isLiked = jsonObject.getBoolean("favorited");
                        if (isLiked) {
                            ibHeart.setImageResource(R.drawable.ic_vector_heart_red);
                        } else {
                            ibHeart.setImageResource(R.drawable.ic_vector_heart);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                }
            });

        }
    }
}
