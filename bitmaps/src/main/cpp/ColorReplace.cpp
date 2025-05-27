/*
 * Copyright (C) 2012 The Android Open Source Project
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

#include <cstdint>
#include <cmath>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.ColorReplace"

namespace renderscript {

    class ColorReplaceTask : public Task {
        const uchar4 *mIn;
        uchar4 *mOut;
        uchar4 mTargetColor;
        uchar4 mReplacementColor;
        float mTolerance;
        bool mInterpolate;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        ColorReplaceTask(const uint8_t *input, uint8_t *output, size_t sizeX, size_t sizeY,
                         uchar4 targetColor, uchar4 replacementColor, float tolerance,
                         bool interpolate, const Restriction *restriction)
                : Task{sizeX, sizeY, 4, true, restriction},
                  mIn{reinterpret_cast<const uchar4 *>(input)},
                  mOut{reinterpret_cast<uchar4 *>(output)},
                  mTargetColor{targetColor},
                  mReplacementColor{replacementColor},
                  mTolerance{tolerance},
                  mInterpolate{interpolate} {}
    };

    void
    ColorReplaceTask::processData(int /* threadIndex */, size_t startX, size_t startY, size_t endX,
                                  size_t endY) {
        for (size_t y = startY; y < endY; y++) {
            size_t offset = mSizeX * y + startX;
            const uchar4 *in = mIn + offset;
            uchar4 *out = mOut + offset;
            for (size_t x = startX; x < endX; x++) {
                auto v = *in;

                // Calculate color distance using Euclidean distance in RGBA space
                auto dr = (float) (v.r - mTargetColor.r);
                auto dg = (float) (v.g - mTargetColor.g);
                auto db = (float) (v.b - mTargetColor.b);
                auto da = (float) (v.a - mTargetColor.a);
                float distance = sqrt(dr * dr + dg * dg + db * db + da * da);

                if (distance <= mTolerance) {
                    if (mInterpolate) {
                        // Interpolate color if within tolerance
                        float ratio = 1.0f - distance / mTolerance;
                        auto r = static_cast<uint8_t>((float) v.r +
                                                      ratio * (float) (mReplacementColor.r - v.r));
                        auto g = static_cast<uint8_t>((float) v.g +
                                                      ratio * (float) (mReplacementColor.g - v.g));
                        auto b = static_cast<uint8_t>((float) v.b +
                                                      ratio * (float) (mReplacementColor.b - v.b));
                        auto a = static_cast<uint8_t>((float) v.a +
                                                      ratio * (float) (mReplacementColor.a - v.a));
                        *out = uchar4{r, g, b, a};
                    } else {
                        *out = mReplacementColor;
                    }
                } else {
                    *out = v;
                }

                in++;
                out++;
            }
        }
    }

    void RenderScriptToolkit::colorReplace(const uint8_t *input, uint8_t *output, size_t sizeX,
                                           size_t sizeY, uint8_t targetR, uint8_t targetG,
                                           uint8_t targetB, uint8_t targetA,
                                           uint8_t replacementR, uint8_t replacementG,
                                           uint8_t replacementB, uint8_t replacementA,
                                           float tolerance, bool interpolate,
                                           const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return;
        }
#endif

        uchar4 targetColor{targetR, targetG, targetB, targetA};
        uchar4 replacementColor{replacementR, replacementG, replacementB, replacementA};
        ColorReplaceTask task(input, output, sizeX, sizeY, targetColor, replacementColor, tolerance,
                              interpolate, restriction);
        processor->doTask(&task);
    }

}  // namespace renderscript