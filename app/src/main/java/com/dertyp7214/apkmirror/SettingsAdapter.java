/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.dertyp7214.apkmirror.settings.Setting;
import com.dertyp7214.apkmirror.settings.SettingCheckBox;
import com.dertyp7214.apkmirror.settings.SettingColor;
import com.dertyp7214.apkmirror.settings.SettingSwitch;

import org.w3c.dom.Text;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Setting> itemList;
    private Activity context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, subTitle;
        public View box;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            subTitle = view.findViewById(R.id.subTitle);
            box = view.findViewById(R.id.box);
        }
    }

    public class ViewHolderCheckBox extends ViewHolder {
        public CheckBox title;

        public ViewHolderCheckBox(View view) {
            super(view);
            title = view.findViewById(R.id.text);
        }
    }

    public class ViewHolderSwitch extends ViewHolder {
        public Switch title;

        public ViewHolderSwitch(View view) {
            super(view);
            title = view.findViewById(R.id.text);
        }
    }

    public class ViewHolderColor extends ViewHolder {
        public TextView title;
        public View box, colorView;

        public ViewHolderColor(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
            colorView = view.findViewById(R.id.colorViewPlate);
        }
    }

    public SettingsAdapter(List<Setting> itemList, Activity context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_normal, parent, false));
            case 1:
                return new ViewHolderCheckBox(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_checkbox, parent, false));
            case 2:
                return new ViewHolderSwitch(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_togglebutton, parent, false));
            case 3:
                return new ViewHolderColor(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_color, parent, false));
            default:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_normal, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 0:
                final ViewHolder viewHolder = (ViewHolder) holder;
                final Setting setting = itemList.get(position);
                if(setting!=null) {
                    viewHolder.title.setText(setting.getText());
                    viewHolder.subTitle.setText(setting.getSubTitle());
                    viewHolder.box.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setting.onClick();
                        }
                    });
                }
                break;
            case 1:
                final ViewHolderCheckBox viewHolderCheckBox = (ViewHolderCheckBox) holder;
                final SettingCheckBox settingCheckBox = (SettingCheckBox) itemList.get(position);
                CheckBox checkBox = viewHolderCheckBox.title;
                checkBox.setText(settingCheckBox.getText());
                checkBox.setChecked(settingCheckBox.isChecked());
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settingCheckBox.setChecked(isChecked);
                    }
                });
                break;
            case 2:
                final ViewHolderSwitch viewHolderSwitch = (ViewHolderSwitch) holder;
                final SettingSwitch settingSwitch = (SettingSwitch) itemList.get(position);
                Switch aSwitch = viewHolderSwitch.title;
                aSwitch.setText(settingSwitch.getText());
                aSwitch.setChecked(settingSwitch.isChecked());
                aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settingSwitch.setChecked(isChecked);
                    }
                });
                break;
            case 3:
                final ViewHolderColor viewHolderColor = (ViewHolderColor) holder;
                final SettingColor settingColor = (SettingColor) itemList.get(position);
                viewHolderColor.title.setText(settingColor.getText());
                LayerDrawable bgDrawable = (LayerDrawable) viewHolderColor.colorView.getBackground();
                final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.plate_color);
                shape.setColor(settingColor.getColorInt());
                viewHolderColor.box.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingColor.onClick(viewHolderColor.colorView);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return itemList.get(position) instanceof SettingCheckBox?1:itemList.get(position) instanceof SettingSwitch?2:itemList.get(position) instanceof SettingColor?3:0;
    }

    public void saveSettings(){
        for(Setting setting : itemList){
            setting.saveSetting();
        }
    }
}