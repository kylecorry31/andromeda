/*
 * Copyright (C) 2021 The Android Open Source Project
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

#include <android/bitmap.h>
#include <cassert>
#include <jni.h>

#include "RenderScriptToolkit.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.JniEntryPoints"

using namespace renderscript;

/**
 * I compared using env->GetPrimitiveArrayCritical vs. env->GetByteArrayElements to get access
 * to the underlying data. On Pixel 4, it's actually faster to not use critical. The code is left
 * here if you want to experiment. Note that USE_CRITICAL could block the garbage collector.
 */
// #define USE_CRITICAL

class ByteArrayGuard {
private:
    JNIEnv *env;
    jbyteArray array;
    jbyte *data;

public:
    ByteArrayGuard(JNIEnv *env, jbyteArray array) : env{env}, array{array} {
#ifdef USE_CRITICAL
        data = reinterpret_cast<jbyte*>(env->GetPrimitiveArrayCritical(array, nullptr));
#else
        data = env->GetByteArrayElements(array, nullptr);
#endif
    }

    ~ByteArrayGuard() {
#ifdef USE_CRITICAL
        env->ReleasePrimitiveArrayCritical(array, data, 0);
#else
        env->ReleaseByteArrayElements(array, data, 0);
#endif
    }

    uint8_t *get() { return reinterpret_cast<uint8_t *>(data); }
};

class IntArrayGuard {
private:
    JNIEnv *env;
    jintArray array;
    jint *data;

public:
    IntArrayGuard(JNIEnv *env, jintArray array) : env{env}, array{array} {
#ifdef USE_CRITICAL
        data = reinterpret_cast<jint*>(env->GetPrimitiveArrayCritical(array, nullptr));
#else
        data = env->GetIntArrayElements(array, nullptr);
#endif
    }

    ~IntArrayGuard() {
#ifdef USE_CRITICAL
        env->ReleasePrimitiveArrayCritical(array, data, 0);
#else
        env->ReleaseIntArrayElements(array, data, 0);
#endif
    }

    int *get() { return reinterpret_cast<int *>(data); }
};

class FloatArrayGuard {
private:
    JNIEnv *env;
    jfloatArray array;
    jfloat *data;

public:
    FloatArrayGuard(JNIEnv *env, jfloatArray array) : env{env}, array{array} {
#ifdef USE_CRITICAL
        data = reinterpret_cast<jfloat*>(env->GetPrimitiveArrayCritical(array, nullptr));
#else
        data = env->GetFloatArrayElements(array, nullptr);
#endif
    }

    ~FloatArrayGuard() {
#ifdef USE_CRITICAL
        env->ReleasePrimitiveArrayCritical(array, data, 0);
#else
        env->ReleaseFloatArrayElements(array, data, 0);
#endif
    }

    float *get() { return reinterpret_cast<float *>(data); }
};

class BitmapGuard {
private:
    JNIEnv *env;
    jobject bitmap;
    AndroidBitmapInfo info;
    int bytesPerPixel;
    void *bytes;
    bool valid;

public:
    BitmapGuard(JNIEnv *env, jobject jBitmap) : env{env}, bitmap{jBitmap}, bytes{nullptr} {
        valid = false;
        if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS) {
            ALOGE("AndroidBitmap_getInfo failed");
            return;
        }
        if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 &&
            info.format != ANDROID_BITMAP_FORMAT_A_8) {
            ALOGE("AndroidBitmap in the wrong format");
            return;
        }
        bytesPerPixel = info.stride / info.width;
        if (bytesPerPixel != 1 && bytesPerPixel != 4) {
            ALOGE("Expected a vector size of 1 or 4. Got %d. Extra padding per line not currently "
                  "supported",
                  bytesPerPixel);
            return;
        }
        if (AndroidBitmap_lockPixels(env, bitmap, &bytes) != ANDROID_BITMAP_RESULT_SUCCESS) {
            ALOGE("AndroidBitmap_lockPixels failed");
            return;
        }
        valid = true;
    }

    ~BitmapGuard() {
        if (valid) {
            AndroidBitmap_unlockPixels(env, bitmap);
        }
    }

    uint8_t *get() const {
        assert(valid);
        return reinterpret_cast<uint8_t *>(bytes);
    }

    int width() const { return info.width; }

    int height() const { return info.height; }

    int vectorSize() const { return bytesPerPixel; }
};

