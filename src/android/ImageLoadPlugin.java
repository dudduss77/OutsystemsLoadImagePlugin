package cordova.plugin.imageloadplugin;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Size;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * This class echoes a string called from JavaScript.
 */
public class ImageLoadPlugin extends CordovaPlugin {



    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getContext();
        Log.i("Cur", "action: " + action);
        if (action.equals("coolMethod")) {
            Log.i("ttt", "json " + args);
            String bucketName = args.getJSONObject(0).getString("BucketName");
            int limit = args.getJSONObject(0).getInt("Limit");
            int offset = args.getJSONObject(0).getInt("Offset");
            Log.i("TAG", "args: " + bucketName);
            Log.i("TAG", "args: " + limit);
            Log.i("TAG", "args: " + offset);
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        coolMethod(getImageFromStorage(context, bucketName, limit, offset).toString(), callbackContext);
                    } catch (JSONException | ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        } else if (action.equals("getBucketName")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        getBucketName(getBucketNameFun(context), callbackContext);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        return false;
    }

    private  String getBucketNameFun(Context context) throws JSONException {
        String[] projection = new String[] {
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME
        };

        String sortOrder = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " ASC";
        ContentResolver cr = context.getContentResolver();

        Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);

        HashSet <String> bucketList = new HashSet<String>();
        try {
            if(cur != null) {
                int idBucketName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

                while(cur.moveToNext()) {
                    String bucketName = cur.getString(idBucketName);
                    bucketList.add(bucketName);
                }
            }
        } finally {
            assert cur != null;
            cur.close();
        }

        JSONArray jsonArray = new JSONArray();

        for(String name : bucketList) {
            JSONObject obj = new JSONObject();
            obj.put("BucketName", name);
            jsonArray.put(obj);
        }

        return jsonArray.toString();
    }

    private JSONArray getImageFromStorage(Context context, String inBucketName, int inLimit, int inOffset) throws JSONException, ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<JSONObject>> future = new ArrayList<>();
        String[] projection = new String[] {
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATE_ADDED,
                MediaStore.Images.ImageColumns.DATA
        };

        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
        String[] selectionArgs = {inBucketName};
        String sortOrder = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC LIMIT " + inLimit + " OFFSET " + inOffset;

        ContentResolver cr = context.getContentResolver();

        Cursor cur = null;
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            Bundle queryArgs = new Bundle();
            String[] sortArgs = {MediaStore.Images.ImageColumns.DATE_ADDED};
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, sortArgs);
            queryArgs.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);
            queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, inLimit);
            queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, inOffset);
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
            cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, queryArgs, null);
        } else {
            cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        }

        JSONArray ret = new JSONArray();
        try {
            if(cur != null) {
                int idColumn = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
                int idColumnName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                int idBucketName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
                int idPath = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                while(cur.moveToNext()) {
                    long id = cur.getLong(idColumn);
                    String fileName = cur.getString(idColumnName);
                    String bucketName = cur.getString(idBucketName);
                    String path = cur.getString(idPath);

                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    future.add(executor.submit(new Callable<JSONObject>() {
                        @Override
                        public JSONObject call() throws Exception {
                            JSONObject imagesData = new JSONObject();
                            Bitmap thumbnail = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                try {
                                    thumbnail = context.getContentResolver().loadThumbnail(contentUri, new Size(500, 500), null);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                            }

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            assert thumbnail != null;
                            thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

                            byte[] byteArray = byteArrayOutputStream.toByteArray();

                            String encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);

                            try {
                                imagesData.put("id", id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                imagesData.put("FileName", fileName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                imagesData.put("BucketName", bucketName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                imagesData.put("Path", path);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                imagesData.put("ImageData", encodedImage);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return imagesData;
                        }
                    }));
                    Log.i("TAG", "getImageFromStorage: " + cur.getPosition());
                }

            }
        } finally {
            assert cur != null;
            cur.close();
        }


        for(Future<JSONObject> val : future) {
            ret.put(val.get());
        }


        executor.shutdown();
        return ret;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    private void getBucketName(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
