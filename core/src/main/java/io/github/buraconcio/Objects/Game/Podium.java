package io.github.buraconcio.Objects.Game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import io.github.buraconcio.Utils.Common.Auxiliaries;
import io.github.buraconcio.Utils.Common.PhysicsEntity;

public class Podium extends PhysicsEntity {
    public enum Type {
        bronze,
        silver,
        gold
    }

    private static final Vector2 size = new Vector2(2f, -1f);
    private Vector2 startingPos;

    public Podium(Vector2 pos, Type type) {
        super(pos, size, "backgrounds/victoryScreen/" + getNameFromType(type) + ".png");

        startingPos = pos;

        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(size.x/2, getHeight()/2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = false;
        fixtureDef.shape = polygonShape;

        body.createFixture(fixtureDef);
        polygonShape.dispose();

        body.setType(BodyType.KinematicBody);
        System.out.println(getHeight());
        body.setLinearVelocity(new Vector2(0f, 4f));
        // getHeight()/animacao.getNumFrames()
    }

    private static String getNameFromType(Type type) {
        String spriteName = switch(type) {
            case gold -> "1";
            case silver -> "2";
            case bronze -> "3";
            default -> "";
        };

        return spriteName;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (getY() >= startingPos.y + getHeight()/2)
            body.setLinearVelocity(new Vector2(0f, 0f));
    }
}
