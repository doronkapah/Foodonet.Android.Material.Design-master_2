package FooDoNetServerClasses;

import android.graphics.Bitmap;

import java.util.Map;

/**
 * Created by ah on 31/08/15.
 */
public interface IDownloadImageCallBack {
    public void OnImageDownloaded(Map<Integer, byte[]> imagesMap);
}
