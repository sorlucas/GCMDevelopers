package com.sergio.example.owngcm.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sergio.example.owngcm.Config;
import com.sergio.example.owngcm.R;
import com.sergio.example.owngcm.sync.SyncService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sergio.example.owngcm.utils.LogUtils.makeLogTag;

/**
 * Created by sergio on 3/07/15.
 */
public class StorageUtils {

    public final static String TAG =  makeLogTag(StorageUtils.class);

    private static Storage mStorageApiHandler;

    // Authorization.
    private static Credential credential;



    public static void build(Context context) throws Exception {
        mStorageApiHandler = getStorage(context, AccountUtils.getActiveAccountName(context));
    }

    /**
     * Get metadate of specific bucket
     * @param bucketName name of bucket
     * @return key values of bucket
     * @throws StorageException
     * @throws IOException
     */
    public static Map<String,String> getMetadataBucket(String bucketName)
            throws StorageException, IOException{

        if (null == mStorageApiHandler) {
            Log.e(TAG, "getMetadataBucket(): no service handler was built");
            throw new StorageException();
        }
        Storage.Buckets.Get getBucket = mStorageApiHandler.buckets().get(bucketName);
        getBucket.setProjection("full");
        Bucket bucket = getBucket.execute();

        Map<String, String> map = new HashMap<>();
        map.put("name: ", bucketName);
        map.put("location: ", bucket.getLocation());
        map.put("timeCreated: ", bucket.getTimeCreated().toString());
        map.put("owner: ", bucket.getOwner().toString());

        return map;
    }

    public static String uploadPhotoToBucket (String bucketName, String filePath)
        throws Exception {

        // TODO: Change to true to useCustomMetadata
        Boolean useCustomMetadata = false;

        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);
        long byteCount;  // size of input stream

        String contentType = URLConnection.guessContentTypeFromStream(inputStream);
        InputStreamContent mediaContent = new InputStreamContent(contentType,inputStream);

        StorageObject objectMetadata = null;

        if (useCustomMetadata) {
            // If you have custom settings for metadata on the object you want to set
            // then you can allocate a StorageObject and set the values here. You can
            // leave out setBucket(), since the bucket is in the insert command's
            // parameters.
            objectMetadata = new StorageObject()
                    .setName(file.getName())
                    .setMetadata(ImmutableMap.of("key1", "value1", "key2", "value2"))
                    .setAcl(ImmutableList.of(
                            new ObjectAccessControl().setEntity("domain-example.com").setRole("READER"),
                            new ObjectAccessControl().setEntity("user-administrator@example.com").setRole("OWNER")
                    ))
                    .setContentDisposition("attachment");
        }

        Storage.Objects.Insert insertObject = mStorageApiHandler.objects().insert(bucketName, objectMetadata, mediaContent);

        if (!useCustomMetadata) {
            // If you don't provide metadata, you will have specify the object
            // name by parameter. You will probably also want to ensure that your
            // default object ACLs (a bucket property) are set appropriately:
            // https://developers.google.com/storage/docs/json_api/v1/buckets#defaultObjectAcl
            insertObject.setName(file.getName());
        }

        // For small files, you may wish to call setDirectUploadEnabled(true), to
        // reduce the number of HTTP requests made to the server.
        if (mediaContent.getLength() > 0 && mediaContent.getLength() <= 2 * 1000 * 1000 /* 2MB */) {
            insertObject.getMediaHttpUploader().setDirectUploadEnabled(true);
        }

        insertObject.execute();

        // TODO: Create url object manualmente (only PUBLIC)
        return "https://storage.googleapis.com/" + Config.BUCKET_PHOTOS_COVER + "/" + file.getName();
    }

    /**
     * Get name y peso de los objetos del bucket
     * @param bucketName Name of bucket
     * @return
     * @throws StorageException
     * @throws IOException
     */
    public static List<String> listBucketObjets (String bucketName)
            throws StorageException, IOException {

        List<String> listObjetsString = new ArrayList<>();

        // List the contents of the bucket.
        Storage.Objects.List listObjects = mStorageApiHandler.objects().list(bucketName);
        com.google.api.services.storage.model.Objects objects;

        do {
            objects = listObjects.execute();
            List<StorageObject> items = objects.getItems();
            if (null == items) {
                System.out.println("There were no objects in the given bucket; try adding some and re-running.");
                break;
            }
            for (StorageObject object : items) {
                listObjetsString.add((object.getName() + " (" + object.getSize() + " bytes)"));
            }
            listObjects.setPageToken(objects.getNextPageToken());
        } while (null != objects.getNextPageToken());

        return listObjetsString;
    }

    /**
     * Build and returns an instance of {@link Storage}
     *
     * @param context context of running application
     * @param email email user
     * @return Storage
     */
    public static Storage getStorage(Context context, String email) throws Exception {

        if (mStorageApiHandler == null) {
            //I had already used .storage_sample.properties file for user information.
            //getProperties() is local method which read the user information stored in storage_sample.properties
            Credential credential = new GoogleCredential.Builder()
                    .setTransport(Config.HTTP_TRANSPORT)
                    .setJsonFactory(Config.JSON_FACTORY)
                    .setServiceAccountId(Config.SERVICE_ACCOUNT_EMAIL)
                    .setServiceAccountPrivateKeyFromP12File(getTempPkc12File(context))
                    .setServiceAccountScopes(Collections.singleton(StorageScopes.DEVSTORAGE_FULL_CONTROL)).build();
            mStorageApiHandler
                    = new Storage.Builder(Config.HTTP_TRANSPORT
                    ,Config.JSON_FACTORY
                    ,credential)
                    .setApplicationName(context.getString(R.string.app_name))
                    .build();
        }
        return mStorageApiHandler;
    }

    private static File getTempPkc12File(Context context) throws IOException {

        // client_secret.p12 export from google API console
        AssetManager am = context.getAssets();
        InputStream pkc12Stream = am.open("client_secret.p12");
        File tempPkc12File = File.createTempFile("temp_pkc12_file", "p12");
        OutputStream tempFileStream = new FileOutputStream(tempPkc12File);

        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = pkc12Stream.read(bytes)) != -1) {
            tempFileStream.write(bytes, 0, read);
        }
        return tempPkc12File;
    }

}
