package com.vanjav.roadline;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Roadline extends ApplicationAdapter {
	private int width;
	private int height;
	private float currX, currY;
	private float density, dpi;

	private boolean gameStarted = false;
	private boolean gameOver = false;
	private boolean newHighScore = false;

	private PointF prevPoint, currPoint;
	private int i;

	private Controller controller;
	private float roadWidth, outlineWidth, lineWidth;

	private Color bgColor = new Color(0.42f, 0.796f, 0.235f, 1);
	private Color outlineColor = new Color(0.388f, 0.78f, 0.224f, 1);
	private Color roadColor = new Color(0.584f, 0.608f, 0.443f, 1);
	private Color lineColor = new Color(1f, 0.792f, 0.271f,1);

    private LinePaint yellow = new LinePaint("yellow", new Color(1f, 0.792f, 0.271f,1f), 0);
    private LinePaint white = new LinePaint("white", new Color(0.863f, 0.871f, 0.816f, 1f), 50);
    private LinePaint orange = new LinePaint("orange", new Color(1f, 0.616f, 0.271f, 1f), 60);
    private LinePaint blue = new LinePaint("blue", new Color(0.271f, 0.871f, 1f, 1f), 100);
    private LinePaint pink = new LinePaint("pink", new Color(1f, 0.576f, 0.655f, 1f), 110);
    private LinePaint rainbow = new LinePaint(
            "rainbow",
            new Color[] {
                    new Color(0.961f, 0.294f, 0.286f, 1f),
                    orange.color,
                    yellow.color,
                    new Color(0.42f, 0.796f, 0.235f, 1),
                    blue.color,
                    new Color(0.863f, 0.51f, 0.969f, 1f),
                    pink.color
            },
            200
    );
    private LinePaint pulsar = new LinePaint(
            "pulsar",
            new Color[] {
                    blue.color,
                    white.color,
            },
            210
    );

    private LinePaint[] linePaints = new LinePaint[] {
            yellow,
            white,
            orange,
            blue,
            pink,
            rainbow,
            pulsar
    };
    private Map<String, LinePaint> linePaintMap = new HashMap<String, LinePaint>();

    private Color[] lineColors = new Color[]{};
    private int lineColorsIndex = 0;

    private float crashRadius, currCrashRadius;

	private TextureAtlas textureAtlas;
	private LinkedList<Sprite> treeSprites;
	private Texture handTexture, vibrate1Texture, vibrate0Texture;
	private Sprite handSprite, vibrate1Sprite, vibrate0Sprite;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	private BitmapFont font500, font250, font125, font125flat;
	private float textScale, textHeight;
	private float titlePositionY, instructionsPositionY, scorePositionY, bestPositionY, restartPositionY;
	private float instructionsWidth;

	private float vibrateX1, vibrateY1, vibrateX2, vibrateY2;

	private String instructionsText = "HOLD     DRAG";

	private Preferences preferences;
	private int score;
	private int highScore;
	private boolean vibrate;
	private LinePaint linePaint;
	private String linePaintKey;

	private Texture.TextureFilter filter = Texture.TextureFilter.Linear;

	@Override
	public void create () {
	    width = Gdx.graphics.getWidth();
	    height =  Gdx.graphics.getHeight();
	    density = Gdx.graphics.getDensity();

		dpi = density*160f;

		roadWidth = 0.5f * this.dpi;       // 0.5 inches
		lineWidth = roadWidth * 0.067f;    // 0.067 of road
		outlineWidth = roadWidth * 1.75f;  // 1.75 of road

        crashRadius = outlineWidth;
        currCrashRadius = lineWidth;

        for (LinePaint linePaint : linePaints) {
            linePaintMap.put(linePaint.name, linePaint);
        }

        textureAtlas = new TextureAtlas("trees.atlas");

        treeSprites = new LinkedList<Sprite>();
        Sprite tempSprite;
        for (i = 0; i < textureAtlas.getRegions().size; i++) {
        	textureAtlas.getRegions().get(i).getTexture().setFilter(filter, filter);
        	tempSprite = textureAtlas.createSprite("tree"+i);
        	tempSprite.setSize(tempSprite.getWidth()/350f * (0.75f * (density*160f)), tempSprite.getHeight()/500f * (1.07f * (density*160f)));
        	treeSprites.add(tempSprite);
		}

		handTexture = new Texture(Gdx.files.internal("hand.png"));
        handTexture.setFilter(filter, filter);
        handSprite = new Sprite(handTexture); //scale set after controller
		handSprite.setSize((84f / 73f) * (59f / 84f) * (roadWidth / 2f), (84f / 73f) * (roadWidth / 2f));
		handSprite.setPosition(width/2f - handSprite.getWidth()/2f, height/2f - handSprite.getHeight()/2f);

		instructionsPositionY = height / 2 + roadWidth / 2 - roadWidth / 4;

		batch = new SpriteBatch();

		font500 = new BitmapFont(Gdx.files.internal("font500.fnt"));
		font500.getRegion().getTexture().setFilter(filter, filter);

		textHeight = height/8;
		textScale = textHeight / font500.getData().capHeight;

		font500.getData().setScale(textScale);

		font250 = new BitmapFont(Gdx.files.internal("font250.fnt"));
		font250.getRegion().getTexture().setFilter(filter, filter);
		font250.getData().setScale(textScale);

		font125 = new BitmapFont(Gdx.files.internal("font125.fnt"));
		font125.getRegion().getTexture().setFilter(filter, filter);
		font125.getData().setScale(textScale);

		font125flat = new BitmapFont(Gdx.files.internal("font125flat.fnt")); //scale set after controller
		font125flat.getRegion().getTexture().setFilter(filter, filter);
		font125flat.getData().setScale(roadWidth/2 / font125flat.getData().capHeight);
		instructionsWidth = new GlyphLayout(font125flat, instructionsText).width;

		if (instructionsWidth > width/2) {
		    float handSpriteOldWidth = handSprite.getWidth();
		    float font125flatOldHeight = font125flat.getCapHeight();

			handSprite.setSize(
					(handSprite.getWidth() / instructionsWidth) * (width / 2f),
					(handSprite.getWidth() / instructionsWidth) * (84f / 59f) * (width / 2f)
			);
			handSprite.setPosition(width/2f - handSprite.getWidth()/2f, height/2f - handSprite.getHeight()/2f);

			font125flat.getData().setScale(font125flat.getScaleX() * handSprite.getWidth()/handSpriteOldWidth);
			instructionsPositionY -= ((font125flatOldHeight - font125flat.getCapHeight()) / 2);

			instructionsWidth = new GlyphLayout(font125flat, instructionsText).width;
		}

		titlePositionY = height - textHeight/2;
		scorePositionY = height - textHeight/2;
		bestPositionY = height - textHeight*2;
		restartPositionY = height - bestPositionY;

		vibrateX1 = height/50;
		vibrateY1 = height/50;

		vibrate1Texture = new Texture(Gdx.files.internal("vibrate1.png"));
		vibrate1Texture.setFilter(filter, filter);
		vibrate1Sprite = new Sprite(vibrate1Texture);
		vibrate1Sprite.setScale(textScale);
		vibrate1Sprite.setOrigin(0, 0);
		vibrate1Sprite.setPosition(vibrateX1, vibrateY1);

		vibrate0Texture = new Texture(Gdx.files.internal("vibrate0.png"));
		vibrate0Texture.setFilter(filter, filter);
		vibrate0Sprite = new Sprite(vibrate0Texture);
		vibrate0Sprite.setScale(textScale);
		vibrate0Sprite.setOrigin(0, 0);
		vibrate0Sprite.setPosition(vibrateX1, vibrateY1);

		vibrateX2 = vibrateX1 + vibrate1Sprite.getWidth()*textScale;
		vibrateY2 = vibrateY1 + vibrate1Sprite.getHeight()*textScale;

		shapeRenderer = new ShapeRenderer();

		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				currX = screenX;
				currY = height - screenY;

				if (!gameStarted || gameOver) {
					if (currX >= vibrateX1 && currX <= vibrateX2 && currY >= vibrateY1 && currY <= vibrateY2) {
						vibrate = !vibrate;
						if (vibrate) {
							Gdx.input.vibrate(100);
						}
						preferences.putBoolean("vibrate", vibrate);
						preferences.flush();
						return true;
					}
				}

				if (gameOver) {
					startNewGame();
					return true;
				}

				if (!gameStarted) {
				    if (currY >= height/2f - roadWidth/2f && currY <= height/2f + roadWidth/2f) {
                        gameStarted = true;

                        return true;
                    }
                }

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
		linePaintKey = preferences.getString("linePaintKey", "yellow");
		linePaint = linePaintMap.get(linePaintKey);
		lineColor = linePaint.color;
		if (linePaint.animated) {
		    lineColors = linePaint.colors;
        }

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        startNewGame();
	}

	public void startNewGame() {
		controller = new Controller(
				width,
				height,
				density,
				textureAtlas.findRegion("tree4").getRegionWidth(),
				roadWidth,
				outlineWidth,
				lineWidth,
				instructionsWidth);

		gameStarted = false;
		gameOver = false;
		newHighScore = false;
		lineColorsIndex = 0;
		currCrashRadius = lineWidth;
	}

	public void gameOver() {
		if (gameStarted) {
			gameOver = true;
			if (vibrate) {
				Gdx.input.vibrate(250);
			}

			if (score > highScore) {
				newHighScore = true;
				highScore = score;
				preferences.putInteger("highScore", highScore);
				preferences.flush();
			}
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

		if (linePaint.animated) {
            shapeRenderer.setColor(
                    lineColors[lineColorsIndex].r,
                    lineColors[lineColorsIndex].g,
                    lineColors[lineColorsIndex].b,
                    1f);
            if (gameStarted) {
                lineColorsIndex++;
                if (lineColorsIndex >= linePaint.colors.length) {
                    lineColorsIndex = 0;
                }
            }
        } else {
            shapeRenderer.setColor(lineColor.r, lineColor.g, lineColor.b, 1);
        }
		for (i = 1; i < controller.getLinePoints().size(); i++) {
			prevPoint = controller.getLinePoints().get(i-1);
			currPoint = controller.getLinePoints().get(i);
			if (prevPoint.x < width + lineWidth) {
				shapeRenderer.rectLine(prevPoint.x, prevPoint.y, currPoint.x, currPoint.y, lineWidth);
				shapeRenderer.circle(prevPoint.x, prevPoint.y, lineWidth / 2);
			}
		}
		if (gameOver && currCrashRadius < crashRadius) {
            Gdx.gl.glEnable(GL20.GL_BLEND);

		    shapeRenderer.setColor(1f, 1f, 1f, 1f - currCrashRadius/crashRadius);
		    shapeRenderer.circle(currPoint.x, currPoint.y, currCrashRadius);

		    currCrashRadius += crashRadius/12;
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
		}

		if (!gameStarted || gameOver) {
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
		    if (newHighScore) {
                font250.draw(batch, "NEW BEST", 0, bestPositionY, width, Align.center, false);
            }
            else {
                font250.draw(batch, "BEST "+highScore, 0, bestPositionY, width, Align.center, false);
            }

			font125.draw(batch, "TAP TO RESTART", 0, restartPositionY, width, Align.center, false);
		}

        Gdx.gl.glDisable(GL20.GL_BLEND);

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		textureAtlas.dispose();
	}
}
