package com.tweetco.clients;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.tweetco.TweetCo;
import com.tweetco.activities.ApiInfo;
import com.tweetco.dao.TweetUser;
import com.tweetco.database.dao.Account;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

/**
 * Created by kirankumar on 19/06/15.
 */
public class AccountClient {

    public Account loadAccountFromServer(final String serverAddress, final String username, final String authToken)
    {
        try {
            MobileServiceClient mobileServiceClient = new MobileServiceClient(serverAddress, authToken, TweetCo.mContext);

            final Account tempAccount = new Account();
            JsonObject element = new JsonObject();
            JsonObject obj = new JsonObject();
            obj.addProperty(ApiInfo.kApiRequesterKey, username);
            mobileServiceClient.invokeApi(ApiInfo.GET_USER_INFO, obj, new ApiJsonOperationCallback() {

                @Override
                public void onCompleted(JsonElement arg0, Exception arg1,
                                        ServiceFilterResponse arg2) {
                    if (arg1 == null) {
                        Gson gson = new Gson();

                        try {
                            TweetUser[] tweetUser = gson.fromJson(arg0, TweetUser[].class);
                            tempAccount.setUsername(tweetUser[0].username);
                            tempAccount.followers = tweetUser[0].followers;
                            tempAccount.followees = tweetUser[0].followees;
                            tempAccount.profileimageurl = tweetUser[0].profileimageurl;
                            tempAccount.profilebgurl = tweetUser[0].profilebgurl;
                            tempAccount.bookmarkedtweets = tweetUser[0].bookmarkedtweets;
                            tempAccount.setServerAddress(serverAddress);
                            tempAccount.setAuthToken(authToken);
                        } catch (JsonSyntaxException exception) {
                            exception.printStackTrace();
                            Log.e("TweetUserRunnable", "unable to parse tweetUser");
                        }
                    } else {
                        Log.e("Item clicked", "Exception fetching tweets received");
                    }

                }
            }, true);

            obj = new JsonObject();
            obj.addProperty(ApiInfo.kApiRequesterKey, username);
            mobileServiceClient.invokeApi(ApiInfo.GET_USER_PROFILE_INFO, obj, new ApiJsonOperationCallback() {

                @Override
                public void onCompleted(JsonElement arg0, Exception arg1,
                                        ServiceFilterResponse arg2) {
                    if(arg1 == null)
                    {
                        Gson gson = new Gson();

                        try
                        {
                            JsonObject jsonObject = arg0.getAsJsonObject().get("profileinfo").getAsJsonArray().get(0).getAsJsonObject();
                            String contactInfo = GetString(jsonObject, ApiInfo.kContactInfoTags);
                            String interesttags = GetString(jsonObject, ApiInfo.kInterestTags);
                            String skilltags = GetString(jsonObject, ApiInfo.kSkillTags);
                            String personaltags = GetString(jsonObject, ApiInfo.kPersonalTags);
                            String workinfo = GetString(jsonObject, ApiInfo.kWorkInfo);
                            //Also Update the Account Table.
                            tempAccount.contactInfo = contactInfo;
                            tempAccount.workDetails = workinfo;
                            tempAccount.interesttags = interesttags;
                            tempAccount.skillstags = skilltags;
                            tempAccount.personalInterestTags = personaltags;                        }
                        catch(JsonSyntaxException exception)
                        {
                            exception.printStackTrace();
                            Log.e("TweetUserRunnable", "unable to parse tweetUser") ;
                        }

                    }
                    else
                    {
                        Log.e("Item clicked","Exception fetching tweets received") ;
                    }

                }
            },true);

            return tempAccount;

        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public void updateServer(final Account pAccount, BitmapDrawable profilePicDrawable, BitmapDrawable bgPicDrawable) throws MalformedURLException
    {
        try {
            MobileServiceClient mobileServiceClient = new MobileServiceClient(pAccount.getServerAddress(), pAccount.getAuthToken(), TweetCo.mContext);

            if(profilePicDrawable != null)
            {
                JsonObject element = new JsonObject();
                element.addProperty(ApiInfo.kApiRequesterKey, pAccount.getUsername());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                Bitmap bitmap = profilePicDrawable.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG,25,bos);
                byte[] bb = bos.toByteArray();
                String image = Base64.encodeToString(bb, 0);
                element.addProperty(ApiInfo.kBase64ImageStringKey, image);

                mobileServiceClient.invokeApi(ApiInfo.UPDATE_USER_IMAGE, element, new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(JsonElement element, Exception exception, ServiceFilterResponse arg2) {
                        if (exception == null) {
                            Log.d("EditProfile", "Profile edit saved");
                            pAccount.profileimageurl = GetString(element.getAsJsonObject(), "profileimageurl");

                        } else {
                            Log.e("EditProfile", "Profile edit save failed");
                        }

                    }
                }, true);
            }

            if(bgPicDrawable != null)
            {
                JsonObject element = new JsonObject();
                element.addProperty(ApiInfo.kApiRequesterKey, pAccount.getUsername());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                Bitmap bitmap = bgPicDrawable.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG,25,bos);
                byte[] bb = bos.toByteArray();
                String image = Base64.encodeToString(bb, 0);
                element.addProperty(ApiInfo.kBase64BGImageStringKey, image);

                mobileServiceClient.invokeApi(ApiInfo.UPDATE_USER_IMAGE, element, new ApiJsonOperationCallback() {

                    @Override
                    public void onCompleted(JsonElement element, Exception exception, ServiceFilterResponse arg2) {
                        if (exception == null) {
                            Log.d("EditProfile", "Profile edit saved");
                            pAccount.profilebgurl = GetString(element.getAsJsonObject(), "profilebgurl");

                        } else {
                            Log.e("EditProfile", "Profile edit save failed");
                        }

                    }
                }, true);
            }

            JsonObject element = new JsonObject();
            element.addProperty(ApiInfo.kApiRequesterKey, pAccount.getUsername());
            if(!TextUtils.isEmpty(pAccount.interesttags)) {
                element.addProperty(ApiInfo.kInterestTags, pAccount.interesttags);
            }

            if(!TextUtils.isEmpty(pAccount.contactInfo)) {
                element.addProperty(ApiInfo.kContactInfoTags, pAccount.contactInfo);
            }


            if(!TextUtils.isEmpty(pAccount.skillstags)) {
                element.addProperty(ApiInfo.kSkillTags, pAccount.skillstags);
            }

            if(!TextUtils.isEmpty(pAccount.personalInterestTags)) {
                element.addProperty(ApiInfo.kPersonalTags, pAccount.personalInterestTags);
            }

            if(!TextUtils.isEmpty(pAccount.workDetails)) {
                element.addProperty(ApiInfo.kWorkInfo, pAccount.workDetails);
            }



            mobileServiceClient.invokeApi(ApiInfo.UPDATE_USER_PROFILE_INFO, element, new ApiJsonOperationCallback() {

                @Override
                public void onCompleted(JsonElement element, Exception exception, ServiceFilterResponse arg2) {
                    if (exception == null) {
                        Log.d("EditProfile", "Profile edit saved");

                    } else {
                        Log.e("EditProfile", "Profile edit save failed");
                    }

                }
            }, true);

        }
        catch (MalformedURLException e)
        {
            throw e;
        }
    }

    private static String GetString(JsonObject object, String name)
    {
        JsonElement element = object.get(name);
        if(element != null)
        {
            return element.getAsString();
        }
        else
        {
            return null;
        }
    }
}
