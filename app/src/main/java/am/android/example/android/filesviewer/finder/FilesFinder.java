package am.android.example.android.filesviewer.finder;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;
import am.android.example.android.filesviewer.finder.validation.TextPainter;
import am.android.example.android.filesviewer.finder.validation.Validateable;
import de.greenrobot.event.EventBus;

public class FilesFinder {
    private final Validateable<File, TextPainter> mValidator;
    private final TextColorProvider mTextColorProvider;
    private SearchFilesAsyncTask mSearchFilesAsyncTask;
    private File[] mRoots;

    public FilesFinder(Collection<File> roots, Validateable<File, TextPainter> validator, TextColorProvider textColorProvider) {
        mValidator = validator;
        mTextColorProvider = textColorProvider;
        mRoots = new File[roots.size()];
        roots.toArray(mRoots);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            task.execute(params);
        }
    }

    public String getFilter() {
        if (mSearchFilesAsyncTask != null)
            return mSearchFilesAsyncTask.getSearchFilter();
        else
            return "";
    }

    public void stop() {
        if (mSearchFilesAsyncTask != null)
            mSearchFilesAsyncTask.cancel(false);
    }

    public void reset(String searchFilter) {
        stop();
        launch(searchFilter);
    }

    public void launch(String searchFilter) {
        mSearchFilesAsyncTask = new SearchFilesAsyncTask(mValidator, searchFilter);
        SearchResultEvent event = new SearchResultEvent(ProcessStatus.PENDING);
        EventBus.getDefault().post(event);
        executeAsyncTask(mSearchFilesAsyncTask, mRoots);
    }

    private void sentNotification(SearchFilesAsyncTask sender,
                                  Collection<SearchedRootInfo> values, boolean isFinished) {
        if (sender != mSearchFilesAsyncTask || sender.isCancelled())
            return;

        SearchResultEvent event = isFinished ?
                new SearchResultEvent(ProcessStatus.FINISHED) :
                new SearchResultEvent(ProcessStatus.RUNNING, values);
        event.setSearchFilter(sender.getSearchFilter());
        event.colorText(mTextColorProvider);
        EventBus.getDefault().post(event);
    }

    class SearchFilesAsyncTask extends SearchFilesAsyncTaskBase {
        public SearchFilesAsyncTask(Validateable<File, TextPainter> validator, String searchFilter) {
            super(validator, searchFilter);
        }

        @Override
        protected Void doInBackground(File... roots) {
            super.doInBackground(roots);
            if (!isCancelled())
                sentNotification(SearchFilesAsyncTask.this, new ArrayList<SearchedRootInfo>(), true);
            return null;
        }

        @Override
        protected void onProgressUpdate(Collection<SearchedRootInfo> values) {
            sentNotification(SearchFilesAsyncTask.this, values, false);
        }
    }
}
