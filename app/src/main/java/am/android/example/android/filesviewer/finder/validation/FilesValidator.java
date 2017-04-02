package am.android.example.android.filesviewer.finder.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesValidator implements Validatable<File, TextPainter> {
    private final static String sOrSplitter = ";";

    public TextPainter isValid(File file, String filter) {
        if (filter == null || filter.length() == 0)
            return new TextPainter();

        String absolutePath = file.getAbsolutePath();
        List<String> usedfilters = new ArrayList<String>();
        for (String cuttedFilter : filter.split(sOrSplitter)) {
            if (cuttedFilter.length() > 0 && !usedfilters.contains(cuttedFilter)
                    && absolutePath.contains(cuttedFilter)) {
                usedfilters.add(cuttedFilter);
            }
        }

        if (usedfilters.size() > 0)
            return new TextPainter(usedfilters);
        else return null;
    }
}
