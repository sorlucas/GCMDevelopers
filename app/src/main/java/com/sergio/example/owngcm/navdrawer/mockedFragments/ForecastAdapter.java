package com.sergio.example.owngcm.navdrawer.mockedFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.api.client.util.DateTime;
import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.navdrawer.ui.DetailActivity;
import com.sergio.example.owngcm.navdrawer.widget.CursorRecyclerViewAdapter;
import com.sergio.example.owngcm.provider.RouteContract;
import com.sergio.example.owngcm.sync.SyncAdapter;
import com.sergio.example.owngcm.utils.UIUtils;

import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorRecyclerViewAdapter<ForecastAdapter.ViewHolder> {

    private static final String TAG = makeLogTag(ForecastAdapter.class);

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    private Context mContext;
    private Activity mActivity;

    interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    private ItemClickListener sDummyCallbacks = new ItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            View heroView = view.findViewById(android.R.id.icon);
            DetailActivity.launch(mActivity, getItemId(position), heroView);
        }
    };

    public ForecastAdapter(Activity activity, Cursor cursor){
        super(activity.getApplicationContext(),cursor);
        this.mContext = activity.getApplicationContext();
        this.mActivity = activity;
    }
    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {
        CardView cardView;
        ImageView photoRouteCover;
        TextView titleView;
        TextView descriptionView;
        TextView cityAndDateView;
        ImageView registerIconView;
        ImageView registerCheckView;
        TextView organizerName;

        ItemClickListener mItemClickListener;

        public ViewHolder(View view, ItemClickListener itemClickListener) {
            super(view);
            cardView = (CardView)view.findViewById(R.id.cardView);
            photoRouteCover = (ImageView) cardView.findViewById(R.id.imageRouteCover);
            titleView = (TextView)cardView.findViewById(R.id.textChatName);
            descriptionView = (TextView)cardView.findViewById(R.id.textDescription);
            cityAndDateView = (TextView)cardView.findViewById(R.id.cityAndDateView);
            registerIconView = (ImageView)cardView.findViewById(R.id.imageViewRegisteredIcon);
            registerCheckView = (ImageView)cardView.findViewById(R.id.imageViewRegistered);
            organizerName = (TextView)cardView.findViewById(R.id.textViewOraganizerName);

            mItemClickListener = itemClickListener;
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Choose the layout type
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.conference_row;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                // TODO: change tu other layout (future day)
                layoutId = R.layout.conference_row;
                break;
            }
        }

        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);

        ViewHolder vh = new ViewHolder(itemView, sDummyCallbacks);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {

        // TODO: Implementar onBindViewHolder with viewType
        int viewType = getItemViewType(cursor.getPosition());
        /*
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                // Get weather icon
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
                        cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID)));
                break;
            }
        }
        */

        String nameChat = cursor.getString(FragmentListChats.COL_NAME_ROUTE);
        String description = cursor.getString(FragmentListChats.COL_DESCRIPTION);
        String topics = cursor.getString(FragmentListChats.COL_TOPICS);
        String cityNameInit = cursor.getString(FragmentListChats.COL_CITY_NAME_INIT);
        String startDate = cursor.getString(FragmentListChats.COL_START_DATE);
        String maxAttendees= cursor.getString(FragmentListChats.COL_MAX_ATTENDEES);
        String urlChatCover = cursor.getString(FragmentListChats.COL_URL_CHAT_COVER);
        String seatsAvailable= cursor.getString(FragmentListChats.COL_SEATS_AVAILABLE);
        final String webSafeKey= cursor.getString(FragmentListChats.COL_WEBSAFE_KEY);
        String organizerDisplayName= cursor.getString(FragmentListChats.COL_ORGANIZER_DISPLAY_NAME);
        String topicGcm = cursor.getString(FragmentListChats.COL_TOPIC_GCM);
        final int isRegistered = cursor.getInt(FragmentListChats.COL_REGISTERED);



        //mImageloaderCover.loadImage("",conferenceViewHolder.photoRouteCover);
        //TODO: Change Glide to ImageLoader
        final ImageView mImageView = viewHolder.photoRouteCover;
        Glide.with(mContext)
                .load(urlChatCover)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(100, 100) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        mImageView.setImageBitmap(resource);
                    }
                });

        // put information on Views
        viewHolder.registerCheckView.setVisibility(isRegistered==1 ? View.VISIBLE : View.INVISIBLE);
        viewHolder.titleView.setText(nameChat);
        viewHolder.descriptionView.setText(description);
        viewHolder.cityAndDateView.setText(cityNameInit + ", " +
                UIUtils.getConferenceDate(mContext, new DateTime(startDate).getValue()));
        viewHolder.organizerName.setText(organizerDisplayName);


        viewHolder.registerIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

                //Create Base Extras, registration and unregistration
                final Bundle bundle = new Bundle();
                bundle.putString(RouteContract.RouteEntry.COLUMN_WEBSAFE_KEY, webSafeKey);
                bundle.putInt(RouteContract.RouteEntry.COLUMN_REGISTERED, isRegistered);

                alertDialogBuilder
                        .setTitle("Estado Chat")
                        .setMessage("Actividad")
                        .setPositiveButton(isRegistered == 1 ? R.string.button_unregister
                                : R.string.button_register, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                SyncAdapter.initializeSyncAdapter(mContext, SyncAdapter.SYNC_MODE_REGISTRATION_CHAT, bundle);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();
            }
        });

    }
    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }
}