package com.sergio.example.owngcm.navdrawer.mockedFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sergio.example.owngcm.service.QuickstartPreferences;
import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.gcm.GcmServerSideSender;
import com.sergio.example.owngcm.model.Message;
import com.sergio.example.owngcm.service.LoggingService;
import com.sergio.example.owngcm.service.RegistrationIntentService;
import com.sergio.example.owngcm.utils.AccountUtils;
import com.sergio.example.owngcm.utils.UIUtils;

import java.io.IOException;


/**
 * Created by neokree on 24/11/14.
 */
public class FragmentChat extends Fragment {

    private ImageView mImageViewIconStatus;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private EditText mMessage;
    private Button mButtonSend;
    private ScrollView mScrollView;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private LoggingService.Logger mLogger;
    private BroadcastReceiver mLoggerCallback;

    private TextView mLogsUI;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mLogger = new LoggingService.Logger(getActivity());

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                mButtonSend.setEnabled(true);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mImageViewIconStatus.setImageResource(R.drawable.online_icon);
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mImageViewIconStatus.setImageResource(R.drawable.offline_icon);
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, container, false);

        mImageViewIconStatus = (ImageView) root.findViewById(R.id.imageViewIconStatus);
        mRegistrationProgressBar = (ProgressBar) root.findViewById(R.id.registrationProgressBar);
        mInformationTextView = (TextView) root.findViewById(R.id.informationTextView);
        mMessage = (EditText) root.findViewById(R.id.editTextMessage);
        mButtonSend = (Button) root.findViewById(R.id.buttonSend);
        mScrollView = (ScrollView) root.findViewById(R.id.scrollViewChat);

        mLogsUI = (TextView) root.findViewById(R.id.logs);

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If Button no enable, no send. Wait
                if (!mButtonSend.isEnabled()) {
                    UIUtils.showSnackBar(getActivity(),
                            getString(R.string.snackbar_wait_init_chat),
                            null,
                            null);
                    return;
                }

                String message = mMessage.getText().toString();
                // If empty message no run
                if (!message.isEmpty()) {
                    doGcmSend();
                }
                mMessage.setText("");
            }
        });

        mLoggerCallback = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case LoggingService.ACTION_CLEAR_LOGS:
                        mLogsUI.setText("");
                        break;
                    case LoggingService.ACTION_LOG:
                        StringBuilder stringBuilder = new StringBuilder();
                        String newLog = intent.getStringExtra(LoggingService.EXTRA_LOG_MESSAGE);
                        String oldLogs = Html.toHtml(new SpannableString(mLogsUI.getText()));
                        stringBuilder.append(oldLogs);
                        appendFormattedLogLine(newLog, stringBuilder);
                        mLogsUI.setText(Html.fromHtml(stringBuilder.toString()));
                        mScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                mScrollView.fullScroll(ScrollView.FOCUS_UP);
                            }
                        });
                        break;
                }
            }
        };

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
        StringBuilder logs = new StringBuilder();
        for (String log : mLogger.getLogsFromFile()) {
            appendFormattedLogLine(log, logs);
            logs.append("<br>");
        }
        mLogsUI.setText(Html.fromHtml(logs.toString()));
        mScrollView.post(new Runnable() {

            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        mLogger.registerCallback(mLoggerCallback);

        Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
        getActivity().startService(intent);
    }

    private void appendFormattedLogLine(String log, StringBuilder stringBuilder) {
        String[] logLines = log.split("\n");
        if (logLines.length > 0) {
            logLines[0] = "<b>" + logLines[0] + "</b>";
            for (String line : logLines) {
                if (line.startsWith("exception: ")) {
                    continue;
                }
                int keySeparator = line.indexOf(": ");
                if (keySeparator > 0) {
                    stringBuilder
                            .append("<b>").append(line.substring(0, keySeparator + 1)).append
                            ("</b>")
                            .append(line.substring(keySeparator + 1)).append("<br>");
                } else {
                    stringBuilder.append(line).append("<br>");
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
        mLogger.unregisterCallback(mLoggerCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
        mLogger.unregisterCallback(mLoggerCallback);
    }

    protected void doGcmSend() {
        final Message.Builder messageBuilder = new Message.Builder();

        messageBuilder.addData(Message.PARAM_USER, AccountUtils.getPlusName(getActivity()));
        messageBuilder.addData(Message.PARAM_MESSAGE, mMessage.getText().toString());

        //TODO: En un futuro guardar la apiKey en un servidor y cuando autentificacion correcta descargarla
        final String apiKey = getString(R.string.downstream_api_key);

        //TODO: En este caso simplemente es el topic
        final String registrationId = getString(R.string.topic_global);


        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                GcmServerSideSender sender = new GcmServerSideSender(apiKey, mLogger);
                try {
                    sender.sendHttpJsonDownstreamMessage(registrationId, messageBuilder.build());
                } catch (final IOException e) {
                    return e.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    UIUtils.showSnackBar(getActivity(),
                            getString(R.string.snackbar_message_failed) + result,
                            null,
                            null);
                }
            }
        }.execute();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_logs: {
                // This is using code:
                final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(getString(R.string.clear_logs));
                alert.setMessage(getString(R.string.message_dialog_clear_logs));

                alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        (new LoggingService.Logger(getActivity())).clearLogs();
                    }
                });
                alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();

                return true;
            }
            default:
                return false;
        }
    }

}
