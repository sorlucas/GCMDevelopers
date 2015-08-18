package com.sergio.example.owngcm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sergio.example.owngcm.navdrawer.mockedFragments.FragmentButton;
import com.sergio.example.owngcm.navdrawer.mockedFragments.FragmentChat;
import com.sergio.example.owngcm.navdrawer.mockedFragments.FragmentCreateChat;
import com.sergio.example.owngcm.navdrawer.mockedFragments.FragmentListChats;
import com.sergio.example.owngcm.service.DownloadResultReceiver;
import com.sergio.example.owngcm.service.UploadRouteService;
import com.sergio.example.owngcm.sync.SyncAdapter;
import com.sergio.example.owngcm.tours.ProductTourActivity;
import com.sergio.example.owngcm.utils.AccountUtils;
import com.sergio.example.owngcm.utils.AndroidDatabaseManager;
import com.sergio.example.owngcm.utils.LoginAndAuthHelper;
import com.sergio.example.owngcm.utils.PrefUtils;
import com.sergio.example.owngcm.utils.UIUtils;

import java.util.concurrent.ExecutionException;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;

import static com.sergio.example.owngcm.utils.LogUtils.LOGD;
import static com.sergio.example.owngcm.utils.LogUtils.LOGE;
import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;
/**
 * Base Activity
 */
