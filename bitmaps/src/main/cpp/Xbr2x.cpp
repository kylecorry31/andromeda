#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.Xbr2x"

namespace renderscript {

    static inline int channelR(uint32_t p) { return static_cast<int>(p & 0xFF); }

    static inline int channelG(uint32_t p) { return static_cast<int>((p >> 8) & 0xFF); }

    static inline int channelB(uint32_t p) { return static_cast<int>((p >> 16) & 0xFF); }

    static inline int channelA(uint32_t p) { return static_cast<int>((p >> 24) & 0xFF); }

    static inline float colorDist(uint32_t c1, uint32_t c2) {
        int r = std::abs(channelR(c1) - channelR(c2));
        int g = std::abs(channelG(c1) - channelG(c2));
        int b = std::abs(channelB(c1) - channelB(c2));
        int a = std::abs(channelA(c1) - channelA(c2));

        double y = std::fabs(0.299 * r + 0.587 * g + 0.114 * b);
        double u = std::fabs(-0.169 * r - 0.331 * g + 0.500 * b);
        double v = std::fabs(0.500 * r - 0.419 * g - 0.081 * b);

        return static_cast<float>(48.0 * y + 7.0 * u + 6.0 * v + 48.0 * a);
    }

// xBR tutorial: https://forums.libretro.com/t/xbr-algorithm-tutorial/123
    class Xbr2xTask : public Task {
        const uint32_t *mIn;
        uint32_t *mOut;
        size_t mInputSizeX;
        size_t mInputSizeY;

        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

    public:
        Xbr2xTask(const uint32_t *input, uint32_t *output,
                  size_t inputSizeX, size_t inputSizeY,
                  const Restriction *restriction)
                : Task{inputSizeX, inputSizeY, 4, false, restriction},
                  mIn{input},
                  mOut{output},
                  mInputSizeX{inputSizeX},
                  mInputSizeY{inputSizeY} {}
    };

