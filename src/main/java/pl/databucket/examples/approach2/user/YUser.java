package pl.databucket.examples.approach2.user;

import lombok.*;
import lombok.experimental.SuperBuilder;
import pl.databucket.client.BaseData;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class YUser extends BaseData {
    YUserProperties properties;
}
