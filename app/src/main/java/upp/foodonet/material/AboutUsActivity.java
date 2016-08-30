package upp.foodonet.material;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Asher on 22.07.2016.
 */
public class AboutUsActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String MY_TAG = "food_about_us";

    private String currentVersion;

    TextView tv_current_version;
    ImageButton btn_like_us;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_about_us);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tv_current_version = (TextView)findViewById(R.id.tv_about_us_version);
        tv_current_version.setText(getString(R.string.about_us_current_version_format).replace("{0}", currentVersion));

        btn_like_us = (ImageButton) findViewById(R.id.btn_like_us_on_facebook);
        btn_like_us.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_like_us_on_facebook:
                PostOnFacebook();
                break;
        }
    }

    private void PostOnFacebook() {
        Intent facebookIntent = new Intent(Intent.ACTION_SEND);
        String msg = getString(R.string.about_us_text) + "\n " + getString(R.string.facebook_page_url) + "\n ";
        facebookIntent.putExtra(Intent.EXTRA_TEXT, msg);
        facebookIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(GetLogoAsTmpFile()));
        facebookIntent.setType("image/*");
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(facebookIntent, PackageManager.MATCH_DEFAULT_ONLY);
        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList) {
            if (resolveInfo.activityInfo.packageName.startsWith("com.facebook.katana")) {
                facebookIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved) {
            startActivityForResult(facebookIntent, 0);
        } else {
            Toast.makeText(this, "Facebook app isn't found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.katana"));
            startActivity(intent);
        }
    }

    private File GetLogoAsTmpFile(){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo_for_icon);
        File imageFile
                = new File(Environment.getExternalStorageDirectory()
                    + getString(R.string.image_folder_path), "tmp.jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.close();
            return imageFile;
        }
        catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    private void DeleteTmpFile(){
        File file
                = new File(Environment.getExternalStorageDirectory()
                    + getString(R.string.image_folder_path), "tmp.jpg");
        if(file.exists())
            file.delete();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DeleteTmpFile();
    }
}
