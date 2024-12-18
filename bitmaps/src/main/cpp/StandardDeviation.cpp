#include <cstdint>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.StandardDeviation"

namespace renderscript {

    class StandardDeviationTask : public Task {
        const uchar4 *mIn;
        const uint8_t mChannel;
        const uint32_t mThreadCount;
        const double mAverage;
        std::vector<double> mTotals;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        StandardDeviationTask(const uint8_t *input, size_t sizeX, size_t sizeY, uint8_t channel,
                              double average, uint32_t threadCount, const Restriction *restriction)
                : Task{sizeX, sizeY, 4, true, restriction},
                  mIn{reinterpret_cast<const uchar4 *>(input)},
                  mChannel{channel},
                  mThreadCount{threadCount},
                  mAverage{average},
                  mTotals(threadCount) {}

        double collate();
    };

    void
    StandardDeviationTask::processData(int threadIndex, size_t startX, size_t startY, size_t endX,
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

                double diff = value - mAverage;

                mTotals[threadIndex] += diff * diff;

                in++;
            }
        }
    }

    double StandardDeviationTask::collate() {
        double sum = 0;
        for (uint32_t t = 0; t < mThreadCount; t++) {
            sum += mTotals[t];
        }
        return sqrt(sum / (mSizeX * mSizeY));
    }

    double RenderScriptToolkit::standardDeviation(const uint8_t *input, size_t sizeX,
                                                  size_t sizeY, uint8_t channel, double average,
                                                  const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return 0;
        }
#endif

        StandardDeviationTask task(input, sizeX, sizeY, channel, average,
                                   processor->getNumberOfThreads(), restriction);
        processor->doTask(&task);
        return task.collate();
    }

}  // namespace renderscript
