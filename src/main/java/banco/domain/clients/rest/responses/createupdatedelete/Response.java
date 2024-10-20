package banco.domain.clients.rest.responses.createupdatedelete;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter

public class Response {
    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private String id;

    @JsonProperty("email")
    private String email;


}