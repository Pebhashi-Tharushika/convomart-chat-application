package lk.mbpt.chatapp.client;

/**
 * most code of this file is written by UltimateZero and Pavlo Buidenkov
 * UltimateZero github page: https://github.com/UltimateZero
 * Pavlo Buidenkov github page: https://github.com/pavlobu
 */

import javafx.scene.image.Image;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class EmojiImageCache {
    private ConcurrentHashMap<String, WeakReference<Image>> map;
    private static EmojiImageCache INSTANCE;

    private EmojiImageCache() {
        map = new ConcurrentHashMap<>();
    }

    public Image getImage(String path) {
        WeakReference<Image> ref = map.get(path);
        if (ref == null || ref.get() == null) {
            ref = new WeakReference<>(new Image(path, true));
            map.put(path, ref);
        }
        return ref.get();
    }

    public static EmojiImageCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EmojiImageCache();
        }
        return INSTANCE;
    }
}