package am.android.example.android.filesviewer.finder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import am.android.example.android.filesviewer.finder.validation.TextPainter;
import am.android.example.android.filesviewer.finder.validation.Validatable;


class SearchFilesAsyncTaskBase extends SmartAsyncTask<File, File, SearchedItemInfo, Void> {
    private final Validatable<File, TextPainter> mValidator;
    private final String mSearchFilter;

    public SearchFilesAsyncTaskBase(Validatable<File, TextPainter> validator, String searchFilter) {
        super();
        mValidator = validator;
        mSearchFilter = searchFilter;
    }

    private void scan(File root) {
        List<File> files = new ArrayList();
        files.add(root);
        while (!files.isEmpty()) {
            File file = files.remove(0);
            if (isCancelled())
                break;

            TextPainter textPainter = mValidator.isValid(file, mSearchFilter);
            if (textPainter != null) {
                sentProgressUpdate(false, root, new SearchedItemInfo(file, textPainter));
            }

            if (isCancelled())
                break;

            if (!file.isDirectory())
                continue;


            File[] dirFiles = file.listFiles();
            if (dirFiles == null)
                continue;

            if (files.size() > 0)
                files.addAll(0, Arrays.asList(dirFiles));
            else
                files.addAll(Arrays.asList(dirFiles));

        }
    }

    public String getSearchFilter() {
        return mSearchFilter;
    }

    @Override
    protected Void doInBackground(File... roots) {
        for (File root : roots) {
            scan(root);
        }
        return null;
    }
}
