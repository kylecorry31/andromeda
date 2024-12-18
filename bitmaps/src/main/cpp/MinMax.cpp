#include <cstdint>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.MinMax"

namespace renderscript {

    class MinMaxTask : public Task {
        const uchar4 *mIn;
        const uint8_t mChannel;
        const uint32_t mThreadCount;
        std::vector<float> mTotals;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        MinMaxTask(const uint8_t *input, size_t sizeX, size_t sizeY, uint8_t channel,
                   uint32_t threadCount, const Restriction *restriction)
                : Task{sizeX, sizeY, 4, true, restriction},
                  mIn{reinterpret_cast<const uchar4 *>(input)},
                  mChannel{channel},
                  mThreadCount{threadCount},
                  mTotals(2 * threadCount) {
            for (uint32_t t = 0; t < mThreadCount; t++) {
                mTotals[t * 2] = 255;
                mTotals[t * 2 + 1] = 0;
            }
        }

        void collate(float *out);
    };

    void
    MinMaxTask::processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                            size_t endY) {
        for (size_t y = startY; y < endY; y++) {
            size_t offset = mSizeX * y + startX;
            const uchar4 *in = mIn + offset;
            for (size_t x = startX; x < endX; x++) {
                auto v = *in;
                float value;
                if (mChannel == 0) {
                    value = v.r;
                } else if (mChannel == 1) {
                    value = v.g;
                } else if (mChannel == 2) {
                    value = v.b;
                } else if (mChannel == 3) {
                    value = v.a;
                } else {
                    value = (float) ((v.r + v.g + v.b) / 3.0);
                }

                if (value < mTotals[threadIndex * 2]) {
                    mTotals[threadIndex * 2] = value;
                }

                if (value > mTotals[threadIndex * 2 + 1]) {
                    mTotals[threadIndex * 2 + 1] = value;
                }

                in++;
            }
        }
    }

    void MinMaxTask::collate(float *out) {
        float min = 255;
        float max = 0;
        for (uint32_t t = 0; t < mThreadCount; t++) {
            if (mTotals[t * 2] < min) {
                min = mTotals[t * 2];
            }

            if (mTotals[t * 2 + 1] > max) {
                max = mTotals[t * 2 + 1];
            }
        }
        out[0] = min;
        out[1] = max;
    }

    void RenderScriptToolkit::minMax(const uint8_t *input, float *output, size_t sizeX,
                                     size_t sizeY, uint8_t channel,
                                     const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return;
        }
#endif

        MinMaxTask task(input, sizeX, sizeY, channel, processor->getNumberOfThreads(), restriction);
        processor->doTask(&task);
        task.collate(output);
    }

}  // namespace renderscript
