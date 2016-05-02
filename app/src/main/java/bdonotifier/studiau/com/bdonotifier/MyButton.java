package bdonotifier.studiau.com.bdonotifier;

import android.content.Context;
import android.os.Build;
import android.widget.Button;

/**
 * Created by Daniel Au on 5/2/2016.
 */
public class MyButton extends Button {

    public MyButton(Context context, int defStyleRes) {
        super(context);

        if (Build.VERSION.SDK_INT < 23) {
            super.setTextAppearance(context, defStyleRes);
        } else {
            super.setTextAppearance(defStyleRes);
        }

    }

}
