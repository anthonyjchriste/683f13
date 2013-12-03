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
        cv2.Canny()
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
        horizon_edge, edges = self.canny(erosion, 0, 20)
        self.display_and_wait("Detect Horizon - Canny", horizon_edge)

        # Increase the edge thickness (may not be needed?)
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE,(3,3))
        dilation = cv2.dilate(horizon_edge, kernel, iterations=1)
        self.display_and_wait("Thicken Edge - Dilation", dilation)

        # Somehow, fill in area below the edge line
        retval, rect = cv2.floodFill(dilation, None, (len(dilation), len(dilation[0]) / 2), 255, loDiff=100, upDiff=254)
        self.display_and_wait("fill", dilation)

        # Now remove the area from the original grayscale image
        retval, mask = cv2.threshold(dilation, 20, 255, cv2.THRESH_BINARY)
        inverse_mask = cv2.bitwise_not(mask)
        gray_copy = cv2.bitwise_and(gray_copy, inverse_mask)
        self.display_and_wait("and", gray_copy)

        return gray_copy


if __name__ == "__main__":
    s = Segmentor()

    for i in range(1, len(sys.argv)):
        print sys.argv[1]
        img = cv2.imread(sys.argv[i])
        img = s.segment(img)
        print sys.argv[i].split(".")[0] + ".out.png"
        cv2.imwrite(sys.argv[i].split(".")[0] + ".out.png", img)






