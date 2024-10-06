package banco.clients.mapper;

import banco.clients.dtos.ClientDto;
import banco.clients.model.Client;

public class ClientMapper {

    public static ClientDto fromEntitytoDtoResponse(Client client) {
        return new ClientDto(client.getName(), client.getUsername(), client.getEmail());
    }

    public static Client fromDtoRequestToEntity(ClientDto dto) {
        return new Client (
                dto.getName(),
                dto.getUsername(),
                dto.getEmail()
        );
    }
}
