package bdonotifier.studiau.com.bdonotifier;

import android.content.Context;
import android.os.Build;
import android.widget.TextView;

/**
 * Created by Daniel Au on 5/2/2016.
 */
public class MyTextView extends TextView {

    public MyTextView(Context context, int defStyleRes) {
        super(context);

        if (Build.VERSION.SDK_INT < 23) {
            super.setTextAppearance(context, defStyleRes);
        } else {
            super.setTextAppearance(defStyleRes);
        }
    }

}
