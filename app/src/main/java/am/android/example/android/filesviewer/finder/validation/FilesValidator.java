package am.android.example.android.filesviewer.finder.validation;

import android.text.TextUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FilesValidator implements Validateable<File, TextPainter> {
    private final static String sOrSplitter = ";";

    public TextPainter isValid(File file, String filter) {
        if (TextUtils.isEmpty(filter))
            return new TextPainter();

        String absolutePath = file.getAbsolutePath();
        Set<String> filters = new HashSet<String>();
        for (String split : filter.split(sOrSplitter)) {
            if (!split.isEmpty() && absolutePath.contains(split))
                filters.add(split);
        }

        return filters.isEmpty() ? null : new TextPainter(filters);
    }
}
