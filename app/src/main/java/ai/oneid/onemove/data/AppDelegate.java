package ai.oneid.onemove.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.appcompat.app.AlertDialog;
import androidx.multidex.MultiDexApplication;

import com.devs.acr.AutoErrorReporter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ai.oneid.onemove.R;
import ai.oneid.onemove.activity.CreateAccount1Activity;

public class AppDelegate extends MultiDexApplication {
    public static int BARCODE_REQUEST_CODE = 0;
    public static String SharedPreferencesTag = "one_move";

    public static String appPath = "/OneMove";
    public static String tempFolder = "/tempFolder";
    public static String apikey = "9c24abc8797a4554a54f3c6c26c705d9";
    public static String credDefIdOneIdentity = "8CXxwinErn2oJiUt3XyKix:3:CL:182:TAG1";

    public AppDelegate() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AutoErrorReporter.get(this)
                .setEmailAddresses("hochankit@gmail.com")
                .setEmailSubject(getString(R.string.app_name) + " - Crash Report")
                .start();
    }

    public void createDirectoryIfNotExist(String directoryPath) {
        try {
            File dir = new File(directoryPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void alert(Activity activity, String title, String message)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static String fileNameGenerator(Context context)
    {
        Calendar now = Calendar.getInstance();
        Date date = now.getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
        String formattedDate = format.format(date);

        return getDeviceId(context) + "-" + formattedDate;
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context)
    {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getResizedImagePathFromOriginalPath(Context context, String imagePath, int desiredWidth, int desiredHeight)
    {
        String resizedFilePath = Environment.getExternalStorageDirectory().toString() + AppDelegate.appPath + "/" + fileNameGenerator(context) + ".jpg";
        Bitmap scaledBitmap = loadResizedBitmap(imagePath, desiredWidth, desiredHeight, true);
        File file = new File(resizedFilePath);
        FileOutputStream fOut;
        try
        {
            fOut = new FileOutputStream(file);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception e)
        { // TODO
            e.printStackTrace();
            //scaledBitmap.recycle();
        }
        if(scaledBitmap != null)
            scaledBitmap.recycle();

        return resizedFilePath;
    }

    public static Bitmap loadResizedBitmap(String filename, int width, int height, boolean exact ) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        if (options.outHeight > 0 && options.outWidth > 0 ) {
            options.inJustDecodeBounds = false;
            options.inSampleSize = 2;

            int oriWidth = options.outWidth;
            int oriHeight = options.outHeight;
            while (options.outWidth  / options.inSampleSize > width &&
                    options.outHeight / options.inSampleSize > height ) {
                options.inSampleSize++;
            }
            options.inSampleSize--;
            bitmap = BitmapFactory.decodeFile(filename, options);
            if ( bitmap != null && exact )
            {
                int bWidth = bitmap.getWidth();
                int bHeight = bitmap.getHeight();
                int nHeight = (int) Math.round(width/( (1.0*bWidth) / bHeight ));

                //int nHeight = Math.round(height * parentRatio);
                bitmap = Bitmap.createScaledBitmap( bitmap, width, nHeight, false );

                try {
                    ExifInterface exif = new ExifInterface(filename);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    }
                    else if (orientation == 3) {
                        matrix.postRotate(180);
                    }
                    else if (orientation == 8) {
                        matrix.postRotate(270);
                    }
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
                catch (Exception e) {

                }
            }
        }
        return bitmap;
    }

    public static String formatString(String text)
    {
        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }

    public enum PasswordStrength
    {
        WEAK(0, Color.RED), MEDIUM(1, Color.argb(255, 220, 185, 0)), STRONG(2, Color.GREEN), VERY_STRONG(3, Color.BLUE);

        //--------REQUIREMENTS--------
        static int REQUIRED_LENGTH = 8;
        static int MAXIMUM_LENGTH = 100;
        static boolean REQUIRE_SPECIAL_CHARACTERS = false;
        static boolean REQUIRE_DIGITS = true;
        static boolean REQUIRE_LOWER_CASE = true;
        static boolean REQUIRE_UPPER_CASE = true;

        int resId;
        int color;

        PasswordStrength(int resId, int color)
        {
            this.resId = resId;
            this.color = color;
        }

        public int getValue()
        {
            return resId;
        }

        public int getColor()
        {
            return color;
        }

        public static PasswordStrength calculatePasswordStrength(String password)
        {
            int currentScore = 0;
            boolean sawUpper = false;
            boolean sawLower = false;
            boolean sawDigit = false;
            boolean sawSpecial = false;

            for (int i = 0; i < password.length(); i++)
            {
                char c = password.charAt(i);
                if (!sawSpecial && !Character.isLetterOrDigit(c))
                {
                    currentScore += 1;
                    sawSpecial = true;
                }
                else
                {
                    if (!sawDigit && Character.isDigit(c))
                    {
                        currentScore += 1;
                        sawDigit = true;
                    }
                    else
                    {
                        if (!sawUpper || !sawLower)
                        {
                            if (Character.isUpperCase(c))
                                sawUpper = true;
                            else
                                sawLower = true;
                            if (sawUpper && sawLower)
                                currentScore += 1;
                        }
                    }
                }
            }

            if (password.length() > REQUIRED_LENGTH)
            {
                if ((REQUIRE_SPECIAL_CHARACTERS && !sawSpecial) || (REQUIRE_UPPER_CASE && !sawUpper) || (REQUIRE_LOWER_CASE && !sawLower) || (REQUIRE_DIGITS && !sawDigit))
                {
                    currentScore = 1;
                }
                else
                {
                    currentScore = 2;
                    if (password.length() > MAXIMUM_LENGTH)
                    {
                        currentScore = 3;
                    }
                }
            }
            else
            {
                currentScore = 0;
            }

            switch (currentScore)
            {
                case 0:
                    return WEAK;
                case 1:
                    return MEDIUM;
                case 2:
                    return STRONG;
                case 3:
                    return VERY_STRONG;
                default:
            }

            return VERY_STRONG;
        }
    }

    public static Bitmap encodeStringAsQRBitmap(String str) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 500, 500, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 500, 0, 0, w, h);
        return bitmap;
    }

    public static boolean isRegistered(Activity activity)
    {
        SharedPreferences preferences = activity.getSharedPreferences(AppDelegate.SharedPreferencesTag, Context.MODE_PRIVATE);
        String address = preferences.getString(activity.getResources().getString(R.string.param_oneid_wallet_address), "");
        if(address.equals("")) {
            Intent intent = new Intent(activity, CreateAccount1Activity.class);
            activity.startActivity(intent);
            activity.finish();
            return false;
        }
        else
        {
            return true;
        }
    }

    public String getCountryZipCode()
    {
        String CountryID="";
        String CountryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl=this.getResources().getStringArray(R.array.country_code);
        for(int i=0;i<rl.length;i++){
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())){
                CountryZipCode=g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    public static String currencyFormat(String amount) {
        try {
            DecimalFormat formatter = new DecimalFormat("###,###,##0.00");
            return formatter.format(Double.parseDouble(amount));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return amount;
        }
    }
}