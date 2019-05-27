package com.example.voicebaseapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.example.voicebaseapp.R;

public class CustomProgressDialog {
    Activity activity;
    Dialog dialog;

    public CustomProgressDialog(Activity activity){
        this.activity = activity;
    }

    public void showDialog() {

        dialog  = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //...set cancelable false so that it's never get hidden
        dialog.setCancelable(false);
        //...that's the layout i told you will inflate later
        dialog.setContentView(R.layout.custom_loading_layout);

        //...initialize the imageView form inflated layout
        ImageView gifImageView = dialog.findViewById(R.id.custom_loading_imageView);

        /*
        it was never easy to load gif into an ImageView before Glide or Others library
        and for doing this we need DrawableImageViewTarget to that ImageView
        */
        DrawableImageViewTarget imageViewTarget = new DrawableImageViewTarget(gifImageView);

        //...now load that gif which we put inside the drawable folder here with the help of Glide

        Glide.with(activity)
                .load(R.drawable.voice1)
                .transition(new DrawableTransitionOptions().crossFade())
                .apply(new RequestOptions().placeholder(R.drawable.loading))
                .apply(new RequestOptions().centerCrop())
                .into(imageViewTarget);

        //...finally show it
        dialog.show();
    }

    //..also create a method which will hide the dialog when some work is done
    public void hideDialog(){
        dialog.dismiss();
    }
}
