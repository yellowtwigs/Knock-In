package com.example.firsttestknocker;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Contact {
    private int Id;
    private String contactFirstName;
    private String contactLastName;
    private String contactPhoneNumber;
    private int contactImage;
    private int contactBackgroundImage;

    public Contact(String contactFirstName, String contactLastName, String contactPhoneNumber, int contactImage, int contactBackgroundImage)
    {
        this.contactFirstName = contactFirstName;
        this.contactLastName = contactLastName;
        this.contactPhoneNumber = contactPhoneNumber;
        this.contactImage = contactImage;
        this.contactBackgroundImage = contactBackgroundImage;
    }

    public String getContactFirstName()
    {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) { this.contactFirstName = contactFirstName; }

    public String getContactLastName()
    {
        return contactLastName;
    }

    public void setContactLastName(String contactName) { this.contactLastName = contactLastName; }

    public int getContactImage() { return contactImage; }

    public void setContactImage(int contactImage) { this.contactImage = contactImage; }

    public String getContactPhoneNumber() {return contactPhoneNumber; }

    public List<Contact> getContactList(List<Contact> listContact) { return listContact; }

    public int getContactBackgroundImage() { return contactBackgroundImage; }

    public void setContactBackgroundImage(int contactBackgroundImage) { this.contactBackgroundImage = contactBackgroundImage; }
}
