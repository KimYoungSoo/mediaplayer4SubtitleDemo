package com.tcl.liugb.subtitledemo;

import java.lang.reflect.Method;

import android.media.MediaPlayer;
import android.util.Log;

public class TestUtil
{

  /**
   * @参数为需要反射的方法名字
   * @返回反射获取的方法
   */
  public static Method reflectMediaPlayerMethod(String str)
  {

    Method tempMethod = null;
    try
    {

      Method[] methods = Class.forName(MediaPlayer.class.getName())
          .getMethods();

      for (Method temp : methods)
      {
        Log.d("lgb", "method name is:" + temp);

        if (temp.getName().equals(str))
        {
          tempMethod = temp;
          Log.d("lgb", "reflected method name is:" + str + "break loop");
          break;
        }

      }

    }
    catch (SecurityException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (ClassNotFoundException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return tempMethod;

  }

}
