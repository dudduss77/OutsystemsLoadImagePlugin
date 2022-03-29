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

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

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
        Log.i("Cur", "execute: ");
        if (action.equals("coolMethod")) {
            // String message = args.getString(0);

            String[] projection = new String[] {
                    //MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns._ID
            };

            String selection = MediaStore.Images.Media.DATE_ADDED + ">= ?";

            //String sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " ASC";
            String sortOrder = MediaStore.Images.ImageColumns.DISPLAY_NAME + " ASC";

            ContentResolver cr = context.getContentResolver();

            Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);

            Log.i("Cur", "onCreate: " + cur);

            JSONArray ret = new JSONArray();

            if(cur != null) {
                Log.i("inside if", "insideIf");
                int idColumn = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
                int idColumnName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
                int idBucketName = cur.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);

                //ArrayList<Bitmap> thumbnailsBitmap = new ArrayList<Bitmap>();

                JSONObject imagesData = new JSONObject();
                while(cur.moveToNext()) {
                    long id = cur.getLong(idColumn);
                    String fileName = cur.getString(idColumnName);
                    String bucketName = cur.getString(idBucketName);

                    // contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    //Bitmap thumbnail = context.getContentResolver().loadThumbnail(contentUri, new Size(640, 480), null);

                    Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] encodedImage = Base64.encode(byteArray, Base64.NO_WRAP);

                    imagesData.put("FileName", fileName);
                    imagesData.put("BucketName", bucketName);
                    imagesData.put("ImageData", new String(encodedImage));

                    ret.put(imagesData);
                    //thumbnailsBitmap.add(thumbnail);
                    //Log.i("Filename", "fileName: " + fileName);
                    Log.i("TAG", "execute: " + new String(encodedImage));
//                    Log.i("Filename", "Json: " + imagesData);
                    //Log.i("Filename", "fileUri: " + contentUri);
                    //Log.i("Filename", "bucketName: " + bucketName);
                    //Log.i("Filename", "encodedImage: " + encodedImage);
                    //Log.i("Filename", "encodedImageLength: " + encodedImage.length());
//                    Log.i("Filename", "Thumbnail Bitmap: " + thumbnail.getByteCount());
//                    Log.i("Filename", "Thumbnail Width: " + thumbnail.getWidth());
//                    Log.i("Filename", "Thumbnail Height: " + thumbnail.getHeight());
                }
            }


            String message = "Hallo i working";
            this.coolMethod(ret.toString(), callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
