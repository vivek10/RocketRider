package com.kingbob.RocketRider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.particle.SpriteParticleSystem;
import org.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.andengine.entity.particle.initializer.AlphaParticleInitializer;
import org.andengine.entity.particle.initializer.BlendFunctionParticleInitializer;
import org.andengine.entity.particle.initializer.ColorParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ColorParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.ScaleParticleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import com.kingbob.RocketRider.R;
import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectSprite;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegionFactory;

public class RocketRiderActivity extends SimpleBaseGameActivity implements
		SensorEventListener, IOnMenuItemClickListener {
	
	private final String APP_ID = "434255733359744";

	private boolean PAUSED = false;
	private boolean gameOver = false;
	protected static final int MENU_PLAY = 0;

	private static int CAMERA_WIDTH = 720;
	private static int CAMERA_HEIGHT = 1280;
	private ITextureRegion mBackgroundTextureRegion;
	private PixelPerfectTextureRegion mRocketTextureRegion;
	private PixelPerfectTextureRegion mWallTextureRegion, mWallTextureRegion2;
	private Sprite mRocket;
	private Sprite pauseBtn;
	private float accelerometerSpeedX;
	private SensorManager sensorManager;
	private int rocketX, rocketY;

	private int addWallCounter = 150;
	private int changeGameSpeedCounter = 0;
	private int gameSpeed = 120;
	private int addWallTime = 30000 / gameSpeed;
	private int changeGameSpeedTime = 1500;
	private int score = 0;

	private Font mFont;
	private Text mText;

	private Scene scene;
	private Scene pauseGame;

	private List<Wall> wallList = new LinkedList<Wall>();
	private TextureRegion mPauseTextureRegion;
	private TextureRegion mPlayTextureRegion;

	Camera camera;
	private Font mFont2;
	private TextureRegion mResumeTextureRegion;
	private ITextureRegion mParticleTextureRegion;

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		try {
			// 1 - Set up bitmap textures
			ITexture backgroundTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/background.png");
						}
					});
			ITexture resumeButtonTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/Resume.png");
						}
					});

			BitmapTextureAtlas rocketTexture = new BitmapTextureAtlas(
					getTextureManager(), 128, 128,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			BitmapTextureAtlas wallTexture = new BitmapTextureAtlas(
					getTextureManager(), 512, 512,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			BitmapTextureAtlas wallTexture2 = new BitmapTextureAtlas(
					getTextureManager(), 256, 256,
					TextureOptions.BILINEAR_PREMULTIPLYALPHA);

			backgroundTexture.load();
			resumeButtonTexture.load();
			rocketTexture.load();
			wallTexture.load();
			wallTexture2.load();

			this.mBackgroundTextureRegion = TextureRegionFactory
					.extractFromTexture(backgroundTexture);
			this.mResumeTextureRegion = TextureRegionFactory
					.extractFromTexture(resumeButtonTexture);

			this.mRocketTextureRegion = PixelPerfectTextureRegionFactory
					.createFromAsset(rocketTexture, this.getAssets(),
							"gfx/rocket.png", 0, 0, 0);
			this.mWallTextureRegion = PixelPerfectTextureRegionFactory
					.createFromAsset(wallTexture, this.getAssets(),
							"gfx/wall.png", 0, 0, 0);
			this.mWallTextureRegion2 = PixelPerfectTextureRegionFactory
					.createFromAsset(wallTexture2, this.getAssets(),
							"gfx/wall2.png", 0, 0, 0);

			Texture fontTexture;
			fontTexture = new BitmapTextureAtlas(getTextureManager(), 256, 256);
			this.mFont = FontFactory.createFromAsset(this.getFontManager(),
					fontTexture, this.getAssets(), "fonts/ROBOTO-MEDIUM.TTF",
					38, true, Color.WHITE);
			mFont.load();

			Texture fontTexture2;
			fontTexture2 = new BitmapTextureAtlas(getTextureManager(), 256, 256);
			mFont2 = FontFactory.createFromAsset(this.getFontManager(),
					fontTexture2, this.getAssets(), "fonts/ROBOTO-MEDIUM.TTF",
					100, true, Color.WHITE);
			mFont2.load();

			
			BitmapTextureAtlas fireTexture = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(fireTexture, this, "gfx/particle_point.png", 0, 0);

			fireTexture.load();
			
			ITexture pauseTexture = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/Pause.png");
						}
					});
			;
			mPauseTextureRegion = TextureRegionFactory
					.extractFromTexture(pauseTexture);

			ITexture playTexture = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/Play.png");
						}
					});
			;
			mPlayTextureRegion = TextureRegionFactory
					.extractFromTexture(playTexture);

			pauseTexture.load();
			playTexture.load();
			

		} catch (IOException e) {
			Debug.e(e);
		}

	}

	@SuppressWarnings("static-access")
	@Override
	protected Scene onCreateScene() {
		sensorManager = (SensorManager) this
				.getSystemService(this.SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				sensorManager.SENSOR_DELAY_GAME);

		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.mEngine.registerUpdateHandler(new IUpdateHandler() {
			public void onUpdate(float pSecondsElapsed) {
				updateRocketPosition();
			}

			public void reset() {
				// TODO Auto-generated method stub
			}
		});

		scene = new Scene();
		Sprite backgroundSprite = new Sprite(0, 0,
				this.mBackgroundTextureRegion, getVertexBufferObjectManager());
		scene.attachChild(backgroundSprite);

		rocketX = 360;
		rocketY = 1090;

		mRocket = new PixelPerfectSprite(rocketX, rocketY,
				this.mRocketTextureRegion, getVertexBufferObjectManager());
		mRocket.setSize(74, 130);
		scene.attachChild(mRocket);

		createMapUpdateTimeHandler();

		mText = new Text(580, 0, mFont, "1234567890",
				this.getVertexBufferObjectManager());
		mText.setText("0");
		scene.attachChild(mText);

		pauseBtn = new Sprite(20, 0, this.mPauseTextureRegion,
				getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP) {
					pause();
				}
				return true;
			}
		};
		scene.attachChild(pauseBtn);
		scene.registerTouchArea(pauseBtn);

		Sprite resumeButton = new Sprite(300, 500, mResumeTextureRegion,
				getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP) {
					resume();
				}
				return true;
			}
		};
		Sprite backgroundSprite2 = new Sprite(0, 0,
				this.mBackgroundTextureRegion, getVertexBufferObjectManager());
		pauseGame = new Scene();
		pauseGame.attachChild(backgroundSprite2);
		resumeButton.setScale(0.75f);
		resumeButton.setPosition(
				(CAMERA_WIDTH - mPlayTextureRegion.getWidth()) / 2, 400);
		pauseGame.registerTouchArea(resumeButton);
		pauseGame.attachChild(resumeButton);

		Text text = new Text(170, 200, mFont2, "PAUSED",
				this.getVertexBufferObjectManager());
		pauseGame.attachChild(text);
		
		addFire(mRocket);

		return scene;
	}
	
	private void addFire(Sprite sprite) {
		final CircleOutlineParticleEmitter particleEmitter = new CircleOutlineParticleEmitter(CAMERA_WIDTH * 0.5f, CAMERA_HEIGHT * 0.5f + 20, 10);
		final SpriteParticleSystem particleSystem = new SpriteParticleSystem(particleEmitter, 60, 60, 360, this.mParticleTextureRegion, this.getVertexBufferObjectManager());
		
//		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
//			@Override
//			public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
//				particleEmitter.setCenter(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
//				return true;
//			}
//		});

		particleSystem.addParticleInitializer(new ColorParticleInitializer<Sprite>(1, 0, 0));
		particleSystem.addParticleInitializer(new AlphaParticleInitializer<Sprite>(0));
		particleSystem.addParticleInitializer(new BlendFunctionParticleInitializer<Sprite>(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE));
		particleSystem.addParticleInitializer(new VelocityParticleInitializer<Sprite>(0, 0, 20, 80));
		particleSystem.addParticleInitializer(new RotationParticleInitializer<Sprite>(0.0f, 360.0f));
		particleSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(1));

		particleSystem.addParticleModifier(new ScaleParticleModifier<Sprite>(0, 1, 1.2f, 2f));
		particleSystem.addParticleModifier(new ColorParticleModifier<Sprite>(0, .5f, 1, 1, 0, 0.5f, 0, 0));
		particleSystem.addParticleModifier(new ColorParticleModifier<Sprite>(.5f, 1, 1, 1, 0.5f, 1, 0, 1));
		particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(0, .5f, 0, 1));
		particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(.5f, 1, 1, 0));
		
		particleEmitter.setCenter(20, 100);
		//scene.attachChild(particleSystem);
		sprite.attachChild(particleSystem);
	}

	private void pause() {
		PAUSED = true;
		mEngine.setScene(pauseGame);
	}

	private void resume() {
		PAUSED = false;
		mEngine.setScene(scene);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				accelerometerSpeedX = -event.values[0];
				break;
			}
		}

	}

	private void updateRocketPosition() {
		if (accelerometerSpeedX != 0) {
			// Set the Boundary limits
			int leftLimit = 0;
			int rightLimit = CAMERA_WIDTH - (int) mRocket.getWidth();

			// Calculate New X,Y Coordinates within Limits
			if (rocketX >= leftLimit)
				rocketX += accelerometerSpeedX * 2.5;
			else
				rocketX = leftLimit;
			if (rocketX <= rightLimit)
				rocketX += accelerometerSpeedX * 2.5;
			else
				rocketX = rightLimit;

			// Double Check That New X,Y Coordinates are within Limits
			if (rocketX < leftLimit)
				rocketX = leftLimit;
			else if (rocketX > rightLimit)
				rocketX = rightLimit;

			mRocket.setPosition(rocketX, rocketY);
		}
	}

	private void createMapUpdateTimeHandler() {
		this.getEngine().registerUpdateHandler(
				new TimerHandler(0.01f, true, new ITimerCallback() {
					@Override
					public void onTimePassed(final TimerHandler pTimerHandler) {
						if (PAUSED == false) {
							addWallCounter++;
							score++;
							mText.setText("" + score);
							changeGameSpeedCounter++;
							if (addWallCounter >= addWallTime) {
								showRandomWall();
								addWallCounter = 0;
								if (changeGameSpeedCounter >= changeGameSpeedTime && gameSpeed < 300) {
									gameSpeed += 20;
									Log.d("TimeHandler", "Increased Speed"+gameSpeed);
									changeGameSpeedCounter = 0;
									addWallTime = 30000 / gameSpeed;
									for (Wall wall : wallList) {
										wall.getHandler().setVelocity(0, gameSpeed);
									}
								}
							}
						}
					}
				}));
	}

	private void showRandomWall() {
		double x = Math.random();
		if (x < 0.1) {
			createWall(0, 0, mWallTextureRegion);
		} else if (x < 0.2) {
			createWall(360, 0, mWallTextureRegion);
		} else if (x < 0.25) {
			createWall(0, 0, mWallTextureRegion2);
		} else if (x < 0.3) {
			createWall(240, 0, mWallTextureRegion2);
		} else if (x < 0.35) {
			createWall(480, 0, mWallTextureRegion2);
		} else if (x < 0.50) {
			createWall(0, 0, mWallTextureRegion2);
			createWall(240, 0, mWallTextureRegion2);
		} else if (x < 0.65) {
			createWall(0, 0, mWallTextureRegion2);
			createWall(480, 0, mWallTextureRegion2);
		} else if (x < 0.8) {
			createWall(240, 0, mWallTextureRegion2);
			createWall(480, 0, mWallTextureRegion2);
		} else if (x < 0.9) {
			createWall(0, 0, mWallTextureRegion);
			createWall(360, 0, mWallTextureRegion2);
		} else {
			createWall(120, 0, mWallTextureRegion2);
			createWall(360, 0, mWallTextureRegion);
		}

	}

	private void createWall(float pX, float pY,
			PixelPerfectTextureRegion wallTextureRegion) {
		if (wallList.size() > 20) {
			Wall removedWall = wallList.remove(0);
			this.mEngine.getScene().detachChild(removedWall);
		}
		final Wall wall = new Wall(pX, pY, wallTextureRegion,
				getVertexBufferObjectManager());
		this.mEngine.getScene().attachChild(wall);
		PhysicsHandler handler = new PhysicsHandler(wall);
		wall.registerUpdateHandler(handler);
		handler.setVelocity(0, gameSpeed);
		wall.setHandler(handler);
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() {
			}

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				if (wall.collidesWith(mRocket)) {
					gameOver = true;
					Intent gameOverActivity = new Intent(
							RocketRiderActivity.this, GameOverActivity.class);
					gameOverActivity.putExtra("score", score);
					mText.setText("");
					startActivity(gameOverActivity);
					overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
				}
			}
		});
		wallList.add(wall);

	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		switch (pMenuItem.getID()) {
		case MENU_PLAY:
			if (scene.hasChildScene()) {
				scene.clearChildScene();
				pauseBtn.setVisible(true);
			}
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onPause() {
		if (gameOver == false) {
			pause();
		}
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (PAUSED == true) {
			resume();
		} else {
			pause();
		}
	}
	
	@Override
	public void onResume() {
		com.facebook.Settings.publishInstallAsync(getApplicationContext(), APP_ID);
		super.onResume();
	}

}
