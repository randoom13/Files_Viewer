package am.android.example.android.filesviewer.finder;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;
import am.android.example.android.filesviewer.finder.validation.TextPainter;
import am.android.example.android.filesviewer.finder.validation.Validatable;
import de.greenrobot.event.EventBus;

public class FilesFinder {
    private final Validatable<File, TextPainter> mValidator;
    private final TextColorProvider mTextColorProvider;
    private final File[] mRoots;
    private SearchFilesAsyncTask mSearchFilesAsyncTask;

    public FilesFinder(File[] roots, Validatable<File, TextPainter> validator, TextColorProvider textColorProvider) {
        mValidator = validator;
        mTextColorProvider = textColorProvider;
        mRoots = roots;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            task.execute(params);
        }
    }

    public void reset(String searchFilter) {
        stop();
        launch(searchFilter);
    }

    public void launch(String searchFilter) {
        mSearchFilesAsyncTask = new SearchFilesAsyncTask(mValidator, searchFilter);
        SearchResultEvent event = new SearchResultEvent(SearchProcessStatus.BEGINNING);
        EventBus.getDefault().post(event);
        executeAsyncTask(mSearchFilesAsyncTask, mRoots);
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

    private void sentNotification(SearchFilesAsyncTask asyncTask,
                                  Map<File, List<SearchedItemInfo>> values, boolean isCompleted) {
        if (asyncTask != mSearchFilesAsyncTask || asyncTask.isCancelled())
            return;

        SearchResultEvent event = isCompleted ?
                new SearchResultEvent(SearchProcessStatus.COMPLETED) :
                new SearchResultEvent(SearchProcessStatus.RUNNING, values);
        event.setSearchFilter(asyncTask.getSearchFilter());
        event.colorSearchedItemInfos(mTextColorProvider);
        EventBus.getDefault().post(event);
    }

    class SearchFilesAsyncTask extends SearchFilesAsyncTaskBase {
        public SearchFilesAsyncTask(Validatable<File, TextPainter> validator, String searchFilter) {
            super(validator, searchFilter);
        }

        @Override
        protected Void doInBackground(File... params) {
            super.doInBackground(params);
            if (!isCancelled())
                sentNotification(SearchFilesAsyncTask.this,
                        new HashMap<File, List<SearchedItemInfo>>(), true);
            return null;
        }

        @Override

        protected void onProgressUpdate(Map<File, List<SearchedItemInfo>> values) {
            sentNotification(SearchFilesAsyncTask.this, values, false);
        }
    }
}
