package banco.domain.clients.rest;


import banco.domain.clients.model.Client;
import banco.domain.clients.rest.responses.createupdatedelete.Request;
import banco.domain.clients.rest.responses.getall.ResponseGetAll;
import banco.domain.clients.rest.responses.getbyid.ResponseGetById;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.concurrent.CompletableFuture;

public interface ClientApiRest {
    String API_CLIENTS_URL = "https://jsonplaceholder.typicode.com/";


    @GET("users")
    Call<ResponseGetAll> getAllSync();

    @GET("users/{id}")
    Call<ResponseGetById> getByIdSync(@Path("id") String id);

    @POST("users")
    CompletableFuture<Response<Client>> createClient(@Body Request request);

    @PUT("users/{id}")
    CompletableFuture<Response<Client>> updateClient(@Path("id") String id, @Body Request request);

    @DELETE("users/{id}")
    CompletableFuture<Response<Client>> deleteClient(@Path("id") String id);

}
