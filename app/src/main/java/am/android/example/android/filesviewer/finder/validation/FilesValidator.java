package am.android.example.android.filesviewer.finder.validation;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesValidator implements Validatable<File, TextPainter> {
    private final static String sOrSplitter = ";";

    public TextPainter isValid(File file, String filter) {
        if (TextUtils.isEmpty(filter))
            return new TextPainter();

        String absolutePath = file.getAbsolutePath();
        List<String> filters = new ArrayList<String>();
        for (String splittedFilter : filter.split(sOrSplitter)) {
            if (!splittedFilter.isEmpty() && !filters.contains(splittedFilter)
                    && absolutePath.contains(splittedFilter)) {
                filters.add(splittedFilter);
            }
        }

        if (!filters.isEmpty())
            return new TextPainter(filters);
        else return null;
    }
}
