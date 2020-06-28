package ru.hiddenproject.audioworld;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface API {
    @GET("/audio/api.php")
    Call<List<Book>> getBooks(@Query("v") String v, @Query("method") String method, @Query("page") int page);

    @GET("/audio/api.php")
    Call<List<Book>> getNewBooks(@Query("v") String v, @Query("method") String method);

    @GET("/audio/api.php")
    Call<List<Book>> searchBook(@Query("v") String v, @Query("method") String method, @Query("query") String query);

    @GET("/audio/api.php")
    Call<Book> getBook(@Query("v") String v, @Query("method") String method, @Query("id") int id);

    @GET("/audio/api.php")
    Call<List<Book>> getUserBooks(@Query("v") String v, @Query("method") String method, @Query("ids") String ids);

    @GET("/audio/api.php")
    Call<User> login(@Query("v") String v, @Query("method") String method, @Query("email") String login, @Query("password") String password);

    @GET("/audio/api.php")
    Call<History> getHistory(@Query("v") String v, @Query("method") String method, @Query("token") String token);

    @GET("/audio/api.php")
    Call<History> saveBook(@Query("v") String v, @Query("method") String method, @Query("token") String token, @Query("id") int id);

    @GET("/audio/api.php")
    Call<History> delBook(@Query("v") String v, @Query("method") String method, @Query("token") String token, @Query("id") int id);

    @GET("/audio/api.php")
    Call<History> findBook(@Query("v") String v, @Query("method") String method, @Query("token") String token, @Query("id") int id);

    @GET("/audio/api.php")
    Call<String> getVersion(@Query("method") String method);
}
