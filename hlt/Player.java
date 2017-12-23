package hlt;

import java.util.*;

public class Player {

    private final Map<Integer, Ship> ships;
    private final Set<Planet> planets = new HashSet<>();
    private final int id;

    public Player(final int id, Map<Integer, Ship> ships) {
        this.id = id;
        this.ships = Collections.unmodifiableMap(ships);
    }

    public Map<Integer, Ship> getShips() {
        return ships;
    }

    public Ship getShip(final int entityId) {
        return ships.get(entityId);
    }

    public void addPlanet(Planet planet) {
        planets.add(planet);
    }

    public Set<Planet> getPlanets() {
        return Collections.unmodifiableSet(planets);
    }

    public int getId() {
        return id;
    }
}
