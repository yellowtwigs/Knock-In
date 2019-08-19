package com.example.knocker.controller.activity.group;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.knocker.R;
import com.example.knocker.controller.CircularImageView;
import com.example.knocker.controller.activity.EditContactActivity;
import com.example.knocker.model.ContactGesture;
import com.example.knocker.model.ContactManager;
import com.example.knocker.model.DbWorkerThread;
import com.example.knocker.model.ModelDB.ContactDB;
import com.example.knocker.model.ModelDB.ContactWithAllInformation;
import com.example.knocker.model.ModelDB.GroupDB;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> implements FloatingActionMenu.MenuStateChangeListener {
    private final Context context;
    private final ContactManager contactManager;
    private final Integer len;
    private FloatingActionMenu selectMenu;
    private ArrayList<FloatingActionMenu> listCircularMenu = new ArrayList<FloatingActionMenu>();
    private String numberForPermission = "";
    private Boolean modeMultiSelect = false;
    private Boolean secondClick = false;
    private ArrayList<ContactWithAllInformation> listOfItemSelected = new ArrayList<>();
    private GridView parentGrid;
    private ArrayList<Integer> sectionPos;
    private int heightWidthImage;


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    public GroupAdapter(Context context, ContactManager contactManager, Integer len)  {
        this.context = context;
        this.contactManager = contactManager;
        this.len = len;
        this.sectionPos = new ArrayList<Integer>();
    }

    public ArrayList<ContactWithAllInformation> getListOfItemSelected() {
        return listOfItemSelected;
    }

    @NotNull
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.grid_contact_item_layout, parent, false);
        System.out.println(parent.getClass());
        //parentGrid=((GridView) parent);
        ViewHolder holder = new ViewHolder(view);
        heightWidthImage = holder.contactRoundedImageView.getLayoutParams().height;
        return holder;
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull final GroupAdapter.ViewHolder holder, final int position) {

        //SharedPreferences sharedPreferences = context.getSharedPreferences("Gridview_column", Context.MODE_PRIVATE);

        //int len = sharedPreferences.getInt("gridview", 4);
        int height = heightWidthImage;
        int width = heightWidthImage;
        System.out.println(" layout params height " + height + " width " + width);
        ConstraintLayout.LayoutParams layoutParamsTV = (ConstraintLayout.LayoutParams) holder.contactFirstNameView.getLayoutParams();
        ConstraintLayout.LayoutParams layoutParamsIV = (ConstraintLayout.LayoutParams) holder.contactRoundedImageView.getLayoutParams();
        if (len == 3) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.05));
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.05));
            layoutParamsTV.topMargin = 30;
            layoutParamsIV.topMargin = 10;
        } else if (len == 4) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.25));
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.25));
            layoutParamsTV.topMargin = 10;
            layoutParamsIV.topMargin = 10;
        } else if (len == 5) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.40));
            ;
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.40));
            ;
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        } else if (len == 6) {
            holder.contactRoundedImageView.getLayoutParams().height = (int) (heightWidthImage - (heightWidthImage * 0.50));
            ;
            holder.contactRoundedImageView.getLayoutParams().width = (int) (heightWidthImage - (heightWidthImage * 0.50));
            ;
            layoutParamsTV.topMargin = 0;
            layoutParamsIV.topMargin = 0;
        }
        ;

        final ContactDB contact = this.contactManager.getContactList().get(position).getContactDB();
        assert contact != null;
        if (contact.getContactPriority() == 0) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityZeroColor));
        } else if (contact.getContactPriority() == 1) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityOneColor));
        } else if (contact.getContactPriority() == 2) {
            holder.contactRoundedImageView.setBorderColor(context.getResources().getColor(R.color.priorityTwoColor));
        }
        if(modeMultiSelect && listOfItemSelected.contains(contactManager.getContactList().get(position))){
            holder.contactRoundedImageView.setImageResource(R.drawable.ic_contact_selected);
        } else {
            if (!contact.getProfilePicture64().equals("")) {
                Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
                holder.contactRoundedImageView.setImageBitmap(bitmap);
            } else {
                holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture())); //////////////
            }
        }
        //region set libbellé group
        // ContactsRoomDatabase main_ContactsDatabase=ContactsRoomDatabase.Companion.getDatabase(context);
        //DbWorkerThread main_mDbWorkerThread=new DbWorkerThread("dbWorkerThread");
        //main_mDbWorkerThread.start() ;
        //List<GroupDB> listDB=main_ContactsDatabase.GroupsDao().getGroupForContact(contact.getId());
        //endregion
        // getItem(position).getFirstGroup(context);
        String firstname = contact.getFirstName();
        String lastName = contact.getLastName();
        String group = "";
        GroupDB firstGroup = getItem(position).getFirstGroup(context);
        if (context instanceof GroupActivity) {
            holder.groupWordingConstraint.setVisibility(View.VISIBLE);

        }
        if (firstGroup == null) {
            System.out.println("no group " + contact.getFirstName() + " " + contact.getLastName());
            SharedPreferences sharedThemePreferences = context.getSharedPreferences("Knocker_Theme", Context.MODE_PRIVATE);
            Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            assert roundedLayout != null;
            if (sharedThemePreferences.getBoolean("darkTheme", false)) {
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColorDark), PorterDuff.Mode.MULTIPLY);
                holder.groupWordingConstraint.setBackground(roundedLayout);
            } else {
                roundedLayout.setColorFilter(context.getResources().getColor(R.color.backgroundColor), PorterDuff.Mode.MULTIPLY);
                holder.groupWordingConstraint.setBackground(roundedLayout);
            }

        } else {
            System.out.println("have group");
            group = firstGroup.getName();
            Drawable roundedLayout = context.getDrawable(R.drawable.rounded_rectangle_group);
            assert roundedLayout != null;
//            roundedLayout.setColorFilter(firstGroup.randomColorGroup(this.context), PorterDuff.Mode.MULTIPLY);
            roundedLayout.setColorFilter(firstGroup.getSection_color(), PorterDuff.Mode.MULTIPLY);
            holder.groupWordingConstraint.setBackground(roundedLayout);
        }

        if (len == 3) {
            Spannable spanFistName = new SpannableString(firstname);
            spanFistName.setSpan(new RelativeSizeSpan(0.95f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);
            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.95f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);
            //holder.contactFirstNameView.;
            if (group.length() > 12)
                group = group.substring(0, 11).concat("..");
            Spannable spanGroup = new SpannableString(group);
            spanGroup.setSpan(new RelativeSizeSpan(0.95f), 0, group.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.groupWordingTv.setText(spanGroup);

        }
        if (len == 4) {
            if (contact.getFirstName().length() > 12)
                firstname = contact.getFirstName().substring(0, 10).concat("..");

            Spannable spanFistName = new SpannableString(firstname);
            spanFistName.setSpan(new RelativeSizeSpan(0.95f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(spanFistName);
            if (contact.getLastName().length() > 12)
                lastName = contact.getLastName().substring(0, 10).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.95f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);

            if (group.length() > 9)
                group = group.substring(0, 8).concat("..");
            Spannable spanGroup = new SpannableString(group);
            spanLastName.setSpan(new RelativeSizeSpan(0.95f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.groupWordingTv.setText(spanGroup);

        }
        if (len == 5) {
            if (contact.getFirstName().length() > 11)
                firstname = contact.getFirstName().substring(0, 9).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.9f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            //holder.contactFirstNameView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            if (contact.getLastName().length() > 11)
                lastName = contact.getLastName().substring(0, 9).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.9f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);

            if (group.length() > 6)
                group = group.substring(0, 5).concat("..");
            Spannable spanGroup = new SpannableString(group);
            spanLastName.setSpan(new RelativeSizeSpan(0.9f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.groupWordingTv.setText(spanGroup);
        }
        if (len == 6) {
            if (contact.getFirstName().length() > 8)
                firstname = contact.getFirstName().substring(0, 7).concat("..");

            holder.contactFirstNameView.setText(firstname);
            Spannable span = new SpannableString(holder.contactFirstNameView.getText());
            span.setSpan(new RelativeSizeSpan(0.81f), 0, firstname.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactFirstNameView.setText(span);
            if (contact.getLastName().length() > 8)
                lastName = contact.getLastName().substring(0, 7).concat("..");

            Spannable spanLastName = new SpannableString(lastName);
            spanLastName.setSpan(new RelativeSizeSpan(0.81f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.contactLastNameView.setText(spanLastName);

            if (group.length() > 6)
                group = group.substring(0, 5).concat("..");
            Spannable spanGroup = new SpannableString(group);
            spanLastName.setSpan(new RelativeSizeSpan(0.81f), 0, lastName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.groupWordingTv.setText(spanGroup);
        }
     /*   if (!contact.getProfilePicture64().equals("")) {
            Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
            holder.contactRoundedImageView.setImageBitmap(bitmap);
        } else {
            holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture())); //////////////
        }*/
        //region circular menu

        //final ImageView buttonMessenger = new ImageView(context);
        final ImageView buttonCall = new ImageView(context);
        final ImageView buttonWhatsApp = new ImageView(context);
        final ImageView buttonSMS = new ImageView(context);
        final ImageView buttonEdit = new ImageView(context);
        final ImageView buttonMail = new ImageView(context);

        //  buttonMessenger.setId(0);
        buttonCall.setId(1);
        buttonSMS.setId(2);
        buttonWhatsApp.setId(3);
        buttonEdit.setId(4);
        buttonMail.setId(5);

        //buttonMessenger.setImageDrawable(iconMessenger);

        buttonCall.setImageResource(R.drawable.ic_google_call);
        buttonWhatsApp.setImageResource(R.drawable.ic_circular_whatsapp);
        buttonSMS.setImageResource(R.drawable.ic_sms_selector);
        buttonEdit.setImageResource(R.drawable.ic_circular_edit);
        buttonMail.setImageResource(R.drawable.ic_circular_gmail);

        SubActionButton.Builder builderIcon = new SubActionButton.Builder((Activity) context);
        builderIcon.setBackgroundDrawable(context.getDrawable(R.drawable.ic_circular));
        builderIcon.setContentView(buttonCall);

        int startAngle;
        int endAngle;
        if ((position - getSectionnedPosition(position)) % len == 0) {
            System.out.println("position vaut " + position + " modulo" + len + " vaut" + position % len);
            startAngle = 90;
            endAngle = -90;
        } else if ((position - getSectionnedPosition(position)) % len == len - 1) {
            System.out.println("position vaut " + position + " modulo" + len + " vaut" + position % len);
            startAngle = 90;
            endAngle = 270;
        } else {
            System.out.println("position vaut " + position + " modulo" + len + " vaut" + position % len);
            startAngle = 0;
            endAngle = -180;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int diametreBoutton = (int) (0.35 * metrics.densityDpi);
        int radiusMenu = (int) (0.45 * metrics.densityDpi);
        int border = (int) (0.0625 * metrics.densityDpi);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(5, 5, 5, 5);

        FloatingActionMenu.Builder builder = new FloatingActionMenu.Builder((Activity) context)
                .setStartAngle(startAngle)
                .setEndAngle(endAngle)
                .setRadius(radiusMenu)
                .addSubActionView(builderIcon.setContentView(buttonEdit, layoutParams).build(), diametreBoutton, diametreBoutton)
                .attachTo(holder.contactRoundedImageView)
                .setStateChangeListener(this)
                .disableAnimations();
        if (appIsInstalled() && !getItem(position).getFirstPhoneNumber().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonWhatsApp, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        if (!getItem(position).getFirstMail().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonMail, layoutParams).build(), diametreBoutton, diametreBoutton);
        }
        if (!getItem(position).getFirstPhoneNumber().equals("")) {
            builder.addSubActionView(builderIcon.setContentView(buttonSMS, layoutParams).build(), diametreBoutton, diametreBoutton)
                    .addSubActionView(builderIcon.setContentView(buttonCall, layoutParams).build(), diametreBoutton, diametreBoutton);
        }

       /* if( appIsInstalled( "com.facebook.orca")){
            builder.addSubActionView(builderIcon.setContentView(buttonMessenger,layoutParams).build(),diametreBoutton,diametreBoutton);
        }*/


        final FloatingActionMenu quickMenu = builder.build();
        listCircularMenu.add(quickMenu);
        //  quickMenu.addSubActionView(builderIcon.setContentView(buttonSMS,layoutParams).build(),diametreBoutton,diametreBoutton)
        View.OnClickListener buttonListener = v -> {

           /* if (v.getId() == buttonMessenger.getId()) {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + "")));
                } catch (ActivityNotFoundException e) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.messenger.com/t/" + "")));
                }
            } else*/
            if (v.getId() == buttonWhatsApp.getId()) {

                ContactWithAllInformation contactWithAllInformation = getItem(position);
                ContactGesture.INSTANCE.openWhatsapp(converter06To33(contactWithAllInformation.getFirstPhoneNumber()), context);
            } else if (v.getId() == buttonEdit.getId()) {

                Intent intent = new Intent(context, EditContactActivity.class);
                intent.putExtra("ContactId", contact.getId());

                if (context instanceof GroupActivity) {
                    intent.putExtra("fromGroupActivity", true);
                }

                context.startActivity(intent);
            } else if (v.getId() == buttonCall.getId()) {

                callPhone(getItem(position).getFirstPhoneNumber());

            } else if (v.getId() == buttonSMS.getId()) {

                String phone = getItem(position).getFirstPhoneNumber();
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", phone, null));
                context.startActivity(i);

            } else if (v.getId() == buttonMail.getId()) {

                String mail = getItem(position).getFirstMail();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                //intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                System.out.println("intent " + Objects.requireNonNull(intent.getExtras()).toString());
                context.startActivity(intent);
            }
            selectMenu.close(false);
        };

        View.OnLongClickListener gridlongClick = v -> {

                if (!modeMultiSelect) {
                    v.setTag(holder);
                   // ContactDB contact1 = contactManager.getContactList().get(position).getContactDB();
                    assert contact != null;

                holder.contactFirstNameView.setText(contact.getFirstName());

                    if (listOfItemSelected.contains(contactManager.getContactList().get(position))) {
                        listOfItemSelected.remove(contactManager.getContactList().get(position));

                    if (!contact.getProfilePicture64().equals("")) {
                        Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
                        holder.contactRoundedImageView.setImageBitmap(bitmap);
                    } else {
                        listOfItemSelected.add(contactManager.getContactList().get(position));
                        holder.contactRoundedImageView.setImageResource(R.drawable.ic_contact_selected);
                        notifyDataSetChanged();
                    }
                } else {
                    listOfItemSelected.add(contactManager.getContactList().get(position));
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_contact_selected);
                    notifyDataSetChanged();
                }
                closeMenu();
                ((GroupManagerActivity) context).gridLongItemClick(position);
                modeMultiSelect = true;
            }
            return true;
        };
        View.OnClickListener gridItemClick = v -> {
            if (modeMultiSelect) {
                if (listOfItemSelected.contains(contactManager.getContactList().get(position))) {
                    listOfItemSelected.remove(contactManager.getContactList().get(position));

                    if (!contact.getProfilePicture64().equals("")) {
                        Bitmap bitmap = base64ToBitmap(contact.getProfilePicture64());
                        holder.contactRoundedImageView.setImageBitmap(bitmap);
                    } else {
                        holder.contactRoundedImageView.setImageResource(randomDefaultImage(contact.getProfilePicture()));
                    }
                    if (listOfItemSelected.isEmpty()) {
                        modeMultiSelect = false;
                    }
                    notifyDataSetChanged();
                } else {
                    listOfItemSelected.add(contactManager.getContactList().get(position));
                    holder.contactRoundedImageView.setImageResource(R.drawable.ic_contact_selected);
                    notifyDataSetChanged();
                }
                ((GroupManagerActivity) context).gridLongItemClick(position);
            } else {
                if (quickMenu.isOpen()) {
                    quickMenu.close(false);
                } else {
                    quickMenu.open(false);
                }
            }
        };
        buttonCall.setOnLongClickListener(v -> {
            String phoneNumber = getItem(position).getSecondPhoneNumber(getItem(position).getFirstPhoneNumber());
            if (!phoneNumber.isEmpty()) {
                callPhone(phoneNumber);
            }
            return true;
        });

        holder.groupWordingConstraint.setOnClickListener(v -> {
            DbWorkerThread main_mDbWorkerThread = new DbWorkerThread("dbWorkerThread");
            main_mDbWorkerThread.start();
            ArrayList<Integer> listPosition = new ArrayList<>();

            if (!secondClick) {
                System.out.println("list contact grid size" + contactManager.getContactList().size());
                for (int i = 0; i < contactManager.getContactList().size(); i++) {
                    if (contactManager.getContactList().get(i).getFirstGroup(context) != null) {
                        if (Objects.equals(contactManager.getContactList().get(i).getFirstGroup(context).getId(), Objects.requireNonNull(contactManager.getContactList().get(position).getFirstGroup(context)).getId())) {
                            System.out.println("id egale a l'autre ");
                            listPosition.add(i);
                        } else {
                            System.out.println(contactManager.getContactList().get(i).getFirstGroup(context).getId() + " id different a " + contactManager.getContactList().get(position).getFirstGroup(context).getId());
                        }
                    }
                }
                ((GroupActivity) context).clickGroupGrid(len, listPosition, parentGrid.getFirstVisiblePosition(), secondClick, true);
            }
        });

        holder.gridContactItemLayout.setOnLongClickListener(gridlongClick);
        holder.contactRoundedImageView.setOnLongClickListener(gridlongClick);
        holder.gridContactItemLayout.setOnClickListener(gridItemClick);
        holder.contactRoundedImageView.setOnClickListener(gridItemClick);
        //buttonMessenger.setOnClickListener(buttonListener);
        buttonWhatsApp.setOnClickListener(buttonListener);
        buttonCall.setOnClickListener(buttonListener);
        buttonSMS.setOnClickListener(buttonListener);
        buttonEdit.setOnClickListener(buttonListener);
        buttonMail.setOnClickListener(buttonListener);

  /*      holder.gridContactItemLayout.setOnClickListener(v -> {
            if (quickMenu.isOpen()) {
                quickMenu.close(false);
            } else {
                quickMenu.open(false);
            }
        });
        holder.contactRoundedImageView.setOnClickListener(v -> {
            if (quickMenu.isOpen()) {
                quickMenu.close(false);
            } else {
                quickMenu.open(false);
            }
        });*/
    }

    public boolean multiSelectMode() {
        return modeMultiSelect;
    }

    @Override
    public void onMenuOpened(FloatingActionMenu floatingActionMenu) {
        System.out.println("menu select");
        if (selectMenu != null) {
            selectMenu.close(false);
        }
        if (multiSelectMode()) {
            floatingActionMenu.close(false);
        }
        selectMenu = floatingActionMenu;

    }

    @Override
    public void onMenuClosed(FloatingActionMenu floatingActionMenu) {
        System.out.println("menu close");
        selectMenu = null;
    }


    public void closeMenu() {

        if (selectMenu != null)
            selectMenu.close(true);

    }

    public void callPhone(final String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            int PERMISSION_CALL_RESULT = 1;
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_RESULT);
            numberForPermission = phoneNumber;
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences("Phone_call", Context.MODE_PRIVATE);
            Boolean popup = sharedPreferences.getBoolean("popup", true);
            if (popup && numberForPermission.isEmpty()) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.main_contact_grid_title)
                        .setMessage(R.string.main_contact_grid_message)
                        .setPositiveButton(android.R.string.yes, (dialog, id) -> context.startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))))
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            } else {
                context.startActivity(new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null)));
                numberForPermission = "";
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView contactFirstNameView;
        TextView contactLastNameView;
        CircularImageView contactRoundedImageView;
        ConstraintLayout gridContactItemLayout;
        ConstraintLayout groupWordingConstraint;
        TextView groupWordingTv;

        ViewHolder(View view) {
            super(view);
            contactRoundedImageView = view.findViewById(R.id.contactRoundedImageView);
            gridContactItemLayout = view.findViewById(R.id.grid_contact_item_layout);
            groupWordingConstraint = view.findViewById(R.id.grid_adapter_wording_group_constraint_layout);
            groupWordingTv = view.findViewById(R.id.grid_adapter_wording_group_tv);
            contactFirstNameView = view.findViewById(R.id.grid_adapter_contactFirstName);
            contactLastNameView = view.findViewById(R.id.grid_adapter_contactLastName);
            heightWidthImage = contactRoundedImageView.getHeight();
        }

/*        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }*/
    }

    public ContactWithAllInformation getItem(int position) {
        return contactManager.getContactList().get(position);
    }

    public void removeItem(int position) {
        contactManager.getContactList().remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return contactManager.getContactList().size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private String converter06To33(String phoneNumber) {
        if (phoneNumber.charAt(0) == '0') {
            return "+33" + phoneNumber;
        }
        return phoneNumber;
    }

    private int randomDefaultImage(int avatarId) {

        switch (avatarId) {
            case 0:
                return R.drawable.ic_user_purple;
            case 1:
                return R.drawable.ic_user_blue;
            case 2:
                return R.drawable.ic_user_knocker;
            case 3:
                return R.drawable.ic_user_green;
            case 4:
                return R.drawable.ic_user_om;
            case 5:
                return R.drawable.ic_user_orange;
            case 6:
                return R.drawable.ic_user_pink;
            default:
                return R.drawable.ic_user_blue;
        }
    }

    private Bitmap base64ToBitmap(String base64) {

        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 2;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    private boolean appIsInstalled() {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getApplicationInfo("com.whatsapp", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setSectionPos(ArrayList<Integer> position) {
        sectionPos = position;
    }

    private int getSectionnedPosition(int position) {
        for (int i = (sectionPos.size() - 1); i > 0; i--) {
            if (sectionPos.get(i) <= position) {
                return sectionPos.get(i);
            }
        }
        return 0;
    }

}