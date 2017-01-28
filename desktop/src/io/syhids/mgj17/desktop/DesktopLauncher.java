package io.syhids.mgj17.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import io.syhids.mgj17.Trumpocalypse;

import static io.syhids.mgj17.TrumpocalypseKt.WORLD_HEIGHT;
import static io.syhids.mgj17.TrumpocalypseKt.WORLD_WIDTH;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Trumpocalypse game = new Trumpocalypse();
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        Graphics.DisplayMode desktop = LwjglApplicationConfiguration.getDesktopDisplayMode();
        float MULT = 0.85f;
        float ratio = (float) WORLD_WIDTH / WORLD_HEIGHT;

        config.width = (int) (desktop.height * ratio * MULT);
        config.height = (int) (desktop.height * MULT);
        config.fullscreen = false;
        new LwjglApplication(game, config);
    }
}
