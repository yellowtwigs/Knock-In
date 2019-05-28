package com.example.knocker.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.knocker.R;
import com.example.knocker.model.EditTextModel;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

/**
 * La Classe qui permet de générer la list des différents champs disponible lors de l'edition d'un contact
 * @author Kenzy Suon
 */
public class CustomAdapterEditText extends BaseAdapter {

    private Context context;
    private static ArrayList<EditTextModel> edit_model_ArrayList;
    private LayoutInflater inflator = null;

    public CustomAdapterEditText(Context context, ArrayList<EditTextModel> edit_model_ArrayList) {
        super();
        this.context = context;
        this.edit_model_ArrayList = edit_model_ArrayList;
        inflator = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return edit_model_ArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return edit_model_ArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_fields_added_layout, null, true);

            holder.textInputLayout = convertView.findViewById(R.id.list_item_field_added_edit_text);
            holder.deleteField = convertView.findViewById(R.id.delete_field);

            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder) convertView.getTag();
        }

        Objects.requireNonNull(holder.textInputLayout.getEditText()).setText(edit_model_ArrayList.get(position).getEditTextValue());
        holder.textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                edit_model_ArrayList.get(position).setEditTextValue(holder.textInputLayout.getEditText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return convertView;
    }

    private class ViewHolder {

        TextInputLayout textInputLayout;
        ImageView deleteField;
    }
}
