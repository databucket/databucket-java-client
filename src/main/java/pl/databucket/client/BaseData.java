package pl.databucket.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseData {
	protected Long id;
	protected Boolean reserved;
	protected String owner;
	protected Integer tagId;
	protected Instant createdAt;
	protected String createdBy;
	protected Instant modifiedAt;
	protected String modifiedBy;
}
