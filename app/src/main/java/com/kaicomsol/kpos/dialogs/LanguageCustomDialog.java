package com.kaicomsol.kpos.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.LanguageSelectListener;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;


public class LanguageCustomDialog {

    private LanguageSelectListener listener;
    public void showDialog(Activity activity, final LanguageSelectListener listener){
        this.listener = listener;
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_language_choose);
        //component initialization
        ImageView img_close = (ImageView) dialog.findViewById(R.id.img_close);
        ImageView img_english = (ImageView) dialog.findViewById(R.id.img_english);
        ImageView img_bangla = (ImageView) dialog.findViewById(R.id.img_bangla);
        ImageView img_japanese = (ImageView) dialog.findViewById(R.id.img_japanese);

        RelativeLayout layout_english = (RelativeLayout) dialog.findViewById(R.id.layout_english);
        RelativeLayout layout_bangla = (RelativeLayout) dialog.findViewById(R.id.layout_bangla);
        RelativeLayout layout_japanese = (RelativeLayout) dialog.findViewById(R.id.layout_japanese);

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        layout_english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLanguageSelect("en");
                dialog.dismiss();
            }
        });

        layout_bangla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLanguageSelect("bn");
                dialog.dismiss();
            }
        });

        layout_japanese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLanguageSelect("ja");
                dialog.dismiss();
            }
        });

        String lang = SharedDataSaveLoad.load(activity,activity.getString(R.string.preference_language_key));
        switch (lang){
            case "en":
                img_english.setVisibility(View.VISIBLE);
                img_bangla.setVisibility(View.GONE);
                img_japanese.setVisibility(View.GONE);
                break;
            case "bn":
                img_english.setVisibility(View.GONE);
                img_bangla.setVisibility(View.VISIBLE);
                img_japanese.setVisibility(View.GONE);
                break;
            case "ja":
                img_english.setVisibility(View.GONE);
                img_bangla.setVisibility(View.GONE);
                img_japanese.setVisibility(View.VISIBLE);
                break;
            default:
                img_english.setVisibility(View.VISIBLE);
                img_bangla.setVisibility(View.GONE);
                img_japanese.setVisibility(View.GONE);
                break;
        }

        dialog.show();

    }


}
