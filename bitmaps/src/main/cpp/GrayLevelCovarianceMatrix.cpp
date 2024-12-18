#include <cstdint>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.GrayLevelCovarianceMatrix"

namespace renderscript {

    class GrayLevelCovarianceMatrixTask : public Task {
        const uchar4 *mIn;
        const uint8_t mChannel;
        const size_t mLevels;
        const bool mSymmetric;
        const bool mNormalize;
        const bool mExcludeTransparent;
        const uint8_t mStepCount;
        const int *mSteps;
        std::vector<size_t> mTotals;
        std::vector<std::vector<size_t>> mGlcm;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

        uchar quantize(float value) const;

    public:
        GrayLevelCovarianceMatrixTask(const uint8_t *input, size_t sizeX, size_t sizeY,
                                      size_t levels,
                                      uint8_t channel, bool symmetric, bool normalize,
                                      bool excludeTransparent,
                                      const int *steps, uint8_t stepCount, uint32_t threadCount,
                                      const Restriction *restriction)
                : Task{sizeX, sizeY, 4, false, restriction},
                  mIn{reinterpret_cast<const uchar4 *>(input)},
                  mChannel{channel},
                  mLevels{levels},
                  mSymmetric{symmetric},
                  mNormalize{normalize},
                  mExcludeTransparent{excludeTransparent},
                  mStepCount{stepCount},
                  mSteps{steps},
                  mTotals(threadCount),
                  mGlcm(threadCount) {
            for (size_t i = 0; i < threadCount; i++) {
                mGlcm[i].resize(levels * levels);
            }
        }

        void collate(float *out);
    };

    void
    GrayLevelCovarianceMatrixTask::processData(int threadIndex, size_t startX, size_t startY,
                                               size_t endX,
                                               size_t endY) {
        for (size_t y = startY; y < endY; y++) {
            for (size_t x = startX; x < endX; x++) {
                size_t offset = mSizeX * y + x;
                const uchar4 *in = mIn + offset;
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

                if (mExcludeTransparent && v.a == 0) {
                    continue;
                }

                auto quantized = quantize(value);

                for (size_t i = 0; i < mStepCount; i++) {
                    auto dx = mSteps[i * 2];
                    auto dy = mSteps[i * 2 + 1];
                    int nx = (int) x + dx;
                    int ny = (int) y + dy;

                    if (nx < 0 || nx >= (int) mSizeX || ny < 0 || ny >= (int) mSizeY) {
                        continue;
                    }

                    size_t newOffset = mSizeX * ny + nx;
                    const uchar4 *newIn = mIn + newOffset;
                    auto newV = *newIn;
                    float newValue;
                    if (mChannel == 0) {
                        newValue = newV.r;
                    } else if (mChannel == 1) {
                        newValue = newV.g;
                    } else if (mChannel == 2) {
                        newValue = newV.b;
                    } else if (mChannel == 3) {
                        newValue = newV.a;
                    } else {
                        newValue = (float) ((newV.r + newV.g + newV.b) / 3.0);
                    }

                    if (mExcludeTransparent && newV.a == 0) {
                        continue;
                    }

                    auto neighborQuantized = quantize(newValue);

                    size_t index = quantized * mLevels + neighborQuantized;
                    mGlcm[threadIndex][index]++;
                    mTotals[threadIndex]++;

                    if (mSymmetric) {
                        index = neighborQuantized * mLevels + quantized;
                        mGlcm[threadIndex][index]++;
                        mTotals[threadIndex]++;
                    }
                }
            }
        }
    }

    uchar GrayLevelCovarianceMatrixTask::quantize(float value) const {
        if (mLevels == 256){
            return (uchar) round(value);
        }

        return (uchar) round(value / 255.0f * (float) (mLevels - 1));
    }


    void GrayLevelCovarianceMatrixTask::collate(float *out) {
        size_t total = 0;
        for (unsigned int mTotal: mTotals) {
            total += mTotal;
        }

        for (auto &glcm: mGlcm) {
            for (size_t j = 0; j < glcm.size(); j++) {
                out[j] += (float) glcm[j];
            }
        }

        if (mNormalize && total > 0) {
            for (size_t j = 0; j < mLevels * mLevels; j++) {
                out[j] /= (float) total;
            }
        }
    }

    void RenderScriptToolkit::glcm(const uint8_t *input, float *output,
                                   size_t sizeX, size_t sizeY, size_t levels,
                                   uint8_t channel, bool symmetric, bool normalize,
                                   bool excludeTransparent, const int *steps, uint8_t stepCount,
                                   const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return;
        }
#endif

        GrayLevelCovarianceMatrixTask task(input, sizeX, sizeY, levels, channel,
                                           symmetric, normalize, excludeTransparent, steps,
                                           stepCount, processor->getNumberOfThreads(),
                                           restriction);
        processor->doTask(&task);
        task.collate(output);
    }

}  // namespace renderscript
