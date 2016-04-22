package net.team88.dotor.shared;

import net.team88.dotor.account.LoginRequest;
import net.team88.dotor.account.SignupResponse;
import net.team88.dotor.comments.Comment;
import net.team88.dotor.comments.CommentsResponse;
import net.team88.dotor.hospitals.HospitalsResponse;
import net.team88.dotor.notifications.NotificationResponse;
import net.team88.dotor.notifications.NotificationsResponse;
import net.team88.dotor.notifications.PushSetting;
import net.team88.dotor.pets.Pet;
import net.team88.dotor.pets.PetResponse;
import net.team88.dotor.reviews.Review;
import net.team88.dotor.reviews.ReviewResponse;
import net.team88.dotor.reviews.ReviewsResponse;
import net.team88.dotor.shared.image.ImageInsertResponse;
import net.team88.dotor.shared.image.ImageResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Eun Leem on 3/21/2016.
 */
public interface DotorWebService {

    @GET("server/status")
    Call<BasicResponse> check();

    @POST("user/register")
        // #TODO register to signup
    Call<SignupResponse> signup();

    @POST("user/login")
    Call<BasicResponse> login(@Body LoginRequest loginRequest);

//    @POST("user/update/email/{email}")
//    Call<BasicResponse> updateEmail(@Path("email") String email);

    @POST("user/update/nickname/{nickname}")
    Call<BasicResponse> updateNickname(@Path("nickname") String nickname);

//    @POST("user/get/{id}")
//    Call<UserResponse> getUser(@Path("id") String id);
//
//    @POST("user/check/email/{email}")
//    Call<BasicResponse> checkEmail(@Path("email") String email);
//
//    @POST("user/check/nickname/{nickname}")
//    Call<BasicResponse> checkNickname(@Path("nickname") String nickname);


    @GET("pet/get/{id}")
    Call<PetResponse> insertPet(@Path("id") String id);

    @POST("pet/insert")
    Call<InsertResponse> insertPet(@Body Pet pet);

    @POST("pet/update")
    Call<BasicResponse> updatePet(@Body Pet pet);

    @POST("pet/delete/{id}")
    Call<BasicResponse> deletePet(@Path("id") String id);


    //    @GET("hospital/get/{id}")
//    Call<HospitalResponse> getHospital(@Path("id") String id);
//
//    @POST("hospital/insert")
//    Call<InsertResponse> insertHospital(@Body Hospital i);
//
//
    @POST("hospitals/nearby")
    Call<HospitalsResponse> getHospitalsNearby(@Body NearbyRequest i);


    @GET("review/{id}")
    Call<ReviewResponse> getReview(@Path("id") String id);

    @POST("review/insert")
    Call<InsertResponse> insertReview(@Body Review review);

//    @POST("review/update")
//    Call<BasicResponse> updateReview(@Body Review review);

    @POST("review/delete/{id}")
    Call<BasicResponse> deleteReview(@Path("id") String id);

    @POST("review/like/{id}")
    Call<BasicResponse> likeReview(@Path("id") String reviewId);


    @GET("reviews/all")
    Call<ReviewsResponse> getReviews();

    @POST("reviews/location")
    Call<ReviewsResponse> getReviewsByLocation(@Body NearbyRequest i);

//    @POST("reviews/pet")
//    Call<ReviewsResponse> getReviewsByPet(@Body Pet i);

    @POST("reviews/category/{category}")
    Call<ReviewsResponse> getReviewsByCategory(@Path("category") String category);

    /**
     * Get Reviews that matches categories.
     *
     * @param categories Multiple category separated by comma
     * @return
     */
    @POST("reviews/category/{categories}")
    Call<ReviewsResponse> getReviewsByCategories(@Path("categories") String categories);


    @GET("reviews/my")
    Call<ReviewsResponse> getMyReviews();

    @Multipart
    @POST("image/insert")
    Call<ImageInsertResponse> uploadImage(
            @Part("image\"; filename=\"tempimage.jpg") RequestBody image,
            @Part("category") RequestBody category,
            @Part("relatedid") RequestBody relatedId);

    @GET("image/{id}")
    Call<ImageResponse> getImage(@Path("id") String id);


    @GET("notification/{id}")
    Call<NotificationResponse> getNotification(@Path("id") String id);

    @GET("notifications")
    Call<NotificationsResponse> getNotifications();

    @POST("notification/readall")
    Call<BasicResponse> readAllNotfications();

    @POST("notification/read/{id}")
    Call<BasicResponse> readNotfication(@Path("id") String notificationId);

    @POST("notification/received/{id}")
    Call<BasicResponse> receivedNotfication(@Path("id") String notificationId);


    @POST("comment/insert/{category}/{relatedid}")
    Call<BasicResponse> insertComment(@Path("category") String category,
                                          @Path("relatedid") String relatedId,
                                          @Body Comment comment);

    @POST("comment/delete/{id}")
    Call<BasicResponse> deleteComment(@Path("id") String id);

    @GET("comments/{category}/{relatedid}")
    Call<CommentsResponse> getComments(@Path("category") String category,
                                       @Path("relatedid") String relatedid);

    @POST("settings/push/upsert")
    Call<BasicResponse> upsertPushSetting(@Body PushSetting setting);


    @POST("report/{category}/{id}")
    Call<BasicResponse> insertReport(@Path("category") String category,
                                         @Path("id") String id);
//
//    @POST("feedback/insert")
//    Call<BasicResponse> insertFeedback(@Body Feedback feedback);
//
//
    @POST("reset")
    Call<BasicResponse> resetDb();

}
