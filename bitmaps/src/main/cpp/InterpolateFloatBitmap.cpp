#include <algorithm>
#include <cmath>
#include <cstdint>
#include <limits>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.InterpolateFloatBitmap"

namespace renderscript {

    static inline float cubicWeight(float t) {
        float a = std::fabs(t);
        if (a <= 1.0f) {
            return 1.0f - 2.5f * a * a + 1.5f * a * a * a;
        } else if (a <= 2.0f) {
            return 2.0f - 4.0f * a + 2.5f * a * a - 0.5f * a * a * a;
        }
        return 0.0f;
    }

    static inline bool isValidFloat(float v) {
        return !std::isnan(v);
    }

    static inline const float *getFloatPixel(const float *input, int x, int y,
                                              int width, int height, int channels) {
        if (x < 0 || x >= width || y < 0 || y >= height) return nullptr;
        return &input[(y * width + x) * channels];
    }

    static bool interpolateBicubic(const float *input, int width, int height, int channels,
                                    float fx, float fy, float *result) {
        int xInt = static_cast<int>(std::floor(fx));
        int yInt = static_cast<int>(std::floor(fy));
        float fracX = fx - xInt;
        float fracY = fy - yInt;

        float rowVals[4];
        for (int c = 0; c < channels; c++) {
            for (int i = 0; i < 4; i++) {
                float value = 0.0f;
                for (int j = 0; j < 4; j++) {
                    int cx = xInt + j - 1;
                    int cy = yInt + i - 1;
                    const float *pixel = getFloatPixel(input, cx, cy, width, height, channels);
                    if (pixel == nullptr || !isValidFloat(pixel[c])) return false;
                    value += pixel[c] * cubicWeight(fracX - (j - 1));
                }
                rowVals[i] = value;
            }
            float finalVal = 0.0f;
            for (int i = 0; i < 4; i++) {
                finalVal += rowVals[i] * cubicWeight(fracY - (i - 1));
            }
            result[c] = finalVal;
        }
        return true;
    }

    static bool interpolateBilinear(const float *input, int width, int height, int channels,
                                     float fx, float fy, float *result) {
        int x0 = static_cast<int>(fx);
        int y0 = static_cast<int>(fy);
        int x1 = x0 + 1;
        int y1 = y0 + 1;

        const float *p00 = getFloatPixel(input, x0, y0, width, height, channels);
        const float *p10 = getFloatPixel(input, x1, y0, width, height, channels);
        const float *p01 = getFloatPixel(input, x0, y1, width, height, channels);
        const float *p11 = getFloatPixel(input, x1, y1, width, height, channels);

        if (!p00 || !p10 || !p01 || !p11) return false;

        float dx = fx - x0;
        float dy = fy - y0;
        float w00 = (1.0f - dx) * (1.0f - dy);
        float w10 = dx * (1.0f - dy);
        float w01 = (1.0f - dx) * dy;
        float w11 = dx * dy;

        for (int c = 0; c < channels; c++) {
            if (!isValidFloat(p00[c]) || !isValidFloat(p10[c]) ||
                !isValidFloat(p01[c]) || !isValidFloat(p11[c])) {
                return false;
            }
            result[c] = p00[c] * w00 + p10[c] * w10 + p01[c] * w01 + p11[c] * w11;
        }
        return true;
    }

    static bool interpolateNearest(const float *input, int width, int height, int channels,
                                    float fx, float fy, int maxSearchRadius, float *result) {
        int xInt = static_cast<int>(std::round(fx));
        int yInt = static_cast<int>(std::round(fy));

        float bestDist = std::numeric_limits<float>::max();
        bool found = false;

        auto process = [&](int cx, int cy) {
            const float *pixel = getFloatPixel(input, cx, cy, width, height, channels);
            if (pixel == nullptr) return;
            for (int c = 0; c < channels; c++) {
                if (!isValidFloat(pixel[c])) return;
            }
            float dx = cx - fx;
            float dy = cy - fy;
            float dist = dx * dx + dy * dy;
            if (dist < bestDist) {
                bestDist = dist;
                found = true;
                for (int c = 0; c < channels; c++) {
                    result[c] = pixel[c];
                }
            }
        };

        for (int r = 0; r <= maxSearchRadius; r++) {
            if (r == 0) {
                process(xInt, yInt);
            } else {
                int left = xInt - r;
                int right = xInt + r;
                int top = yInt - r;
                int bottom = yInt + r;
                for (int cx = left; cx <= right; cx++) {
                    process(cx, top);
                    process(cx, bottom);
                }
                for (int cy = top + 1; cy < bottom; cy++) {
                    process(left, cy);
                    process(right, cy);
                }
            }
            if (found) return true;
        }
        return found;
    }

