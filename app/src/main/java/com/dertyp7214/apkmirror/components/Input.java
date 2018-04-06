/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dertyp7214.apkmirror.R;

public class Input extends LinearLayout {

    private TextInputEditText inputEditText;
    private RelativeLayout background;
    private SubmitListener listener;
    private Context context;
    private Button search;

    public Input(Context context) {
        this(context, null);
    }

    public Input(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Input(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Input(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeViews(context);
    }

    private void initializeViews(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.input, this);
        this.context=context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        inputEditText = this.findViewById(R.id.input_querry);
        search = this.findViewById(R.id.btn_send);
        background = this.findViewById(R.id.background);

        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null)
                    listener.onSubmit(inputEditText.getText().toString());
            }
        });

        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(listener!=null)
                        listener.onSubmit(v.getText().toString());
                }
                inputEditText.clearFocus();
                return true;
            }
        });
    }

    public void setBackgroundColor(int color){
        background.setBackgroundColor(color);
    }

    public void setText(String text){
        inputEditText.setText(text);
    }

    public void setTextColor(int color){
        inputEditText.setTextColor(color);
        inputEditText.setHintTextColor(color);
    }

    public void setImage(Drawable drawable){
        search.setBackground(drawable);
    }

    public void setImageTint(int color){
        search.setBackgroundTintList(new ColorStateList(new int[][]{}, new int[]{color}));
    }

    public void setSubmitListener(SubmitListener listener){
        this.listener=listener;
    }

    public interface SubmitListener{
        void onSubmit(String text);
    }
}
