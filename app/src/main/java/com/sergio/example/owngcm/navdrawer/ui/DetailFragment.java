/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sergio.example.owngcm.navdrawer.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.common.Attraction;
import com.sergio.example.owngcm.provider.RouteContract;
import com.sergio.example.owngcm.utils.UIUtils;


/**
 * The tourist attraction detail fragment which contains the details of a
 * a single attraction (contained inside
 * {@link com.sergio.example.owngcm.navdrawer.ui.DetailActivity}).
 */
public class DetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String EXTRA_ATTRACTION = "attraction";

    private Attraction mAttraction = new Attraction();
    
    private static final int FORECAST_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data. Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            RouteContract.RouteEntry.TABLE_NAME + "." + RouteContract.RouteEntry._ID,
            RouteContract.RouteEntry.COLUMN_NAME_ROUTE,
            RouteContract.RouteEntry.COLUMN_DESCRIPTION,
            RouteContract.RouteEntry.COLUMN_TOPICS,
            RouteContract.RouteEntry.COLUMN_CITY_NAME_INIT,
            RouteContract.RouteEntry.COLUMN_START_DATE,
            RouteContract.RouteEntry.COLUMN_MAX_ATTENDEES,
            RouteContract.RouteEntry.COLUMN_URL_CHAT_COVER,
            RouteContract.RouteEntry.COLUMN_SEATS_AVAILABLE,
            RouteContract.RouteEntry.COLUMN_WEBSAFE_KEY,
            RouteContract.RouteEntry.COLUMN_ORGANIZER_DISPLAY_NAME,
            RouteContract.RouteEntry.COLUMN_TOPIC_GCM,
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these must change.
    public static final int COL_ROUTE_ID = 0;
    public static final int COL_NAME_ROUTE = 1;
    public static final int COL_DESCRIPTION = 2;
    public static final int COL_TOPICS = 3;
    public static final int COL_CITY_NAME_INIT = 4;
    public static final int COL_START_DATE = 5;
    public static final int COL_MAX_ATTENDEES = 6;
    public static final int COL_URL_CHAT_COVER = 7;
    public static final int COL_SEATS_AVAILABLE = 8;
    public static final int COL_WEBSAFE_KEY = 9;
    public static final int COL_ORGANIZER_DISPLAY_NAME = 10;
    public static final int COL_TOPIC_GCM = 11;

    TextView nameTextView;
    TextView descTextView;
    TextView cityAndDateView;
    TextView distanceTextView;
    ImageView imageView;

    public static DetailFragment createInstance(Long routeId) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ATTRACTION, routeId);
        detailFragment.setArguments(bundle);
        return detailFragment;
    }

    public DetailFragment() {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Long routeId = getArguments().getLong(EXTRA_ATTRACTION);
        if (routeId == 0) {
            getActivity().finish();
            return;
        }
        // TODO: Implement to catch routeId and catch from provider
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        descTextView = (TextView) view.findViewById(R.id.descriptionTextView);
        cityAndDateView = (TextView) view.findViewById(R.id.cityAndDate);
        distanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        imageView = (ImageView) view.findViewById(R.id.imageView);


        //TODO: Change when add module common
        /*
        int imageSize = getResources().getDimensionPixelSize(R.dimen.image_size)
                * Constants.IMAGE_ANIM_MULTIPLIER;
        * */

        int imageSize = getResources().getDimensionPixelSize(R.dimen.image_size)
                * 2;

        Glide.with(getActivity())
                .load("http")
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.color.lighter_gray)
                .override(imageSize, imageSize)
                .into(imageView);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        
        Long routeId = getArguments().getLong(EXTRA_ATTRACTION);

        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.
        Uri routesUri = RouteContract.RouteEntry.buildRouteUri(routeId);

        return new CursorLoader(getActivity(),
                routesUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToNext();
        mAttraction.setRouteDataFromCursor(data);

        nameTextView.setText(mAttraction.getName());
        descTextView.setText(mAttraction.getDescription());
        cityAndDateView.setText(mAttraction.getCityNameInit() + ", " +
                UIUtils.getConferenceDate(getActivity(), mAttraction.getStartDate()));
        // TODO: change to real distance
        distanceTextView.setText(String.valueOf(mAttraction.getSeatsAvailable()));

        Glide.with(getActivity())
                .load(mAttraction.getUrlChatCoveer())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(200, 200) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        imageView.setImageBitmap(resource);
                    }
                });

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
