package com.vanjav.roadline;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

import java.util.LinkedList;

public class Roadline extends ApplicationAdapter {
	private int width;
	private int height;
	private float currX, currY;
	private float density;

	private boolean paused = false;
	private boolean gameStarted = false;
	private boolean gameOver = false;
	private boolean firstSetup = true;

	private PointF prevPoint, currPoint;
	private int i;

	private Controller controller;
	private float roadWidth, outlineWidth, lineWidth;

	private Color bgColor = new Color(0.42f, 0.796f, 0.235f, 1);
	private Color outlineColor = new Color(0.388f, 0.78f, 0.224f, 1);
	private Color roadColor = new Color(0.584f, 0.608f, 0.443f, 1);
	private Color lineColor = new Color(1f, 0.792f, 0.271f,1);
	private Color ledgeColor = new Color(0.463f, 0.541f, 0.424f, 1);

	private TextureAtlas textureAtlas;
	private LinkedList<Sprite> treeSprites;
	private Texture handTexture, vibrate1Texture, vibrate0Texture;
	private Sprite handSprite, vibrate1Sprite, vibrate0Sprite;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	private BitmapFont font500, font250, font125, font125flat;
	private float textScale, textHeight;
	private float titlePositionY, instructionsPositionY, scorePositionY, bestPositionY;
	private float instructionsWidth;

	private float vibrateX1, vibrateY1, vibrateX2, vibrateY2;

	private String instructionsText = "HOLD     DRAG";

	private Preferences preferences;
	private int score;
	private int highScore;
	private boolean vibrate;
	private LinePaint linePaint;

