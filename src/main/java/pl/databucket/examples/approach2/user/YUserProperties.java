package pl.databucket.examples.approach2.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class YUserProperties {
    String eyeColor;
    YUserContact contact;
    Integer number;
}


