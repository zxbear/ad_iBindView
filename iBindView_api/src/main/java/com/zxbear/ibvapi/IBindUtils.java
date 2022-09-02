package com.zxbear.ibvapi;

import android.os.Build;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;

public class IBindUtils {

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public static <T> T findRequiredViewAsType(View source, @IdRes int id,
                                               Class<T> cls) {
        View view = findRequiredView(source, id);
        return castView(view, id, cls);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public static View findRequiredView(View source, @IdRes int id) {
        View view = source.findViewById(id);
        if (view != null) {
            return view;
        }
        String name = getResourceEntryName(source, id);
        throw new IllegalStateException("Required view '"
                + name
                + "' with ID "
                + id
                + " for "
                + " was not found. If this view is optional add '@Nullable' (fields) or '@Optional'"
                + " (methods) annotation.");
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public static <T> T castView(View view, @IdRes int id, Class<T> cls) {
        try {
            return cls.cast(view);
        } catch (ClassCastException e) {
            String name = getResourceEntryName(view, id);
            throw new IllegalStateException("View '"
                    + name
                    + "' with ID "
                    + id
                    + " for "
                    + " was of the wrong type. See cause for more info.", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unchecked")
    public static <T> List<T> listFilteringNull(T... views) {
        return new ImmutableList<>(arrayFilteringNull(views));
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayFilteringNull(T... views) {
        int end = 0;
        int length = views.length;
        for (int i = 0; i < length; i++) {
            T view = views[i];
            if (view != null) {
                views[end++] = view;
            }
        }
        return end == length
                ? views
                : Arrays.copyOf(views, end);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private static String getResourceEntryName(View view, @IdRes int id) {
        if (view.isInEditMode()) {
            return "<unavailable while editing>";
        }
        return view.getContext().getResources().getResourceEntryName(id);
    }
}
