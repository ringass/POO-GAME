package io.github.poo.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;

import io.github.poo.game.Main;
import io.github.poo.game.Objects.Player;

public class ServerScreen implements Screen {
    private final Main game;
    private final Stage stage;
    private final Skin skin;
    private Table topInfo;

    public ServerScreen(Main game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        this.skin = new Skin(Gdx.files.internal("uiskin.json"));
    }

    @Override
    public void show() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        root.add(createLeftColumn()).expand().fill().left().pad(75);
        root.add(createRightColumn()).expand().fill().top().right().pad(75);

        root.setDebug(true);
    }

    private Table createLeftColumn() {
        Table leftColumn = new Table();
        leftColumn.setFillParent(false);

        Table topInfo = new Table();
        topInfo.top().left();
        Label title = new Label("MATCH LOBBY", skin);
        title.setFontScale(2.5f);
        TextButton startButton = new TextButton("Start Match", skin);
        TextButton mapButton = new TextButton("Map", skin);

        topInfo.add(title).left().padBottom(20);
        topInfo.row();
        topInfo.add(startButton).left().padBottom(10).size(200, 100);
        topInfo.row();
        topInfo.add(mapButton).left().padBottom(20).size(90, 65);

        Table bottomInfo = new Table();
        bottomInfo.bottom().left();
        Image mapImage = new Image(new Texture("teste.jpg"));
        TextButton quitButton = new TextButton("Quit", skin);

        bottomInfo.add(mapImage).left().size(600, 400).padBottom(10);
        bottomInfo.row();
        bottomInfo.add(quitButton).left().padBottom(10).size(90, 65);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        leftColumn.add(topInfo).expandY().top().left();
        leftColumn.row();
        leftColumn.add().expand(); // espaço no meio dos dois / maleavel
        leftColumn.row();
        leftColumn.add(bottomInfo).bottom().left();

        // leftColumn.setDebug(true);
        // topInfo.setDebug(true);
        // bottomInfo.setDebug(true);

        return leftColumn;
    }

    private Table createRightColumn() {
        Table rightColumn = new Table();
        rightColumn.setFillParent(false);
        rightColumn.top().right().pad(40);
        Label playersLabel = new Label("4 Player(s) (4 Max)", skin); // receber N de algum lugar
        playersLabel.setFontScale(1.1f);

        topInfo = new Table();
        topInfo.top().right();

        topInfo.add(playersLabel).left().padBottom(10);
        topInfo.row();

        // topInfo.add(createPlayerRow("Arthur",
        // "user-icons/user1.png")).left().padBottom(5);
        // topInfo.row();
        // topInfo.add(createPlayerRow("Davi",
        // "user-icons/user2.png")).left().padBottom(5);
        // topInfo.row();
        // topInfo.add(createPlayerRow("Mario",
        // "user-icons/user3.png")).left().padBottom(5);
        // topInfo.row();
        // topInfo.add(createPlayerRow("Murilo",
        // "user-icons/user4.png")).left().padBottom(5);

        for (Player p : game.getPlayerManager().getAllPlayers()) {
            topInfo.add(createPlayerRow(p.getUsername(), "user-icons/" + p.getAvatar())).left().padBottom(5);
            topInfo.row();
        }

        Table botInfo = new Table();

        Label Alabel = new Label("Socket Server", skin);
        Label Blabel = new Label("Criado pela resenha", skin);

        botInfo.add(Alabel);
        botInfo.add(Blabel);

        rightColumn.add(topInfo).expandY().top().right();
        rightColumn.row();
        rightColumn.add().expand();
        rightColumn.row();
        rightColumn.add(botInfo).bottom().right().padBottom(-7);

        // topInfo.setDebug(true);
        // botInfo.setDebug(true);
        // rightColumn.setDebug(true);

        return rightColumn;
    }

    private Table createPlayerRow(String playerName, String imagePath) {
        Table row = new Table();
        Image image = new Image(new Texture(imagePath));
        TextField playerField = new TextField(playerName, skin);
        row.add(image).padRight(8);
        row.add(playerField).width(300);
        return row;
    }

    //LEMBRAR DE PUXAR NA BOMBA DO SERVER TODA VEZ QUE ENTRAR ALGUEM NO SERVER;

    public void refreshPlayers() {
        topInfo.clear();

        Label playersLabel = new Label(
                game.getPlayerManager().getAllPlayers().size() + " Player(s) (4 Max)",
                skin);
        playersLabel.setFontScale(1.1f);

        topInfo.add(playersLabel).left().padBottom(10);
        topInfo.row();

        for (Player p : game.getPlayerManager().getAllPlayers()) {
            topInfo.add(createPlayerRow(p.getUsername(), "user-icons/" + p.getAvatar())).left().padBottom(5);
            topInfo.row();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0.1f, 0, 1, true);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
