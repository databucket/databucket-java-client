package pl.databucket.examples.approach2.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class YUserContact {
    String email;
    String phone;
}