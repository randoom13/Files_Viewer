package am.android.example.android.filesviewer.finder;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class SmartAsyncTask<Params, ProgressK, ProgressV, Result>
        extends AsyncTask<Params, Void, Result> {

    private int mMaxItemsCount;
    private int mMaxDelay;
    private long mStartTime = 0l;
    private Map<ProgressK, List<ProgressV>> mValues = new HashMap<ProgressK, List<ProgressV>>();

    public SmartAsyncTask() {
        setMaxDelay(1000);
        setMaxItemsCount(100);
    }

    public int getMaxItemsCount() {
        return mMaxItemsCount;
    }

    public void setMaxItemsCount(int maxItemsCount) {
        this.mMaxItemsCount = maxItemsCount;
    }

    public int getMaxDelay() {
        return mMaxDelay;
    }

    public void setMaxDelay(int mMaxDelay) {
        this.mMaxDelay = mMaxDelay;
    }

    private int ItemsCount() {
        int length = 0;
        for (List<ProgressV> items : mValues.values()) {
            length += items.size();
        }
        return length;
    }

    protected void sentProgressUpdate(Boolean force, ProgressK k, ProgressV v) {
        if (k != null && v != null) {
            if (!mValues.containsKey(k))
                mValues.put(k, new ArrayList<ProgressV>());

            mValues.get(k).add(v);
        }

        boolean isSent = force || ItemsCount() >= getMaxItemsCount()
                || (SystemClock.elapsedRealtime() - mStartTime >= getMaxDelay());

        if (isSent)
            onProgressUpdate();
    }

    protected void onProgressUpdate() {
        mStartTime = SystemClock.elapsedRealtime();
        onProgressUpdate(mValues);
        mValues.clear();
    }

    protected void onProgressUpdate(Map<ProgressK, List<ProgressV>> values) {
    }
}
