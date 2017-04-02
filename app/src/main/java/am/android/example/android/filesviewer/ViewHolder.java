package am.android.example.android.filesviewer;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by alexander on 12.02.17.
 */
class ViewHolder {
    private ImageView mIcon = null;
    private TextView mText = null;

    ViewHolder(View row) {
        this.mIcon = (ImageView) row.findViewById(R.id.icon);
        this.mText = (TextView) row.findViewById(android.R.id.text1);
    }

    TextView getText() {
        return mText;
    }

    ImageView getIcon() {
        return mIcon;
    }
}
