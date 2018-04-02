/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.settings;

        import android.animation.ValueAnimator;
        import android.animation.ValueAnimator.AnimatorUpdateListener;
        import android.app.Dialog;
        import android.content.Context;
        import android.graphics.drawable.GradientDrawable;
        import android.graphics.drawable.LayerDrawable;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.RelativeLayout;
        import android.widget.SeekBar;
        import android.widget.TextView;
        import android.widget.SeekBar.OnSeekBarChangeListener;
        import com.hacker.module.colorpicker.Color.Color;
        import com.hacker.module.colorpicker.Color.ColorSeekBar;
        import com.hacker.module.colorpicker.ColorList;
        import com.hacker.module.colorpicker.ColorLists.ColorLists;
        import com.hacker.module.colorpicker.R.id;
        import com.hacker.module.colorpicker.R.layout;
        import java.util.ArrayList;
        import java.util.List;

public class ColorPicker extends Dialog {
    private TextView redTxt;
    private TextView greenTxt;
    private TextView blueTxt;
    private TextView hashTag;
    private ColorSeekBar redBar;
    private ColorSeekBar greenBar;
    private ColorSeekBar blueBar;
    private float red;
    private float green;
    private float blue;
    private View colorView;
    private Context c;
    private GradientDrawable shape;
    private EditText hexCode;
    private ColorPicker.Listener listener;
    private boolean drag = true;
    private ValueAnimator anim;
    private int animTime;
    private Button btn_ok;
    private Button btn_cancel;
    private Button btn_back;
    private List<ColorList> colorList;
    private ColorPicker.Theme theme;

