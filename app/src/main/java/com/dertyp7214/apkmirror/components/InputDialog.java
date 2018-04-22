package com.dertyp7214.apkmirror.components;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.dertyp7214.apkmirror.R;

public class InputDialog {

    private Context context;
    private Listener listener;
    private AlertDialog.Builder builder;

    public InputDialog(String title, String text, String hint, Context context){
        this.context=context;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        final EditText input = new EditText(context);
        input.setHint(hint);
        input.setText(text);
        input.setMaxLines(1);
        builder.setView(input);

        builder.setPositiveButton(context.getString(R.string.popup_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onSubmit(input.getText().toString());
                dialog.cancel();
            }
        });
        builder.setNegativeButton(context.getString(R.string.popup_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    public void setListener(Listener listener){
        this.listener=listener;
    }

    public void show(){
        builder.show();
    }

    public interface Listener{
        void onSubmit(String text);
        void onCancel();
    }
}
