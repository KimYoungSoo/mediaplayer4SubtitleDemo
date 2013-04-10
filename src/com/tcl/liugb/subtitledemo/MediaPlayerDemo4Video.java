package com.tcl.liugb.subtitledemo;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.tcl.lgb.test.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.SubtitleTrackInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author liugb
 * 
 */

public class MediaPlayerDemo4Video extends Activity implements
    OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
    OnVideoSizeChangedListener, SurfaceHolder.Callback, OnInfoListener
{

  private static final String TAG = "MediaPlayerDemo";
  private int mVideoWidth;
  private int mVideoHeight;
  private MediaPlayer mMediaPlayer;
  private SurfaceView mVideoSurfaceView;
  private SurfaceView mSubtitleSurfaceView;
  private SurfaceHolder holder;

  private String videoPath;
  private Bundle extras;
  private static final String MEDIA = "media";
  private static final int LOCAL_AUDIO = 1;
  private static final int STREAM_AUDIO = 2;
  private static final int RESOURCES_AUDIO = 3;
  private static final int LOCAL_VIDEO = 4;
  private static final int STREAM_VIDEO = 5;
  private int nms;
  private int internalSubtitleNum;
  private boolean mIsVideoSizeKnown = false;
  private boolean mIsVideoReadyToBePlayed = false;
  private Button mPlay;
  private Button mPause;
  private Button switchSubtitle;
  private Button insertSubtitle;
  private TextView mSubtitleTextView;
  // 外挂字幕地址
  private String subtitlePath = "/mnt/sdcard/Harry.Potter.and.the.Order.of.the.Phoenix2007_en.srt";
  private boolean bIsPaused = false;
  private boolean bIsReleased = false;

  /**
   * 
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);

    setContentView(R.layout.main);
    mVideoSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView1);
    mSubtitleTextView = (TextView) findViewById(R.id.video_subtitle_text);
    // start->设置字幕surfaceView
    mSubtitleSurfaceView = (SurfaceView) findViewById(R.id.video_subtitle_surface);
    mSubtitleSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
    mSubtitleSurfaceView.setBackgroundColor(Color.TRANSPARENT);
    mSubtitleSurfaceView.setVisibility(View.VISIBLE);
    mSubtitleSurfaceView.getHolder().addCallback(mSubtiteHolder);
    // end->设置字幕surfaceView
    Log.d(TAG, "start set surfaceview..........");
    holder = mVideoSurfaceView.getHolder();
    holder.addCallback(this);
    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    Log.d(TAG, "end set surfaceview..........");
    getWindow().setFormat(PixelFormat.UNKNOWN);
    initVideoPath();
    mPlay = (Button) findViewById(R.id.play);
    mPause = (Button) findViewById(R.id.pause);
    // 暂停button
    mPause.setOnClickListener(new OnClickListener()
    {

      @Override
      public void onClick(View arg0)
      {

        if (mMediaPlayer.isPlaying())
        {
          mMediaPlayer.pause();
        }
        else
        {
          mMediaPlayer.start();
        }
      }
    });
    switchSubtitle = (Button) findViewById(R.id.reset);

    // 切换内置字幕
    switchSubtitle.setOnClickListener(new OnClickListener()
    {

      @Override
      public void onClick(View v)
      {
        // TODO Auto-generated method stub

        if (nms < internalSubtitleNum)
        {
          Log.d(TAG,
              "&&&&&&&&&&&&&&&& nms < internalSubtitleNum &&&&&&&&&&&&&&&&&");
        }
        else
        {
          nms = 0;
        }
        setSubtitleNum(nms);
        nms++;
      }

    });
    // 插入外挂字幕文件
    insertSubtitle = (Button) findViewById(R.id.stop);

    insertSubtitle.setOnClickListener(new OnClickListener()
    {

      @Override
      public void onClick(View v)
      {
        Log.d(TAG, "&&&&&&&&&&&& insert external subtitle &&&&&&&&&&&&&&&&");
        Class<?> paramType = String.class;
        try
        {
          Method setSubtitleDataSource = Class.forName(
              MediaPlayer.class.getName()).getMethod("setSubtitleDataSource",
              paramType);
          // Method setSubtitleDataSource = TestUtil
          // .reflectMediaPlayerMethod("setSubtitleDataSource");
          File file = new File(getsubtitlePath(videoPath));
          if (file.exists())
          {
            setSubtitleDataSource.invoke(mMediaPlayer, subtitlePath);
            Log.d(TAG,
                "&&&&&&&&&&&& external subtitle path is not exists &&&&&&&&&&&&&");
          }

        }
        catch (Exception e)
        {
          Log.d(
              TAG,
              " reflect subtitleDataSource method happend error"
                  + e.getMessage());
        }
        // 此处固定，只加入一条外置字幕时，srt文件固定在sdcard中,字幕总数等于内置字幕加上外置字幕，所以如果加一条外置字幕，总数==internalSubtitleNumber
        // + 1
        setSubtitleNum(internalSubtitleNum);

      }

    });

  }

  /**
   * 初始化mediaplayer after surface created
   */
  private void playVideo()
  {
    doCleanUp();
    // Create a new media player and set the listeners
    mMediaPlayer = new MediaPlayer();
    try
    {
      mMediaPlayer.setDataSource(videoPath);
      mMediaPlayer.setDisplay(holder);
      mMediaPlayer.prepare();
    }
    catch (Exception e)
    {
      // TODO: handle exception
      Log.d(TAG, "set DataSource error...........");
    }
    mMediaPlayer.setOnBufferingUpdateListener(this);
    mMediaPlayer.setOnCompletionListener(this);
    mMediaPlayer.setOnPreparedListener(this);
    mMediaPlayer.setOnVideoSizeChangedListener(this);
    mMediaPlayer.setOnInfoListener(this);
    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

  }

  /*
   * catch (Exception e) { Log.e(TAG, "error: " + e.getMessage(), e); }
   */
  // }

  public void onBufferingUpdate(MediaPlayer arg0, int percent)
  {
    Log.d(TAG, "onBufferingUpdate percent:" + percent);

  }

  public void onCompletion(MediaPlayer arg0)
  {
    Log.d(TAG, "onCompletion called");
  }

  public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
  {
    Log.v(TAG, "onVideoSizeChanged called");
    if (width == 0 || height == 0)
    {
      Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
      return;
    }
    mIsVideoSizeKnown = true;
    mVideoWidth = width;
    mVideoHeight = height;
    if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown)
    {
      startVideoPlayback();
    }
  }

  public void onPrepared(MediaPlayer mediaplayer)
  {
    Log.d(TAG, "onPrepared called");
    mIsVideoReadyToBePlayed = true;
    if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown)
    {
      startVideoPlayback();
      setSubtiteSurface();
      internalSubtitleNum = getInternSubNum();
      try
      {
        // 首先关掉内置字幕显示，显示视频内嵌字幕（编码时内嵌的字幕文件）
        Method offSubtitleTrack = Class.forName(MediaPlayer.class.getName())
            .getMethod("offSubtitleTrack");
        // Method method1 =
        // TestUtil.reflectMediaPlayerMethod("offSubtitleTrack");
        offSubtitleTrack.invoke(mMediaPlayer);
      }
      catch (Exception e)
      {
        Log.d(TAG, "exception is:" + e.getMessage());
      }
    }
  }

  public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k)
  {
    Log.d(TAG, "surfaceChanged called");

  }

  public void surfaceDestroyed(SurfaceHolder surfaceholder)
  {
    Log.d(TAG, "surfaceDestroyed called");
  }

  public void surfaceCreated(SurfaceHolder holder)
  {
    Log.d(TAG, "surfaceCreated called");
    // playVideo(extras.getInt(MEDIA));
    playVideo();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    releaseMediaPlayer();
    doCleanUp();
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    releaseMediaPlayer();
    doCleanUp();
  }

  private void releaseMediaPlayer()
  {
    if (mMediaPlayer != null)
    {
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  private void doCleanUp()
  {
    mVideoWidth = 0;
    mVideoHeight = 0;
    mIsVideoReadyToBePlayed = false;
    mIsVideoSizeKnown = false;
  }

  private void startVideoPlayback()
  {
    Log.v(TAG, "startVideoPlayback");
    holder.setFixedSize(mVideoWidth, mVideoHeight);
    mMediaPlayer.start();
  }

  private SurfaceHolder.Callback mSubtiteHolder = new SurfaceHolder.Callback()
  {

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
      // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
      // TODO Auto-generated method stub

    }

  };

  public void setSubtiteSurface()
  {
    // TODO Auto-generated method stub

    if (mMediaPlayer != null)
    {
      try
      {
        // mMediaPlayer.setSubtitleDisplay(holder);
        Class<?> paramType = mSubtitleSurfaceView.getHolder().getClass();
        Method setSubtitleDisplay = Class.forName(MediaPlayer.class.getName())
            .getMethod("setSubtitleDisplay", paramType);
        // Method setSubtitleDisplay =
        // TestUtil.reflectMediaPlayerMethod("setSubtitleDisplay");
        setSubtitleDisplay.invoke(mMediaPlayer,
            mSubtitleSurfaceView.getHolder());
        Log.d(TAG,
            "&&&&&&&&&&&&&&&&&&&&&&&setSubtitleDisplay&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    switch (keyCode)
    {
    case KeyEvent.KEYCODE_MENU:
      Log.d(TAG, "onkeyDown --->menu");
      return true;
    }
    // TODO Auto-generated method stub
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra)
  {
    // TODO Auto-generated method stub

    Log.d(TAG, "MediaPlayer->Info: " + what + "," + extra);
    if (what == 900 && extra == 1)
    {

      // Method method1 = TestUtil.reflectMediaPlayerMethod("getSubtitleData");
      try
      {
        Method getSubtitleData = Class.forName(MediaPlayer.class.getName())
            .getMethod("getSubtitleData");
        String subtitle = (String) getSubtitleData.invoke(mMediaPlayer);
        Log.d(TAG, "setsubtiteltext = " + subtitle);
        mSubtitleTextView.setText(subtitle);
      }
      catch (Exception e)
      {

        // TODO: handle exception
      }

    }
    else if (what == 900 && extra == 0)
    {
      mSubtitleTextView.setText("");
    }
    return true;
  }

  /**
   * @视频内置字幕总数
   */
  private int getInternSubNum()
  {
    int nms = 0;
    try
    {
      Method getAllSubtitleTrackInfo = Class.forName(
          MediaPlayer.class.getName()).getMethod("getAllSubtitleTrackInfo");
      SubtitleTrackInfo subtitleAllTrackInfo = (SubtitleTrackInfo) getAllSubtitleTrackInfo
          .invoke(mMediaPlayer);
      if (subtitleAllTrackInfo != null)
      {
        // 显示该影片所包含的所有字幕数
        nms = subtitleAllTrackInfo.getAllInternalSubtitleCount();
      }
    }
    catch (Exception e)
    {

      Log.e(
          TAG,
          "reflect getAllSubtitleTrackInfo method happend error->"
              + e.getMessage());
      return nms;
    }
    Log.d(TAG, "getInternSubNum = " + nms);
    return nms;
  }

  /**
   * 设置字幕的track number
   * 
   * @param number
   */
  private void setSubtitleNum(int number)
  {
    try
    {
      Method offSubtitleTrack = Class.forName(MediaPlayer.class.getName())
          .getMethod("offSubtitleTrack");
      offSubtitleTrack.invoke(mMediaPlayer);
      Log.d(TAG, "offSubtitleTrack");
      Method onSubtitleTrack = Class.forName(MediaPlayer.class.getName())
          .getMethod("onSubtitleTrack");
      onSubtitleTrack.invoke(mMediaPlayer);
      Log.d(TAG, "onSubtitleTrack");
      // Class<?> paramType = Class.forName("java.lang.Integer");
      Class<?> paramType = int.class;
      Method setSubtitleTrack = Class.forName(MediaPlayer.class.getName())
          .getMethod("setSubtitleTrack", paramType);
      Log.d(TAG, "current setted subtitle number is:" + number);
      setSubtitleTrack.invoke(mMediaPlayer, number);
      // Method method =
      // TestUtil.reflectMediaPlayerMethod("setSubtitleTrack");
      // method.invoke(mMediaPlayer, new Integer(nms));
      Log.d(TAG, "setSubtitleTrack");
    }
    catch (Exception e)
    {
      Log.d(TAG, "reject mediaplayer error->" + e.getMessage());
    }
  }

  /**
   * 初始化视频path
   */
  private void initVideoPath()
  {
    Intent intent = getIntent();
    if (intent == null)
    {
      // 直接关闭界面
      Log.d(TAG, "intent == null............");
      return;
    }
    String type = intent.getType();
    Log.d(TAG, "type is:" + type);
    if (type == null)
    {
      // 直接关闭
      Log.d(TAG, "type == null............");
      return;
    }
    // 播放单部视频的类型
    if (type.startsWith("video/"))
    {
      Uri data = intent.getData();
      if (data == null)
      {
        Log.d(TAG, "data == null............");
        return;
      }
      Log.d(
          TAG,
          " video path is:" + data.getPath() + " data full path"
              + data.toString());
      if (data.getEncodedPath() != null)
      {
        if (intent.getScheme().equals("http"))
        {
          Log.d(TAG, " schema is http");
          videoPath = Uri.decode(data.toString());
        }
        else
        {
          Log.d(TAG, " schema is dfdf:" + data.getScheme());
          videoPath = Uri.decode(data.toString());
        }

      }
      else
      {
        Log.d(TAG, "data.getEncodePath==null");

        return;
      }

      // 播放多个视频的类型
    }
    else if (type.equals("application/vnd.tcl.playlist-video"))
    {
      ArrayList<Parcelable> parcelableArrayListExtra = null;
      try
      {
        parcelableArrayListExtra = intent
            .getParcelableArrayListExtra("playlist");
      }
      catch (Exception e)
      {
        // 直接关闭activity

        return;

      }
      if (parcelableArrayListExtra == null
          || parcelableArrayListExtra.size() == 0)
      {
        // 直接关闭activity

        return;
      }
      // 传递过来的索引
      int currPlayIndex = intent.getIntExtra("index", 0);
      if (parcelableArrayListExtra.size() <= currPlayIndex)
      {
        currPlayIndex = 0;
      }
      // 判断傳遞過來的列表中的对象是不是URI的实例如果不是的退出
      if (!(parcelableArrayListExtra.get(0) instanceof Uri))
      {
        // 直接关闭activity
        Log.d(TAG, "is not uri ...........");
        return;
      }
      else
      {
        videoPath = ((Uri) parcelableArrayListExtra.get(currPlayIndex))
            .getEncodedPath();
      }
    }
    else
    {
      Log.e(TAG, "the error type.....");

      return;

    }

    Log.d(TAG, "transmited path is: " + videoPath);
  }

  /**
   * @return查找视频同目录下的srt文件
   */
  private String getsubtitlePath(String videoPath)
  {
    String subtitlePath = null;
    if (videoPath == null)
    {
      Log.e(TAG, "&&&&&&&&&&&& videopath is null &&&&&&&&&&&&&&&");
      return null;
    }
    StringBuffer strBuffer = new StringBuffer();
    strBuffer.append(videoPath.substring(0, videoPath.lastIndexOf("/") + 1));
    File file = new File(strBuffer.toString());
    Log.d(TAG, "&&&&&&&&&&&&&& video directory is:" + strBuffer.toString());
    if (file.exists())
    {
      Log.d(TAG, "&&&&&&&&&&&&& file.exists() &&&&&&&&&& ");
      if (file.isDirectory())
      {
        Log.d(TAG, "&&&&&&&&&&&&& file.isDirectory() &&&&&&&&&& ");
        File childFile[] = file.listFiles();
        for (int i = 0; i < childFile.length; i++)
        {
          if (childFile[i].isFile()
              && childFile[i].getAbsolutePath().endsWith("srt"))
          {
            subtitlePath = childFile[i].getAbsolutePath();
            Log.d(TAG, "&&&&&&&&&&&&&&& subtitle path is:" + subtitlePath);
            break;
          }
        }
      }
    }
    return subtitlePath;

  }
}