/**
 * Copies the content of Kotlin Range2d object into the equivalent C++ struct.
 */
class RestrictionParameter {
private:
    bool isNull;
    Restriction restriction;

public:
    RestrictionParameter(JNIEnv *env, jobject jRestriction) : isNull{jRestriction == nullptr} {
        if (isNull) {
            return;
        }
        /* TODO Measure how long FindClass and related functions take. Consider passing the
         * four values instead. This would also require setting the default when Range2D is null.
         */
        jclass restrictionClass = env->FindClass("com/kylecorry/andromeda/bitmaps/Range2d");
        if (restrictionClass == nullptr) {
            ALOGE("RenderScriptToolit. Internal error. Could not find the Kotlin Range2d class.");
            isNull = true;
            return;
        }
        jfieldID startXId = env->GetFieldID(restrictionClass, "startX", "I");
        jfieldID startYId = env->GetFieldID(restrictionClass, "startY", "I");
        jfieldID endXId = env->GetFieldID(restrictionClass, "endX", "I");
        jfieldID endYId = env->GetFieldID(restrictionClass, "endY", "I");
        restriction.startX = env->GetIntField(jRestriction, startXId);
        restriction.startY = env->GetIntField(jRestriction, startYId);
        restriction.endX = env->GetIntField(jRestriction, endXId);
        restriction.endY = env->GetIntField(jRestriction, endYId);
    }

    Restriction *get() { return isNull ? nullptr : &restriction; }
};

