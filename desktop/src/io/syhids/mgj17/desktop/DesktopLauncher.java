package io.syhids.mgj17.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.syhids.mgj17.Trumpocalypse;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Trumpocalypse game = new Trumpocalypse();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		float MULT = 2.4f;
		config.width = (int)(game.WORLD_WIDTH*MULT);
		config.height = (int)(game.WORLD_HEIGHT*MULT);
		config.fullscreen = false;
		new LwjglApplication(game, config);
	}
}