    class InterpolateFloatBitmapTask : public Task {
        const float *mIn;
        float *mOut;
        int mInputWidth;
        int mInputHeight;
        int mChannels;
        int mOutputWidth;
        int mOutputHeight;
        float mSrcStartX;
        float mSrcStartY;
        float mSrcEndX;
        float mSrcEndY;
        int mMaxSearchRadius;

        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        InterpolateFloatBitmapTask(const float *input, float *output,
                                    int inputWidth, int inputHeight, int channels,
                                    int outputWidth, int outputHeight,
                                    float srcStartX, float srcStartY,
                                    float srcEndX, float srcEndY,
                                    int maxSearchRadius,
                                    const Restriction *restriction)
                : Task{static_cast<size_t>(outputWidth), static_cast<size_t>(outputHeight),
                       4, false, restriction},
                  mIn{input}, mOut{output},
                  mInputWidth{inputWidth}, mInputHeight{inputHeight},
                  mChannels{channels},
                  mOutputWidth{outputWidth}, mOutputHeight{outputHeight},
                  mSrcStartX{srcStartX}, mSrcStartY{srcStartY},
                  mSrcEndX{srcEndX}, mSrcEndY{srcEndY},
                  mMaxSearchRadius{maxSearchRadius} {}
    };

    void InterpolateFloatBitmapTask::processData(int /* threadIndex */, size_t startX,
                                                  size_t startY, size_t endX, size_t endY) {
        float tempPixel[4];

        for (size_t y = startY; y < endY; y++) {
            for (size_t x = startX; x < endX; x++) {
                float inputX = (mOutputWidth > 1)
                               ? mSrcStartX + (mSrcEndX - mSrcStartX) *
                                               (static_cast<float>(x) / (mOutputWidth - 1))
                               : mSrcStartX;
                float inputY = (mOutputHeight > 1)
                               ? mSrcStartY + (mSrcEndY - mSrcStartY) *
                                               (static_cast<float>(y) / (mOutputHeight - 1))
                               : mSrcStartY;

                bool success = interpolateBicubic(mIn, mInputWidth, mInputHeight, mChannels,
                                                  inputX, inputY, tempPixel);
                if (!success) {
                    success = interpolateBilinear(mIn, mInputWidth, mInputHeight, mChannels,
                                                  inputX, inputY, tempPixel);
                }
                if (!success) {
                    success = interpolateNearest(mIn, mInputWidth, mInputHeight, mChannels,
                                                 inputX, inputY, mMaxSearchRadius, tempPixel);
                }

                size_t outIdx = (y * mOutputWidth + x) * mChannels;
                if (success) {
                    for (int c = 0; c < mChannels; c++) {
                        mOut[outIdx + c] = tempPixel[c];
                    }
                } else {
                    for (int c = 0; c < mChannels; c++) {
                        mOut[outIdx + c] = std::numeric_limits<float>::quiet_NaN();
                    }
                }
            }
        }
    }

    void RenderScriptToolkit::interpolateFloatBitmap(const float *input, float *output,
                                                      size_t inputWidth, size_t inputHeight,
                                                      size_t channels,
                                                      size_t outputWidth, size_t outputHeight,
                                                      float srcStartX, float srcStartY,
                                                      float srcEndX, float srcEndY,
                                                      int maxSearchRadius) {
        InterpolateFloatBitmapTask task(input, output,
                                        static_cast<int>(inputWidth),
                                        static_cast<int>(inputHeight),
                                        static_cast<int>(channels),
                                        static_cast<int>(outputWidth),
                                        static_cast<int>(outputHeight),
                                        srcStartX, srcStartY, srcEndX, srcEndY,
                                        maxSearchRadius, nullptr);
        processor->doTask(&task);
    }

}  // namespace renderscript
