package am.android.example.android.filesviewer.finder;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class SmartAsyncTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

    protected final List<Progress> mValues = new ArrayList<Progress>();
    private int mMaxItemsSize;
    private int mMaxDelay;
    private long mStartTime = 0l;

    public SmartAsyncTask() {
        setMaxDelay(100);
        setMaxItemsSize(100);
    }

    public int getMaxItemsSize() {
        return mMaxItemsSize;
    }

    public void setMaxItemsSize(int maxItemsSize) {
        this.mMaxItemsSize = maxItemsSize;
    }

    public int getMaxDelay() {
        return mMaxDelay;
    }

    public void setMaxDelay(int ms) {
        this.mMaxDelay = ms;
    }

    abstract protected int ValuesSize();

    abstract protected void addValue(Progress value);

    protected void sentProgressUpdate(Boolean force, Progress value) {
        addValue(value);
        boolean isSent = force
                || (SystemClock.elapsedRealtime() - mStartTime) >= getMaxDelay()
                || ValuesSize() >= getMaxItemsSize();

        if (isSent)
            onProgressUpdate();
    }

    protected void onProgressUpdate() {
        mStartTime = SystemClock.elapsedRealtime();
        if (!mValues.isEmpty()) {
            onProgressUpdate(mValues);
            mValues.clear();
        }
    }

    protected void onProgressUpdate(Collection<Progress> values) {
    }
}
