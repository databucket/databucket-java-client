package pl.databucket.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.databucket.examples.approach2.user.YUser;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultiDataResponse {

    Integer page;
    Long limit;
    Long total;
    Integer totalPages;
    String sort;
    Long reserved;
    Long available;
    List<Map<String, Object>> customData;
    List<Object> data;
    String message;

}
