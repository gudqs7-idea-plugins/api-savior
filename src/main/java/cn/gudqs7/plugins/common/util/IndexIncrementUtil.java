package cn.gudqs7.plugins.common.util;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wq
 */
public class IndexIncrementUtil {

    private static final AtomicReference<Integer> INDEX = new AtomicReference<>(0);

    public static int getIndex() {
        return INDEX.getAndSet(INDEX.get() + 1);
    }

    public static void clear() {
        INDEX.set(0);
    }

}
