
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

        final double CONQUERING_SLOW_BOUND = 1 / gameMap.getAllPlayers().size();

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

            double myPlanetPercentage = gameMap.getMyPlayer().getPlanets().size() / gameMap.getAllPlanets().size();
            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                boolean hasMove = false;

                for (final Planet planet : gameMap.nearbyPlanetsByDistance(ship).values()) {
                    if (planet.isOwned() && !(CONQUERING_SLOW_BOUND < myPlanetPercentage && !planet.isFull() && planet.getOwner() == myId)) {
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
//                    Map<Double, Ship> nearby = gameMap.nearbyShipsByDistance(ship);
//                    for (final Ship e : nearby.values()) {
//                        if (e.getOwner() != myId) {
//                            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, e, Constants.MAX_SPEED);
//                            if (newThrustMove != null) {
//                                moveList.add(newThrustMove);
//                            }
//                            break;
//                        }
//                    }
                    for (final Entity e : gameMap.nearbyEntitiesByDistance(ship).values()) {
                        if (e instanceof Ship && e.getOwner() != myId) {
                            final Ship enemy = (Ship) e;
                            final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, enemy, Constants.MAX_SPEED);
                            if (newThrustMove != null) {
                                moveList.add(newThrustMove);
                            }
                            break;
                        }
                        if (e instanceof Planet && ((Planet) e).isOwned() && e.getOwner() == myId) {
                            Planet planet = (Planet) e;
                            if (!planet.isFull()) {
                                if (ship.canDock(planet)) {
                                    moveList.add(new DockMove(ship, planet));
                                    break;
                                } else {
                                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, planet, Constants.MAX_SPEED);
                                    if (newThrustMove != null) {
                                        moveList.add(newThrustMove);
                                        planetsOnList.add(planet);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

            }
            Networking.sendMoves(moveList);
        }
    }
}
