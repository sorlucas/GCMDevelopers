package com.sergio.example.owngcm.navdrawer.mockedFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.sergio.example.owngcm.BaseActivity;
import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.provider.RouteContract;
import com.sergio.example.owngcm.service.UploadRouteService;

import com.sergio.example.owngcm.utils.UIUtils;

import java.io.File;
import java.util.SimpleTimeZone;

import it.neokree.materialnavigationdrawer.elements.MaterialSection;

import static com.sergio.example.owngcm.utils.LogUtils.LOGD;
import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;
/**
 * Created by syp on 12/08/15.
 */
public class FragmentCreateChat extends Fragment implements
        ImageChooserListener {

    private static final String TAG = makeLogTag(BaseActivity.class);

    //Declarate UI Rerferences
    private ViewGroup mRoot;  // To removeAllViews()
    private EditText mChatName;
    private EditText mDescription;
    private String mTopics;
    private EditText mCity;
    private EditText mMaxAttendes;

    //Delarate Views to catch photo route
    private ImageView imageViewThumbnail;
    private String urlPhotoPath;
    private ImageChooserManager imageChooserManager;
    private ProgressBar pbar;
    private String filePath;
    private int chooserType;

    //Declare buttons
    private FloatingActionButton mButtonCreate;
    private FloatingActionButton mButtonCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_create_chat, container, false);

        mChatName = (EditText) mRoot.findViewById(R.id.editTextChatName);
        mDescription = (EditText) mRoot.findViewById(R.id.editTextDescription);
        mCity = (EditText) mRoot.findViewById(R.id.editTextCity);
        mMaxAttendes = (EditText) mRoot.findViewById(R.id.editTextMaxAttendes);

        //Create an display Spinner
        Spinner spinner = (Spinner) mRoot.findViewById(R.id.spinnerTopics);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTopics = (String) parent.getItemAtPosition(position);
                LOGD(TAG, "Topic selected in spinner" + mTopics);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.topic_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Photos
        Button buttonTakePicture = (Button) mRoot.findViewById(R.id.buttonTakePicture);
        buttonTakePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        Button buttonChooseImage = (Button) mRoot.findViewById(R.id.buttonChooseImage);
        buttonChooseImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        imageViewThumbnail = (ImageView) mRoot.findViewById(R.id.imageViewCover);
        pbar = (ProgressBar) mRoot.findViewById(R.id.progressBar);
        pbar.setVisibility(View.GONE);

        mButtonCreate = (FloatingActionButton) mRoot.findViewById(R.id.button_create);
        mButtonCancel = (FloatingActionButton) mRoot.findViewById(R.id.button_cancel);

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadRoute();
            }
        });

        return mRoot;
    }

    private void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, "WhatssDo", true);
        imageChooserManager.setImageChooserListener(this);
        try {
            pbar.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        try {
            pbar.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void uploadRoute() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(), UploadRouteService.class);

        /* Send optional extras to Download IntentService */
        intent.putExtra("receiver", ((BaseActivity) getActivity()).getmReceiver());
        intent.putExtra(RouteContract.RouteEntry.COLUMN_NAME_ROUTE, mChatName.getText().toString());
        intent.putExtra(RouteContract.RouteEntry.COLUMN_DESCRIPTION, mDescription.getText().toString());
        intent.putExtra(RouteContract.RouteEntry.COLUMN_TOPICS, mTopics);
        intent.putExtra(RouteContract.RouteEntry.COLUMN_CITY_NAME_INIT, mCity.getText().toString());
        intent.putExtra(RouteContract.RouteEntry.COLUMN_MAX_ATTENDEES, mMaxAttendes.getText().toString());
        intent.putExtra(RouteContract.RouteEntry.COLUMN_URL_CHAT_COVER, urlPhotoPath);
        getActivity().startService(intent);

        // Reset Views
        mRoot.removeAllViewsInLayout();
        TextView viewWarning = new TextView(getActivity());
        viewWarning.setText(R.string.warning_create_many_chats);
        viewWarning.setGravity(Gravity.CENTER_HORIZONTAL);
        viewWarning.setGravity(Gravity.CENTER_VERTICAL);

        // Add view Warning (no se puede crear canal seguido
        mRoot.addView(viewWarning);
        //TODO: FUTURA Implementacion hacer esperar un tiempo antes de crear otro canal

        // Abrir el Navdrawer y marcar chat activos por defecto
        MaterialSection section = ((BaseActivity) getActivity()).getSectionByTitle(getString(R.string.navdrawer_item_chats_active));
        section.select();
        ((BaseActivity) getActivity()).onClick(section);
        
    }

    @Override
    public void onImageChosen(final ChosenImage chosenImage) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                pbar.setVisibility(View.GONE);
                if (chosenImage != null) {
                    urlPhotoPath = chosenImage.getFilePathOriginal();
                    imageViewThumbnail.setImageURI(Uri.parse(new File(chosenImage
                            .getFileThumbnail()).toString()));
                }
            }
        });
    }

    @Override
    public void onError(final String reason) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                pbar.setVisibility(View.GONE);
                UIUtils.showSnackBar(getActivity(),
                        reason,
                        null,
                        null);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE
                || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        } else {
            pbar.setVisibility(View.GONE);
        }
    }

    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType,
                "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }
}
