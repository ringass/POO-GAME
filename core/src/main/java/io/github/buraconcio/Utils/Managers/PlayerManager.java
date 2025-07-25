package io.github.buraconcio.Utils.Managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.badlogic.gdx.math.Vector2;

import io.github.buraconcio.Objects.Game.Player;
import io.github.buraconcio.Utils.Common.Constants;
import io.github.buraconcio.Network.UDP.UdpPackage;
import io.github.buraconcio.Network.UDP.UdpPackage.PackType;

// singleton
public class PlayerManager {
    private List<Player> players = new CopyOnWriteArrayList<>();
    private static PlayerManager instance;
    private int localPlayerID;
    private Player localPlayer;

    private List<Integer> orderOfArrival;

    public PlayerManager() {
        players = new ArrayList<>();
        orderOfArrival = new ArrayList<>();
    }

    public static synchronized PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }

        return instance;
    }

    public void addPlayer(Player player) {
        removePlayerbyId(player.getId());

        players.add(player);
    }

    public void removePlayerbyId(int id) {
        Iterator<Player> it = players.iterator();

        while (it.hasNext()) {
            Player p = it.next();

            if (p.getId() == id) {
                // p.dispose();
                it.remove();
            }
        }
    }

    public void removePlayerbyUser(String name) {
        Iterator<Player> it = players.iterator();

        while (it.hasNext()) {
            Player p = it.next();

            if (p.getUsername() == name) {
                p.dispose();
                it.remove();
            }
        }
    }

    public Player getPlayer(int id) {
        for (Player p : players) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public boolean exists(int id) {
        for (Player p : players) {
            if (p.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public List<Player> getAllPlayers() {
        return List.copyOf(players);
    }

    public void setPlayers(List<Player> newPlayers) {
        for (Player player : players) {
            player.dispose();
        }

        players.clear();
        players.addAll(newPlayers);
    }

    public void updatePlayers(List<UdpPackage> update) {
        Runnable task = () -> {

            for (UdpPackage pack : update) { // modificar para comparar por PackType do UDPpackage e atualizar oq
                                             // realmente importa

                // testing ball for now
                int playerId = pack.getId();
                PackType type = pack.getTypeP();

                if (playerId == Constants.localP().getId())
                    continue;

                Player p = PlayerManager.getInstance().getPlayer(playerId);

                if (p == null)
                    continue;

                if (type == PackType.BALL) {
                    Vector2 ballPos = new Vector2(pack.getBallX(), pack.getBallY());
                    Vector2 ballVel = new Vector2(pack.getBallVX(), pack.getBallVY());

                    p.update(ballPos, ballVel, pack.getBallState());

                } else if (type == PackType.OBSTACLE) {

                    Vector2 obstaclePos = new Vector2(pack.getObsX(), pack.getObsY());

                    p.update(obstaclePos, pack.getObsId(),
                            pack.getObsRotationIndex());

                } else {
                    p.update(pack.getObsPlaced(), pack.getflagPlaced());
                    continue;
                }

            }
        };
        PhysicsManager.getInstance().schedule(task);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void setLocalPlayer(Player player) {

        this.localPlayer = player;
        addPlayer(player);

    }

    public void setAllCanSelect(boolean flag) {
        for (Player p : players) {
            p.setCanSelect(flag);
        }
    }

    public void setAllBallsInteractable(boolean flag) {
        for (Player p : players) {
            p.setBallInteractable(flag);
        }
    }

    public void updateStars(Map<Integer, Integer> starsMap) {
        for (Player p : players) {
            if (starsMap.containsKey(p.getId())) {
                p.setStars(starsMap.get(p.getId()));
            }
        }
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public void syncLocalPlayer() {
        for (Player p : players) {
            if (p.getId() == localPlayer.getId()) {
                setLocalPlayer(p);
                break;
            }
        }
    }

    public void setAllBallsAlive() {
        for (Player p : players) {
            if (p.getBall() != null)
                p.getBall().setAlive(true);
        }
    }

    public boolean areAllBallsAlive() {
        for (Player p : players) {
            if (p.getBall() != null && !p.getBall().isAlive())
                return false;
        }

        return true;
    }

    public boolean areAllBallsDead() {
        for (Player p : players) {
            if (p.getBall() != null && p.getBall().isAlive()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasEveryonePlaced() {
        for (Player p : players) {
            if (!p.hasPlacedObstacle()) {

                if(Constants.DEBUG)
                    System.out.println("ainda nao coloquei");

                return false;
            }
        }
        return true;
    }

    public void setEveryonePlaced(boolean flag) {
        for (Player p : players) {
            p.setHasPlacedObstacle(flag);
        }
    }

    public void clear() {
        players.clear();
    }

    public boolean hasEveryoneClaimed() {

        for (Player p : players) {
            if (p.canSelect()) {
                return false;
            }
        }

        return true;
    }

    public boolean getWin() {
        for (Player p : players) {
            if (p.getStars() >= GameManager.getInstance().getPointsToWin()) {
                return true;
            }
        }
        return false;
    }

    public void addToArrivalOrder(int id) {
        if (!orderOfArrival.contains(id)) {
            orderOfArrival.add(id);
        }
    }

    public List<Integer> getArrivalOrder() {
        return orderOfArrival;
    }

    public void updateArrivalOrder() {
        if (orderOfArrival.isEmpty())
            return;

        int firstId = orderOfArrival.get(0);
        int lastId = orderOfArrival.get(orderOfArrival.size() - 1);

        for (Player p : players) {
            if (p.getId() == firstId) {
                p.firstOrLast(true);
            } else if (p.getId() == lastId) {
                p.firstOrLast(false);
            }
        }

        orderOfArrival.clear();
    }

    public void resetAllScoredFlag(){
        for(Player p : players){
            p.resetScoredFlag();
        }
    }

}
