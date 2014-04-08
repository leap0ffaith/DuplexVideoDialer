package com.revesoft.itelmobiledialer.video.utility;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
public class CameraProfileChecker {
	
//	private final int[] qualityLevel = {CamcorderProfile.QUALITY_1080P, CamcorderProfile.QUALITY_480P,
//		CamcorderProfile.QUALITY_720P, CamcorderProfile.QUALITY_CIF, CamcorderProfile.QUALITY_HIGH,
//		CamcorderProfile.QUALITY_LOW, CamcorderProfile.QUALITY_QCIF, CamcorderProfile.QUALITY_QVGA,
//		CamcorderProfile.QUALITY_TIME_LAPSE_1080P, CamcorderProfile.QUALITY_TIME_LAPSE_480P,
//		CamcorderProfile.QUALITY_TIME_LAPSE_720P, CamcorderProfile.QUALITY_TIME_LAPSE_CIF,
//		CamcorderProfile.QUALITY_TIME_LAPSE_HIGH, CamcorderProfile.QUALITY_TIME_LAPSE_LOW,
//		CamcorderProfile.QUALITY_TIME_LAPSE_QCIF, CamcorderProfile.QUALITY_TIME_LAPSE_QVGA
//	};
	
	private static final int TOTAL_QUALITY_LEVELS = 16;
	
	private final int[] supportedQualityLevels = new int[TOTAL_QUALITY_LEVELS];
	private int totalQualityLevelsSupported = 0;
	
//	public void checkProfile(){
//	
//		for(int i = 0; i < qualityLevel.length; i++){
//			if (CamcorderProfile.hasProfile(qualityLevel[i])){
//				CamcorderProfile profile = CamcorderProfile.get(qualityLevel[i]);
//				
//				if(profile.fileFormat == MediaRecorder.OutputFormat.MPEG_4 
//						&& profile.videoCodec == MediaRecorder.VideoEncoder.H264){
//					supportedQualityLevels[totalQualityLevelsSupported++] = qualityLevel[i];
//				}
//				
//				Log.d("DEBUG", "Quality: " + profile.quality);
//				Log.d("DEBUG", "File format: " + profile.fileFormat);
//			    Log.d("DEBUG", "VideoBitRate: " + profile.videoBitRate);
//				Log.d("DEBUG", "VideoCodec: " + profile.videoCodec);
//				Log.d("DEBUG", "Frame Rate: " + profile.videoFrameRate);
//				Log.d("DEBUG", "Frame Height: " + profile.videoFrameHeight);
//				Log.d("DEBUG", "Frame Width: " + profile.videoFrameWidth);
//				Log.d("DEBUG", "End of Profile values");
//			}
//		}
//		
//	}
	
	public int[] getSupportedQualityLevels(){
		int[] qualityLevels = new int[totalQualityLevelsSupported];
		System.arraycopy(supportedQualityLevels, 0, qualityLevels, 0, totalQualityLevelsSupported);
		return qualityLevels;
	}
	
	
	

}
