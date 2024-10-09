package banco.domain.clients.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientDto {
    private String name;
    private String username;
    private String email;
}
