package clientlib.domain.client;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
public class ClientCommand {
    private List<ClientAction> actions = new ArrayList<>();
}
