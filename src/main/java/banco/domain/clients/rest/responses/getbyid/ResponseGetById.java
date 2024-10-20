package banco.domain.clients.rest.responses.getbyid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ResponseGetById {
    @JsonProperty("data")
    private ClientGetById data;

    public ClientGetById getData() {
        return data;
    }



}
