package com.sergio.example.owngcm.navdrawer.mockedFragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.model.DecoratedConference;
import com.sergio.example.owngcm.navdrawer.widget.AttractionsRecyclerView;
import com.sergio.example.owngcm.provider.RouteContract;
import com.sergio.example.owngcm.sync.RegisterConferenceLoader;
import com.sergio.example.owngcm.sync.SyncAdapter;
import com.sergio.example.owngcm.utils.AccountUtils;
import com.sergio.example.owngcm.utils.StringUtils;

import java.util.List;

import static com.sergio.example.owngcm.utils.LogUtils.LOGD;
import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * Created by syp on 18/08/15.
 */
public class FragmentListChats extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String TAG = makeLogTag(FragmentListChats.class);

    // Specific launch type sync to SyncAdapter
    private String mTypeModeSync;

    private ForecastAdapter mAdapter;
    private AttractionsRecyclerView mRecyclerView;

    // Deteremine actual position RecycleView. When rotate savedInstances
    private int mPosition = RecyclerView.INVALID_TYPE;
    private static final String SELECTED_KEY = "selected_position";


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
            RouteContract.RouteEntry.COLUMN_REGISTERED
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
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
    public static final int COL_REGISTERED = 12;

    /**
     * Create a new instance of FragmentListChats, providing "typeMode" of sync as an argument.
     * @param typeModeSync SYNC_MODE_SEARCH,
     *                     SYNC_MODE_ATTENDES_CHATS
     *                     or SYNC_MODE_REGISTRATION_CHAT
     * @return new Instance FragmentListChats with arguments
     */
    public static FragmentListChats newInstance(String typeModeSync) {
        LOGD(TAG, "FragmentListChats.newInstance(typeModeSync)");

        FragmentListChats f = new FragmentListChats();

        // Put type SYNC_MDE_TYPE as an argument.
        Bundle args = new Bundle();
        args.putString(SyncAdapter.SYNC_MODE_TYPE, typeModeSync);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        mTypeModeSync = getArguments() != null
                ? getArguments().getString(SyncAdapter.SYNC_MODE_TYPE) : null;

        // TODO: cambiar initializeSyncAdapter (Temporizador) para BaseActivity. Sincronizar todo en newely
        SyncAdapter.initializeSyncAdapter(getActivity(), mTypeModeSync, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_sessions, container, false);
        mRecyclerView = (AttractionsRecyclerView) root.findViewById(R.id.sessions_collection_view);
        mRecyclerView.setEmptyView(root.findViewById(android.R.id.empty));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.list_columns)));
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Init Loader to catch information from contentProvider
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        String selection = null;
        String[] selectionArg = null;
        String sortOrder = RouteContract.RouteEntry.COLUMN_START_DATE + " ASC";

        if (mTypeModeSync.equals(SyncAdapter.SYNC_MODE_ATTENDES_CHATS)) {
            // Pas to provider
            //chat/organizer_display_name = ?
            selection =
                    RouteContract.RouteEntry.TABLE_NAME +
                            "." + RouteContract.RouteEntry.COLUMN_REGISTERED + " = ? ";
            selectionArg = new String[]{"1"};
        }

        // Con selection= null y selectionArg = null, devuelve todas
        return new CursorLoader(getActivity(),
                RouteContract.RouteEntry.CONTENT_URI,
                FORECAST_COLUMNS,
                selection,
                selectionArg,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dataCursor) {

        mAdapter = new ForecastAdapter(getActivity(),dataCursor);
        mRecyclerView.setAdapter(mAdapter);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO: When tablets rotate, the currently selected list item needs to be saved.
        if (mPosition != RecyclerView.INVALID_TYPE) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void reload() {
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this).startLoading();
    }

    /*
    private void notifyAdapterDataSetChanged() {
        // We have to set up a new adapter (as opposed to just calling notifyDataSetChanged()
        // because we might need MORE view types than before, and ListView isn't prepared to
        // handle the case where its existing adapter suddenly needs to increase the number of
        // view types it needs.
        if (conferences != null){
            mRecyclerView.setAdapter(new ConferenceDataAdapter(getActivity(),conferences));
        }

    }
    */
}