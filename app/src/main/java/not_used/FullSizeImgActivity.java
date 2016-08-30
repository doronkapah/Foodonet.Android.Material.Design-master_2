//package upp.foodonet.material;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.drawable.BitmapDrawable;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.ImageView;
//
//import CommonUtilPackage.CommonUtil;
//
///**
// * Created by nikolaiVolodin on 19/10/2015.
//// */
//public class FullSizeImgActivity extends Activity
//{
//
//    private static final String MY_TAG = "food_imgFullScreen";
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.image_layout);
//
//        Intent i = this.getIntent();
//        String fileName = i.getStringExtra("fileName");
//        if(fileName == null || fileName.length() == 0){
//            Log.e(MY_TAG, "got empty filename string from Extras");
//            finish();
//        }
//
//
//        ImageView imgFullSize = (ImageView)findViewById(R.id.iv_full_image_size);
//
//        BitmapDrawable fullSizeImage = CommonUtil.GetBitmapDrawableFromFile(fileName,
//                getString(R.string.image_folder_path), 1000, 1000);
//
//        imgFullSize.setImageDrawable(fullSizeImage);
//
//    }
//}