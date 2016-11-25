package com.sourcecanyon.whatsClone.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class PreferenceManager {


    private static SharedPreferences mSharedPreferences;
    private static final String PREFS_NAME = "MEMBERS_APP";
    private static final String MEMBERS = "Members_selected";


    /**
     * method to set token
     *
     * @param token    this is the first parameter for setToken  method
     * @param mContext this is the second parameter for setToken  method
     * @return return value
     */
    public static boolean setToken(String token, Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("token", token);
        return editor.commit();
    }

    /**
     * method to get token
     *
     * @param mContext this is the first parameter for getToken  method
     * @return return value
     */
    public static String getToken(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences("settings", 0);
        return mSharedPreferences.getString("token", null);
    }


    /**
     * method to setID
     *
     * @param ID       this is the first parameter for setID  method
     * @param mContext this is the second parameter for setID  method
     * @return return value
     */
    public static boolean setID(int ID, Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("id", ID);
        return editor.commit();
    }

    /**
     * method to getID
     *
     * @param mContext this is  parameter for getID  method
     * @return return value
     */
    public static int getID(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences("settings", 0);
        return mSharedPreferences.getInt("id", 0);
    }

    /**
     * method to set contacts size
     *
     * @param size     this is the first parameter for setContactSize  method
     * @param mContext this is the second parameter for setContactSize  method
     * @return return value
     */
    public static boolean setContactSize(int size, Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("size", size);
        return editor.commit();
    }

    /**
     * method to get contacts size
     *
     * @param mContext this is  parameter for getContactSize  method
     * @return return value
     */
    public static int getContactSize(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences("settings", 0);
        return mSharedPreferences.getInt("size", 0);

    }


    /**
     * method to save new members to group
     *
     * @param context            this is the first parameter for saveMembers  method
     * @param membersGroupModels this is the second parameter for saveMembers  method
     */
    private static void saveMembers(Context context, List<MembersGroupModel> membersGroupModels) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonMembers = gson.toJson(membersGroupModels);

        editor.putString(MEMBERS, jsonMembers);

        editor.commit();
    }

    /**
     * method to add member
     *
     * @param context           this is the first parameter for addMember  method
     * @param membersGroupModel this is the second parameter for addMember  method
     */
    public static void addMember(Context context, MembersGroupModel membersGroupModel) {
        List<MembersGroupModel> membersGroupModelArrayList = getMembers(context);
        if (membersGroupModelArrayList == null)
            membersGroupModelArrayList = new ArrayList<MembersGroupModel>();
        membersGroupModelArrayList.add(membersGroupModel);
        saveMembers(context, membersGroupModelArrayList);
    }

    /**
     * method to remove member
     *
     * @param context           this is the first parameter for removeMember  method
     * @param membersGroupModel this is the second parameter for removeMember  method
     */
    public static void removeMember(Context context, MembersGroupModel membersGroupModel) {
        ArrayList<MembersGroupModel> membersGroupModelArrayList = getMembers(context);
        if (membersGroupModelArrayList != null) {
            membersGroupModelArrayList.remove(membersGroupModel);
            saveMembers(context, membersGroupModelArrayList);
        }
    }

    /**
     * method to clear members
     *
     * @param context this is  parameter for clearMembers  method
     */
    public static void clearMembers(Context context) {


        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.putString(MEMBERS, null);

        editor.commit();
    }

    /**
     * method to get all members
     *
     * @param context this is parameter for getMembers  method
     * @return return value
     */
    public static ArrayList<MembersGroupModel> getMembers(Context context) {
        SharedPreferences settings;
        List<MembersGroupModel> membersGroupModels;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(MEMBERS)) {
            String jsonMembers = settings.getString(MEMBERS, null);
            Gson gson = new Gson();
            MembersGroupModel[] membersItems = gson.fromJson(jsonMembers,
                    MembersGroupModel[].class);

            membersGroupModels = Arrays.asList(membersItems);
            membersGroupModels = new ArrayList<MembersGroupModel>(membersGroupModels);
        } else
            return null;

        return (ArrayList<MembersGroupModel>) membersGroupModels;
    }

}
