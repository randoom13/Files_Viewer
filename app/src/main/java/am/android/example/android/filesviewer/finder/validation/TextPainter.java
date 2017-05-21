package am.android.example.android.filesviewer.finder.validation;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;

import java.util.List;

public class TextPainter {
    private final String[] mFilters;

    public TextPainter() {
        mFilters = new String[0];
    }

    public TextPainter(List<String> filters) {
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
                colorString.setSpan(new BackgroundColorSpan(provider.getBackgroundColor(paintingString, filter)),
                        index, index + filter.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = TextUtils.indexOf(colorString, filter, index + filter.length());
            }
        }
        return colorString;
    }
}
