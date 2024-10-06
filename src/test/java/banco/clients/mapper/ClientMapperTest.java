package banco.clients.mapper;

import banco.clients.dtos.ClientDto;
import banco.clients.model.Client;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {
    Client client = new Client("test", "userTest", "email");

    ClientDto dto = new ClientDto("nameDto", "usernameDto", "emailDto");

    ClientMapper mapper = new ClientMapper();

    @Test
    void fromEntitytoDtoResponse() {

        var result = mapper.fromEntitytoDtoResponse(client);

        assertAll(
                () -> assertEquals(result.getName(), client.getName()),
                () -> assertEquals(result.getUsername(), client.getUsername()),
                () -> assertEquals(result.getEmail(), client.getEmail()),
                () -> assertInstanceOf(ClientDto.class, result)
        );
    }

    @Test
    void fromDtoRequestToEntity() {

        var result = mapper.fromDtoRequestToEntity(dto);

        assertAll(
                () -> assertInstanceOf(UUID.class, result.getId()),
                () -> assertEquals(result.getName(), dto.getName()),
                () -> assertEquals(result.getUsername(), dto.getUsername()),
                () -> assertEquals(result.getEmail(), dto.getEmail()),
                () -> assertNull(result.getCard()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNotNull(result.getUpdatedAt()),
                () -> assertInstanceOf(LocalDateTime.class, result.getCreatedAt()),
                () -> assertInstanceOf(LocalDateTime.class, result.getUpdatedAt()),
                () -> assertInstanceOf(Client.class, result)
        );
    }
}