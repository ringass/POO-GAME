package io.github.buraconcio.Objects.Obstacles;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.buraconcio.Utils.Common.Auxiliaries;

public class MetalBox extends Obstacle {
    private static String imagePath = "obstacles/box/metal/metal32x32.png";
    private static String jsonPath = "obstacles/box/metal/metal32x32.json";

    private static Vector2 size = new Vector2(3f, 3f);

    public MetalBox(Vector2 pos) {
        super(pos, size, Auxiliaries.animationFromFiles(imagePath, jsonPath));

        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(size.x/2, size.y/2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);

        polygonShape.dispose();
    }


    @Override
    public void place() {
        super.place();
        body.getFixtureList().forEach(fixture -> fixture.setSensor(false));
    }
}
