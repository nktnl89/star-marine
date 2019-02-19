package clientlib.domain.server;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Portal {
    private long source;
    private long target;
}
