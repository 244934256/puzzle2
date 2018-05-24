package com.purity.puzzle.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.purity.puzzle.R;
import com.purity.puzzle.view.SuccessToast;

/**
 * Created by purity on 2016/10/2.
 */
public class ToastUtil {
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;

    public static final int SUCCESS = 1;


    static SuccessToast successToastView;


    public static void makeText(Context context, String msg, int length, int type) {

        Toast toast = new Toast(context);


        switch (type) {
            case 1: {
                View layout = LayoutInflater.from(context).inflate(R.layout.success_toast_layout, null, false);
                TextView text = (TextView) layout.findViewById(R.id.toastMessage);
                text.setText(msg);
                successToastView = (SuccessToast) layout.findViewById(R.id.successView);
                successToastView.startAnim();
                text.setBackgroundResource(R.drawable.success_toast);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                toast.setView(layout);
                break;
            }

        }
        toast.setDuration(length);
        toast.show();
    }

}
