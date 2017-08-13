package am.android.example.android.filesviewer.finder.validation;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;

import java.util.Set;

public class TextPainter {
    private final String[] mFilters;

    public TextPainter() {
        mFilters = new String[0];
    }

    public TextPainter(Set<String> filters) {
        mFilters = new String[filters.size()];
        filters.toArray(mFilters);
    }

    public static Spannable getColorlessString(String paintingString) {
        return new SpannableString(paintingString);
    }

    public Spannable getPaintedString(String paintingString, TextColorProvider provider) {
        Spannable colorString = getColorlessString(paintingString);

        if (provider == null)
            return colorString;

        for (String filter : mFilters) {
            int index = TextUtils.indexOf(colorString, filter);
            while (index >= 0) {
                int length = index + filter.length();
                colorString.setSpan(new BackgroundColorSpan(provider.getBackgroundColor(paintingString, filter)),
                        index, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = TextUtils.indexOf(colorString, filter, length);
            }
        }
        return colorString;
    }
}
