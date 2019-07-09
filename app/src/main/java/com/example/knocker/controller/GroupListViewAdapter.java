package com.example.knocker.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.example.knocker.R;
import com.example.knocker.model.ContactList;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.example.knocker.model.ModelDB.GroupWithContact;

import java.util.ArrayList;
import java.util.List;

public class GroupListViewAdapter extends BaseAdapter {
    ArrayList<GroupWithContact> group;
    Context context;
    Integer len;

    public GroupListViewAdapter(ArrayList<GroupWithContact> group, Context context,Integer len){
        this.group=group;
        this.context=context;
        this.len=len;
    }
    @Override
    public int getCount() {

        System.out.println("count"+ group.size());
        return group.size();
    }

    @Override
    public GroupWithContact getItem(int position) {
        return group.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listView= convertView;
        LayoutInflater layoutInflater=LayoutInflater.from(context);
        if(listView==null){
            listView= layoutInflater.inflate(R.layout.group_listview_adapter,null);
        }
        GroupWithContact group =getItem(position);
        ArrayList<ContactWithAllInformation> contacts=  group.getListContact(context);
        ContactList contactList= new ContactList(context);
        ArrayList<ContactWithAllInformation> tmp=contactList.getContacts();
        tmp.retainAll(contacts);
        contactList.setContacts(tmp);

        ViewHolder holder = new ViewHolder();
        holder.groupGrid=listView.findViewById(R.id.grid_group);
        ContactGridViewAdapter adapter= new ContactGridViewAdapter(context,contactList,len);
        return listView;
    }

    static class ViewHolder {
        GridView groupGrid;
    }
}
