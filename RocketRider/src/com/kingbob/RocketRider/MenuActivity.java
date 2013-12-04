package com.kingbob.RocketRider;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.particle.SpriteParticleSystem;
import org.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.andengine.entity.particle.emitter.PointParticleEmitter;
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
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
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
import android.opengl.GLES20;
import android.view.MotionEvent;

public class MenuActivity extends SimpleBaseGameActivity implements
		IOnMenuItemClickListener {
	
	private final String APP_ID = "434255733359744";

	private static int CAMERA_WIDTH = 720;
	private static int CAMERA_HEIGHT = 1280;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mParticleTextureRegion;

	protected static final int MENU_PLAY = 0, MENU_HELP = 1;

	private boolean inHelpScreen = false;

	private ITextureRegion mBackgroundTextureRegion;

	private Font mFont;

	protected ITextureRegion playButtonTextureRegion;
	Camera camera;
	MenuScene scene;
	private TextureRegion menuButtonTextureRegion;
	private TextureRegion helpButtonTextureRegion;

	final Scene helpScene = new Scene();
	private Font mFont2;

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

			backgroundTexture.load();

			ITexture playButtonTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/Play.png");
						}
					});

			playButtonTexture.load();

			this.playButtonTextureRegion = TextureRegionFactory
					.extractFromTexture(playButtonTexture);
			this.mBackgroundTextureRegion = TextureRegionFactory
					.extractFromTexture(backgroundTexture);

			Texture fontTexture;
			fontTexture = new BitmapTextureAtlas(getTextureManager(), 256, 256);
			mFont = FontFactory.createFromAsset(this.getFontManager(),
					fontTexture, this.getAssets(), "fonts/ROBOTO-MEDIUM.TTF",
					100, true, Color.WHITE);
			mFont.load();

			Texture fontTexture2;
			fontTexture2 = new BitmapTextureAtlas(getTextureManager(), 256, 256);
			mFont2 = FontFactory.createFromAsset(this.getFontManager(),
					fontTexture2, this.getAssets(), "fonts/ROBOTO-LIGHT.TTF",
					48, true, Color.WHITE);
			mFont2.load();

			ITexture helpButtonTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/Help.png");
						}
					});
			helpButtonTexture.load();
			this.helpButtonTextureRegion = TextureRegionFactory
					.extractFromTexture(helpButtonTexture);

			ITexture menuButtonTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/Menu.png");
						}
					});
			menuButtonTexture.load();
			this.menuButtonTextureRegion = TextureRegionFactory
					.extractFromTexture(menuButtonTexture);

		} catch (IOException e) {
			Debug.e(e);
		}

	}

	@Override
	protected Scene onCreateScene() {
		scene = new MenuScene(camera);

		Sprite backgroundSprite = new Sprite(0, 0,
				this.mBackgroundTextureRegion, getVertexBufferObjectManager());
		scene.attachChild(backgroundSprite);

		scene.buildAnimations();

		final SpriteMenuItem playMenuItem = new SpriteMenuItem(MENU_PLAY,
				this.playButtonTextureRegion, getVertexBufferObjectManager());
		playMenuItem.setScale(0.75f);
		playMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		playMenuItem.setPosition(
				(CAMERA_WIDTH - playButtonTextureRegion.getWidth()) / 2, 500);
		scene.addMenuItem(playMenuItem);

		final SpriteMenuItem helpMenuItem = new SpriteMenuItem(MENU_HELP,
				this.helpButtonTextureRegion, getVertexBufferObjectManager());
		helpMenuItem.setScale(0.75f);
		helpMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		helpMenuItem.setPosition(
				(CAMERA_WIDTH - helpButtonTextureRegion.getWidth()) / 2, 650);
		scene.addMenuItem(helpMenuItem);

		scene.setBackgroundEnabled(false);

		scene.setOnMenuItemClickListener(this);

		Text text1 = new Text(120, 200, mFont, "ROCKET",
				this.getVertexBufferObjectManager());
		Text text2 = new Text(300, 300, mFont, "RIDER",
				this.getVertexBufferObjectManager());

		scene.attachChild(text1);
		scene.attachChild(text2);

		Sprite menuButton = new Sprite(300, 500, menuButtonTextureRegion,
				getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP) {
					mEngine.setScene(scene);
					inHelpScreen = false;
				}
				return true;
			}
		};
		Sprite backgroundSprite2 = new Sprite(0, 0,
				this.mBackgroundTextureRegion, getVertexBufferObjectManager());
		helpScene.attachChild(backgroundSprite2);
		menuButton.setScale(0.75f);
		menuButton.setPosition(
				(CAMERA_WIDTH - menuButtonTextureRegion.getWidth()) / 2, 600);
		helpScene.registerTouchArea(menuButton);
		helpScene.attachChild(menuButton);

		Text text3 = new Text(20, 100, mFont2,
				"Avoid hitting the obstacles with ",
				this.getVertexBufferObjectManager());
		Text text4 = new Text(20, 160, mFont2,
				"your rocket ship! Tilt your phone",
				this.getVertexBufferObjectManager());
		Text text5 = new Text(20, 220, mFont2, "left or right to move.",
				this.getVertexBufferObjectManager());
		helpScene.attachChild(text3);
		helpScene.attachChild(text4);
		helpScene.attachChild(text5);

		return scene;
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		switch (pMenuItem.getID()) {
		case MENU_PLAY:
			Intent rocketRiderActivity = new Intent(getApplicationContext(),
					RocketRiderActivity.class);
			startActivity(rocketRiderActivity);
			overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			return true;
		case MENU_HELP:
			mEngine.setScene(helpScene);
			inHelpScreen = true;
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onBackPressed() {
		if (inHelpScreen) {
			mEngine.setScene(scene);
			inHelpScreen = false;
		} else {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
		}
	}
	
	@Override
	public void onResume() {
		com.facebook.Settings.publishInstallAsync(getApplicationContext(), APP_ID);
		super.onResume();
	}
}