	@Override
	public void create () {
	    width = Gdx.graphics.getWidth();
	    height =  Gdx.graphics.getHeight();
	    density = Gdx.graphics.getDensity();

        textureAtlas = new TextureAtlas("trees.atlas");

        treeSprites = new LinkedList<Sprite>();
        Sprite tempSprite;
        for (i = 0; i < textureAtlas.getRegions().size; i++) {
        	tempSprite = textureAtlas.createSprite("tree"+i);
        	tempSprite.setSize(tempSprite.getWidth()/350f * (0.75f * (density*160f)), tempSprite.getHeight()/500f * (1.07f * (density*160f)));
        	treeSprites.add(tempSprite);
		}

		handTexture = new Texture(Gdx.files.internal("hand.png"));
        handSprite = new Sprite(handTexture); //scale set after controller

		batch = new SpriteBatch();

		font500 = new BitmapFont(Gdx.files.internal("font500.fnt"));

		textHeight = height/8;
		textScale = textHeight / font500.getData().capHeight;

		font500.getData().setScale(textScale);

		font250 = new BitmapFont(Gdx.files.internal("font250.fnt"));
		font250.getData().setScale(textScale);

		font125 = new BitmapFont(Gdx.files.internal("font125.fnt"));
		font125.getData().setScale(textScale);

		font125flat = new BitmapFont(Gdx.files.internal("font125flat.fnt")); //scale set after controller

		titlePositionY = height - textHeight/2;
		scorePositionY = height - textHeight/2;
		bestPositionY = height - textHeight*2;

		vibrateX1 = height/50;
		vibrateY1 = height/50;

		vibrate1Texture = new Texture(Gdx.files.internal("vibrate1.png"));
		vibrate1Sprite = new Sprite(vibrate1Texture);
		vibrate1Sprite.setScale(textScale);
		vibrate1Sprite.setOrigin(0, 0);
		vibrate1Sprite.setPosition(vibrateX1, vibrateY1);

		vibrate0Texture = new Texture(Gdx.files.internal("vibrate0.png"));
		vibrate0Sprite = new Sprite(vibrate0Texture);
		vibrate0Sprite.setScale(textScale);
		vibrate0Sprite.setOrigin(0, 0);
		vibrate0Sprite.setPosition(vibrateX1, vibrateY1);

		vibrateX2 = vibrateX1 + vibrate1Sprite.getWidth()*textScale;
		vibrateY2 = vibrateY1 + vibrate1Sprite.getHeight()*textScale;

		instructionsWidth = new GlyphLayout(font125, instructionsText).width;

		shapeRenderer = new ShapeRenderer();

		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				currX = screenX;
				currY = height - screenY;

				if (gameOver) {
					startNewGame();
					return true;
				}

				if (!gameStarted) {
					if (currX >= vibrateX1 && currX <= vibrateX2 && currY >= vibrateY1 && currY <= vibrateY2) {
						vibrate = !vibrate;
						preferences.putBoolean("vibrate", vibrate);
						preferences.flush();
						return true;
					}
				}

				gameStarted = true;

				return true;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				currX = screenX;
				currY = height - screenY;

				return true;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			    if (!gameOver) {
			        gameOver();
			        return true;
                }

				return false;
			}
		});

		preferences = Gdx.app.getPreferences("preferences");
		highScore = preferences.getInteger("highScore", 0);
		vibrate = preferences.getBoolean("vibrate", true);

		startNewGame();
	}

	public void startNewGame() {
		controller = new Controller(width, height, density, textureAtlas.findRegion("tree4").getRegionWidth(), instructionsWidth);

		if (firstSetup) {
			roadWidth = controller.getRoadWidth();
			outlineWidth = controller.getOutlineWidth();
			lineWidth = controller.getLineWidth();

			font125flat.getData().setScale(roadWidth / 2 / font125flat.getData().capHeight);
			handSprite.setSize((84f / 73f) * (59f / 84f) * (roadWidth / 2f), (84f / 73f) * (roadWidth / 2f));
			handSprite.setPosition(width / 2 - handSprite.getWidth() / 2, height / 2 - handSprite.getHeight() / 2);

			instructionsPositionY = height / 2 + roadWidth / 2 - roadWidth / 4;

			firstSetup = false;
		}

		gameStarted = false;
		gameOver = false;
	}

	public void gameOver() {
		if (gameStarted) {
			gameOver = true;
			Gdx.input.vibrate(250);

			if (score > highScore) {
				highScore = score;
				preferences.putInteger("highScore", highScore);
				preferences.flush();
			}

			// show restart menu
		}
	}

	@Override
	public void render () {
		update(Gdx.graphics.getDeltaTime());
		draw();
	}

	private void update(float deltaTimeSeconds) {
		if (gameStarted && !gameOver) {
			controller.addLinePoint(currX, currY);

			if (!controller.update(deltaTimeSeconds)) {
				gameOver();
			}
		}
	}

	private Sprite currTree;

	private void draw() {
		Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		shapeRenderer.setColor(outlineColor.r, outlineColor.g, outlineColor.b, 1);
		for (i = 1; i < controller.getRoadPoints().size(); i++) {
			prevPoint = controller.getRoadPoints().get(i-1);
			currPoint = controller.getRoadPoints().get(i);
			if (prevPoint.x < width + outlineWidth) {
				shapeRenderer.rectLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, outlineWidth);
				shapeRenderer.circle(prevPoint.x, prevPoint.y, outlineWidth/2);
			}
		}

		shapeRenderer.setColor(roadColor.r, roadColor.g, roadColor.b, 1);
		for (i = 1; i < controller.getRoadPoints().size(); i++) {
			prevPoint = controller.getRoadPoints().get(i-1);
			currPoint = controller.getRoadPoints().get(i);
			if (prevPoint.x < width + roadWidth) {
				shapeRenderer.rectLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, roadWidth);
				shapeRenderer.circle(prevPoint.x, prevPoint.y, roadWidth / 2);
			}
		}

		shapeRenderer.setColor(lineColor.r, lineColor.g, lineColor.b, 1);
		for (i = 1; i < controller.getLinePoints().size(); i++) {
			prevPoint = controller.getLinePoints().get(i-1);
			currPoint = controller.getLinePoints().get(i);
			if (prevPoint.x < width + lineWidth) {
				shapeRenderer.rectLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, lineWidth);
				shapeRenderer.circle(prevPoint.x, prevPoint.y, lineWidth / 2);
			}
		}

		shapeRenderer.end();

		batch.begin();

		if (!gameStarted) {
			font125flat.draw(batch, instructionsText, 0, instructionsPositionY, width, Align.center, false);
			handSprite.draw(batch);
		}

		for (i = 1; i < controller.getTreePoints().size(); i++) {
			currPoint = controller.getTreePoints().get(i);
			currTree = treeSprites.get(((TreePointF) currPoint).size);

			if (currPoint.x < width + currTree.getWidth()) {
				currTree.setPosition(currPoint.x - currTree.getWidth()/2, currPoint.y - currTree.getHeight()/2);
				currTree.draw(batch);
			}
		}

		score = (int) Math.floor(controller.getScore());

		if (!gameStarted) {
			font250.draw(batch, "ROADLINE", 0, titlePositionY, width, Align.center, false);
			if (vibrate) {
				vibrate1Sprite.draw(batch);
			}
			else {
				vibrate0Sprite.draw(batch);
			}
		}

		if (gameStarted) {
			font500.draw(batch, ""+score, 0, scorePositionY, width, Align.center, false);
		}

		if (gameOver) {
			font250.draw(batch, "BEST "+highScore, 0, bestPositionY, width, Align.center, false);
		}

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		textureAtlas.dispose();
	}
}
