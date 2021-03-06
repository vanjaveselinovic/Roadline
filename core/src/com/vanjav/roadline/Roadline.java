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
	private boolean useGameOverLinePaint = false;

	private PointF prevPoint, currPoint;
	private int i;

	private Controller controller;
	private float roadWidth, outlineWidth, lineWidth;

	private Color bgColor = new Color(0.42f, 0.796f, 0.235f, 1);
	private Color outlineColor = new Color(0.388f, 0.78f, 0.224f, 1);
	private Color roadColor = new Color(0.584f, 0.608f, 0.443f, 1);
	private Color lineColor = new Color(1f, 0.792f, 0.271f,1);
	private Color gameOverLineColor = lineColor;
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
			10,
			200
	);
	private LinePaint pulsar = new LinePaint(
			"pulsar",
			new Color[] {
					new Color(1f, 1f, 1f, 1f),
					new Color(0f, 0f, 0f, 1f)
			},
			50,
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

	private Color[] gameOverLineColors = new Color[]{};
	private int gameOverLineColorsIndex = 0;

	private Color[] rainbowColors = rainbow.colors;
	private int rainbowColorsIndex = 0;

	private Color[] pulsarColors = pulsar.colors;
	private int pulsarColorsIndex = 0;

	private float crashRadius, currCrashRadius;

	private TextureAtlas treesAtlas, otherAtlas;
	private LinkedList<Sprite> treeSprites;
	private float biggestTreeWidth;
	private Sprite handSprite, vibrate1Sprite, vibrate0Sprite;
	private Sprite colorBaseSprite, colorHandSprite, lockSprite, unlockedSprite, colorLockSprite, colorUnlockedSprite;
	private Sprite bestSprite, newBestSprite, vvGamesLogoSprite;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	private BitmapFont font500, font250, font125, font125flat, font75flat, font50flat;
	private float textScale, textHeight;
	private float titlePositionY, instructionsPositionY, scorePositionY;
    private float bestPositionY, bestSpritePositionY, bestSpritePositionX, bestPositionX, restartPositionY;
	private float instructionsWidth, bestSpriteWidth;

	private float vibrateX1, vibrateY1, vibrateX2, vibrateY2;
	private float colorCenterX, colorCenterY, colorRadius, colorX1, colorY1, colorX2, colorY2, copyrightY;
	private float colorOutlineX, colorRoadX, colorLineX, numColors, colorLineHeight, lockX, lockY, lockWidth, lockHeight;

	private String instructionsText = "HOLD     DRAG";

	private Preferences preferences;
	private int score;
	private int highScore;
	private boolean vibrate;
	private LinePaint linePaint, gameOverLinePaint;
	private String linePaintKey;

	private Texture.TextureFilter filter = Texture.TextureFilter.Linear;

	private int inputPaintIndex;

	private int firstToUnlock = -1;
	private int lastToUnlock = -1;
	private int lastAlreadyUnlocked = -1;

	private int unlockFrameCount = 0;

	private boolean showUnlockTease = false;
	private int unlockTeaseFrameCount = 0;

	private int colorSelectionFrameCount = 0;
	private float colorSelectionOffset = 0;
	private boolean colorSelectionClosing = false;
	private float colorSelectionShadowAlpha = 0;

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

		font50flat = new BitmapFont(Gdx.files.internal("font125flat.fnt"));
		font50flat.getRegion().getTexture().setFilter(filter, filter);
		font50flat.getData().setScale(textScale*0.4f);

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

		vibrateX1 = textHeight/4;
		vibrateY1 = textHeight/4;

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
		colorBaseSprite.setPosition(width - colorBaseSprite.getWidth()*colorBaseSprite.getScaleX() - vibrateX1, vibrateY1);

		colorHandSprite  = otherAtlas.createSprite("colorhand");
		colorHandSprite.setScale(textScale);
		colorHandSprite.setOrigin(0, 0);
		colorHandSprite.setPosition(width - colorHandSprite.getWidth()*colorHandSprite.getScaleX() - vibrateX1, vibrateY1);

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
		font75flat.getRegion().getTexture().setFilter(filter, filter);
		font75flat.getData().setScale(lockHeight/1.5f / font75flat.getData().capHeight);

		lockY = colorLineHeight/2 - (lockHeight - font75flat.getCapHeight())/2;

		unlockedSprite = otherAtlas.createSprite("unlocked");
		unlockedSprite.setScale(textScale);
		unlockedSprite.setOrigin(0, 0);

		colorLockSprite = otherAtlas.createSprite("colorlock");
		colorLockSprite.setScale(textScale);
		colorLockSprite.setOrigin(0, 0);
		colorLockSprite.setPosition(width - colorLockSprite.getWidth()*colorLockSprite.getScaleX() - vibrateX1, vibrateY1);

		colorUnlockedSprite = otherAtlas.createSprite("colorunlocked");
		colorUnlockedSprite.setScale(textScale);
		colorUnlockedSprite.setOrigin(0, 0);
		colorUnlockedSprite.setPosition(width - colorUnlockedSprite.getWidth()*colorUnlockedSprite.getScaleX() - vibrateX1, vibrateY1);

		bestSprite = otherAtlas.createSprite("best");
		bestSprite.setScale(textScale);
		bestSprite.setOrigin(0, 0);
        bestSpriteWidth = bestSprite.getWidth()*bestSprite.getScaleX();
        bestSpritePositionY = height - textHeight*2f - font250.getCapHeight() - 15*textScale;

		newBestSprite = otherAtlas.createSprite("newbest");
		newBestSprite.setScale(textScale);
		newBestSprite.setOrigin(0, 0);
		newBestSprite.setPosition(width/2f - newBestSprite.getWidth()*newBestSprite.getScaleX()/2f, bestSpritePositionY);

		vvGamesLogoSprite = otherAtlas.createSprite("vvgameslogo");
		vvGamesLogoSprite.setScale(textScale);
		vvGamesLogoSprite.setOrigin(0, 0);
		vvGamesLogoSprite.setPosition(
		        width/2f - vvGamesLogoSprite.getWidth()*vvGamesLogoSprite.getScaleX()/2f,
                vibrateY1 + vibrate1Sprite.getHeight()*vibrate1Sprite.getScaleY()/2 + font75flat.getCapHeight()/4f);
		copyrightY = vibrateY1 + vibrate1Sprite.getHeight()*vibrate1Sprite.getScaleY()/2 - font75flat.getCapHeight()/4f;

		shapeRenderer = new ShapeRenderer();

		Gdx.input.setInputProcessor(new InputAdapter(){
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				currX = screenX;
				currY = height - screenY;

				if (!gameStarted || gameOver) {
					if (!colorSelectionOpen) {
						//vibrate toggle button
						if (currX >= vibrateX1 && currX <= vibrateX2 && currY >= vibrateY1 && currY <= vibrateY2) {
							vibrate = !vibrate;
							if (vibrate) {
								Gdx.input.vibrate(100);
							}

							preferences.putBoolean("vibrate", vibrate);
							preferences.flush();

							return true;
						}

						//color selection button
						if (currX >= colorX1 && currX <= colorX2 && currY >= colorY1 && currY <= colorY2
								&& !colorSelectionClosing) {
							colorSelectionOpen = true;

							rainbowColorsIndex = 0;
							pulsarColorsIndex = 0;

							colorSelectionFrameCount = 0;
							colorSelectionOffset = 0;

							return true;
						}
					}
					else { //colorSelectionOpen
						//shadow
						if (currX < width - outlineWidth) {
							colorSelectionOpen = false;

							lineColor = linePaint.color;

							lastAlreadyUnlocked = lastToUnlock;
							firstToUnlock = -1;
							lastToUnlock = -1;
							unlockFrameCount = 0;
							unlockTeaseFrameCount = 0;
							showUnlockTease = false;
							colorSelectionFrameCount = 0;
							colorSelectionOffset = 0;
							colorSelectionClosing = true;

							return true;
						}
						//color selection
						else {
							for (inputPaintIndex = 0; inputPaintIndex < numColors; inputPaintIndex++) {
								if (currY > inputPaintIndex*colorLineHeight
										&& currY < inputPaintIndex*colorLineHeight + colorLineHeight) {
									if (highScore >= linePaints[inputPaintIndex].pointsToUnlock) { //unlocked
										colorSelectionOpen = false;

										if (gameOver && !useGameOverLinePaint) {
											gameOverLinePaint = linePaint;
											gameOverLineColor = gameOverLinePaint.color;
											gameOverLineColorsIndex = lineColorsIndex;
											if (gameOverLinePaint.animated) {
												gameOverLineColors = gameOverLinePaint.colors;
											}

											useGameOverLinePaint = true;
										}

										linePaint = linePaints[inputPaintIndex];
										lineColor = linePaint.color;
										if (linePaint.animated) {
											lineColors = linePaint.colors;
										}
										lineColorsIndex = 0;

										preferences.putString("linePaintKey", linePaint.name);
										preferences.flush();

										lastAlreadyUnlocked = lastToUnlock;
										firstToUnlock = -1;
										lastToUnlock = -1;
										unlockFrameCount = 0;
										unlockTeaseFrameCount = 0;
										showUnlockTease = false;
										colorSelectionFrameCount = 0;
										colorSelectionOffset = 0;
										colorSelectionClosing = true;

										return true;
									}
									else { //locked
										return false;
									}
								}
							}

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

		setBestPosition();

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

		useGameOverLinePaint = false;

		unlockFrameCount = 0;

		showUnlockTease = false;
		unlockTeaseFrameCount = 0;

		colorSelectionFrameCount = 0;
		colorSelectionOffset = 0;
	}

	int gameOverLinePaintIndex;

	public void gameOver() {
		if (gameStarted) {
			gameOver = true;

			if (vibrate) {
				Gdx.input.vibrate(250);
			}

			if (score > highScore) {
				for (gameOverLinePaintIndex = 0; gameOverLinePaintIndex < numColors; gameOverLinePaintIndex++) {
					if (highScore >= linePaints[gameOverLinePaintIndex].pointsToUnlock) {
						lastAlreadyUnlocked = gameOverLinePaintIndex;
					}
				}

				newHighScore = true;
				highScore = score;

				preferences.putInteger("highScore", highScore);
				preferences.flush();

				firstToUnlock = -1;
				lastToUnlock = -1;

				for (gameOverLinePaintIndex = lastAlreadyUnlocked + 1; gameOverLinePaintIndex < numColors; gameOverLinePaintIndex++) {
					if (highScore >= linePaints[gameOverLinePaintIndex].pointsToUnlock) {
						if (firstToUnlock == -1) {
							firstToUnlock = gameOverLinePaintIndex;
						}
						lastToUnlock = gameOverLinePaintIndex;
					}
				}

				if (firstToUnlock > -1) {
					showUnlockTease = true;
				}

				setBestPosition();
			}
		}
	}

	public void setBestPosition() {
		bestSpritePositionX = (width - bestSpriteWidth*1.1f - new GlyphLayout(font250, ""+highScore).width)/2f;
		bestSprite.setPosition(bestSpritePositionX, bestSpritePositionY);
		bestPositionX = bestSpritePositionX + bestSpriteWidth*1.1f;
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
	private Color colorLineColor;

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

		if (useGameOverLinePaint) {
			if (gameOverLinePaint.animated) {
				gameOverLineColor = gameOverLineColors[gameOverLineColorsIndex];

				gameOverLineColorsIndex++;
				if (gameOverLineColorsIndex >= gameOverLinePaint.colors.length) {
					gameOverLineColorsIndex = 0;
				}
			}
			shapeRenderer.setColor(gameOverLineColor.r, gameOverLineColor.g, gameOverLineColor.b, 1);
		}
		else {
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

			vvGamesLogoSprite.draw(batch);
			font50flat.draw(batch, "©2018 VV GAMES", 0, copyrightY, width, Align.center, false);

			colorBaseSprite.draw(batch);

			if (!colorSelectionOpen || (colorSelectionOpen && colorSelectionFrameCount < 10)) {

				batch.end();
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

				shapeRenderer.setColor(lineColor.r, lineColor.g, lineColor.b, 1);
				shapeRenderer.circle(colorCenterX, colorCenterY, colorRadius);

				shapeRenderer.end();
				batch.begin();

				if (showUnlockTease) {
					if (unlockTeaseFrameCount < 15) {
						colorLockSprite.draw(batch);
					}
					else {
						colorUnlockedSprite.draw(batch);
					}

					unlockTeaseFrameCount++;
					if (unlockTeaseFrameCount >= 60) {
						unlockTeaseFrameCount = 0;
					}
				} else {
					colorHandSprite.draw(batch);
				}
			}
		}

		if (gameStarted && !newHighScore) {
			font500.draw(batch, ""+score, 0, scorePositionY, width, Align.center, false);
		}
		if (gameStarted && newHighScore) {
			font500.draw(batch, ""+highScore, 0, scorePositionY, width, Align.center, false);
		}

		if (gameOver) {
			if (newHighScore) {
				//font250.draw(batch, "NEW BEST", 0, bestPositionY, width, Align.center, false);
                newBestSprite.draw(batch);
			}
			else {
			    bestSprite.draw(batch);
				font250.draw(batch, ""+highScore, bestPositionX, bestPositionY);
			}

			font125.draw(batch, "TAP TO RESTART", 0, restartPositionY, width, Align.center, false);
		}

		Gdx.gl.glDisable(GL20.GL_BLEND);

		batch.end();

		if (colorSelectionOpen || colorSelectionClosing) {
			if (colorSelectionFrameCount < 10) {
				colorSelectionFrameCount++;
				if (!colorSelectionClosing) {
					colorSelectionOffset = outlineWidth - outlineWidth * (colorSelectionFrameCount / 10f);
					colorSelectionShadowAlpha = 0.5f * (colorSelectionFrameCount / 10f);
				}
				else {
					colorSelectionOffset = outlineWidth * (colorSelectionFrameCount / 10f);
					colorSelectionShadowAlpha = 0.5f - 0.5f * (colorSelectionFrameCount / 10f);
				}
			} else {
				colorSelectionClosing = false;
			}

			Gdx.gl.glEnable(GL20.GL_BLEND);

			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

			shapeRenderer.setColor(shadowColor.r, shadowColor.g, shadowColor.b, colorSelectionShadowAlpha);
			shapeRenderer.rect(0, 0, width, height);

			shapeRenderer.setColor(outlineColor.r, outlineColor.g, outlineColor.b, 1f);
			shapeRenderer.rect(colorOutlineX + colorSelectionOffset, 0, outlineWidth, height);

			shapeRenderer.setColor(roadColor.r, roadColor.g, roadColor.b, 1f);
			shapeRenderer.rect(colorRoadX + colorSelectionOffset, 0, roadWidth, height);

			for (i = 0; i < numColors; i++) {
				colorLineColor = linePaints[i].color;

				if (i == 5) { //rainbow
					colorLineColor = rainbowColors[rainbowColorsIndex];

					rainbowColorsIndex++;
					if (rainbowColorsIndex >= rainbow.colors.length) {
						rainbowColorsIndex = 0;
					}
				}
				else if (i == 6) {
					colorLineColor = pulsarColors[pulsarColorsIndex];

					pulsarColorsIndex++;
					if (pulsarColorsIndex >= pulsar.colors.length) {
						pulsarColorsIndex = 0;
					}
				}

				shapeRenderer.setColor(colorLineColor.r, colorLineColor.g, colorLineColor.b, 1f);
				shapeRenderer.rect(colorLineX + colorSelectionOffset, i * colorLineHeight, lineWidth, colorLineHeight + 10);
			}

			shapeRenderer.end();

			batch.begin();

			for (i = 0; i < numColors; i++) {
				if (highScore < linePaints[i].pointsToUnlock) {
					lockSprite.setPosition(lockX + colorSelectionOffset, i*colorLineHeight + lockY + lineWidth/2f);
					lockSprite.draw(batch);

					font75flat.draw(
							batch,
							""+linePaints[i].pointsToUnlock,
							colorOutlineX + colorSelectionOffset,
							i*colorLineHeight + lockY - lineWidth/2,
							outlineWidth,
							Align.center,
							false
					);
				}

				if (i >= firstToUnlock && i <= lastToUnlock) {
					if (unlockFrameCount < 15) {
						lockSprite.setPosition(lockX + colorSelectionOffset, i*colorLineHeight + lockY + lineWidth/2f);
						lockSprite.draw(batch);

						font75flat.draw(
								batch,
								""+linePaints[i].pointsToUnlock,
								colorOutlineX + colorSelectionOffset,
								i*colorLineHeight + lockY - lineWidth/2f,
								outlineWidth,
								Align.center,
								false
						);
					}
					else {
						unlockedSprite.setPosition(
								lockX + lineWidth,
								i*colorLineHeight + lockY + lineWidth/2f - (1f/(height*0.75f))*(float)Math.pow(((unlockFrameCount-15)/75f)*height, 2));
						unlockedSprite.draw(batch);
					}
				}
			}

			if (firstToUnlock > -1 && unlockFrameCount < 90 && colorSelectionFrameCount >= 10) {
				unlockFrameCount++;
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
