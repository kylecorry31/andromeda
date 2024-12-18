#include <cstdint>

#include "RenderScriptToolkit.h"
#include "TaskProcessor.h"
#include "Utils.h"

#define LOG_TAG "renderscript.toolkit.BlobFinder"

namespace renderscript {

    struct Point {
        size_t x;
        size_t y;
    };

    struct Rect {
        size_t left;
        size_t top;
        size_t right;
        size_t bottom;
    };

    class BlobFinderTask : public Task {
        const uchar4 *mIn;
        const uint8_t mChannel;
        const float mThreshold;
        std::vector<std::vector<Rect>> mRects;

        // Process a 2D tile of the overall work. threadIndex identifies which thread does the work.
        void processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                         size_t endY) override;

        std::vector<Rect> getAllRects();

        void removeDuplicates(std::vector<Rect> &rects);

        void sortByArea(std::vector<Rect> &rects);

        Rect findBlob(size_t x, size_t y);

    public:
        BlobFinderTask(const uint8_t *input, size_t sizeX, size_t sizeY, float threshold,
                       uint8_t channel, uint32_t threadCount, const Restriction *restriction)
                : Task{sizeX, sizeY, 4, false, restriction},
                  mIn{reinterpret_cast<const uchar4 *>(input)},
                  mChannel{channel},
                  mThreshold{threshold},
                  mRects(threadCount) {}

        void collate(size_t maxBlobs, int *out);
    };

    void
    BlobFinderTask::processData(int threadIndex, size_t startX, size_t startY, size_t endX,
                                size_t endY) {
        // Rects of the form x, y, width, height, x, y, width, height, ...
        std::vector<Rect> &rects = mRects[threadIndex];

        for (size_t y = startY; y < endY; y++) {
            for (size_t x = startX; x < endX; x++) {
                // First check to see if any of the clusters contain this value
                bool foundMatch = false;
                size_t clusterIndex;
                for (size_t i = 0; i < rects.size(); i++) {
                    Rect rect = rects[i];
                    if (x >= rect.left && x < rect.right && y >= rect.top && y < rect.bottom) {
                        clusterIndex = i;
                        foundMatch = true;
                        break;
                    }
                }

                if (foundMatch) {
                    // Skip to the right of the cluster
                    x = rects[clusterIndex].right;
                    continue;
                }

                // No existing cluster found, search for one
                auto blob = findBlob(x, y);
                if (blob.left != 0 || blob.top != 0 || blob.right != 0 || blob.bottom != 0) {
                    rects.push_back(blob);
                }
            }
        }
    }

    Rect BlobFinderTask::findBlob(size_t x, size_t y) {
        std::vector<Point> visited;
        std::vector<Point> toVisit;
        size_t minX = 0;
        size_t minY = 0;
        size_t maxX = 0;
        size_t maxY = 0;
        bool found = false;

        toVisit.push_back({x, y});

        while (!toVisit.empty()) {
            Point current = toVisit.back();
            toVisit.pop_back();

            // If we've already visited this point, skip it
            bool skip = false;
            for (auto &i: visited) {
                if (i.x == current.x && i.y == current.y) {
                    skip = true;
                    break;
                }
            }
            if (skip) {
                continue;
            }

            visited.push_back(current);

            // If we're out of bounds, skip
            if (current.x < 0 || current.x >= mSizeX || current.y < 0 ||
                current.y >= mSizeY) {
                continue;
            }

            size_t offset = mSizeX * current.y + current.x;
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

            if (value >= mThreshold) {
                if (!found || current.x < minX) {
                    minX = current.x;
                }

                if (!found || current.y < minY) {
                    minY = current.y;
                }

                if (!found || current.x > maxX) {
                    maxX = current.x;
                }

                if (!found || current.y > maxY) {
                    maxY = current.y;
                }

                found = true;

                Point p1 = {current.x + 1, current.y};
                Point p2 = {current.x - 1, current.y};
                Point p3 = {current.x, current.y + 1};
                Point p4 = {current.x, current.y - 1};
                toVisit.push_back(p1);
                toVisit.push_back(p2);
                toVisit.push_back(p3);
                toVisit.push_back(p4);
            }
        }

        if (found) {
            return Rect{minX, minY, maxX + 1, maxY + 1};
        } else {
            return Rect{0, 0, 0, 0};
        }
    }

    void BlobFinderTask::removeDuplicates(std::vector<Rect> &rects) {
        for (size_t i = 0; i < rects.size(); i++) {
            for (size_t j = i + 1; j < rects.size(); j++) {
                Rect &a = rects[i];
                Rect &b = rects[j];
                if (a.left == b.left && a.top == b.top && a.right == b.right &&
                    a.bottom == b.bottom) {
                    // Remove the duplicate
                    rects.erase(rects.begin() + j);
                    j--;
                }
            }
        }
    }

    void BlobFinderTask::sortByArea(std::vector<Rect> &rects) {
        std::sort(rects.begin(), rects.end(), [](const Rect &a, const Rect &b) {
            return (a.right - a.left) * (a.bottom - a.top) >
                   (b.right - b.left) * (b.bottom - b.top);
        });
    }

    std::vector<Rect> BlobFinderTask::getAllRects() {
        // Flatten
        std::vector<Rect> rects;
        for (auto &mRect: mRects) {
            for (auto &rect: mRect) {
                rects.push_back(rect);
            }
        }

        // Merge rects that are the same
        removeDuplicates(rects);

        // Sort the rects by size
        sortByArea(rects);

        return rects;
    }

    void BlobFinderTask::collate(size_t maxBlobs, int *out) {

        // Step 1: Get all the rects
        std::vector<Rect> sortedRects = getAllRects();

        // Step 2: Copy the largest N clusters to the output, but ignore duplicates
        for (size_t i = 0; i < maxBlobs; i++) {
            if (i >= sortedRects.size()) {
                // Fill the rest with zeros
                out[i * 4] = 0;
                out[i * 4 + 1] = 0;
                out[i * 4 + 2] = 0;
                out[i * 4 + 3] = 0;
                continue;
            }

            // Copy the rect to the output and move to the next
            Rect rect = sortedRects[i];
            out[i * 4] = (int) rect.left;
            out[i * 4 + 1] = (int) rect.top;
            out[i * 4 + 2] = (int) rect.right;
            out[i * 4 + 3] = (int) rect.bottom;
        }
    }

    void RenderScriptToolkit::findBlobs(const uint8_t *input, int *output, size_t maxBlobs,
                                        size_t sizeX, size_t sizeY, float threshold,
                                        uint8_t channel, const Restriction *restriction) {
#ifdef ANDROID_RENDERSCRIPT_TOOLKIT_VALIDATE
        if (!validRestriction(LOG_TAG, sizeX, sizeY, restriction)) {
            return;
        }
#endif

        BlobFinderTask task(input, sizeX, sizeY, threshold, channel,
                            processor->getNumberOfThreads(),
                            restriction);
        processor->doTask(&task);
        task.collate(maxBlobs, output);
    }

}  // namespace renderscript
