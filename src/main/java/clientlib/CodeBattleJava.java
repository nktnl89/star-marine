package clientlib;

import clientlib.domain.server.GalaxySnapshot;
import clientlib.domain.server.Planet;
import clientlib.domain.server.PlanetType;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static clientlib.domain.server.PlanetType.TYPE_A;

@Slf4j
public class CodeBattleJava {

    /**
     * Задача игрока: реализовать логику формирования команд на отправку дронов в этом методе.
     * Для отправки дронов с планеты на планету используется метод {@link CodeBattleJavaClient#sendDrones(long, long, long)}
     * <br/><br/>
     * Получить снапшот галактики можно используя метол {@link CodeBattleJavaClient#getGalaxy()}
     * <br/><br/>
     * Получение всех аннексированных твоими дронами планет можно методом {@link CodeBattleJavaClient#getMyPlanets()}
     * <br/><br/>
     * Получение всех соседей планеты по её илентификатору: {@link CodeBattleJavaClient#getNeighbours(long)} (long)}
     * <br/><br/>
     * Получение описания планеты по её идентификатору: @{@link CodeBattleJavaClient#getPlanetById(long)}
     */
    public static void main(String[] args) throws URISyntaxException {

        // Указываем адрес сервера, токен и логин игрока
        CodeBattleJavaClient client = new CodeBattleJavaClient("epruizhsa0001t2:8081", "57d6b90f-e150-4925-af1d-8a1d0ccc8da8", "bot_kotoriy_ne_smog");

        client.run((cl) -> {
            GalaxySnapshot snapshot = cl.getGalaxy();
            if (!snapshot.getErrors().isEmpty()) {
                // выводим информацию об ошибках, если таковые есть (например, с клиента отправлено невалидное действие)
                log.error("Error occurred", snapshot.getErrors());
            }

            for (Planet myPlanet : cl.getMyPlanets()) {
                List<Planet> neighbours = cl.getNeighbours(myPlanet.getId());
                if (!isEnoughDroidsForMyPlanet(myPlanet)) {
                    cl.sendDrones(myPlanet.getId(), 0, 0);
                } else {
                    if (randomDestiny()) {
                        cl.sendDrones(myPlanet.getId(), chooseYourEnemy(myPlanet, neighbours).getId(), (long) (myPlanet.getDroids() * 0.3));
                    } else {
                        cl.sendDrones(myPlanet.getId(), chooseYourConfederate(myPlanet, neighbours).getId(), (long) (myPlanet.getDroids() * 0.3));
                    }
                }
            }
//            cl.getMyPlanets().forEach(planet -> { // получаем список своих планет
//
//                planet.getNeighbours().stream()   // для каждой аннексированной планеты получаем её соседей
//                        .filter(neighbourId -> neighbourId != planet.getId() )
//                        // отсылаем дронов с аннексированных планет на соседние планеты
//                        .forEach(neighbourId -> cl.sendDrones(planet.getId(), neighbourId, planet.getDroids() / planet.getNeighbours().size()));
//            });
        });
    }

    private static boolean canWeWin(Planet myPlanet, Planet enemy) {
        return myPlanet.getDroids() > enemy.getDroids();
    }

    private static boolean randomDestiny() {
        Random random = new Random();
        return random.nextBoolean();
    }

    private static boolean isEnoughDroidsForMyPlanet(Planet planet) {
        boolean result = false;
        switch (planet.getType()) {
            case TYPE_A:
                result = (planet.getDroids() / 100) * 100 >= 80;
                break;
            case TYPE_B:
                result = (planet.getDroids() / 100) * 100 >= 150;
                break;
            case TYPE_C:
                result = (planet.getDroids() / 100) * 100 >= 350;
                break;
            case TYPE_D:
                result = (planet.getDroids() / 100) * 100 >= 700;
                break;
        }
        return result;
    }

    private boolean isPlanetMine(List<Planet> myPlanets, long planetId) {
        for (Planet planet : myPlanets) {
            if (planet.getId() == planetId) {
                return true;
            }
        }
        return false;
    }

    private static Planet chooseYourEnemy(Planet myPlanet, List<Planet> neighbours) {
        Planet enemyPlanet = null;
        neighbours.sort(new Comparator<Planet>() {
            @Override
            public int compare(Planet o1, Planet o2) {
                return (int) (o1.getDroids() - o2.getDroids());
            }
        });
        for (Planet neighbour : neighbours) {
            if (neighbour.getType().equals(PlanetType.TYPE_D)) {
                enemyPlanet = neighbour;
                break;
            }
            if (!(neighbour.getOwner() == myPlanet.getOwner())) {
                enemyPlanet = neighbour;
                break;
            }
        }

        if (enemyPlanet == null) {
            enemyPlanet = neighbours.get(0);//chooseYourConfederate(myPlanet, neighbours);
        }
        return enemyPlanet;
    }

    private static Planet chooseYourConfederate(Planet myPlanet, List<Planet> neighbours) {
        Planet confederatePlanet = null;
        neighbours.sort(new Comparator<Planet>() {
            @Override
            public int compare(Planet o1, Planet o2) {
                return (int) (o1.getDroids() - o2.getDroids());
            }
        });
        for (Planet neighbour : neighbours) {
            if (!(neighbour.getOwner() == null)) {
                if (neighbour.getOwner().equals(myPlanet.getOwner()) && !(neighbour.getType().equals(PlanetType.TYPE_D))) {
                    confederatePlanet = neighbour;
                    break;
                }
            }
        }
        if (confederatePlanet == null) {
            confederatePlanet = neighbours.get(neighbours.size()-1);//chooseYourEnemy(myPlanet, neighbours);
        }
        return confederatePlanet;
    }
}
