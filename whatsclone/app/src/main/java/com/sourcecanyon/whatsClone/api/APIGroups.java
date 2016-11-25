package com.sourcecanyon.whatsClone.api;


import com.sourcecanyon.whatsClone.app.EndPoints;
import com.sourcecanyon.whatsClone.models.groups.GroupResponse;
import com.sourcecanyon.whatsClone.models.groups.GroupsModel;
import com.sourcecanyon.whatsClone.models.groups.MembersGroupModel;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public interface APIGroups {

    /**
     * method to get all groups
     *
     * @return this is return value
     */
    @GET(EndPoints.GROUPS_lIST)
    Observable<List<GroupsModel>> groups();

    /**
     * method to create group
     *
     * @param userID this is the first parameter for  createGroup method
     * @param name   this is the second parameter for  createGroup method
     * @param image  this is the thirded parameter for  createGroup method
     * @param ids    this is the fourth  parameter for  createGroup method
     * @param date   this is the fifth parameter for  createGroup method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.CREATE_GROUP)
    Call<GroupResponse> createGroup(@Part("userID") int userID,
                                    @Part("name") RequestBody name,
                                    @Part("image\"; filename=\"picture.jpg\" ") RequestBody image,
                                    @Part("ids") RequestBody ids,
                                    @Part("date") String date);

    /**
     * method to add members to group
     *
     * @param groupID this is  the first parameter for  addMembers method
     * @param ids     this is  the second parameter for  addMembers method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.ADD_MEMBERS_TO_GROUP)
    Call<GroupResponse> addMembers(@Part("groupID") int groupID,
                                   @Part("ids") int ids);

    /**
     * method to remove member from group
     *
     * @param groupID this is the first parameter for  removeMember method
     * @param id      this is the second parameter for  removeMember method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.REMOVE_MEMBER_FROM_GROUP)
    Call<GroupResponse> removeMember(@Part("groupID") int groupID,
                                     @Part("id") int id);

    /**
     * method to make a member an admin
     *
     * @param groupID this is the first parameter for  makeAdmin method
     * @param id      this is the second parameter for  makeAdmin method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.MAKE_MEMBER_AS_ADMIN)
    Call<GroupResponse> makeAdmin(@Part("groupID") int groupID,
                                  @Part("id") int id);

    /**
     * method to get group information
     *
     * @param groupID this is  parameter for  getGroup method
     * @return this is return value
     */
    @GET(EndPoints.GET_GROUP)
    Observable<GroupsModel> getGroup(@Path("groupID") int groupID);

    /**
     * method to get group members list
     *
     * @param groupID this is  parameter for  groupMembers method
     * @return this is return value
     */
    @GET(EndPoints.GROUP_MEMBERS_lIST)
    Observable<List<MembersGroupModel>> groupMembers(@Path("groupID") int groupID);

    /**
     * method to delete group
     *
     * @param groupID this is  parameter for  deleteGroup method
     * @return this is return value
     */
    @DELETE(EndPoints.DELETE_GROUP)
    Observable<GroupResponse> deleteGroup(@Path("groupID") int groupID);

    /**
     * method to exit a group
     *
     * @param groupID this is  parameter for  exitGroup method
     * @return this is return value
     */
    @PUT(EndPoints.EXIT_GROUP)
    Observable<GroupResponse> exitGroup(@Path("groupID") int groupID);

    /**
     * method to upload group image
     *
     * @param image   this is the first parameter for  uploadImage method
     * @param groupID this is the second parameter for  uploadImage method
     * @return this is return value
     */
    @Multipart
    @POST(EndPoints.UPLOAD_GROUP_PROFILE_IMAGE)
    Call<GroupResponse> uploadImage(@Part("image\"; filename=\"picture.jpg\" ") RequestBody image,
                                    @Part("groupID") int groupID);
}
