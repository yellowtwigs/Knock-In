package com.example.firsttestknocker;

import android.graphics.Bitmap;

public class Contact {
    private String contactName;
    private String contactPhoneNumber;
    private int contactImage;

    public Contact(String contactName, String contactPhoneNumber, int contactImage)
    {
        this.contactName = contactName;
        this.contactPhoneNumber = contactPhoneNumber;
        this.contactImage = contactImage;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    public int getContactImage() {
        return contactImage;
    }

    public void setContactImage(int contactImage) {
        this.contactImage = contactImage;
    }

    public String getContactPhoneNumber() {return contactPhoneNumber;
    }
}
