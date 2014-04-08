#include <jni.h>
#include <android/log.h>

/*standard library*/
#include <time.h>
#include <math.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>
#include <stdbool.h>
#include<time.h>

/*ffmpeg headers*/
#include <libavutil/avstring.h>
#include <libavutil/pixdesc.h>
#include <libavutil/imgutils.h>
#include <libavutil/samplefmt.h>
#include <libavutil/opt.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavcodec/avcodec.h>
#include <libavcodec/avfft.h>

//#include <x264.h>

#define CODEC_ID_H263 263
#define CODEC_ID_H264 264

/*
 128x96
 176x144
 352x288
 704x576
 1408x1152
 * */

#define RES_128x96 1
#define RES_176x144 2
#define RES_352x288 3
#define RES_704x576 4
#define RES_1408x1152 5

//variables for encoder
AVCodec *codec;
AVCodecContext *codecCtx;

//variables for decoder
AVCodec *dcodec;
AVCodecContext *dcodecCtx;

/***************************functions registers all available file formats and codecs with the library************************/
JNIEXPORT void JNICALL Java_com_revesoft_itelmobiledialer_video_decoding_FFMPEGDecoder_register(JNIEnv* env,jobject obj)
{
	//__android_log_write(ANDROID_LOG_INFO, "debug", "b4 register");
	av_register_all();
	avcodec_register_all();
	__android_log_write(ANDROID_LOG_INFO, "ffmpeg_decoder", "codec registered");
}

JNIEXPORT jint JNICALL Java_com_revesoft_itelmobiledialer_video_decoding_FFMPEGDecoder_initdecoder(
		JNIEnv* env, jobject obj, jint id, jint res_id) {
	if (id == CODEC_ID_H264)
		dcodec = avcodec_find_decoder(AV_CODEC_ID_H264);
	else if (id == CODEC_ID_H263)
		dcodec = avcodec_find_decoder(AV_CODEC_ID_H263);
	else {
		__android_log_write(ANDROID_LOG_INFO, "ffmpeg_decoder",
				"Wrong codec id!! decoder not found");
		return -2;
	}
	if (dcodec == NULL) {
		__android_log_write(ANDROID_LOG_INFO, "ffmpeg_decoder",
				"decoder not found");
		return -3;
	}

	dcodecCtx = avcodec_alloc_context3(dcodec);

	// set properties for decoder. h264 packet contains information how it has been decoded. we dont need to specify them as we already did that part in initencoder function

	dcodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;

	if (res_id == RES_128x96) {
		dcodecCtx->width = 128;
		dcodecCtx->height = 96;
	} else if (res_id == RES_176x144) {
		dcodecCtx->width = 176;
		dcodecCtx->height = 144;
	} else if (res_id == RES_352x288) {
		dcodecCtx->width = 352;
		dcodecCtx->height = 288;
	} else if (res_id == RES_704x576) {
		dcodecCtx->width = 704;
		dcodecCtx->height = 576;
	} else if (res_id == RES_1408x1152) {
		dcodecCtx->width = 1408;
		dcodecCtx->height = 1152;
	}

	dcodecCtx->flags2 |= CODEC_FLAG2_FAST;

	if (avcodec_open2(dcodecCtx, dcodec, NULL) < 0) {
		__android_log_write(ANDROID_LOG_INFO, "ffmpeg_decoder",
				"decoder not opened");
		return -4;
	}

	return 1;
}

JNIEXPORT jint JNICALL Java_com_revesoft_itelmobiledialer_video_decoding_FFMPEGDecoder_getHeight(
		JNIEnv* env, jobject obj) {
	return dcodecCtx->height;
}

JNIEXPORT jint JNICALL Java_com_revesoft_itelmobiledialer_video_decoding_FFMPEGDecoder_getWidth(
		JNIEnv* env, jobject obj) {
	return dcodecCtx->width;
}

JNIEXPORT void JNICALL Java_com_revesoft_itelmobiledialer_video_decoding_FFMPEGDecoder_closedecoder(JNIEnv* env,jobject obj)
{
	avcodec_close(dcodecCtx);
	av_free(dcodecCtx);
}

