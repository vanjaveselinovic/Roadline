package com.vanjav.roadline;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

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

	private PointF prevPoint, currPoint;
	private int i;

	private Controller controller;
	private float roadWidth, outlineWidth, lineWidth;

	private Color bgColor = new Color(0.431f, 0.804f, 0.239f, 1);
	private Color outlineColor = new Color(0.388f, 0.78f, 0.224f, 1);
	private Color roadColor = new Color(0.584f, 0.608f, 0.443f, 1);
	private Color lineColor = new Color(1f, 0.792f, 0.271f,1);

	private TextureAtlas textureAtlas;
	private LinkedList<Sprite> treeSprites;

	private SpriteBatch batch;
	private BitmapFont bigFont;
	private ShapeRenderer shapeRenderer;

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

		batch = new SpriteBatch();
		bigFont = new BitmapFont(Gdx.files.internal("postazbold.fnt"));
		bigFont.getData().setScale((height/8) / bigFont.getData().capHeight);
		shapeRenderer = new ShapeRenderer();

		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (gameOver) {
					startNewGame();
					return true;
				}

				currX = screenX;
				currY = height - screenY;

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

		startNewGame();
	}

	public void startNewGame() {
		controller = new Controller(width, height, density, textureAtlas.findRegion("tree4").getRegionWidth());

		roadWidth = controller.getRoadWidth();
		outlineWidth = controller.getOutlineWidth();
		lineWidth = controller.getLineWidth();

		gameStarted = false;
		gameOver = false;
	}

	public void gameOver() {
		if (gameStarted) {
			gameOver = true;
			Gdx.input.vibrate(250);
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

	private String score;
	private Sprite currTree;
	private GlyphLayout glyphLayout;

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

		for (i = 1; i < controller.getTreePoints().size(); i++) {
			currPoint = controller.getTreePoints().get(i);
			currTree = treeSprites.get(((TreePointF) currPoint).size);

			if (currPoint.x < width + currTree.getWidth()) {
				currTree.setPosition(currPoint.x - currTree.getWidth()/2, currPoint.y - currTree.getHeight()/2);
				currTree.draw(batch);
			}
		}

		score = ""+Math.round(controller.getScore()*10.0)/10.0;
		if (score.charAt(0) == '0') score = score.substring(1);

		bigFont.draw(batch, ""+score, 0, height - 50, width, Align.center, false);

		/*
		canvas.drawText("line points: "+controller.getLinePoints().size(), 50, 50, paintDebug);
		canvas.drawText("road points: "+controller.getRoadPoints().size(), 50, 100, paintDebug);
		canvas.drawText("tree points: "+controller.getTreePoints().size(), 50, 200, paintDebug);
		*/

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		textureAtlas.dispose();
	}
}
