package clientlib;

import clientlib.domain.client.ClientAction;
import clientlib.domain.client.ClientCommand;
import clientlib.domain.server.GalaxySnapshot;
import clientlib.domain.server.Planet;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * Основной объект клиента для взаимодействия с сервером.
 */
@Slf4j
public class CodeBattleJavaClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private Consumer<CodeBattleJavaClient> handler;
    private final String player;
    /**
     * @return Cнапшот галактики
     */
    @Getter
    private GalaxySnapshot galaxy;
    private List<ClientAction> actions = new ArrayList<>();

    public CodeBattleJavaClient(String serverAddress, String token, String player) throws URISyntaxException {
        super(new URI(format("ws://%s/galaxy", serverAddress)), new Draft_6455(), ImmutableMap.of("token", token), 0);
        this.player = player;
    }

    /**
     * @return аннексированные тобой планеты или пустая коллекция, если таковых не найдено
     */
    public List<Planet> getMyPlanets() {
        return this.galaxy.getPlanets().stream()
                .filter(planet -> player.equals(planet.getOwner()))
                .collect(Collectors.toList());
    }

    /**
     * @param planetId идентификатор планеты
     * @return планета с идентификатором <tt>planetId</tt> или <tt>null</tt>, если такой планеты не найлено
     */
    public Planet getPlanetById(long planetId) {
        return this.galaxy.getPlanets().stream()
                .filter(planet -> planet.getId() == planetId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Получение соседних планет относительно планеты <tt>planetId</tt>
     * @param planetId идентификатор планеты для получения её соседей
     * @return список соседних планет
     */
    public List<Planet> getNeighbours(long planetId) {
        return ofNullable(getPlanetById(planetId))
                .map(planet -> galaxy.getPlanets().stream()
                        .filter(p -> planet.getNeighbours().contains(p.getId()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Добавление команды отправки дронов
     * @param from идентификатор аннексированной планеты, с который ты собираешься выслать дронов
     * @param to идентификатор планеты, на которую ты высылаешь дронов
     * @param drones количество пересылаемых дронов
     */
    public void sendDrones(long from, long to, long drones) {
        this.actions.add(new ClientAction(from, to, drones));
    }

    @Override
    public void onOpen(ServerHandshake handShakeData) {
        log.info("Connection established");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("Disconnected: {} {}", code, reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error("Error:", ex);
    }

    @Override
    public void onMessage(String message) {
        log.debug("Received command <<< {}", message);
        this.actions = new ArrayList<>();
        this.galaxy = ofNullable(message)
                .map(msg -> GSON.fromJson(msg, GalaxySnapshot.class))
                .orElse(null);
        this.handler.accept(this);
        sendMsg();
    }

    public void run(Consumer<CodeBattleJavaClient> handler) {
        this.handler = handler;
        connect();
    }

    private void sendMsg() {
        String txtCommand = GSON.toJson(new ClientCommand(actions));
        log.debug("Sending command >>> {}", txtCommand);
        send(txtCommand);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
}
