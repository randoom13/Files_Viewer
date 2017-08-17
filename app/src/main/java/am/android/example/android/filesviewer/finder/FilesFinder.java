package am.android.example.android.filesviewer.finder;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import java.io.File;
import java.util.Collection;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;
import am.android.example.android.filesviewer.finder.validation.TextPainter;
import am.android.example.android.filesviewer.finder.validation.Validateable;
import de.greenrobot.event.EventBus;

public class FilesFinder implements SearchFilesAsyncTask.Notificapable {
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
        if (mSearchFilesAsyncTask != null) {
            mSearchFilesAsyncTask.cancel(false);
            mSearchFilesAsyncTask.unsubscribeNotification();
        }
    }

    public void reset(String searchFilter) {
        stop();
        launch(searchFilter);
    }

    public void launch(String searchFilter) {
        mSearchFilesAsyncTask = new SearchFilesAsyncTask(mValidator, searchFilter);
        mSearchFilesAsyncTask.setNotification(this);
        SearchResultEvent event = new SearchResultEvent(SearchResultEvent.PENDING_PROCESS_STATUS);
        EventBus.getDefault().post(event);
        executeAsyncTask(mSearchFilesAsyncTask, mRoots);
    }

    @Override
    public void sentNotification(SearchFilesAsyncTask sender,
                                 Collection<SearchedRootInfo> values, boolean isFinished) {
        if (sender != mSearchFilesAsyncTask || sender.isCancelled())
            return;

        SearchResultEvent event = isFinished ?
                new SearchResultEvent(SearchResultEvent.FINISHED_PROCESS_STATUS) :
                new SearchResultEvent(SearchResultEvent.RUNNING_PROCESS_STATUS, values);
        event.setSearchFilter(sender.getSearchFilter());
        event.colorText(mTextColorProvider);
        EventBus.getDefault().post(event);
    }
}