    void Xbr2xTask::processData(int /* threadIndex */, size_t startX,
                                size_t startY, size_t endX,
                                size_t endY) {
        const int width = static_cast<int>(mInputSizeX);
        const int height = static_cast<int>(mInputSizeY);
        const size_t destinationWidth = mInputSizeX * 2;

        auto get = [&](int x, int y) -> uint32_t {
            int sx = std::max(0, std::min(width - 1, x));
            int sy = std::max(0, std::min(height - 1, y));
            return mIn[sy * width + sx];
        };

        for (size_t y = startY; y < endY; y++) {
            const int iy = static_cast<int>(y);
            for (size_t x = startX; x < endX; x++) {
                const int ix = static_cast<int>(x);

                // 5x5 neighborhood (clamped at edges):
                //     A1 B1 C1
                // A0  A  B  C  C4
                // D0  D  E  F  F4
                // G0  G  H  I  I4
                //     G5 H5 I5
                const uint32_t a = get(ix - 1, iy - 1);
                const uint32_t b = get(ix, iy - 1);
                const uint32_t c = get(ix + 1, iy - 1);
                const uint32_t d = get(ix - 1, iy);
                const uint32_t e = get(ix, iy);
                const uint32_t f = get(ix + 1, iy);
                const uint32_t g = get(ix - 1, iy + 1);
                const uint32_t h = get(ix, iy + 1);
                const uint32_t i = get(ix + 1, iy + 1);
                const uint32_t a1 = get(ix - 1, iy - 2);
                const uint32_t b1 = get(ix, iy - 2);
                const uint32_t c1 = get(ix + 1, iy - 2);
                const uint32_t a0 = get(ix - 2, iy - 1);
                const uint32_t c4 = get(ix + 2, iy - 1);
                const uint32_t d0 = get(ix - 2, iy);
                const uint32_t f4 = get(ix + 2, iy);
                const uint32_t g0 = get(ix - 2, iy + 1);
                const uint32_t i4 = get(ix + 2, iy + 1);
                const uint32_t g5 = get(ix - 1, iy + 2);
                const uint32_t h5 = get(ix, iy + 2);
                const uint32_t i5 = get(ix + 1, iy + 2);

                // 4x4 output for original E pixel
                // E0 E1
                // E2 E3
                uint32_t e0 = e;
                uint32_t e1 = e;
                uint32_t e2 = e;
                uint32_t e3 = e;

                // E3 (bottom-right) - edge between H and F
                {
                    float wd1 = colorDist(e, c) + colorDist(e, g) + colorDist(i, f4) +
                                colorDist(i, h5) + 4 * colorDist(h, f);
                    float wd2 = colorDist(h, d) + colorDist(h, i5) + colorDist(f, i4) +
                                colorDist(f, b) + 4 * colorDist(e, i);
                    if (wd1 < wd2) {
                        uint32_t newColor = (colorDist(e, f) <= colorDist(e, h)) ? f : h;
                        if (f == g && h == c) {
                            e3 = newColor;
                            e2 = newColor;
                            e1 = newColor;
                        } else if (f == g) {
                            e3 = newColor;
                            e2 = newColor;
                        } else if (h == c) {
                            e3 = newColor;
                            e1 = newColor;
                        } else {
                            e3 = newColor;
                        }
                    }
                }

                // E1 (top-right) - edge between B and F
                {
                    float wd1 = colorDist(e, i) + colorDist(e, a) + colorDist(c, f4) +
                                colorDist(c, b1) + 4 * colorDist(b, f);
                    float wd2 = colorDist(b, d) + colorDist(b, c1) + colorDist(f, h) +
                                colorDist(f, c4) + 4 * colorDist(e, c);
                    if (wd1 < wd2) {
                        uint32_t newColor = (colorDist(e, f) <= colorDist(e, b)) ? f : b;
                        if (f == a && b == i) {
                            e1 = newColor;
                            e0 = newColor;
                            e3 = newColor;
                        } else if (f == a) {
                            e1 = newColor;
                            e0 = newColor;
                        } else if (b == i) {
                            e1 = newColor;
                            e3 = newColor;
                        } else {
                            e1 = newColor;
                        }
                    }
                }

                // E0 (top-left) - edge between B and D
                {
                    float wd1 = colorDist(e, g) + colorDist(e, c) + colorDist(a, d0) +
                                colorDist(a, b1) + 4 * colorDist(b, d);
                    float wd2 = colorDist(b, f) + colorDist(b, a1) + colorDist(d, h) +
                                colorDist(d, a0) + 4 * colorDist(e, a);
                    if (wd1 < wd2) {
                        uint32_t newColor = (colorDist(e, d) <= colorDist(e, b)) ? d : b;
                        if (d == c && b == g) {
                            e0 = newColor;
                            e1 = newColor;
                            e2 = newColor;
                        } else if (d == c) {
                            e0 = newColor;
                            e1 = newColor;
                        } else if (b == g) {
                            e0 = newColor;
                            e2 = newColor;
                        } else {
                            e0 = newColor;
                        }
                    }
                }

                // E2 (bottom-left) - edge between D and H
                {
                    float wd1 = colorDist(e, i) + colorDist(e, a) + colorDist(g, d0) +
                                colorDist(g, h5) + 4 * colorDist(h, d);
                    float wd2 = colorDist(h, f) + colorDist(h, g5) + colorDist(d, b) +
                                colorDist(d, g0) + 4 * colorDist(e, g);
                    if (wd1 < wd2) {
                        uint32_t newColor = (colorDist(e, d) <= colorDist(e, h)) ? d : h;
                        if (d == i && h == a) {
                            e2 = newColor;
                            e3 = newColor;
                            e0 = newColor;
                        } else if (d == i) {
                            e2 = newColor;
                            e3 = newColor;
                        } else if (h == a) {
                            e2 = newColor;
                            e0 = newColor;
                        } else {
                            e2 = newColor;
                        }
                    }
                }

                const size_t di = (y * 2) * destinationWidth + (x * 2);
                mOut[di] = e0;
                mOut[di + 1] = e1;
                mOut[di + destinationWidth] = e2;
                mOut[di + destinationWidth + 1] = e3;
            }
        }
    }

    void RenderScriptToolkit::xbr2x(const uint8_t *input, uint8_t *output,
                                    size_t sizeX, size_t sizeY) {
        const auto *in = reinterpret_cast<const uint32_t *>(input);
        auto *out = reinterpret_cast<uint32_t *>(output);

        Xbr2xTask task(in, out, sizeX, sizeY, nullptr);
        processor->doTask(&task);
    }

}  // namespace renderscript
