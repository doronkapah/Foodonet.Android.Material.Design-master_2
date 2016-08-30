package CommonUtilPackage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
//import android.net.http.AndroidHttpClient;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.util.IOUtils;
import com.facebook.Profile;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.provider.Settings.Secure;
import android.view.Display;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.methods.HttpGet;

import DataModel.FCPublication;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;
import upp.foodonet.material.FooDoNetSQLProvider;
import upp.foodonet.material.R;
import upp.foodonet.material.SignInActivity;

/**
 * Created by Asher on 01.09.2015.
 */
public class CommonUtil {

    private static final String MY_TAG = "food_CommonUtil";

    public static String GetIMEI(Context context) {
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        return tm.getDeviceId();
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    public static double GetKilometersBetweenLatLongs(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;
        double R = 6378.137; // Radius of earth in KM
        double dLat = (lat2 - lat1) * Math.PI / 180;
        double dLon = (lon2 - lon1) * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d; // meters
    }

    public static Bitmap decodeScaledBitmapFromSdCard(String filePath,
                                                      int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static Bitmap decodeScaledBitmapFromByteArray(byte[] bytes,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static Bitmap decodeScaledBitmapFromDrawableResource(Resources resources, int drawableID,
                                                                int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, drawableID, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, drawableID, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static String GetTokenFromSharedPreferences(Context context, int tokenRepositoryID, int tokenKeyID) {
        String token = "";
        SharedPreferences sp = context.getSharedPreferences(context.getResources().getString(tokenRepositoryID), Context.MODE_PRIVATE);
        token = sp.getString(context.getResources().getString(tokenKeyID), "");
        return token;
    }

    public static String GetDistanceString(LatLng point1, LatLng point2, Context context) {
        if (point1 == null || point2 == null) {
            return context.getResources().getString(R.string.pub_det_cant_get_distance);
        }
        if (context == null)
            throw new NullPointerException("got null context");
        double distance = CommonUtil.GetKilometersBetweenLatLongs(point1, point2);
        if (distance > 1) {
            distance = Math.round(distance);
            return String.valueOf(((int) distance))
                    + " " + context.getResources().getString(R.string.pub_det_km_from_you);
        } else {
            distance = Math.round(distance * 1000);
            return String.valueOf(((int) distance))
                    + " " + context.getResources().getString(R.string.pub_det_metr_from_you);
        }
    }

    public static String GetDistanceStringFromCurrentLocation(LatLng point1, Context context) {
        LatLng currentLocation = GetMyLocationFromPreferences(context);
        return GetDistanceString(currentLocation, point1, context);
    }

    public static double GetDistanceInKM(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null)
            return -1;
        return GetKilometersBetweenLatLongs(point1, point2);
    }

    public static BitmapDrawable GetBitmapDrawableFromFile(String fileName, String imageSubFolder, int width, int heigth) {
        if (fileName == null || fileName.length() == 0) return null;
        File photo = new File(fileName);
        if (!photo.exists())
            photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);
        if (!photo.exists()) return null;
        BitmapDrawable result = null;
        try {
            FileInputStream fis = new FileInputStream(photo.getPath());
            byte[] imageBytes = IOUtils.toByteArray(fis);
            Bitmap bImage = CommonUtil.decodeScaledBitmapFromByteArray(imageBytes, width, heigth);
            bImage = PreRotateBitmapFromFile(bImage, photo);
            result = new BitmapDrawable(bImage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void CopyFile(File src, File dst) throws IOException {
        if (!src.exists())
            throw new IOException("CopyFile - source file doesn't exists");
        if (dst.exists()) {
            Log.w(MY_TAG, "CopyFile - destination file exists and will be overwritten");
            dst.delete();
        }
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String GetFilterStringFromPreferences(Context context) {
        SharedPreferences sp
                = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_text_filter_key),
                Context.MODE_PRIVATE);
        String result = sp.getString(context.getString(R.string.shared_preferences_text_filter_text_key), "");
        return result;
    }

    public static LatLng GetMyLocationFromPreferences(Context context) {
        SharedPreferences sp
                = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_my_location_key), Context.MODE_PRIVATE);
        float lat = sp.getFloat(context.getString(R.string.shared_preferences_my_latitude_key), -1000);
        float lon = sp.getFloat(context.getString(R.string.shared_preferences_my_longitude_key), -1000);
        return new LatLng(lat, lon);
    }

    public static void UpdateFilterMyLocationPreferences(Context context, LatLng myLocation) {
        if (myLocation == null) {
            Log.e(MY_TAG, "UpdateFilterMyLocationPreferences got null location");
        }
        Log.i(MY_TAG, "UpdateFilterMyLocationPreferences saves myLocation: lat:" + myLocation.latitude + " long:" + myLocation.longitude);
        SharedPreferences sp
                = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_my_location_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (sp.contains(context.getString(R.string.shared_preferences_my_latitude_key))) {
            editor.remove(context.getString(R.string.shared_preferences_my_latitude_key));
            editor.commit();
        }
        if (sp.contains(context.getString(R.string.shared_preferences_my_longitude_key))) {
            editor.remove(context.getString(R.string.shared_preferences_my_longitude_key));
            editor.commit();
        }
        editor.putFloat(context.getString(R.string.shared_preferences_my_latitude_key), ((float) myLocation.latitude));
        editor.putFloat(context.getString(R.string.shared_preferences_my_longitude_key), ((float) myLocation.longitude));
        editor.commit();
    }

    public static InputStream ConvertFileToInputStream(String fileName, String imageSubFolder) {
        InputStream is = null;

        File photo = new File(Environment.getExternalStorageDirectory() + imageSubFolder, fileName);

        try {
            is = new FileInputStream(photo.getPath());

            //is.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return is;
    }

    public static byte[] CompressImageByteArrayByMaxSize(byte[] result, int maxImageWidthHeight) {
//        bitmap = BitmapFactory.decodeByteArray(result, 0,
//                result.length);
        Bitmap bitmap = decodeScaledBitmapFromByteArray(result, 800, 800);
        bitmap = CompressBitmapByMaxSize(bitmap, maxImageWidthHeight);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap CompressBitmapByMaxSize(Bitmap bitmap, int maxImageWidthHeight) {
        int finalWidth = bitmap.getWidth();
        int finalHeight = bitmap.getHeight();
        double scaleRate = 1;
        if (finalWidth > maxImageWidthHeight || finalHeight > maxImageWidthHeight)
            scaleRate = (finalWidth > finalHeight ? finalWidth : finalHeight) / maxImageWidthHeight;
        finalWidth = (int) Math.round(finalWidth / scaleRate);
        finalHeight = (int) Math.round(finalHeight / scaleRate);
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    public static byte[] GetByteArrayFromFile(String fullPath) {
        File file = new File(fullPath);
        if (file.exists()) {
            try {
                InputStream is = new FileInputStream(file);
                return IOUtils.toByteArray(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void CopyImageFileWithCompressionBySize(File fSource, File fDestination, int maxSize) {
        if (!fSource.exists()) return;
        byte[] result = CompressImageByteArrayByMaxSize(GetByteArrayFromFile(fSource.getAbsolutePath()), maxSize);
        if (fDestination.exists()) fDestination.delete();
        OutputStream out = null;
        try {
            out = new FileOutputStream(fDestination);
            out.write(result, 0, result.length);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String GetDateTimeStringFromGate(Date date) {
        if (date == null)
            return "";
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return GetDateTimeStringFromCalendar(c);
    }

    public static String GetDateTimeStringFromCalendar(Calendar calendar) {
        if (calendar == null)
            return "";
        String hours = (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String minutes = (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.MINUTE));
        String days = (calendar.get(Calendar.DATE) < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.DATE));
        String month = (calendar.get(Calendar.MONTH) + 1 < 10 ? "0" : "") + String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String years = String.valueOf(calendar.get(Calendar.YEAR));
        return hours + ":" + minutes + " " + days + "/" + month + "/" + years;
    }

    public static Map<String, LatLng> GetPreviousAddressesMapFromCursor(Cursor cursor) {
        Map<String, LatLng> result = new HashMap<>();
        if (cursor.moveToFirst())
            do {
                result.put(cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_ADDRESS_KEY)),
                        new LatLng(cursor.getDouble(cursor.getColumnIndex(FCPublication.PUBLICATION_LATITUDE_KEY)),
                                cursor.getDouble(cursor.getColumnIndex(FCPublication.PUBLICATION_LONGITUDE_KEY))));
            } while (cursor.moveToNext());
        return result;
    }

    public static BitmapDrawable GetImageFromFileForPublicationCursor(Context context, Cursor cursor, int imageSize) {
        final int id = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_UNIQUE_ID_KEY));
        final int version = cursor.getInt(cursor.getColumnIndex(FCPublication.PUBLICATION_VERSION_KEY));
        final boolean cursorHasPhotoUrl = cursor.getColumnIndex(FCPublication.PUBLICATION_PHOTO_URL) != -1;
        String imagePath = "";
        if (cursorHasPhotoUrl)
            imagePath = cursor.getString(cursor.getColumnIndex(FCPublication.PUBLICATION_PHOTO_URL));
        return GetImageFromFileForPublication(context, id, version, imagePath, imageSize);
    }

    public static BitmapDrawable GetImageFromFileForPublication(Context context, int id, int version, String imagePath, int imageSize) {
        BitmapDrawable imageDrawable = null;
        String fileName = GetFileNameByIdAndVersion(id, version);
        imageDrawable = CommonUtil.GetBitmapDrawableFromFile(fileName,
                context.getString(R.string.image_folder_path), imageSize, imageSize);

        if (imageDrawable == null && imagePath != null && imagePath.length() > 0) {
            imageDrawable = CommonUtil.GetBitmapDrawableFromFile(imagePath, "", imageSize, imageSize);
        }
        return imageDrawable;
    }

    public static String GetFileNameByIdAndVersion(int id, int version) {
        if (id <= 0) {
            return "n" + (id * -1) + "." + version + ".jpg";
        } else {
            return id + "." + version + ".jpg";
        }
    }

    public static ProgressDialog ShowProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(message);
        progressDialog.show();
        return progressDialog;
    }

    public static String GetFileNameByPublication(FCPublication publication) {
        return String.valueOf(publication.getUniqueId() > 0
                ? String.valueOf(publication.getUniqueId())
                : "n" + String.valueOf(publication.getUniqueId() * -1))
                + "." + String.valueOf(publication.getVersion()) + ".jpg";
    }

    public static void PutCommonPreferenceIsDataLoaded(Context context, boolean isDataLoaded) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_data_loaded), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.shared_preferences_data_loaded_key), isDataLoaded);
        editor.commit();
        Log.i(MY_TAG, "IsDataLoaded set to: " + String.valueOf(isDataLoaded));
    }

    public static boolean GetFromPreferencesIsDataLoaded(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_data_loaded), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.shared_preferences_data_loaded_key), false);
    }

    public static void PutCommonPreferenceIsRegisteredDevice(Context context, boolean isDataLoaded) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_is_device_registered), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.shared_preferences_is_device_registered_key), isDataLoaded);
        editor.commit();
        Log.i(MY_TAG, "IsRegistered set to: " + String.valueOf(isDataLoaded));
    }

    public static boolean GetFromPreferencesIsDeviceRegistered(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_is_device_registered), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.shared_preferences_is_device_registered_key), false);
    }

    public static void PutCommonPreferencesIsRegisteredGoogleFacebook(Context context, GoogleSignInAccount account) {
        PutCommonPreferencesSocialAccountData(context, "google", account.getDisplayName(), account.getIdToken(), account.getId());
        SaveMyUserNameToPreferences(context, account.getDisplayName());
        SaveMyEmailToPreferences(context, account.getEmail());
    }

    public static void PutCommonPreferencesIsRegisteredGoogleFacebook(Context context, Profile account) {
        PutCommonPreferencesSocialAccountData(context, "facebook", account.getName(), account.getId(), account.getId());
        SaveMyUserNameToPreferences(context, account.getName());
        SaveMyEmailToPreferences(context, "");
    }

    private static void PutCommonPreferencesSocialAccountData(Context context, String socialAccountType, String socialAccountName, String socialAccountToken, String socialAccountID) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_google_facebook_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.shared_preferences_is_registered_to_google_facebook_key), true);
        editor.putString(context.getString(R.string.shared_preferences_social_account_type_key), socialAccountType);
        editor.putString(context.getString(R.string.shared_preferences_social_account_name_key), socialAccountName);
        editor.putString(context.getString(R.string.shared_preferences_social_account_token_key), socialAccountToken);
        editor.putString(context.getString(R.string.shared_preferences_social_account_id), socialAccountID);
        editor.commit();
        Log.i(MY_TAG, "Registered to " + socialAccountType + ", name: " + socialAccountName);
    }

    public static void ClearPreferencesSocialAccountDataForLogout(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_google_facebook_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.shared_preferences_is_registered_to_google_facebook_key), false);
        editor.putString(context.getString(R.string.shared_preferences_social_account_type_key), null);
        editor.putString(context.getString(R.string.shared_preferences_social_account_name_key), null);
        editor.putString(context.getString(R.string.shared_preferences_social_account_token_key), null);
        editor.putString(context.getString(R.string.shared_preferences_social_account_id), null);
        editor.commit();
        Log.i(MY_TAG, "Cleared registration data for logout");
    }

    public static String GetSocialAccountTypeFromPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_google_facebook_data_token), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.shared_preferences_social_account_type_key), "");
    }

    public static String GetSocialAccountIDFromPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_google_facebook_data_token), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.shared_preferences_social_account_id), "");
    }

    public static String GetSocialAccountNameFromPreferences(Context context) {
        SharedPreferences sp
                = context.getSharedPreferences(context.getString(R.string.shared_preferences_google_facebook_data_token),
                Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.shared_preferences_social_account_name_key), "");
    }

    public static boolean GetFromPreferencesIsRegisteredToGoogleFacebook(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_google_facebook_data_token), Context.MODE_PRIVATE);
        return sp.getBoolean(context.getString(R.string.shared_preferences_is_registered_to_google_facebook_key), false);
    }

    public static boolean GetFromPreferencesIsRegisteredWithPhoneNumber(Context context) {
        return GetMyPhoneNumberFromPreferences(context).length() != 0;

    }

    public static boolean RemoveImageByPublication(FCPublication publication, Context context) {
        File img = new File(Environment.getExternalStorageDirectory()
                + context.getString(R.string.image_folder_path), GetFileNameByPublication(publication));
        if (!img.exists()) return true;
        return img.delete();
    }

    public static void ReportLocationToServer(Context context) {
        GetMyLocationAsync locationAsync = new GetMyLocationAsync(
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE), context);
        locationAsync.switchToReportLocationMode(true);
        locationAsync.setIMEI(GetIMEI(context));
        locationAsync.execute();
    }

    public static boolean CheckPhoneNumberString(Context context, String phoneNumber) {
        String phonePattern = context.getString(R.string.regex_israel_phone_number);
        return phoneNumber.matches(phonePattern);
    }

    public static void SetEditTextIsValid(Context context, EditText field, boolean isValid) {
        field.getBackground()
                .setColorFilter(isValid ? context.getResources().getColor(R.color.validation_green_text_color) :
                        context.getResources().getColor(R.color.validation_red_text_color), PorterDuff.Mode.SRC_ATOP);
        Bitmap validationBitmap = CommonUtil.decodeScaledBitmapFromDrawableResource(context.getResources(),
                isValid ? R.drawable.validation_ok : R.drawable.validation_wrong,
                context.getResources().getDimensionPixelSize(R.dimen.address_dialog_validation_img_size),
                context.getResources().getDimensionPixelSize(R.dimen.address_dialog_validation_img_size));
        Drawable validationDrawable = new BitmapDrawable(validationBitmap);
        field.setCompoundDrawablesWithIntrinsicBounds(validationDrawable, null, null, null);
        field.setCompoundDrawablePadding(10);
    }

    public static void RemoveValidationFromEditText(Context context, EditText field) {
        field.getBackground().setColorFilter(context.getResources()
                .getColor(R.color.basic_blue), PorterDuff.Mode.SRC_ATOP);
        field.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private static Tracker GetGoogleAnalyticsTracker(Context context) {
        return GoogleAnalytics.getInstance(context).newTracker(context.getString(R.string.google_analytics_id));
    }

    public static void PostGoogleAnalyticsUIEvent(Context context, String screenName, String uiControlName, String uiEventType) {
        Tracker tracker = GetGoogleAnalyticsTracker(context);
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.EventBuilder().setCategory("UI").setAction(uiEventType).setLabel(uiControlName).build());
    }

    public static void PostGoogleAnalyticsActivityOpened(Context context, String screenName) {
        Tracker tracker = GetGoogleAnalyticsTracker(context);
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.EventBuilder().setCategory("ActivityOpened").build());
    }

    public static Bitmap LoadAndSavePicture(InputStream is, String url, int maxImageWidthHeight, String imageFolderPath, String fileName) {
        InputStream input = is;
        try {
            HttpURLConnection connection = null;
            URL urlurl = new URL(url);
            connection = (HttpURLConnection) urlurl.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(1000);
            connection.setUseCaches(false);
            input = new BufferedInputStream(connection.getInputStream());

            if(imageFolderPath != null) {
                File photo = new File(Environment.getExternalStorageDirectory() + imageFolderPath, fileName);
                if (photo.exists()) {
                    photo.delete();
                }
                photo.createNewFile();
                OutputStream output = new FileOutputStream(photo.getAbsolutePath());
                byte data[] = new byte[1024];
                int count;
                try {
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                catch (Exception e){
                    photo.delete();
                    throw e;
                }


/*
            if (imageFolderPath != null) {
                try {
                    FileOutputStream fos = new FileOutputStream(photo.getPath());
                    fos.write(result);
                    fos.close();
                } catch (IOException e) {
                    Log.e(MY_TAG, "cant save image");
                }
                Log.i(MY_TAG, "succeeded load and image " + photo.getPath());
            }
*/
                Bitmap bitmap = decodeScaledBitmapFromSdCard(photo.getAbsolutePath(), 90, 90);
                bitmap = PreRotateBitmapFromFile(bitmap, photo);
                return bitmap;
            } else {
                byte[] result = IOUtils.toByteArray(input);
                result = CommonUtil.CompressImageByteArrayByMaxSize(result, maxImageWidthHeight);
                Log.i(MY_TAG, "Compressed image to " + (int) Math.round(result.length / 1024) + " kb");
                Bitmap b = BitmapFactory.decodeByteArray(result, 0, result.length);
                return b;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(MY_TAG, "cant load image for: " + fileName);
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Bitmap PreRotateBitmapFromFile(Bitmap bitmap, File photoFile){
        if(bitmap == null)
            return null;
        Bitmap result = null;
        try {
            ExifInterface ei = new ExifInterface(photoFile.getAbsolutePath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix mtx = new Matrix();
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    //rotate CCW
                    mtx.preRotate(90);
                    result = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    //rotate CW
                    mtx.preRotate(-90);
                    result = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
                    break;

                //CONSIDER OTHER CASES HERE....

                default:
                    result = bitmap;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    return result;
    }

    public static boolean CheckIfStringsDiffer(String string1, String string2) {
        if (string1 == null && string2 != null) return true;
        if (string1 != null && string2 == null) return true;
        if (string1 == null && string2 == null) return false;
        return (string1.compareTo(string2) != 0);
    }

    public static String GetNetworkUrl(String baseUrl, String photoURL) {
        return baseUrl + photoURL + "?type=large";
    }

    public static int GetMyUserID(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        return sp.getInt(context.getString(R.string.shared_preferences_user_data_id_key), -1);
    }

    public static boolean SaveMyUserID(Context context, int userID) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.shared_preferences_user_data_id_key), userID);
        return editor.commit();
    }

    public static String GetMyPhoneNumberFromPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.shared_preferences_user_data_phone_num_key), "");
    }

    public static boolean SaveMyPhoneNumberToPreferences(Context context, String phoneNum) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.shared_preferences_user_data_phone_num_key), phoneNum);
        return editor.commit();
    }

    public static String GetMyUserNameFromPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.shared_preferences_user_data_user_name), "");
    }

    public static boolean SaveMyUserNameToPreferences(Context context, String userName) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.shared_preferences_user_data_user_name), userName);
        return editor.commit();
    }

    public static String GetMyEmailFromPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.shared_preferences_user_data_email), "");
    }

    public static boolean SaveMyEmailToPreferences(Context context, String phoneNum) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getString(R.string.shared_preferences_user_data_email), phoneNum);
        return editor.commit();
    }

    public static LatLngBounds GetBoundsByCenterLatLng(LatLng center, double maxDistance) {
        double distance = maxDistance * 1.5;
        return new LatLngBounds
                (new LatLng(center.latitude - distance, center.longitude - distance),
                        new LatLng(center.latitude + distance, center.longitude + distance));
    }

    public static double GetDistance(LatLng pos1, LatLng pos2) {
        return Math.sqrt(Math.pow(pos1.latitude - pos2.latitude, 2) + Math.pow(pos1.longitude - pos2.longitude, 2));
    }

    public static Point GetScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static void HideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static AlertDialog ShowDialogNeedToRegister(final Context context, final int doAfterRegistrationCode, final IPleaseRegisterDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.dialog_title_please_register));
        builder.setMessage(context.getString(R.string.dialog_message_please_register));
        String positiveText = context.getString(R.string.dialog_button_register_now);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.YesRegisterNow(doAfterRegistrationCode);
                dialogInterface.dismiss();
            }
        });
        String negativeText = context.getString(R.string.dialog_button_no_thanks);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static byte[] BitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //todo: error null pointer could be thrown here, check if reproducable
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static int GetNotificationsSettingsRadius(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.notif_settings_sp_key), Context.MODE_PRIVATE);
        int result = sp.getInt(context.getString(R.string.notif_settings_radius_key), context.getResources().getInteger(R.integer.notifications_settings_default_radius));
        if (result == context.getResources().getInteger(R.integer.notifications_settings_default_radius)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(context.getString(R.string.notif_settings_radius_key), context.getResources().getInteger(R.integer.notifications_settings_default_radius));
            editor.commit();
        }
        return result;
    }

    public static void SetNotificationsSettingsRadius(Context context, int radius) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.notif_settings_sp_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(context.getString(R.string.notif_settings_radius_key), radius);
        editor.commit();
    }

    public static void SetDefaultNotificationsSettingsRadius(Context context) {
        SetNotificationsSettingsRadius(context, context.getResources().getInteger(R.integer.notifications_settings_default_radius));
    }

    public static boolean GetNotificationsSettingsIsOn(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.notif_settings_sp_key), Context.MODE_PRIVATE);
        boolean result = sp.getBoolean(context.getString(R.string.notif_settings_is_on_key), true);
        if (result) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(context.getString(R.string.notif_settings_is_on_key), true);
            editor.commit();
        }
        return result;
    }

    public static void SetNotificationsSettingsIsOn(Context context, boolean isOn) {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.notif_settings_sp_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(context.getString(R.string.notif_settings_is_on_key), isOn);
        editor.commit();
    }

    public static String GetTimeLeftString(Context context, Date start, Date end){
        long timeSpanInSeconds = (end.getTime() - start.getTime())/1000;
        long secondsLeft = (timeSpanInSeconds >= 60 ? timeSpanInSeconds % 60 : timeSpanInSeconds);
        long timeSpanInMinutes = timeSpanInSeconds / 60;
        long minutesLeft = (timeSpanInMinutes >= 60 ? timeSpanInMinutes % 60 : timeSpanInMinutes);
        long timeSpanInHours = timeSpanInMinutes / 60;
        long hoursLeft = (timeSpanInHours >= 24 ? timeSpanInHours % 24 : timeSpanInHours);
        long timeSpanInDays = timeSpanInHours / 24;
        return timeSpanInDays >= 1
                ? context.getString(R.string.time_left_format_more_than_day).replace("{0}", String.valueOf(timeSpanInDays)).replace("{1}", String.valueOf(hoursLeft))
                : context.getString(R.string.time_left_format_less_one_day).replace("{0}", String.valueOf(timeSpanInHours)).replace("{1}", String.valueOf(minutesLeft));
    }

    public static void ClearUserDataOnLogOut(Context context, boolean sharedPrefsOnly){
        //clearing user data from SharedPreferences
        SharedPreferences spSocialNetworkData = context.getSharedPreferences(context.getString(R.string.shared_preferences_google_facebook_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editorSND = spSocialNetworkData.edit();
        editorSND.clear();
        editorSND.commit();
        SharedPreferences spUserData = context.getSharedPreferences(context.getString(R.string.shared_preferences_user_data_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editorUD = spUserData.edit();
        editorUD.clear();
        editorUD.commit();

        //delete avatar picture
        File avatarPic = new File(Environment.getExternalStorageDirectory() + context.getString(R.string.image_folder_path),
                context.getString(R.string.user_avatar_file_name));
        if(avatarPic.exists()) {
            avatarPic.delete();
            Log.i(MY_TAG, "user avatar image file deleted");
        }

        if(sharedPrefsOnly)
            return;

        //clear non public data from db
        int groupsDeleted = context.getContentResolver().delete(FooDoNetSQLProvider.URI_GROUP, null, null);
        Cursor cursor = context.getContentResolver()
                .query(FooDoNetSQLProvider.CONTENT_URI,
                        FCPublication.GetColumnNamesArray(),
                        FCPublication.PUBLICATION_AUDIENCE_KEY + " != 0", null, null);
        ArrayList<FCPublication> pubs = FCPublication.GetArrayListOfPublicationsFromCursor(cursor, false);
        cursor.close();
        String pubIDs[] = new String[pubs.size()];
        int picsDeleted = 0;
        for(int i = 0; i < pubIDs.length; i++) {
            pubIDs[i] = String.valueOf(pubs.get(i).getUniqueId());
            File pubPic = new File(Environment.getExternalStorageDirectory() + context.getString(R.string.image_folder_path),
                    context.getString(R.string.publication_picture_file_name_format)
                            .replace("{0}", String.valueOf(pubs.get(i).getUniqueId()))
                            .replace("{1}", String.valueOf(pubs.get(i).getVersion())));
            if(pubPic.exists()) {
                pubPic.delete();
                picsDeleted++;
            }
        }
        int registeredDeleted = 0;
        int reportsDeleted = 0;
        int publicationsDeletedFromDB = 0;
        if(pubIDs.length > 0) {
            registeredDeleted
                    = context.getContentResolver().delete(FooDoNetSQLProvider.URI_GET_ALL_REGS,
                    RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID + " = ", pubIDs);
            reportsDeleted
                    = context.getContentResolver().delete(FooDoNetSQLProvider.URI_GET_ALL_REPORTS,
                    PublicationReport.PUBLICATION_REPORT_FIELD_KEY_PUBLICATION_ID + " = ", pubIDs);
            publicationsDeletedFromDB
                    = context.getContentResolver()
                    .delete(FooDoNetSQLProvider.CONTENT_URI, FCPublication.PUBLICATION_AUDIENCE_KEY + " != 0", null);
        }
        Log.i(MY_TAG, "Cleared db on logout. Pubs deleted: " + String.valueOf(publicationsDeletedFromDB)
                        + ", regs deleted: " + String.valueOf(registeredDeleted)
                        + ", reps deleted: " + String.valueOf(reportsDeleted)
                        + ", groups deleted: " + String.valueOf(groupsDeleted)
                        + ", pub pics deleted: " + String.valueOf(picsDeleted));
        Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
        intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                ServicesBroadcastReceiver.ACTION_CODE_RELOAD_DATA_SUCCESS);
        context.sendBroadcast(intent);
    }

    public static void SetIsApplicationRunningInForeground(Context ctx, boolean isRunning){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.is_app_running_in_foreground_token), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(ctx.getString(R.string.is_app_running_in_foreground_key), isRunning);
        editor.commit();
    }

    public static boolean GetIsApplicationRunningInForeground(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.is_app_running_in_foreground_token), Context.MODE_PRIVATE);
        return sp.getBoolean(ctx.getString(R.string.is_app_running_in_foreground_key), false);
    }

    public static void SavePendingBroadcastToSharedPreferences(Context context, Intent intent){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_pending_broadcast), context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = sp.edit();
        if(sp.contains(context.getString(R.string.shared_preferences_pending_broadcast_value)))
            editor.remove(context.getString(R.string.shared_preferences_pending_broadcast_value));
        editor.putInt(context.getString(R.string.shared_preferences_pending_broadcast_value),
                intent.getIntExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY, -1));
        editor.commit();
    }

    public static void ClearPendingBroadcastFromSharedPreferences(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_preferences_pending_broadcast), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(context.getString(R.string.shared_preferences_pending_broadcast_value));
        editor.commit();
    }

    public static int GetPendingBroadcastTypeFromSharedPreferences(Context context){
        SharedPreferences sp = context.getSharedPreferences(
                context.getString(R.string.shared_preferences_pending_broadcast), context.MODE_PRIVATE);
        return sp.getInt(context.getString(R.string.shared_preferences_pending_broadcast_value), -1);
    }


}
