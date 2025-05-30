package io.github.buraconcio.Utils;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.lang.Runnable;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Contact;

// singleton
public class PhysicsManager {
    private static PhysicsManager instance;

    World world;
    Set<Contact> contactList;
    ArrayList<Runnable> box2dScheduler;

    public PhysicsManager() {
        world = new World(new Vector2(0f, 0f), true);
        contactList = new HashSet<Contact>();
        box2dScheduler = new ArrayList();
    }

    public static synchronized PhysicsManager getInstance() {
        if (instance == null) {
            instance = new PhysicsManager();
        }

        return instance;
    }

    public void schedule(Runnable task) {
        box2dScheduler.add(task);
    }

    public void clearScheduler() {
        box2dScheduler.clear();
    }

    public ArrayList<Runnable> getBox2dScheduler() {
        return box2dScheduler;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Set<Contact> getContactList() {
        return contactList;
    }

    public void addContact(Contact contact) {
        contactList.add(contact);
    }

    public void removeContact(Contact contact) {
        contactList.remove(contact);
    }

    public void resetContactList() {
        contactList.clear();
    }
}
