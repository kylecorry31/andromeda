#include <cstdint>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.Moment"

namespace renderscript {

    class MomentTask : public Task {
        const uchar4 *mIn;
        const uint8_t mChannel;
        const uint32_t mThreadCount;
        std::vector<double> mTotals;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        MomentTask(const uint8_t *input, size_t sizeX, size_t sizeY, uint8_t channel,
                   uint32_t threadCount, const Restriction *restriction)
                : Task{sizeX, sizeY, 4, false, restriction},
                  mIn{reinterpret_cast<const uchar4 *>(input)},
                  mChannel{channel},
                  mThreadCount{threadCount},
                  mTotals(3 * threadCount) {}

        void collate(float *out);
    };

    void
    MomentTask::processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                            size_t endY) {
        for (size_t y = startY; y < endY; y++) {
            size_t offset = mSizeX * y + startX;
            const uchar4 *in = mIn + offset;

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

                mTotals[threadIndex * 3] += x * value;
                mTotals[threadIndex * 3 + 1] += y * value;
                mTotals[threadIndex * 3 + 2] += value;

                in++;
            }
        }
    }

    void MomentTask::collate(float *out) {
        double momentX = 0;
        double momentY = 0;
        double total = 0;
        for (uint32_t t = 0; t < mThreadCount; t++) {
            momentX += mTotals[t * 3];
            momentY += mTotals[t * 3 + 1];
            total += mTotals[t * 3 + 2];
        }

        if (total == 0) {
            out[0] = 0;
            out[1] = 0;
            return;
        }

        momentX /= total;
        momentY /= total;
        out[0] = (float) momentX;
        out[1] = (float) momentY;
    }

    void RenderScriptToolkit::moment(const uint8_t *input, float *output, size_t sizeX,
                                     size_t sizeY, uint8_t channel,
                                     const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return;
        }
#endif

        MomentTask task(input, sizeX, sizeY, channel, processor->getNumberOfThreads(), restriction);
        processor->doTask(&task);
        task.collate(output);
    }

}  // namespace renderscript
