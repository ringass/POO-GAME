package io.github.buraconcio.Objects.Game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.CircleShape;

import io.github.buraconcio.Utils.Common.Constants;
import io.github.buraconcio.Utils.Common.PhysicsEntity;
import io.github.buraconcio.Utils.Managers.PhysicsManager;
import io.github.buraconcio.Utils.Managers.PlayerManager;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Ball extends PhysicsEntity {
    private Player player;
    private Group labelGroup;
    private Vector2 mouseMovement;

    private float zSpeed = 0f;
    private float z = 0f;
    private static final float gravity = 9.8f;
    private boolean isAirborne = false;
    private boolean isAlive = true;

    private static final float angDamp = 1f;
    private static final float linDamp = 0.5f;

    private Sprite endSprite;
    private Sprite segmentSprite;
    private Sprite tipSprite;

    // para a phase de select_obj
    private boolean canInteract = false;

    public Ball(Vector2 pos, float d, Player player, String skinBall) {
        super(pos, new Vector2(d, d), skinBall);

        body.setType(BodyType.DynamicBody);
        body.setLinearDamping(linDamp);
        body.setAngularDamping(angDamp);

        CircleShape circle = new CircleShape();
        circle.setRadius(d / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;

        body.createFixture(fixtureDef);
        circle.dispose();

        Skin skinLabel = new Skin(Gdx.files.internal("fonts/pixely/labels/labelPixely.json"));
        Label labelUserName = new Label(player.getUsername(), skinLabel, "labelPixelyWhite32");
        labelUserName.setColor(0, 0, 0, 1);
        labelUserName.setAlignment(Align.center);
        labelUserName.pack();

        labelGroup = new Group(); // melhor jeito de escalar texto ... zuado
        labelGroup.addActor(labelUserName);
        labelGroup.setScale(0.02f);

        PhysicsManager.getInstance().addToStage(labelGroup);

        this.player = player;

        mouseMovement = new Vector2(0, 0);

        // shooting segment sprites
        Texture texture = new Texture(Gdx.files.internal("shooting-guide/shootingGuideEnd.png"));
        endSprite = new Sprite(texture);

        texture = new Texture(Gdx.files.internal("shooting-guide/shootingGuideSegment.png"));
        segmentSprite = new Sprite(texture);

        texture = new Texture(Gdx.files.internal("shooting-guide/shootingGuideTip.png"));
        tipSprite = new Sprite(texture);

    }

    public boolean isStill() {
        return body.getLinearVelocity().len() < Constants.STILL_TOLERANCE;
    }

    public void applyImpulse(Vector2 impulse) {
        if (!canInteract) return;

        body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
    }

    public Vector2 calculateImpulse(Vector2 mouse1, Vector2 mouse2) {
        Vector2 diff = mouse1.sub(mouse2);

        float magnitude = (diff.len() / Constants.MAX_IMPULSE_DISTANCE) * Constants.MAX_IMPULSE;
        if (magnitude > Constants.MAX_IMPULSE)
            magnitude = Constants.MAX_IMPULSE;

        diff.setLength(magnitude);

        return diff;
    }

    public void jump(float impulse) {
        // o que fazer se cai em um obstaculo / parede ?
        zSpeed = impulse;
        isAirborne = true;

        body.getFixtureList().forEach(fixture -> fixture.setSensor(true));
        body.setAngularDamping(0f);
        body.setLinearDamping(0f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        zSpeed -= gravity * delta;
        z += zSpeed;

        if (z <= 2f) { // arbitrario
            isAirborne = false;

            body.getFixtureList().forEach(fixture -> fixture.setSensor(false));
        }

        if (z <= 0) {
            z = 0;
            zSpeed = 0;

            body.setAngularDamping(angDamp);
            body.setLinearDamping(linDamp);
        }

        animacao.setOrigin(getWidth() / 2, getHeight() / 2);
        animacao.setScale(1 + z / 15);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        labelGroup.setPosition(
                getX() - labelGroup.getWidth() / 2,
                getY() + getHeight() + 0.5f);

        if (canInteract && mouseMovement.len() > 0.01f && isStill()) {
            final float segmentLength = 1f;

            endSprite.setSize(segmentLength, segmentLength);
            endSprite.setOriginCenter();

            // prioritize positioning end sprite center at end of line seg
            Vector2 center = new Vector2(getX() + getWidth() / 2, getY() + getHeight() / 2);

            endSprite.setPosition(center.x - mouseMovement.x - endSprite.getWidth() / 2,
                    center.y - mouseMovement.y - endSprite.getHeight() / 2);
            endSprite.setRotation(mouseMovement.angleDeg());
            endSprite.draw(batch);

            // prioritize positioning tip to represent actual impulse
            Vector2 impulse = calculateImpulse(new Vector2(0f, 0f), mouseMovement).scl(0.1f);

            tipSprite.setSize(segmentLength, segmentLength);
            tipSprite.setOriginCenter();

            tipSprite.setPosition(center.x - impulse.x - tipSprite.getWidth() / 2,
                    center.y - impulse.y - tipSprite.getHeight() / 2);
            tipSprite.setRotation(mouseMovement.angleDeg());
            tipSprite.draw(batch);

            Vector2 seg = new Vector2(tipSprite.getX() - endSprite.getX(), tipSprite.getY() - endSprite.getY());
            segmentSprite.setSize(seg.len(), segmentLength);
            segmentSprite.setOriginCenter();
            segmentSprite.setRotation(mouseMovement.angleDeg());

            Vector2 pos = new Vector2(endSprite.getX() + endSprite.getWidth() / 2,
                    endSprite.getY() + endSprite.getHeight() / 2);
            pos.add(seg.scl(0.5f));
            pos.sub(new Vector2(segmentSprite.getWidth() * 0.5f, segmentSprite.getHeight() * 0.5f));
            segmentSprite.setPosition(pos.x, pos.y);
            segmentSprite.draw(batch);
        }

        super.draw(batch, parentAlpha);
    }

    public void enterHole() {
        body.setLinearVelocity(new Vector2(0f, 0f));
        body.setAwake(false);

        labelGroup.setVisible(false);

        setAlive(false);
    }

    public void setShootingGuide(Vector2 mouse1, Vector2 mouse2) {
        // mouseMovement = mouse1.sub(mouse2);
        mouseMovement.x = mouse1.x - mouse2.x;
        mouseMovement.y = mouse1.y - mouse2.y;
    }

    public void resetShootingGuide() {
        mouseMovement.x = mouseMovement.y = 0;
    }

    public void setAngle(float angle) {
        body.setTransform(body.getPosition(), body.getTransform().getRotation() + angle); // 90 em rad
    }

    public void setPos(Vector2 pos) {
        body.setTransform(pos, body.getTransform().getRotation());
    }

    public void setVelocity(Vector2 vel) {
        body.setLinearVelocity(vel);
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAirborne() {
        return isAirborne;
    }

    public void setCanInteract(boolean canInteract)
    {
        this.canInteract = canInteract;
        setAlive(canInteract);
    }

    public boolean canInteract()
    {
        return canInteract;
    }

    public boolean isAlive(){
        return isAlive;
    }

    public void resetLinearDamping() {
        body.setLinearDamping(linDamp);
    }

    public void setAlive(boolean flag){ //encaixar isso em uma funcao de renascer elas no spawn point
        isAlive = flag;
        canInteract = flag;
        labelGroup.setVisible(flag);
        setVisible(flag);
        animacao.setVisible(flag);

        body.getFixtureList().forEach(fixture -> fixture.setSensor(!flag));

        if (flag == false && PlayerManager.getInstance().getLocalPlayer().getId() != player.getId()) {
            teleport(new Vector2(-100f, -100f));
        }
    }

    public void addToStage(Stage stage) {
        super.addToStage(stage);

        stage.addActor(labelGroup);
    }
}
