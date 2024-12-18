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

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.Threshold"

namespace renderscript {

    class ThresholdTask : public Task {
        const uchar4 *mIn;
        uchar4 *mOut;
        float mThreshold;
        uint8_t mChannel;
        bool mBinary;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        ThresholdTask(const uint8_t *input, uint8_t *output, size_t sizeX, size_t sizeY,
                      float threshold, bool binary, uint8_t channel,
                      const Restriction *restriction)
                : Task{sizeX, sizeY, 4, true, restriction},
                  mIn{reinterpret_cast<const uchar4 *>(input)},
                  mOut{reinterpret_cast<uchar4 *>(output)},
                  mThreshold{threshold},
                  mChannel{channel},
                  mBinary{binary} {}
    };

    void
    ThresholdTask::processData(int /* threadIndex */, size_t startX, size_t startY, size_t endX,
                               size_t endY) {
        for (size_t y = startY; y < endY; y++) {
            size_t offset = mSizeX * y + startX;
            const uchar4 *in = mIn + offset;
            uchar4 *out = mOut + offset;
            for (size_t x = startX; x < endX; x++) {
                auto v = *in;
                double value;
                if (mChannel == 0) {
                    value = v.r;
                } else if (mChannel == 1) {
                    value = v.g;
                } else if (mChannel == 2) {
                    value = v.b;
                } else if (mChannel == 3) {
                    value = v.a;
                } else {
                    value = (v.r + v.g + v.b) / 3.0;
                }

                if (value > mThreshold && !mBinary) {
                    *out = uchar4{v.r, v.g, v.b, v.a};
                } else if (mChannel < 0 || mChannel > 3) {
                    uchar replacement = value > mThreshold ? 255 : 0;
                    *out = uchar4{replacement, replacement, replacement, v.a};
                } else {
                    uchar replacement = value > mThreshold ? 255 : 0;
                    *out = uchar4{v.r, v.g, v.b, v.a};
                    (*out)[mChannel] = replacement;
                }
                in++;
                out++;
            }
        }
    }

    void RenderScriptToolkit::threshold(const uint8_t *input, uint8_t *output, size_t sizeX,
                                        size_t sizeY,
                                        float threshold, bool binary, uint8_t channel,
                                        const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return;
        }
#endif

        ThresholdTask task(input, output, sizeX, sizeY, threshold, binary, channel, restriction);
        processor->doTask(&task);
    }

}  // namespace renderscript
