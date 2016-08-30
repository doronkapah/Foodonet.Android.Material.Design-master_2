package CommonUtilPackage;

import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Asher on 06.02.2016.
 */
public class ImageDictionarySyncronized {
    private Map<Integer, Drawable> imageDictionary;

    public ImageDictionarySyncronized(){
        imageDictionary = new HashMap<>();
    }

    public Drawable Get(int key){
        synchronized (imageDictionary){
            if(imageDictionary.containsKey(key)) {
                final Drawable drawable = imageDictionary.get(key);
                return drawable;
            }
            return null;
        }
    }

    public void Put(int key, Drawable value){
        synchronized (imageDictionary){
            if(!imageDictionary.containsKey(key))
                imageDictionary.put(key, value);
        }
    }

    public void Clear(){
        synchronized (imageDictionary){
            imageDictionary.clear();
        }
    }
}
