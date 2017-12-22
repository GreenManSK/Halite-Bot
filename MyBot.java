
import hlt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Togepi");
        int myId = gameMap.getMyPlayerId();

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                        "; height: " + gameMap.getHeight() +
                        "; players: " + gameMap.getAllPlayers().size() +
                        "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        for (; ; ) {
            moveList.clear();
            networking.updateMap(gameMap);

            HashSet<Planet> planetsOnList = new HashSet<>();
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                boolean hasMove = false;
                for (final Planet planet : gameMap.getAllPlanets().values()) {
                    if (planet.isOwned()) {
                        continue;
                    }

                    if (ship.canDock(planet)) {
                        moveList.add(new DockMove(ship, planet));
                        hasMove = true;
                        break;
                    }

                    if (planetsOnList.contains(planet)) {
                        continue;
                    }

                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        hasMove = true;
                        planetsOnList.add(planet);
                    }

                    break;
                }

                if (!hasMove) {
                    Map<Double, Entity> nearby = gameMap.nearbyEntitiesByDistance(ship);
                    for (Entity e : nearby.values()) {
                        if (e.getOwner() != myId && e instanceof Ship) {
                            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, e, Constants.MAX_SPEED);
                            if (newThrustMove != null) {
                                moveList.add(newThrustMove);
                            }
                            break;
                        }
                    }
                }

            }
            Networking.sendMoves(moveList);
        }
    }
}