    public ColorPicker(Context context) {
        super(context);
        this.c = context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.setContentView(layout.color_picker);
        this.hexCode = (EditText) this.findViewById(id.hexTxt);
        this.redTxt = (TextView) this.findViewById(id.txtRed);
        this.greenTxt = (TextView) this.findViewById(id.txtGreen);
        this.blueTxt = (TextView) this.findViewById(id.txtBlue);
        this.hashTag = (TextView) this.findViewById(id.textView);
        this.redBar = (ColorSeekBar) this.findViewById(id.red);
        this.greenBar = (ColorSeekBar) this.findViewById(id.green);
        this.blueBar = (ColorSeekBar) this.findViewById(id.blue);
        this.colorView = this.findViewById(id.colorView);
        LayerDrawable bgDrawable = (LayerDrawable) this.colorView.getBackground();
        this.shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(id.color_plate);
        this.redBar.setBarColor(Color.MATERIAL_RED);
        this.redBar.setThumbColor(Color.MATERIAL_RED);
        this.redBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ColorPicker.this.red = (float) i;
                ColorPicker.this.setAllColors(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue);
                ColorPicker.this.listener.updateColor(ColorPicker.this.getIntFromColor(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue));
                if (!ColorPicker.this.anim.isRunning()) {
                    ColorPicker.this.setHex(ColorPicker.this.getIntFromColor(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue));
                }

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.greenBar.setBarColor(Color.MATERIAL_GREEN);
        this.greenBar.setThumbColor(Color.MATERIAL_GREEN);
        this.greenBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ColorPicker.this.green = (float) i;
                ColorPicker.this.setAllColors(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue);
                ColorPicker.this.listener.updateColor(ColorPicker.this.getIntFromColor(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue));
                if (!ColorPicker.this.anim.isRunning()) {
                    ColorPicker.this.setHex(ColorPicker.this.getIntFromColor(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue));
                }

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.blueBar.setBarColor(Color.MATERIAL_BLUE);
        this.blueBar.setThumbColor(Color.MATERIAL_BLUE);
        this.blueBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                ColorPicker.this.blue = (float) i;
                ColorPicker.this.setAllColors(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue);
                ColorPicker.this.listener.updateColor(ColorPicker.this.getIntFromColor(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue));
                if (!ColorPicker.this.anim.isRunning()) {
                    ColorPicker.this.setHex(ColorPicker.this.getIntFromColor(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue));
                }

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.Setup();
        this.btn_ok = (Button) this.findViewById(id.btn_ok);
        this.btn_cancel = (Button) this.findViewById(id.btn_cancel);
        this.btn_back = (Button) this.findViewById(id.btn_back);
        this.btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ColorPicker.this.actionOK();
            }
        });
        this.btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ColorPicker.this.actionCancel();
            }
        });
        if (this.colorList == null) {
            this.btn_back.setVisibility(View.INVISIBLE);
        }

        this.btn_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ColorPicker colorPicker = ColorPicker.this;
                ColorPresets colorPresets = new ColorPresets(c, colorList, ColorPicker.this);
                colorPicker.hide();
                colorPresets.show();
                colorPresets.setTheme(colorPicker.theme);
            }
        });
        this.hexCode.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                if (ColorPicker.this.hexCode.getText().length() == 6) {
                    int color = Color.parseColor("#" + ColorPicker.this.hexCode.getText().toString());
                    ColorPicker.this.setAllColors(Color.red(color), Color.green(color), Color.blue(color), true);
                    ColorPicker.this.listener.updateColor(ColorPicker.this.getIntFromColor(ColorPicker.this.red, ColorPicker.this.green, ColorPicker.this.blue));
                    ColorPicker.this.redBar.setProgress(Color.red(color));
                    ColorPicker.this.greenBar.setProgress(Color.green(color));
                    ColorPicker.this.blueBar.setProgress(Color.blue(color));
                }

            }
        });
    }

    public void actionOK() {
        this.listener.color(this.getIntFromColor(this.red, this.green, this.blue));
    }

    public void actionCancel() {
        this.listener.cancel();
    }

    public void setColorLists(ColorList colorList1, ColorList colorList2, ColorList colorList3, ColorList colorList4, ColorList colorList5, ColorList colorList6, ColorList colorList7, ColorList colorList8) {
        this.colorList = new ArrayList();
        this.colorList.add(colorList1);
        this.colorList.add(colorList2);
        this.colorList.add(colorList3);
        this.colorList.add(colorList4);
        this.colorList.add(colorList5);
        this.colorList.add(colorList6);
        this.colorList.add(colorList7);
        this.colorList.add(colorList8);
    }

    public void setColorLists(ColorLists colorLists1, ColorLists colorLists2, ColorLists colorLists3, ColorLists colorLists4, ColorLists colorLists5, ColorLists colorLists6, ColorLists colorLists7, ColorLists colorLists8) {
        this.colorList = new ArrayList();
        this.colorList.add(colorLists1.getList());
        this.colorList.add(colorLists2.getList());
        this.colorList.add(colorLists3.getList());
        this.colorList.add(colorLists4.getList());
        this.colorList.add(colorLists5.getList());
        this.colorList.add(colorLists6.getList());
        this.colorList.add(colorLists7.getList());
        this.colorList.add(colorLists8.getList());
    }

    public void setTheme(ColorPicker.Theme theme) {
        RelativeLayout ly = (RelativeLayout) this.findViewById(id.ly);
        this.theme = theme;
        switch (theme) {
            case LIGHT:
                ly.setBackgroundColor(Color.MATERIAL_LIGHT);
                this.btn_ok.setTextColor(Color.MATERIAL_DARK);
                this.btn_cancel.setTextColor(Color.MATERIAL_DARK);
                this.btn_back.setTextColor(Color.MATERIAL_DARK);
                this.hexCode.setTextColor(Color.MATERIAL_DARK);
                this.redTxt.setTextColor(Color.MATERIAL_DARK);
                this.greenTxt.setTextColor(Color.MATERIAL_DARK);
                this.blueTxt.setTextColor(Color.MATERIAL_DARK);
                this.hashTag.setTextColor(Color.MATERIAL_DARK);
                break;
            case DARK:
                ly.setBackgroundColor(Color.MATERIAL_DARK);
                this.btn_ok.setTextColor(Color.MATERIAL_LIGHT);
                this.btn_cancel.setTextColor(Color.MATERIAL_LIGHT);
                this.btn_back.setTextColor(Color.MATERIAL_LIGHT);
                this.hexCode.setTextColor(Color.MATERIAL_LIGHT);
                this.redTxt.setTextColor(Color.MATERIAL_LIGHT);
                this.greenTxt.setTextColor(Color.MATERIAL_LIGHT);
                this.blueTxt.setTextColor(Color.MATERIAL_LIGHT);
                this.hashTag.setTextColor(Color.MATERIAL_LIGHT);
        }

    }

    public void setListener(ColorPicker.Listener listener) {
        this.listener = listener;
    }

    private void Setup() {
        int color = -7829368;
        this.setAllColors(Color.red(color), Color.green(color), Color.blue(color), false);
    }

    public void setColor(int color) {
        this.intColor(color);
    }

    public void setColor(String color) {
        this.stringColor(color);
    }

    public void setAnimationTime(int time) {
        this.animTime = time;
    }

    private void stringColor(String color) {
        int tmp = Color.parseColor(color);
        this.setAllColors(Color.red(tmp), Color.green(tmp), Color.blue(tmp), false);
    }

    private void intColor(int color) {
        this.setAllColors(Color.red(color), Color.green(color), Color.blue(color), false);
    }

    private void setAllColors(float r, float g, float b) {
        this.setAllColors((int) r, (int) g, (int) b, true);
    }

    private void setAllColors(int r, int g, int b, boolean self) {
        int color = this.getIntFromColor((float) r, (float) g, (float) b);
        int rc = Color.red(color);
        int gc = Color.green(color);
        int bc = Color.blue(color);
        this.red = (float) rc;
        this.green = (float) gc;
        this.blue = (float) bc;
        if (!self) {
            this.animateSeek(this.redBar, 0, rc, this.animTime);
            this.animateSeek(this.greenBar, 0, gc, this.animTime);
            this.animateSeek(this.blueBar, 0, bc, this.animTime);
            this.setHex(color);
        }

        this.redTxt.setText(String.valueOf(rc));
        this.greenTxt.setText(String.valueOf(gc));
        this.blueTxt.setText(String.valueOf(bc));
        this.shape.setColor(color);
    }

    private void animateSeek(final SeekBar seekBar, int from, int toVal, int time) {
        this.anim = ValueAnimator.ofInt(new int[]{from, toVal});
        this.anim.setDuration((long) time);
        this.anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int animProgress = (Integer) animation.getAnimatedValue();
                seekBar.setProgress(animProgress);
            }
        });
        this.anim.start();
    }

    private void setHex(int color) {
        String hex = String.format("#%06X", 16777215 & color);
        this.hexCode.setText(hex.replace("#", ""));
    }

    private int getIntFromColor(float Red, float Green, float Blue) {
        int R = Math.round(255.0F * (256.0F - Red));
        int G = Math.round(255.0F * (256.0F - Green));
        int B = Math.round(255.0F * (256.0F - Blue));
        R = R << 16 & 16711680;
        G = G << 8 & '\uff00';
        B &= 255;
        return -16777216 | R | G | B;
    }

    public int getColorInt() {
        return this.getIntFromColor(this.red, this.green, this.blue);
    }

    public String getColorString() {
        return String.format("#%06X", 16777215 & this.getIntFromColor(this.red, this.green, this.blue));
    }

    public interface Listener {
        void color(int var1);

        void updateColor(int var1);

        void cancel();
    }

    public static enum Theme {
        DARK,
        LIGHT;

        private Theme() {
        }
    }
}
