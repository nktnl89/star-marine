package clientlib.domain.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GalaxySnapshot {
    @Builder.Default
    private List<Planet> planets = new ArrayList<>();
    @Builder.Default
    private List<Disaster> disasters = new ArrayList<>();
    @Builder.Default
    private List<Portal> portals = new ArrayList<>();
    @Builder.Default
    private List<String> errors = new ArrayList<>();
}
