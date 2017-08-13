package am.android.example.android.filesviewer.finder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import am.android.example.android.filesviewer.finder.validation.TextPainter;
import am.android.example.android.filesviewer.finder.validation.Validateable;

class SearchFilesAsyncTaskBase extends SmartAsyncTask<File, SearchedRootInfo, Void> {
    private final Validateable<File, TextPainter> mValidator;
    private final String mSearchFilter;

    public SearchFilesAsyncTaskBase(Validateable<File, TextPainter> validator, String searchFilter) {
        super();
        mValidator = validator;
        mSearchFilter = searchFilter;
    }

    @Override
    protected void addValue(SearchedRootInfo newRootInfo) {
        boolean handled = false;
        for (SearchedRootInfo rootInfo : mValues) {
            if (rootInfo.isSameRoot(newRootInfo)) {
                rootInfo.addAllFrom(newRootInfo);
                handled = true;
                break;
            }
        }

        if (!handled)
            mValues.add(newRootInfo);
    }

    @Override
    protected int ValuesSize() {
        int size = 0;
        for (SearchedRootInfo rootInfo : mValues) {
            size += rootInfo.size();
        }
        return size;
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
                SearchedRootInfo rootInfo = new SearchedRootInfo(root, new SearchedItemInfo(file, textPainter));
                sentProgressUpdate(false, rootInfo);
            }

            if (isCancelled())
                break;

            if (!file.isDirectory())
                continue;

            File[] dirFiles = file.listFiles();
            if (dirFiles == null)
                continue;
            Collection<File> dirFilesCollection = Arrays.asList(dirFiles);
            if (files.size() > 0)
                files.addAll(0, dirFilesCollection);
            else
                files.addAll(dirFilesCollection);
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
        if (!isCancelled())
            this.onProgressUpdate();
        return null;
    }
}
