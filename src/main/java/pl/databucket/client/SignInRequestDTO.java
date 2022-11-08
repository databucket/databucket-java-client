package pl.databucket.client;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInRequestDTO {

    private String username;
    private String password;
    private Integer projectId;

}
