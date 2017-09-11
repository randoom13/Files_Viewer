package am.android.example.android.filesviewer;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.github.johnkil.print.PrintView;

import am.android.example.android.filesviewer.finder.SearchedItemInfo;
import am.android.example.android.filesviewer.finder.SearchedRootInfo;


class ViewHolder {
    public final PrintView icon;
    public final TextView text;
    public final boolean isHeader;
    public final PrintView expandedIcon;
    private final View mRow;
    private int mHeaderPosition;

    ViewHolder(View row, boolean isHeader) {
        this.isHeader = isHeader;
        if (isHeader) {
            expandedIcon = (PrintView) row.findViewById(R.id.expanded_icon);
            icon = null;
        } else {
            icon = (PrintView) row.findViewById(R.id.icon);
            expandedIcon = null;
        }
        this.text = (TextView) row.findViewById(android.R.id.text1);
        mRow = row;
    }

    public int getHeaderPosition() {
        return mHeaderPosition;
    }

    public void setHeaderPosition(int headerPosition) {
        mHeaderPosition = headerPosition;
    }

    void bind(SearchedItemInfo itemInfo) {
        Resources resources = mRow.getResources();
        int iconResourceId = itemInfo.getIsDir() ? R.string.ic_folder : R.string.ic_drive_file;
        icon.setIconText(resources.getString(iconResourceId));
        text.setText(itemInfo.getText());
    }

    void bind(SearchedRootInfo rootInfo, int titleIdResource) {
        CharSequence title;
        Resources resources = mRow.getResources();
        int expandedIconId = rootInfo.isExpanded() ? R.string.ic_keyboard_arrow_down :
                R.string.ic_keyboard_arrow_right;
        expandedIcon.setIconText(resources.getString(expandedIconId));
        int visibility = rootInfo.size() == 0 ? View.INVISIBLE : View.VISIBLE;
        expandedIcon.setVisibility(visibility);
        title = String.format(resources.getString(R.string.header_format),
                resources.getString(titleIdResource), rootInfo.size());
        text.setText(title);
    }
}
