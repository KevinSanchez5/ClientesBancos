package banco.domain.clients.rest.responses.getall;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ResponseGetAll {

    @JsonProperty("per_page")
    private int perPage;

    @JsonProperty("total")
    private int total;

    @JsonProperty("data")
    private List<ClientGetAll>data;

    @JsonProperty("page")
    private int page;

    @JsonProperty("total_pages")
    private int totalPages;


}