/********** as our data is nv21 format. encoder expects that provided data will be in yuv420p format ********/
/********** so we need to convert data from nv21 to yuv420p format ********/
static uint8_t * nv21_to_yuv420p(uint8_t * data, unsigned width,
		unsigned height) {

	uint8_t * buf_source = data;
	int len_target = (width * height * 3) / 2;
	uint8_t * buf_target = (uint8_t *) av_malloc(len_target * sizeof(uint8_t));
	memcpy(buf_target, buf_source, width * height);
	register unsigned i;
	for (i = 0; i < (width * height / 4); i++) {
		buf_target[(width * height) + i] = buf_source[(width * height) + 2 * i
				+ 1];
		buf_target[(width * height) + (width * height / 4) + i] =
				buf_source[(width * height) + 2 * i];
	}
	return buf_target;
}

JNIEXPORT jintArray JNICALL Java_com_revesoft_itelmobiledialer_video_decoding_FFMPEGDecoder_decode(
		JNIEnv* env, jobject obj, jint datasize, jbyteArray Data) {

	int decode;
	int got_frame;

	uint8_t *rgb_buffer;
	AVFrame *dframe;
	AVFrame *dframergb;
	AVPacket dc_packet;

	jboolean iscopy = JNI_TRUE;
	jbyte * packData = (*env)->GetByteArrayElements(env, Data, &iscopy);

	/*** this frame contains output value after decoding ***/
	dframe = avcodec_alloc_frame();
	dframe->format = dcodecCtx->pix_fmt; //pixel format is yuv420p
	dframe->width = dcodecCtx->width;
	dframe->height = dcodecCtx->height;

	/***we use this frame for yuv to rgb conversion, used in sws_scale() function as destination***/
	dframergb = avcodec_alloc_frame();
	dframergb->format = AV_PIX_FMT_RGB32; //ARGB_8888 configuration assumes rgb pixel data is stored in 4 bytes
	dframergb->width = dcodecCtx->width;
	dframergb->height = dcodecCtx->height;

	int num_bytes = avpicture_get_size(AV_PIX_FMT_RGB32, dcodecCtx->width,
			dcodecCtx->height);
	rgb_buffer = (uint8_t *) av_malloc(num_bytes * sizeof(uint8_t));

	avpicture_fill((AVPicture*) dframergb, rgb_buffer, PIX_FMT_RGB32,
			dcodecCtx->width, dcodecCtx->height); // filled frame with a blank picture.

	av_init_packet(&dc_packet); // crate a new packet with encoded data and size

	/***** initialize packet *****/
	dc_packet.data = packData;
	dc_packet.size = datasize;

	/*Decode a frame of video. Takes input raw video data from frame and writes the next output packet, if available, to avpkt*/
	// dc_packet = Contains the input buffer
	// dframe = The AVFrame in which the decoded video frame will be stored
	// got_frame = 0 if no frame could be de-compressed, otherwise, it is non-zero.
	decode = avcodec_decode_video2(dcodecCtx, dframe, &got_frame, &dc_packet);

	if (got_frame > 0) {
		__android_log_write(ANDROID_LOG_INFO, "ffmpeg_decoder",
				"decoding successful");
		struct SwsContext* scaleContext = sws_getContext(dcodecCtx->width,
				dcodecCtx->height, AV_PIX_FMT_YUV420P, dcodecCtx->width,
				dcodecCtx->height,
				AV_PIX_FMT_RGB32, SWS_FAST_BILINEAR, NULL, NULL, NULL);

		/***** frame conversion from yuv to rgb *****/
		sws_scale(scaleContext, (const uint8_t * const *) dframe->data,
				dframe->linesize, 0, dcodecCtx->height, dframergb->data,
				dframergb->linesize);
		sws_freeContext(scaleContext); //must be freed after use
	} else
		__android_log_write(ANDROID_LOG_INFO, "ffmpeg_decoder",
				"decoding failed");

	/***** Get data in int array from frame. we use int array since Bitmap.createBitmap funtion
	 that create bitmap from rgb data takes int array as input *****/
	jintArray decoded_data = (*env)->NewIntArray(env,
			dcodecCtx->width * dcodecCtx->height);
	(*env)->SetIntArrayRegion(env, decoded_data, 0,
			dcodecCtx->width * dcodecCtx->height, (jint*) dframergb->data[0]);

	/*****free all after use*****/
	av_free_packet(&dc_packet);
	av_free(rgb_buffer);
	av_free(dframergb);
	av_free(dframe);

	(*env)->ReleaseByteArrayElements(env, Data, packData, JNI_ABORT);

	return decoded_data;
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

