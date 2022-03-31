package cordova.plugin.imageloadplugin;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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


/**
 * This class echoes a string called from JavaScript.
 */
public class ImageLoadPlugin extends CordovaPlugin {



    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getContext();
        //Log.i("Cur", "execute: ");
        if (action.equals("coolMethod")) {

//            String[] projection = new String[] {
//                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
//                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
//                    MediaStore.Images.ImageColumns._ID,
//                    MediaStore.Images.ImageColumns.DATE_ADDED
//            };
//
//            //String selection = MediaStore.Images.Media.DATE_ADDED + ">= ?";
//
//            //String sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " ASC";
//            String sortOrder = MediaStore.Images.ImageColumns.DATE_ADDED + " ASC";
//
//            ContentResolver cr = context.getContentResolver();
//
//            Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
//
//            //Log.i("Cur", "onCreate: " + cur);
//
//            JSONArray ret = new JSONArray();
//            try {
//                if(cur != null) {
//                    //Log.i("inside if", "insideIf");
//                    int idColumn = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
//                    int idColumnName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
//                    int idBucketName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
//
//                    while(cur.moveToNext()) {
//                        JSONObject imagesData = new JSONObject();
//                        long id = cur.getLong(idColumn);
//                        String fileName = cur.getString(idColumnName);
//                        String bucketName = cur.getString(idBucketName);
//
//                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
//                        Bitmap thumbnail = null;
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                            try {
//                                thumbnail = context.getContentResolver().loadThumbnail(contentUri, new Size(250, 250), null);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            thumbnail = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null);
//                        }
//
//                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                        assert thumbnail != null;
//                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//
//                        byte[] byteArray = byteArrayOutputStream.toByteArray();
//
//                        String encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);
//
//                        imagesData.put("FileName", fileName);
//                        imagesData.put("BucketName", bucketName);
//                        imagesData.put("ImageData", encodedImage);
//                        ret.put(imagesData);
//
//                        //Log.i("Filename", "fileName: " + fileName);
//                        //Log.i("TAG", "execute: " + new String(encodedImage));
////                    Log.i("Filename", "Json: " + imagesData);
//                        //Log.i("Filename", "fileUri: " + contentUri);
//                        //Log.i("Filename", "bucketName: " + bucketName);
//                        //Log.i("Filename", "encodedImage: " + encodedImage);
//                        //Log.i("Filename", "encodedImageLength: " + encodedImage.length());
//                        //Log.i("Filename", "Thumbnail Bitmap: " + thumbnail.getByteCount());
////                    Log.i("Filename", "Thumbnail Width: " + thumbnail.getWidth());
////                    Log.i("Filename", "Thumbnail Height: " + thumbnail.getHeight());
//                    }
//                }
//            } finally {
//                assert cur != null;
//                cur.close();
//            }

            //Log.i("TT", "ret" +  ret.toString());

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        coolMethod(getImageFromStorage(context).toString(), callbackContext);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
//            this.coolMethod(getImageFromStorage(context).toString(), callbackContext);
            return true;
        }
        return false;
    }

    private JSONArray getImageFromStorage(Context context) throws JSONException {
        String[] projection = new String[] {
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATE_ADDED
        };

        //String selection = MediaStore.Images.Media.DATE_ADDED + ">= ?";

        //String sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " ASC";
        String sortOrder = MediaStore.Images.ImageColumns.DATE_ADDED + " ASC";

        ContentResolver cr = context.getContentResolver();

        Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);

        //Log.i("Cur", "onCreate: " + cur);

        JSONArray ret = new JSONArray();
        try {
            if(cur != null) {
                //Log.i("inside if", "insideIf");
                int idColumn = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
                int idColumnName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                int idBucketName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

                while(cur.moveToNext()) {
                    JSONObject imagesData = new JSONObject();
                    long id = cur.getLong(idColumn);
                    String fileName = cur.getString(idColumnName);
                    String bucketName = cur.getString(idBucketName);

                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    Bitmap thumbnail = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        try {
                            thumbnail = context.getContentResolver().loadThumbnail(contentUri, new Size(250, 250), null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        thumbnail = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                    }

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    assert thumbnail != null;
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                    String encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);

                    imagesData.put("FileName", fileName);
                    imagesData.put("BucketName", bucketName);
                    imagesData.put("ImageData", encodedImage);
                    ret.put(imagesData);

                    //Log.i("Filename", "fileName: " + fileName);
                    //Log.i("TAG", "execute: " + new String(encodedImage));
//                    Log.i("Filename", "Json: " + imagesData);
                    //Log.i("Filename", "fileUri: " + contentUri);
                    //Log.i("Filename", "bucketName: " + bucketName);
                    //Log.i("Filename", "encodedImage: " + encodedImage);
                    //Log.i("Filename", "encodedImageLength: " + encodedImage.length());
                    //Log.i("Filename", "Thumbnail Bitmap: " + thumbnail.getByteCount());
//                    Log.i("Filename", "Thumbnail Width: " + thumbnail.getWidth());
//                    Log.i("Filename", "Thumbnail Height: " + thumbnail.getHeight());
                }
            }
        } finally {
            assert cur != null;
            cur.close();
        }
        return ret;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
