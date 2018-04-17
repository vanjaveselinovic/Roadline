package com.vanjav.roadline.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.vanjav.roadline.Roadline;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.height = 2000;
		config.width = 1000;
		new LwjglApplication(new Roadline(), config);
	}
}
