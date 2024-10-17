package banco.domain.clients.mapper;

import banco.domain.clients.dtos.ClientDto;
import banco.domain.clients.mapper.ClientMapper;
import banco.domain.clients.model.Client;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {
    Client client = new Client(1L, "test", "userTest", "email");

    ClientDto dto = new ClientDto("nameDto", "usernameDto", "emailDto");


    @Test
    void fromEntitytoDtoResponse() {

        var result = ClientMapper.fromEntitytoDtoResponse(client);

        assertAll(
                () -> assertEquals(result.getName(), client.getName()),
                () -> assertEquals(result.getUsername(), client.getUsername()),
                () -> assertEquals(result.getEmail(), client.getEmail()),
                () -> assertInstanceOf(ClientDto.class, result)
        );
    }

    @Test
    void fromDtoRequestToEntity() {

        var result = ClientMapper.fromDtoRequestToEntity(dto);

        assertAll(
                () -> assertNull(result.getId()),
                () -> assertEquals(result.getName(), dto.getName()),
                () -> assertEquals(result.getUsername(), dto.getUsername()),
                () -> assertEquals(result.getEmail(), dto.getEmail()),
                () -> assertNotNull(result.getCards()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNotNull(result.getUpdatedAt()),
                () -> assertInstanceOf(LocalDateTime.class, result.getCreatedAt()),
                () -> assertInstanceOf(LocalDateTime.class, result.getUpdatedAt()),
                () -> assertInstanceOf(Client.class, result)
        );
    }
}