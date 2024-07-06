package openblocks.lib.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

public class CollectionUtils {
    public static <T> T cycle(List<T> list, T value, boolean backwards) {
        return cycleInternal(list, value, list::indexOf, backwards);
    }

    public static <T extends Enum<T>> T cycle(T[] values, T value, boolean backwards) {
        return cycleInternal(Arrays.asList(values), value, Enum::ordinal, backwards);
    }

    private static <T> T cycleInternal(List<T> list, T value, ToIntFunction<T> indexOf, boolean backwards) {
        int index = indexOf.applyAsInt(value);
        if (backwards)
            return list.get(index == 0 ? list.size() - 1 : index - 1);
        else
            return list.get((index + 1) % list.size());
    }
}
