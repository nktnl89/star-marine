package clientlib;

import clientlib.domain.server.GalaxySnapshot;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

@Slf4j
public class CodeBattleJava {

    /**
        Задача игрока: реализовать логику формирования команд на отправку дронов в этом методе.
        Для отправки дронов с планеты на планету используется метод {@link CodeBattleJavaClient#sendDrones(long, long, long)}
        <br/><br/>
        Получить снапшот галактики можно используя метол {@link CodeBattleJavaClient#getGalaxy()}
        <br/><br/>
        Получение всех аннексированных твоими дронами планет можно методом {@link CodeBattleJavaClient#getMyPlanets()}
        <br/><br/>
        Получение всех соседей планеты по её илентификатору: {@link CodeBattleJavaClient#getNeighbours(long)} (long)}
        <br/><br/>
        Получение описания планеты по её идентификатору: @{@link CodeBattleJavaClient#getPlanetById(long)}
     */
    public static void main(String[] args) throws URISyntaxException {

        // Указываем адрес сервера, токен и логин игрока
        CodeBattleJavaClient client = new CodeBattleJavaClient("epruizhsa0001t2:8081", "790e2124-6a51-47fe-8b1c-5112b806919f", "ululu_bot");

        client.run((cl) -> {
            GalaxySnapshot snapshot = cl.getGalaxy();
            if (!snapshot.getErrors().isEmpty()) {
                // выводим информацию об ошибках, если таковые есть (например, с клиента отправлено невалидное действие)
                log.error("Error occurred", snapshot.getErrors());
            }

            cl.getMyPlanets().forEach(planet -> { // получаем список своих планет
                planet.getNeighbours().stream()   // для каждой аннексированной планеты получаем её соседей
                        .filter(neighbourId -> neighbourId != planet.getId())
                        // отсылаем дронов с аннексированных планет на соседние планеты
                        .forEach(neighbourId -> cl.sendDrones(planet.getId(), neighbourId, planet.getDroids() / planet.getNeighbours().size()));
            });
        });
    }
}
