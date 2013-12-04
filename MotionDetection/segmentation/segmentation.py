import numpy as np
import cv2

import os
import sys

class Segmentor:
    def display_and_wait(self, title, img):
        cv2.imshow(title, img)
        key = cv2.waitKey(0)
        cv2.destroyAllWindows()

    def canny(self, img, th1, th2):
        edges = cv2.Canny(img, th1, th2)
        #cv2.Canny()
        img[edges != 0] = 255
        return img, edges

    def segment(self, img):
        img_copy = img.copy()
        #display_and_wait("Original", img_copy)

        # Convert to grayscale
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        gray_copy = gray.copy()
        self.display_and_wait("Gray", gray)


        # Remove stars
        blur = cv2.GaussianBlur(gray, (3, 3), 0)
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(3,3))
        erosion = cv2.erode(blur, kernel, iterations=2)
        self.display_and_wait("Remove Stars - Erosion", erosion)

        # Detect horizon
        horizon_edge, edges = self.canny(erosion, 40, 40)

        #for row in edges:
        #    print row
        #cv2.imwrite("edge_map.png", edges)

        self.display_and_wait("Detect Horizon - Canny", horizon_edge)

        # Increase the edge thickness (may not be needed?)
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(3,3))
        dilation = cv2.dilate(horizon_edge, kernel, iterations=3)
        self.display_and_wait("Thicken Edge - Dilation", dilation)



        # Somehow, fill in area below the edge line
        retval, rect = cv2.floodFill(dilation, None, (0, 0), 255, loDiff=100, upDiff=100)
        self.display_and_wait("fill", dilation)

        # Erode small lines

        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (20,20))
        erode = cv2.erode(dilation, kernel)
        erode = cv2.dilate(erode, kernel)
        self.display_and_wait("Eroded", erode)


        # Now remove the area from the original grayscale image
        retval, mask = cv2.threshold(erode, 254, 255, cv2.THRESH_BINARY)

        self.display_and_wait("binary threshold", mask)

        gray_copy = cv2.bitwise_and(gray_copy, mask)
        self.display_and_wait("and", gray_copy)

        return gray_copy


if __name__ == "__main__":
    s = Segmentor()

    img = cv2.imread(sys.argv[1])
    img = s.segment(img)
    cv2.imwrite("segmented.png", img)






