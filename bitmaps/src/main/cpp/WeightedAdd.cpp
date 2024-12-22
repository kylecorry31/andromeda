#include <cstdint>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.WeightedAdd"

namespace renderscript {

    class WeightedAddTask : public Task {
        const uchar4 *mIn1;
        const uchar4 *mIn2;
        uchar4 *mOut;
        float mWeight1;
        float mWeight2;
        bool mAbsolute;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        WeightedAddTask(const uint8_t *input1, const uint8_t *input2, uint8_t *output, size_t sizeX,
                        size_t sizeY,
                        float weight1, float weight2, bool absolute,
                        const Restriction *restriction)
                : Task{sizeX, sizeY, 4, true, restriction},
                  mIn1{reinterpret_cast<const uchar4 *>(input1)},
                  mIn2{reinterpret_cast<const uchar4 *>(input2)},
                  mOut{reinterpret_cast<uchar4 *>(output)},
                  mWeight1{weight1},
                  mWeight2{weight2},
                  mAbsolute{absolute} {}
    };

    void
    WeightedAddTask::processData(int /* threadIndex */, size_t startX, size_t startY, size_t endX,
                                 size_t endY) {
        for (size_t y = startY; y < endY; y++) {
            size_t offset = mSizeX * y + startX;
            const uchar4 *in1 = mIn1 + offset;
            const uchar4 *in2 = mIn2 + offset;
            uchar4 *out = mOut + offset;
            for (size_t x = startX; x < endX; x++) {
                auto v1 = *in1;
                double r1 = v1.r;
                double g1 = v1.g;
                double b1 = v1.b;
                double a1 = v1.a;

                auto v2 = *in2;
                double r2 = v2.r;
                double g2 = v2.g;
                double b2 = v2.b;
                double a2 = v2.a;

                double r3 = r1 * mWeight1 + r2 * mWeight2;
                double g3 = g1 * mWeight1 + g2 * mWeight2;
                double b3 = b1 * mWeight1 + b2 * mWeight2;
                double a3 = a1 * mWeight1 + a2 * mWeight2;
                if (mAbsolute) {
                    r3 = std::abs(r3);
                    g3 = std::abs(g3);
                    b3 = std::abs(b3);
                    a3 = std::abs(a3);
                }

                // Clamp
                if (r3 > 255) {
                    r3 = 255;
                } else if (r3 < 0) {
                    r3 = 0;
                }

                if (g3 > 255) {
                    g3 = 255;
                } else if (g3 < 0) {
                    g3 = 0;
                }

                if (b3 > 255) {
                    b3 = 255;
                } else if (b3 < 0) {
                    b3 = 0;
                }

                if (a3 > 255) {
                    a3 = 255;
                } else if (a3 < 0) {
                    a3 = 0;
                }

                *out = {static_cast<uint8_t>(r3), static_cast<uint8_t>(g3),
                        static_cast<uint8_t>(b3),
                        static_cast<uint8_t>(a3)};

                in1++;
                in2++;
                out++;
            }
        }
    }

    void
    RenderScriptToolkit::weightedAdd(const uint8_t *input1, const uint8_t *input2, uint8_t *output,
                                     size_t sizeX,
                                     size_t sizeY,
                                     float weight1, float weight2, bool absolute,
                                     const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return;
        }
#endif

        WeightedAddTask task(input1, input2, output, sizeX, sizeY, weight1, weight2, absolute,
                             restriction);
        processor->doTask(&task);
    }

}  // namespace renderscript
