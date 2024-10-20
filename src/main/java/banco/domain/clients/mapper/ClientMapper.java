package banco.domain.clients.mapper;

import banco.domain.clients.model.Client;
import banco.domain.clients.rest.responses.createupdatedelete.Request;
import banco.domain.clients.rest.responses.createupdatedelete.Response;
import banco.domain.clients.rest.responses.getall.ClientGetAll;
import banco.domain.clients.rest.responses.getbyid.ClientGetById;

public class ClientMapper {


    public static Client toClientFromCreate(ClientGetAll clientGetAll) {
        return Client.builder()
                .name(clientGetAll.getName())
                .email(clientGetAll.getEmail())
                .username(clientGetAll.getUsername())
                .build();
    }

    public static  Client toClientFromCreate(ClientGetById clientGetById) {
        return Client.builder()
                .name(clientGetById.getName())
                .email(clientGetById.getEmail())
                .username(clientGetById.getUsername())
                .build();
    }

    public static Request toRequest(Client client) {
        return Request.builder()
                .name(client.getName())
                .email(client.getEmail())
                .username(client.getUsername())
                .build();
    }

    public static Client toClientFromCreate(Response response) {
        return Client.builder()
                .id(Long.parseLong(response.getId()))
                .name(response.getName())
                .email(response.getEmail())
                .build();
    }

    public static Client toClientFromUpdate(Response response, int id ) {
        return Client.builder()
                .id((long) id)
                .name(response.getName())
                .email(response.getEmail())
                .build();
    }

    public static Client toClientFromCreate(Client body) {
        return Client.builder()
                .name(body.getName())
                .email(body.getEmail())
                .username(body.getUsername())
                .build();
    }
}
