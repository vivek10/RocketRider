package com.kingbob.RocketRider;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
//import com.facebook.*;

public class GameOverActivity extends SimpleBaseGameActivity implements
		IOnMenuItemClickListener {
	
	private VertexBufferObjectManager VBOM;

	private static int CAMERA_WIDTH = 720;
	private static int CAMERA_HEIGHT = 1280;

	protected static final int PLAY = 0, MENU = 1, SHARE = 2;
	int currentScore=0;

	private ITextureRegion mBackgroundTextureRegion, playButtonTextureRegion,
			menuButtonTextureRegion;

	private Font mFont, mFont2, mFont3;

	Camera camera;
	MenuScene scene;

	private TextureRegion shareButtonTextureRegion;

	@Override
	public EngineOptions onCreateEngineOptions() {
		camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	protected void onCreateResources() {
		try {
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
							return getAssets().open("gfx/PlayAgain.png");
						}
					});

			ITexture menuButtonTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/Menu.png");
						}
					});
			
			ITexture shareButtonTexture = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("gfx/ShareScore.png");
						}
					});

			playButtonTexture.load();
			menuButtonTexture.load();
			shareButtonTexture.load();

			this.playButtonTextureRegion = TextureRegionFactory
					.extractFromTexture(playButtonTexture);
			this.mBackgroundTextureRegion = TextureRegionFactory
					.extractFromTexture(backgroundTexture);
			this.menuButtonTextureRegion = TextureRegionFactory
					.extractFromTexture(menuButtonTexture);
			this.shareButtonTextureRegion = TextureRegionFactory
					.extractFromTexture(shareButtonTexture);

			Texture fontTexture, fontTexture2, fontTexture3;

			fontTexture = new BitmapTextureAtlas(getTextureManager(), 256, 256);
			fontTexture2 = new BitmapTextureAtlas(getTextureManager(), 256, 256);
			fontTexture3 = new BitmapTextureAtlas(getTextureManager(), 256, 256);

			mFont = FontFactory.createFromAsset(this.getFontManager(),
					fontTexture, this.getAssets(), "fonts/ROBOTO-MEDIUM.TTF",
					64, true, Color.WHITE);

			mFont2 = FontFactory.createFromAsset(this.getFontManager(),
					fontTexture2, this.getAssets(), "fonts/ROBOTO-LIGHT.TTF",
					48, true, Color.WHITE);
			mFont3 = FontFactory.createFromAsset(this.getFontManager(),
					fontTexture3, this.getAssets(), "fonts/ROBOTO-LIGHT.TTF",
					48, true, Color.RED);

			mFont.load();
			mFont2.load();
			mFont3.load();

		} catch (IOException e) {
			Debug.e(e);
		}

	}

	@Override
	protected Scene onCreateScene() {
		scene = new MenuScene(camera);

		scene.buildAnimations();

		Sprite backgroundSprite = new Sprite(0, 0,
				this.mBackgroundTextureRegion, getVertexBufferObjectManager());
		scene.attachChild(backgroundSprite);

		final SpriteMenuItem playMenuItem = new SpriteMenuItem(PLAY,
				this.playButtonTextureRegion, getVertexBufferObjectManager());
		playMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		playMenuItem.setScale(0.75f);
		playMenuItem.setPosition(
				(CAMERA_WIDTH - playButtonTextureRegion.getWidth()) / 2, 520);

		final SpriteMenuItem menuMenuItem = new SpriteMenuItem(MENU,
				this.menuButtonTextureRegion, getVertexBufferObjectManager());
		menuMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuMenuItem.setScale(0.75f);
		menuMenuItem.setPosition(
				(CAMERA_WIDTH - playButtonTextureRegion.getWidth()) / 2, 680);
		
		final SpriteMenuItem shareMenuItem = new SpriteMenuItem(SHARE,
				this.shareButtonTextureRegion, getVertexBufferObjectManager());
		shareMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		shareMenuItem.setScale(0.75f);
		shareMenuItem.setPosition(
				(CAMERA_WIDTH - shareButtonTextureRegion.getWidth()) / 2, 842);

		scene.addMenuItem(menuMenuItem);
		scene.addMenuItem(playMenuItem);
		scene.addMenuItem(shareMenuItem);

		scene.setBackgroundEnabled(false);

		scene.setOnMenuItemClickListener(this);

		currentScore = getIntent().getExtras().getInt("score");

		SharedPreferences sharedPref = this.getSharedPreferences(
				getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		int highScore = sharedPref.getInt("saved_high_score", 0);

		if (currentScore > highScore) {
			highScore = currentScore;
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putInt("saved_high_score", highScore);
			editor.commit();
		}

		Text text1 = new Text(270, 200, mFont, "GAME",
				this.getVertexBufferObjectManager());
		Text text2 = new Text(280, 275, mFont, "OVER",
				this.getVertexBufferObjectManager());
		Text text3 = new Text(180, 400, mFont2, "Your Score: " + currentScore,
				this.getVertexBufferObjectManager());
		Text text4 = new Text(120, 460, mFont3, "Your High Score: ",
				this.getVertexBufferObjectManager());
		Text text5 = new Text(490, 460, mFont2, "" + highScore,
				this.getVertexBufferObjectManager());
		Log.d("SCORE", "score: " + getIntent().getExtras().getInt("score"));

		scene.attachChild(text1);
		scene.attachChild(text2);
		scene.attachChild(text3);
		scene.attachChild(text4);
		scene.attachChild(text5);
		

		return scene;
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		switch (pMenuItem.getID()) {
		case PLAY:
			Intent rocketRiderActivity = new Intent(getApplicationContext(),
					RocketRiderActivity.class);
			startActivity(rocketRiderActivity);
			overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			return true;
		case MENU:
			Intent menuActivity = new Intent(getApplicationContext(),
					MenuActivity.class);
			startActivity(menuActivity);
			overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
			return true;
		case SHARE:
			facebookLogin();
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent menuActivity = new Intent(getApplicationContext(),
				MenuActivity.class);
		startActivity(menuActivity);
		overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		com.facebook.Settings.publishInstallAsync(getApplicationContext(),getString(R.string.app_id));
		super.onResume();
	}
	
	private void facebookLogin() {
		VBOM = getVertexBufferObjectManager();
		  Session.openActiveSession(this, true, new Session.StatusCallback() {
			    // callback when session changes state
			    @SuppressWarnings("deprecation")
				@Override
			    public void call(Session session, SessionState state, Exception exception) {
			    	if (session.isOpened()) {
			    		// make request to the /me API
			    		Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
			    		  // callback after Graph API response with user object
			    		  @Override
			    		  public void onCompleted(GraphUser user, Response response) {
			    			  if (user != null) {
			    				  Log.d("FACEBOOK", "got user");
			    				  postToFacebookWall();
			    			  } else {
			    				  Log.d("FACEBOOK", "did not get user");
			    			  }
			    		  }
			    		});
			    	}
			    	else 
			    	{
			    		Log.d("FACEBOOK", "couldn't log in");
			    	}
			    }
			  });
	}
	
	public void postToFacebookWall() {
		Bundle params = new Bundle();
		params.putString("name", "I just got a score of " + currentScore +" in Rocket Rider!");
		//params.putString("caption", "Your games name");
		params.putString("description", "Can you beat my score?");
		params.putString("link", "https://play.google.com/store/apps/details?id=com.kingbob.RocketRider&hl=en");
		//params.putString("picture", "https://yourwebsite.com/youricon.png");
		 
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(this, Session.getActiveSession(),params)).setOnCompleteListener(new OnCompleteListener() {
		        @Override
		        public void onComplete(Bundle values, FacebookException error) {
		                if (error == null) {
		                        final String postId = values.getString("post_id");
		                        if (postId != null) {
		                        // POSTED
		                        } else {
		                        // POST CANCELLED
		                        }
		                } else if (error instanceof FacebookOperationCanceledException) {
		                        // POST CANCELLED
		                        } else {
		                        // ERROR POSTING
		                        }
		                }
		 
		}).build();
		feedDialog.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
}