public class BaseActivity extends MaterialNavigationDrawer implements
        MaterialAccountListener,
        LoginAndAuthHelper.Callbacks,
        DownloadResultReceiver.Receiver {

    private static final String TAG = makeLogTag(BaseActivity.class);

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth
    private LoginAndAuthHelper mLoginAndAuthHelper;

    // Class to inform when is running uploadChat
    private DownloadResultReceiver mReceiver;

    // Click Listener
    View.OnClickListener mOnClickListenerRefresh; //

    public DownloadResultReceiver getmReceiver() {
        return mReceiver;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        // Declarate Receivers
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(this);

    }

    @Override
    protected void onStop() {
        LOGD(TAG, "onStop");
        super.onStop();
        if (mLoginAndAuthHelper != null) {
            mLoginAndAuthHelper.stop();
        }
    }

    @Override
    public void init(Bundle savedInstanceState) {

        // TODO: Probar que cuentas hay, para asi utilizar la nuestra, no la de anonimo.
        // o tambien podriamos crear en shared preferences la ultima cuenta utilizada y asi ponerla
        MaterialAccount account = new MaterialAccount(
                this.getResources(),
                "Anonimo",
                "anonimo@whoIsit.com",
                R.drawable.photo,
                R.drawable.bamboo);
        this.addAccount(account);

        // set listener
        this.setAccountListener(this);

        // Check if the EULA has been accepted; if not, show it.
        if (!PrefUtils.isTosAccepted(this)) {
            startActivity(new Intent(this, ProductTourActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();

        }

        //TODO: quitar en un futuro, demasiada peticion para cojer ruta creada
        // Declarate onClicks to SnackBar
        mOnClickListenerRefresh = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inicializo el SyncAdapter con los chats creados por mi
                SyncAdapter.initializeSyncAdapter(getApplicationContext(), SyncAdapter.SYNC_MODE_ATTENDES_CHATS, null);

            }
        };

        // create sections
        int colorTheme = getResources().getColor(R.color.theme);
        
        // Create Fragments Chats Actives (owns) and explores from default Fragment
        Fragment fragmentOwnsChats = FragmentListChats.newInstance(SyncAdapter.SYNC_MODE_ATTENDES_CHATS);
        Fragment fragmentExplorer = FragmentListChats.newInstance(SyncAdapter.SYNC_MODE_SEARCH);

        this.addSection(newSection(getString(R.string.navdrawer_item_chats_active),R.drawable.ic_chat_white_24dp, fragmentOwnsChats).setSectionColor(colorTheme));
        this.addSection(newSection(getString(R.string.navdrawer_item_add_chat), R.drawable.ic_create_white_24dp, new FragmentCreateChat()).setSectionColor(colorTheme));
        this.addSection(newSection(getString(R.string.navdrawer_item_explore),R.drawable.ic_explore_white_24dp,fragmentExplorer).setSectionColor(colorTheme));
        this.addSection(newSection(getString(R.string.navdrawer_item_people_ive_met),R.drawable.ic_person_pin_white_24dp,new FragmentButton()).setSectionColor(colorTheme));
        this.addSection(newSection(getString(R.string.navdrawer_item_photo_gallery), R.drawable.ic_photo_library_white_24dp, new FragmentChat()).setSectionColor(colorTheme));
        // create bottom section
        this.addBottomSection(newSection(getString(R.string.navdrawer_item_settings), R.drawable.ic_settings_white_24dp, new Intent(this, SettingsActivity.class)));

        startLoginProcess();
    }

    @Override
    public void onAccountOpening(MaterialAccount account) {
        //TODO: implment account opening (MaterialNavDrawer)
    }

    @Override
    public void onChangeAccount(MaterialAccount newAccount) {
        //TODO: implment change account (MaterialNavDrawer)
        Log.i(TAG, "onChangeAccount.");
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void startLoginProcess() {
        LOGD(TAG, "Starting login process.");
        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "No active account, attempting to pick a default.");
            String defaultAccount = AccountUtils.getDefaultAccount(this);
            if (defaultAccount == null) {
                LOGE(TAG, "Failed to pick default account (no accounts). Failing.");
                complainMustHaveGoogleAccount();
                return;
            }
            LOGD(TAG, "Default to: " + defaultAccount);
            AccountUtils.setActiveAccount(this, defaultAccount);
        }

        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "Can't proceed with login -- no account chosen.");
            return;
        } else {
            LOGD(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(this));
        }

        String accountName = AccountUtils.getActiveAccountName(this);
        LOGD(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(this));

        if (mLoginAndAuthHelper != null && mLoginAndAuthHelper.getAccountName().equals(accountName)) {
            LOGD(TAG, "Helper already set up; simply starting it.");
            mLoginAndAuthHelper.start();
            return;
        }

        LOGD(TAG, "Starting login process with account " + accountName);

        if (mLoginAndAuthHelper != null) {
            LOGD(TAG, "Tearing down old Helper, was " + mLoginAndAuthHelper.getAccountName());
            if (mLoginAndAuthHelper.isStarted()) {
                LOGD(TAG, "Stopping old Helper");
                mLoginAndAuthHelper.stop();
            }
            mLoginAndAuthHelper = null;
        }

        LOGD(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthHelper = new LoginAndAuthHelper(this, this, accountName);
        mLoginAndAuthHelper.start();
    }

    private void complainMustHaveGoogleAccount() {
        LOGD(TAG, "Complaining about missing Google account.");
        new AlertDialog.Builder(this)
                .setTitle(R.string.google_account_required_title)
                .setMessage(R.string.google_account_required_message)
                .setPositiveButton(R.string.add_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        promptAddAccount();
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void promptAddAccount() {
        Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mLoginAndAuthHelper == null || !mLoginAndAuthHelper.onActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_base, menu);

        configureStandardMenuItems(menu);

        return true;

    }

    private void configureStandardMenuItems(Menu menu) {
        MenuItem debugItem = menu.findItem(R.id.menu_view_database);
        if (debugItem != null) {
            debugItem.setVisible(BuildConfig.DEBUG);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_settings:
                Intent settings = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.menu_view_database:
                if (BuildConfig.DEBUG) {
                    startActivity(new Intent(this,
                            AndroidDatabaseManager.class));
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlusInfoLoaded(String accountName) {

    }

    /**
     * Called when authentication succeeds. This may either happen because the user just
     * authenticated for the first time (and went through the sign in flow), or because it's
     * a returning user.
     * @param accountName name of the account that just authenticated successfully.
     * @param newlyAuthenticated If true, this user just authenticated for the first time.
     * If false, it's a returning user.
     */
    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {
        LOGD(TAG, "onAuthSuccess, account " + accountName + ", newlyAuthenticated=" + newlyAuthenticated);

        final Bitmap[] photoProfile = new Bitmap[1];
        final Bitmap[] photoCover = new Bitmap[1];


        // Remove old data for account to implement new (no se repitan)
        MaterialAccount oldAccount = getAccountByTitle(AccountUtils.getPlusName(BaseActivity.this));
        if (oldAccount != null) {
            this.removeAccount(oldAccount);
        }

        /**
         * Create AsyncTask to download photos profiles and create MaterialAccount.
         */
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    photoProfile[0] = Glide.with(getApplicationContext())
                            .load(AccountUtils.getPlusImageUrl(getApplicationContext()))
                            .asBitmap()
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();

                    photoCover[0] = Glide.with(getApplicationContext())
                            .load(AccountUtils.getPlusCoverUrl(getApplicationContext()))
                            .asBitmap()
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();

                    MaterialAccount account = new MaterialAccount(
                            BaseActivity.this.getResources(),
                            AccountUtils.getPlusName(BaseActivity.this),
                            AccountUtils.getActiveAccountName(BaseActivity.this),
                            photoProfile[0],
                            photoCover[0]);
                    BaseActivity.this.addAccount(account);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onAuthFailure(String accountName) {
        LOGD(TAG, "Auth failed for account " + accountName);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case UploadRouteService.STATUS_RUNNING:
                setProgressBarIndeterminateVisibility(true);
                break;
            case UploadRouteService.STATUS_FINISHED:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);
                //String[] results = resultData.getStringArray("result");
                closeDrawer();
                UIUtils.showSnackBar(this,
                        getString(R.string.snackbar_upload_new_chat),
                        getString(R.string.snackbar_upload_new_chat_action),
                        mOnClickListenerRefresh);

                break;
            case UploadRouteService.STATUS_ERROR:
                /* Handle the error */
                closeDrawer();
                String error = resultData.getString(Intent.EXTRA_TEXT);
                UIUtils.showSnackBar(this,
                        error,
                        null,
                        null);
                break;
        }
    }

}
