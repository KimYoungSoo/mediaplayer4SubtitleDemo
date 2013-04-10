/*
** Copyright (c) 2010-2011 MStar Semiconductor, Inc.
** All rights reserved.
**
** Unless otherwise stipulated in writing, any and all information contained
** herein regardless in any format shall remain the sole proprietary of
** MStar Semiconductor Inc. and be kept in strict confidence
** ("MStar Confidential Information") by the recipient.
** Any unauthorized act including without limitation unauthorized disclosure,
** copying, use, reproduction, sale, distribution, modification, disassembling,
** reverse engineering and compiling of the contents of MStar Confidential
** Information is unlawful and strictly prohibited. MStar hereby reserves the
** rights to any and all damages, losses, costs and expenses resulting therefrom.
*/

package android.media;
import android.os.Parcel;
import android.util.Log;

/**
   Class to hold the subtitle's information.  SubtitleTrackInfo are used
   for human consumption and can be embedded in the media (e.g
   shoutcast) or available from an external source. The source can be
   local (e.g thumbnail stored in the DB) or remote (e.g caption
   server).

 */
public class SubtitleTrackInfo
{
    private final static String TAG = "SubtitleTrackInfo";
    private final int MAX_SUBTITLE_TRACK = 20;
    private final int INFO_NUM = 3;
 
    private int mSubtitleType;
    private int mSubtitleCodeType;
    private String mSubtitleLanguageType;
    private int SubtitleLanguageType;
    private Parcel mParcel;
    private Metadata mMeta;
    int buffer[];
  
    private int mSubtitleNum;
    public SubtitleTrackInfo(Metadata metadata) {
        mMeta = metadata;
        SubtitleLanguageType = -1;
    };

    public SubtitleTrackInfo(Parcel reply, int subtitleNum) {
        mParcel = reply;
        mSubtitleNum = subtitleNum;
        SubtitleLanguageType = 0;
        int len = mParcel.readInt();
        int num = mParcel.readInt();  //total info number
        if(num < 2){
            Log.d(TAG,"The video is NOT playing through MstarPlayer and all the subtitle trakc infomations are invalid!!!");
            num = 2;        // At least have two info: InternalSubtitleNum and ImageSubtitleNum
        }
        buffer = new int[num];
        if(mSubtitleNum > MAX_SUBTITLE_TRACK)
        {
          Log.d(TAG,"Subtitle's number overflow! ");
            mSubtitleNum = MAX_SUBTITLE_TRACK;
        }
        for (int i = 0; i < mSubtitleNum; i++) {
            for (int j = 0; j < INFO_NUM; j++) {
                buffer[i * INFO_NUM + j] = mParcel.readInt();
            }
        }
        buffer[mSubtitleNum * INFO_NUM] = mParcel.readInt();
        buffer[mSubtitleNum * INFO_NUM + 1] = mParcel.readInt();
    };

    public int getSubtitleType() {
        if (mMeta.has(Metadata.SUBTITLE_TYPE)) {
            mSubtitleType = mMeta.getInt(Metadata.SUBTITLE_TYPE);
        } else {
            mSubtitleType = -2;
        }
        return mSubtitleType;
    }

    public int getSubtitleCodeType() {
        if (mMeta.has(Metadata.SUBTITLE_CODE_TYPE)) {
            mSubtitleCodeType = mMeta.getInt(Metadata.SUBTITLE_CODE_TYPE);
        } else {
            mSubtitleCodeType = -2;
        }
        return mSubtitleCodeType;
    }

    public String getSubtitleLanguageType(boolean isChinese) {
        if (SubtitleLanguageType == -1 && mMeta.has(Metadata.SUBTITLE_LANGUAGE_TYPE)) {
            SubtitleLanguageType = mMeta.getInt(Metadata.SUBTITLE_LANGUAGE_TYPE);
        }

        if (SubtitleLanguageType == 0) {
            if (isChinese) {
                mSubtitleLanguageType = ""; //ºº×ÖÂÒÂë£¬ÔÝÊ±È¥µô
            } else {
                mSubtitleLanguageType = "unknown";
            }
        } else if (SubtitleLanguageType == 1) {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "German";
            }
        } else if (SubtitleLanguageType == 2) {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "English";
            }
        } else if (SubtitleLanguageType == 3) {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "French";
            }
        } else if (SubtitleLanguageType == 4) {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "Italian";
            }
        } else if (SubtitleLanguageType == 5) {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "Russian";
            }
        } else if (SubtitleLanguageType == 6) {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "Chinese_Simplified";
            }
        } else if (SubtitleLanguageType == 7) {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "Chinese_Traditional";
            }
        } else {
            if (isChinese) {
                mSubtitleLanguageType = "";
            } else {
                mSubtitleLanguageType = "undefined";
            }
        }
        return mSubtitleLanguageType;
    }
    
    public void getSubtitleType(int[] info) {
        for (int i = 0; i < mSubtitleNum; i++) {
            info[i] = buffer[i * INFO_NUM];
        }
    }

    public void getSubtitleCodeType(int[] info) {
        for (int i = 0; i < mSubtitleNum; i++) {
            info[i] = buffer[i * INFO_NUM + 1];
        }
    }

    public void getSubtitleLanguageType(String[] info, boolean isChinese) {
        for (int i = 0; i < mSubtitleNum; i++) {
            SubtitleLanguageType = buffer[i * INFO_NUM + 2];
            info[i] = getSubtitleLanguageType(isChinese);
        }
    }

    public int getAllSubtitleCount(){
        return mSubtitleNum;
    }
  
    public int getAllInternalSubtitleCount() {
        return buffer[mSubtitleNum * INFO_NUM];
    }

    public int getAllImageSubtitleCount() {
        return buffer[mSubtitleNum * INFO_NUM + 1];
    }
  
}
