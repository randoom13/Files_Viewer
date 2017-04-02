package am.android.example.android.filesviewer.finder;

import java.io.File;

import am.android.example.android.filesviewer.finder.validation.TextColorProvider;
import am.android.example.android.filesviewer.finder.validation.TextPainter;

public class SearchedItemInfo {
    private final String mPath;
    private final boolean mIsDir;
    private final long mLength;
    private final TextPainter mTextPainter;
    private CharSequence mColorString;

    public SearchedItemInfo(File file, TextPainter painter) {
        mPath = file.getAbsolutePath();
        mIsDir = file.isDirectory();
        mLength = file.length();
        mTextPainter = painter;
    }

    void colorText(TextColorProvider colorProvider) {
        mColorString = mTextPainter.getPaintedString(mPath, colorProvider);
    }

    public CharSequence getText() {
        if (mColorString != null)
            return mColorString;
        else return TextPainter.getColorlessString(mPath);
    }

    public long getLength() {
        return mLength;
    }

    public boolean getIsDir() {
        return mIsDir;
    }
}
