package clientlib.domain.client;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ClientAction {
    private long from;
    private long to;
    private long unitsCount;
}
