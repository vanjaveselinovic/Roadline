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
	private boolean colorSelectionOpen = false;

	private PointF prevPoint, currPoint;
	private int i;

	private Controller controller;
	private float roadWidth, outlineWidth, lineWidth;

	private Color bgColor = new Color(0.42f, 0.796f, 0.235f, 1);
	private Color outlineColor = new Color(0.388f, 0.78f, 0.224f, 1);
	private Color roadColor = new Color(0.584f, 0.608f, 0.443f, 1);
	private Color lineColor = new Color(1f, 0.792f, 0.271f,1);
	private Color shadowColor = new Color(0f, 0.282f, 0.353f, 1f);

    private LinePaint yellow = new LinePaint("yellow", new Color(1f, 0.792f, 0.271f,1f), 0);
    private LinePaint white = new LinePaint("white", new Color(0.863f, 0.871f, 0.816f, 1f), 50);
    private LinePaint orange = new LinePaint("orange", new Color(1f, 0.616f, 0.271f, 1f), 75);
    private LinePaint blue = new LinePaint("blue", new Color(0.271f, 0.871f, 1f, 1f), 100);
    private LinePaint pink = new LinePaint("pink", new Color(1f, 0.576f, 0.655f, 1f), 125);
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
                    new Color(1f, 1f, 1f, 1f),
                    new Color(0f, 0f, 0f, 1f)
            },
            300
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

    private Color[] rainbowColors = rainbow.colors;
    private int rainbowColorsIndex = 0;

    private Color[] pulsarColors = pulsar.colors;
    private int pulsarColorsIndex = 0;

    private float crashRadius, currCrashRadius;

	private TextureAtlas treesAtlas, otherAtlas;
	private LinkedList<Sprite> treeSprites;
	private float biggestTreeWidth;
	private Sprite handSprite, vibrate1Sprite, vibrate0Sprite, colorBaseSprite, colorHandSprite, lockSprite;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	private BitmapFont font500, font250, font125, font125flat, font75flat;
	private float textScale, textHeight;
	private float titlePositionY, instructionsPositionY, scorePositionY, bestPositionY, restartPositionY;
	private float instructionsWidth;

	private float vibrateX1, vibrateY1, vibrateX2, vibrateY2;
    private float colorCenterX, colorCenterY, colorRadius, colorX1, colorY1, colorX2, colorY2;
    private float colorOutlineX, colorRoadX, colorLineX, numColors, colorLineHeight, lockX, lockY, lockWidth, lockHeight;

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

        treesAtlas = new TextureAtlas("trees.atlas");

        treeSprites = new LinkedList<Sprite>();
        Sprite tempSprite;
        for (i = 0; i < treesAtlas.getRegions().size; i++) {
        	treesAtlas.getRegions().get(i).getTexture().setFilter(filter, filter);
        	tempSprite = treesAtlas.createSprite("tree"+i);
        	tempSprite.setSize(tempSprite.getWidth()/350f * (0.75f * (density*160f)), tempSprite.getHeight()/500f * (1.07f * (density*160f)));
        	treeSprites.add(tempSprite);
		}

		biggestTreeWidth = treeSprites.get(4).getWidth();

		otherAtlas = new TextureAtlas("other.atlas");
        for (i = 0; i < otherAtlas.getRegions().size; i++) {
            otherAtlas.getRegions().get(i).getTexture().setFilter(filter, filter);
        }

        handSprite = otherAtlas.createSprite("hand");
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

		vibrateX1 = 0;
		vibrateY1 = 0;

		vibrate1Sprite = otherAtlas.createSprite("vibrate1");
		vibrate1Sprite.setScale(textScale);
		vibrate1Sprite.setOrigin(0, 0);
		vibrate1Sprite.setPosition(vibrateX1, vibrateY1);

		vibrate0Sprite = otherAtlas.createSprite("vibrate0");
		vibrate0Sprite.setScale(textScale);
		vibrate0Sprite.setOrigin(0, 0);
		vibrate0Sprite.setPosition(vibrateX1, vibrateY1);

		vibrateX2 = vibrateX1 + vibrate1Sprite.getWidth()*textScale;
		vibrateY2 = vibrateY1 + vibrate1Sprite.getHeight()*textScale;

		colorBaseSprite  = otherAtlas.createSprite("colorbase");
		colorBaseSprite.setScale(textScale);
		colorBaseSprite.setOrigin(0, 0);
		colorBaseSprite.setPosition(width - colorBaseSprite.getWidth()*colorBaseSprite.getScaleX(), 0);

        colorHandSprite  = otherAtlas.createSprite("colorhand");
        colorHandSprite.setScale(textScale);
        colorHandSprite.setOrigin(0, 0);
        colorHandSprite.setPosition(width - colorHandSprite.getWidth()*colorHandSprite.getScaleX(), 0);

        colorCenterX = colorBaseSprite.getX() + colorBaseSprite.getWidth()*colorBaseSprite.getScaleX()/2;
        colorCenterY = colorBaseSprite.getY() + colorBaseSprite.getHeight()*colorBaseSprite.getScaleY()/2;
        colorRadius = 128*textScale/2;

        colorX1 = colorBaseSprite.getX();
        colorY1 = colorBaseSprite.getY();
        colorX2 = colorX1 + colorBaseSprite.getWidth()*textScale;
        colorY2 = colorY2 + colorBaseSprite.getHeight()*textScale;

        colorOutlineX = width - outlineWidth;
        colorRoadX = width - outlineWidth + (outlineWidth - roadWidth)/2;
        colorLineX = width - outlineWidth + (outlineWidth - lineWidth)/2;
        numColors = linePaints.length;
        colorLineHeight = height/linePaints.length;

        lockSprite = otherAtlas.createSprite("lock");
        lockSprite.setScale(textScale);
        lockSprite.setOrigin(0, 0);

        lockWidth = lockSprite.getWidth()*textScale;
        lockHeight = lockSprite.getHeight()*textScale;
        lockX = width - outlineWidth + (outlineWidth - lockWidth)/2;

        font75flat = new BitmapFont(Gdx.files.internal("font125flat.fnt")); //scale set after controller
        font125flat.getRegion().getTexture().setFilter(filter, filter);
        font125flat.getData().setScale(lockHeight/1.5f / font75flat.getData().capHeight);

        lockY = (colorLineHeight - (lockHeight + font75flat.getData().capHeight + lineWidth))/2 + lockHeight + lineWidth/2;

		shapeRenderer = new ShapeRenderer();

		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				currX = screenX;
				currY = height - screenY;

				if (!gameStarted || gameOver) {
				    if (!colorSelectionOpen) {
                        if (currX >= vibrateX1 && currX <= vibrateX2 && currY >= vibrateY1 && currY <= vibrateY2) {
                            vibrate = !vibrate;
                            if (vibrate) {
                                Gdx.input.vibrate(100);
                            }

                            preferences.putBoolean("vibrate", vibrate);
                            preferences.flush();

                            return true;
                        }

                        if (currX >= colorX1 && currX <= colorX2 && currY >= colorY1 && currY <= colorY2) {
                            colorSelectionOpen = true;

                            rainbowColorsIndex = 0;
                            pulsarColorsIndex = 0;

                            return true;
                        }
                    }
                    else {
				        if (currX < width - outlineWidth) {
				            colorSelectionOpen = false;

				            lineColor = linePaint.color;

				            return true;
                        }
                        else {
				            return false;
                        }
                    }
				}

				if (gameOver) {
					startNewGame();
					return true;
				}

				if (!gameStarted) {
				    if (currY >= height/2f - roadWidth/2f && currY <= height/2f + roadWidth/2f) {
                        gameStarted = true;
                        lineColorsIndex = 0;

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
                (int) biggestTreeWidth,
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
		    lineColor = lineColors[lineColorsIndex];

            lineColorsIndex++;
            if (lineColorsIndex >= linePaint.colors.length) {
                lineColorsIndex = 0;
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

			colorBaseSprite.draw(batch);

            if (!colorSelectionOpen) {

                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                shapeRenderer.setColor(lineColor.r, lineColor.g, lineColor.b, 1);
                shapeRenderer.circle(colorCenterX, colorCenterY, colorRadius);

                shapeRenderer.end();
                batch.begin();

                colorHandSprite.draw(batch);
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

		if (colorSelectionOpen) {
		    Gdx.gl.glEnable(GL20.GL_BLEND);

		    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		    shapeRenderer.setColor(shadowColor.r, shadowColor.g, shadowColor.b, 0.5f);
		    shapeRenderer.rect(0, 0, width, height);

		    shapeRenderer.setColor(outlineColor.r, outlineColor.g, outlineColor.b, 1f);
		    shapeRenderer.rect(colorOutlineX, 0, outlineWidth, height);

		    shapeRenderer.setColor(roadColor.r, roadColor.g, roadColor.b, 1f);
		    shapeRenderer.rect(colorRoadX, 0, roadWidth, height);

		    for (i = 0; i < numColors; i++) {
		        lineColor = linePaints[i].color;

                if (i == 5) { //rainbow
                    lineColor = rainbowColors[rainbowColorsIndex];

                    rainbowColorsIndex++;
                    if (rainbowColorsIndex >= rainbow.colors.length) {
                        rainbowColorsIndex = 0;
                    }
                }
                else if (i == 6) {
                    lineColor = pulsarColors[pulsarColorsIndex];

                    pulsarColorsIndex++;
                    if (pulsarColorsIndex >= pulsar.colors.length) {
                        pulsarColorsIndex = 0;
                    }
                }

		        shapeRenderer.setColor(lineColor.r, lineColor.g, lineColor.b, 1f);
		        shapeRenderer.rect(colorLineX, i * colorLineHeight, lineWidth, colorLineHeight);
            }

		    shapeRenderer.end();

		    batch.begin();

		    for (i = 0; i < numColors; i++) {
		        if (highScore < linePaints[i].pointsToUnlock) {
		            lockSprite.setPosition(lockX, i*colorLineHeight + lockY + lineWidth/2);
		            lockSprite.draw(batch);

		            font125flat.draw(
		                    batch,
                            ""+linePaints[i].pointsToUnlock,
                            colorOutlineX,
                            i*colorLineHeight + lockY - lineWidth/2,
                            outlineWidth,
                            Align.center,
                            false
                    );
                }
            }

		    batch.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
	}

	@Override
	public void dispose () {
		batch.dispose();
		treesAtlas.dispose();
		otherAtlas.dispose();
	}
}