extern "C" JNIEXPORT jlong JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_createNative(JNIEnv * /*env*/, jobject /*thiz*/) {
    return reinterpret_cast<jlong>(new RenderScriptToolkit());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_destroyNative(
        JNIEnv * /*env*/, jobject /*thiz*/, jlong native_handle) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    delete toolkit;
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeBlend(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jint jmode, jbyteArray source_array,
        jbyteArray dest_array, jint size_x, jint size_y, jobject restriction) {
    auto toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    auto mode = static_cast<RenderScriptToolkit::BlendingMode>(jmode);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard source{env, source_array};
    ByteArrayGuard dest{env, dest_array};

    toolkit->blend(mode, source.get(), dest.get(), size_x, size_y, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeBlendBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jint jmode, jobject source_bitmap,
        jobject dest_bitmap, jobject restriction) {
    auto toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    auto mode = static_cast<RenderScriptToolkit::BlendingMode>(jmode);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard source{env, source_bitmap};
    BitmapGuard dest{env, dest_bitmap};

    toolkit->blend(mode, source.get(), dest.get(), source.width(), source.height(), restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeBlur(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array, jint vectorSize,
        jint size_x, jint size_y, jint radius, jbyteArray output_array, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};

    toolkit->blur(input.get(), output.get(), size_x, size_y, vectorSize, radius, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeBlurBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jint radius, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};

    toolkit->blur(input.get(), output.get(), input.width(), input.height(), input.vectorSize(),
                  radius, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeColorMatrix(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jint input_vector_size, jint size_x, jint size_y, jbyteArray output_array,
        jint output_vector_size, jfloatArray jmatrix, jfloatArray add_vector, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};
    FloatArrayGuard matrix{env, jmatrix};
    FloatArrayGuard add{env, add_vector};

    toolkit->colorMatrix(input.get(), output.get(), input_vector_size, output_vector_size, size_x,
                         size_y, matrix.get(), add.get(), restrict.get());
}

extern "C" JNIEXPORT void JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeColorMatrixBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jfloatArray jmatrix, jfloatArray add_vector, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};
    FloatArrayGuard matrix{env, jmatrix};
    FloatArrayGuard add{env, add_vector};

    toolkit->colorMatrix(input.get(), output.get(), input.vectorSize(), output.vectorSize(),
                         input.width(), input.height(), matrix.get(), add.get(), restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeConvolve(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array, jint vectorSize,
        jint size_x, jint size_y, jbyteArray output_array, jfloatArray coefficients,
        jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};
    FloatArrayGuard coeffs{env, coefficients};

    switch (env->GetArrayLength(coefficients)) {
        case 9:
            toolkit->convolve3x3(input.get(), output.get(), vectorSize, size_x, size_y,
                                 coeffs.get(), restrict.get());
            break;
        case 25:
            toolkit->convolve5x5(input.get(), output.get(), vectorSize, size_x, size_y,
                                 coeffs.get(), restrict.get());
            break;
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeConvolveBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jfloatArray coefficients, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};
    FloatArrayGuard coeffs{env, coefficients};

    switch (env->GetArrayLength(coefficients)) {
        case 9:
            toolkit->convolve3x3(input.get(), output.get(), input.vectorSize(), input.width(),
                                 input.height(), coeffs.get(), restrict.get());
            break;
        case 25:
            toolkit->convolve5x5(input.get(), output.get(), input.vectorSize(), input.width(),
                                 input.height(), coeffs.get(), restrict.get());
            break;
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeHistogram(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jint vector_size, jint size_x, jint size_y, jintArray output_array, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    IntArrayGuard output{env, output_array};

    toolkit->histogram(input.get(), output.get(), size_x, size_y, vector_size, restrict.get());
}

extern "C" JNIEXPORT void JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeHistogramBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jintArray output_array, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    IntArrayGuard output{env, output_array};

    toolkit->histogram(input.get(), output.get(), input.width(), input.height(), input.vectorSize(),
                       restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeHistogramDot(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jint vector_size, jint size_x, jint size_y, jintArray output_array,
        jfloatArray coefficients, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    IntArrayGuard output{env, output_array};
    FloatArrayGuard coeffs{env, coefficients};

    toolkit->histogramDot(input.get(), output.get(), size_x, size_y, vector_size, coeffs.get(),
                          restrict.get());
}

extern "C" JNIEXPORT
void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeHistogramDotBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jintArray output_array, jfloatArray coefficients, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    IntArrayGuard output{env, output_array};
    FloatArrayGuard coeffs{env, coefficients};

    toolkit->histogramDot(input.get(), output.get(), input.width(), input.height(),
                          input.vectorSize(), coeffs.get(), restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeLut(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jbyteArray output_array, jint size_x, jint size_y, jbyteArray red_table,
        jbyteArray green_table, jbyteArray blue_table, jbyteArray alpha_table,
        jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};

    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};
    ByteArrayGuard red{env, red_table};
    ByteArrayGuard green{env, green_table};
    ByteArrayGuard blue{env, blue_table};
    ByteArrayGuard alpha{env, alpha_table};

    toolkit->lut(input.get(), output.get(), size_x, size_y, red.get(), green.get(), blue.get(),
                 alpha.get(), restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeLutBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jbyteArray red_table, jbyteArray green_table, jbyteArray blue_table,
        jbyteArray alpha_table, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};

    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};
    ByteArrayGuard red{env, red_table};
    ByteArrayGuard green{env, green_table};
    ByteArrayGuard blue{env, blue_table};
    ByteArrayGuard alpha{env, alpha_table};

    toolkit->lut(input.get(), output.get(), input.width(), input.height(), red.get(), green.get(),
                 blue.get(), alpha.get(), restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeLut3d(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jbyteArray output_array, jint size_x, jint size_y, jbyteArray cube_values, jint cubeSizeX,
        jint cubeSizeY, jint cubeSizeZ, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};
    ByteArrayGuard cube{env, cube_values};

    toolkit->lut3d(input.get(), output.get(), size_x, size_y, cube.get(), cubeSizeX, cubeSizeY,
                   cubeSizeZ, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeLut3dBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jbyteArray cube_values, jint cubeSizeX, jint cubeSizeY,
        jint cubeSizeZ, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};
    ByteArrayGuard cube{env, cube_values};

    toolkit->lut3d(input.get(), output.get(), input.width(), input.height(), cube.get(), cubeSizeX,
                   cubeSizeY, cubeSizeZ, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeResize(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jint vector_size, jint input_size_x, jint input_size_y, jbyteArray output_array,
        jint output_size_x, jint output_size_y, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};

    toolkit->resize(input.get(), output.get(), input_size_x, input_size_y, vector_size,
                    output_size_x, output_size_y, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeResizeBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};

    toolkit->resize(input.get(), output.get(), input.width(), input.height(), input.vectorSize(),
                    output.width(), output.height(), restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeYuvToRgb(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jbyteArray output_array, jint size_x, jint size_y, jint format) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};

    toolkit->yuvToRgb(input.get(), output.get(), size_x, size_y,
                      static_cast<RenderScriptToolkit::YuvFormat>(format));
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeYuvToRgbBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array, jint size_x,
        jint size_y, jobject output_bitmap, jint format) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    BitmapGuard output{env, output_bitmap};
    ByteArrayGuard input{env, input_array};

    toolkit->yuvToRgb(input.get(), output.get(), size_x, size_y,
                      static_cast<RenderScriptToolkit::YuvFormat>(format));
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeThreshold(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jbyteArray output_array, jint size_x, jint size_y, jfloat threshold,
        jboolean binary, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};

    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};

    toolkit->threshold(input.get(), output.get(), size_x, size_y, threshold, binary, channel,
                       restrict.get());
}

extern "C" JNIEXPORT void JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeThresholdBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jfloat threshold,
        jboolean binary, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};

    toolkit->threshold(input.get(), output.get(), input.width(), input.height(), threshold, binary,
                       channel, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeWeightedAdd(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array1,
        jbyteArray input_array2,
        jbyteArray output_array, jint size_x, jint size_y, jfloat weight1, jfloat weight2,
        jboolean absolute, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};

    ByteArrayGuard input1{env, input_array1};
    ByteArrayGuard input2{env, input_array2};
    ByteArrayGuard output{env, output_array};

    toolkit->weightedAdd(input1.get(), input2.get(), output.get(), size_x, size_y, weight1, weight2,
                         absolute,
                         restrict.get());
}

extern "C" JNIEXPORT void JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeWeightedAddBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap1,
        jobject input_bitmap2, jobject output_bitmap, jfloat weight1, jfloat weight2,
        jboolean absolute, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input1{env, input_bitmap1};
    BitmapGuard input2{env, input_bitmap2};
    BitmapGuard output{env, output_bitmap};

    toolkit->weightedAdd(input1.get(), input2.get(), output.get(), input1.width(), input1.height(),
                         weight1, weight2,
                         absolute, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeMinMax(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jfloatArray output_array, jint size_x,
        jint size_y, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    FloatArrayGuard output{env, output_array};

    toolkit->minMax(input.get(), output.get(), size_x, size_y, channel, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeMinMaxBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jfloatArray output_array, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    FloatArrayGuard output{env, output_array};

    toolkit->minMax(input.get(), output.get(), input.width(), input.height(), channel,
                    restrict.get());
}

extern "C" JNIEXPORT jdouble JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeAverage(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array, jint size_x,
        jint size_y, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};

    return toolkit->average(input.get(), size_x, size_y, channel, restrict.get());
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeAverageBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};

    return toolkit->average(input.get(), input.width(), input.height(), channel, restrict.get());
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeStandardDeviation(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array, jint size_x,
        jint size_y, jbyte channel, jdouble average, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};

    return toolkit->standardDeviation(input.get(), size_x, size_y, channel, average,
                                      restrict.get());
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeStandardDeviationBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jbyte channel, jdouble average, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};

    return toolkit->standardDeviation(input.get(), input.width(), input.height(), channel, average,
                                      restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeMoment(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jfloatArray output_array, jint size_x,
        jint size_y, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    FloatArrayGuard output{env, output_array};

    toolkit->moment(input.get(), output.get(), size_x, size_y, channel, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeMomentBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jfloatArray output_array, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    FloatArrayGuard output{env, output_array};

    toolkit->moment(input.get(), output.get(), input.width(), input.height(), channel,
                    restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeFindBlobs(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jintArray output_array, jint maxBlobs, jint size_x,
        jint size_y, jfloat threshold, jbyte channel, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    IntArrayGuard output{env, output_array};

    toolkit->findBlobs(input.get(), output.get(), maxBlobs, size_x, size_y, threshold, channel,
                       restrict.get());
}

extern "C" JNIEXPORT void JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeFindBlobsBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jintArray output_array, jint maxBlobs, jfloat threshold, jbyte channel,
        jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    IntArrayGuard output{env, output_array};

    toolkit->findBlobs(input.get(), output.get(), maxBlobs, input.width(), input.height(),
                       threshold, channel,
                       restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeGlcm(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jfloatArray output_array, jint size_x, jint size_y, jint levels, jbyte channel,
        jboolean symmetric, jboolean normalize, jboolean excludeTransparent, jintArray steps,
        jbyte stepCount, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    ByteArrayGuard input{env, input_array};
    FloatArrayGuard output{env, output_array};
    IntArrayGuard stepArray{env, steps};

    toolkit->glcm(input.get(), output.get(), size_x, size_y, levels, channel, symmetric, normalize,
                  excludeTransparent, stepArray.get(), stepCount, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeGlcmBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jfloatArray output_array, jint levels, jbyte channel, jboolean symmetric,
        jboolean normalize, jboolean excludeTransparent, jintArray steps, jbyte stepCount,
        jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    FloatArrayGuard output{env, output_array};
    IntArrayGuard stepArray{env, steps};

    toolkit->glcm(input.get(), output.get(), input.width(), input.height(), levels, channel,
                  symmetric,
                  normalize, excludeTransparent, stepArray.get(), stepCount, restrict.get());
}

extern "C" JNIEXPORT void JNICALL Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeColorReplace(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jbyteArray input_array,
        jbyteArray output_array, jint size_x, jint size_y, jbyte targetR, jbyte targetG,
        jbyte targetB, jbyte targetA, jbyte replacementR, jbyte replacementG, jbyte replacementB,
        jbyte replacementA, jfloat tolerance, jboolean interpolate, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};

    ByteArrayGuard input{env, input_array};
    ByteArrayGuard output{env, output_array};

    toolkit->colorReplace(input.get(), output.get(), size_x, size_y, targetR, targetG, targetB,
                          targetA, replacementR, replacementG, replacementB, replacementA,
                          tolerance, interpolate, restrict.get());
}

extern "C" JNIEXPORT void JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeColorReplaceBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap, jbyte targetR, jbyte targetG, jbyte targetB, jbyte targetA,
        jbyte replacementR, jbyte replacementG, jbyte replacementB, jbyte replacementA,
        jfloat tolerance, jboolean interpolate, jobject restriction) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    RestrictionParameter restrict{env, restriction};
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};

    toolkit->colorReplace(input.get(), output.get(), input.width(), input.height(), targetR,
                          targetG, targetB, targetA, replacementR, replacementG, replacementB,
                          replacementA, tolerance, interpolate, restrict.get());
}

extern "C" JNIEXPORT void JNICALL
Java_com_kylecorry_andromeda_bitmaps_Toolkit_nativeXbr2xBitmap(
        JNIEnv *env, jobject /*thiz*/, jlong native_handle, jobject input_bitmap,
        jobject output_bitmap) {
    RenderScriptToolkit *toolkit = reinterpret_cast<RenderScriptToolkit *>(native_handle);
    BitmapGuard input{env, input_bitmap};
    BitmapGuard output{env, output_bitmap};

    toolkit->xbr2x(input.get(), output.get(), input.width(), input.height());
}