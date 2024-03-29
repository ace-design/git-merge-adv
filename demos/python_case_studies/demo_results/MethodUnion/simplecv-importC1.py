from SimpleCV.base import *
from SimpleCV.Features.Features import Feature,FeatureSet
from SimpleCV.Color import Color
from SimpleCV.ImageClass import Image
from SimpleCV.Features.Detection import ShapeContextDescriptor
import math
import scipy.stats as sps
from SimpleCV.Features import Line,Corner
class Blob(Feature):
    seq = ''
    mContour = []
    mConvexHull = []
    mMinRectangle = []
    mHu = []
    mPerimeter = 0
    mArea = 0
    m00 = 0
    m01 = 0
    m10 = 0
    m11 = 0
    m20 = 0
    m02 = 0
    m21 = 0
    m12 = 0
    mContourAppx = None
    mLabel = ''
    mLabelColor = []
    mAvgColor = []
    mHoleContour = []
    pickle_skip_properties = set(('mImg', 'mHullImg', 'mMask', 'mHullMask'))
    def __init__(self):
        self._scdescriptors = None
        self.mContour = []
        self.mConvexHull = []
        self.mMinRectangle = [-1, -1, -1, -1, -1]
        self.mContourAppx = []
        self.mHu = [-1, -1, -1, -1, -1, -1, -1]
        self.mPerimeter = 0
        self.mArea = 0
        self.m00 = 0
        self.m01 = 0
        self.m10 = 0
        self.m11 = 0
        self.m20 = 0
        self.m02 = 0
        self.m21 = 0
        self.m12 = 0
        self.mLabel = 'UNASSIGNED'
        self.mLabelColor = []
        self.mAvgColor = [-1, -1, -1]
        self.image = None
        self.mHoleContour = []
        self.points = []
    
    def __getstate__(self):
        skip = self.pickle_skip_properties
        newdict = {}
        for k, v in self.__dict__.items():
            if k in skip:
                continue
            else:
                newdict[k] = v
        return newdict
    
    def __setstate__(self, mydict):
        iplkeys = []
        for k in mydict:
            if re.search('__string', k):
                iplkeys.append(k)
            else:
                self.__dict__[k] = mydict[k]
        for k in iplkeys:
            realkey = k[:-len('__string')]
            self.__dict__[realkey] = cv.CreateImageHeader((self.width(), self.
                height()), cv.IPL_DEPTH_8U, 1)
            cv.SetData(self.__dict__[realkey], mydict[k])
    
    def perimeter(self):
        """
            **SUMMARY**
    
            This function returns the perimeter as an integer number of pixel lengths.
    
            **RETURNS**
    
            Integer
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].perimeter()
    
            """
        return self.mPerimeter
    
    def hull(self):
        """
            **SUMMARY**
    
            This function returns the convex hull points as a list of x,y tuples.
    
            **RETURNS**
    
            A list of x,y tuples.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].hull()
    
            """
        return self.mConvexHull
    
    def contour(self):
        """
            **SUMMARY**
    
            This function returns the contour points as a list of x,y tuples.
    
            **RETURNS**
    
            A list of x,y tuples.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].contour()
    
            """
        return self.mContour
    
    def meanColor(self):
        """
            **SUMMARY**
    
            This function returns a tuple representing the average color of the blob.
    
            **RETURNS**
    
            A RGB triplet of the average blob colors.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].meanColor()
    
            """
        hack = self.mBoundingBox[0], self.mBoundingBox[1], self.mBoundingBox[2
            ], self.mBoundingBox[3]
        cv.SetImageROI(self.image.getBitmap(), hack)
        avg = cv.Avg(self.image.getBitmap(), self.mMask._getGrayscaleBitmap())
        cv.ResetImageROI(self.image.getBitmap())
        return tuple(reversed(avg[0:3]))
    
    def area(self):
        """
            **SUMMARY**
    
            This method returns the area of the blob in terms of the number of
            pixels inside the contour.
    
            **RETURNS**
    
            An integer of the area of the blob in pixels.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].area()
            >>> print blobs[0].area()
    
            """
        return self.mArea
    
    def minRect(self):
        """
            Returns the corners for the smallest rotated rectangle to enclose the blob.
            The points are returned as a list of  (x,y) tupples.
            """
        ang = self.mMinRectangle[2]
        ang = 2 * pi * (float(ang) / 360.0)
        tx = self.minRectX()
        ty = self.minRectY()
        w = self.minRectWidth() / 2.0
        h = self.minRectHeight() / 2.0
        derp = np.matrix([[cos(ang), -1 * sin(ang), tx], [sin(ang), cos(ang),
            ty], [0, 0, 1]])
        tl = np.matrix([-1.0 * w, h, 1.0])
        tr = np.matrix([w, h, 1.0])
        bl = np.matrix([-1.0 * w, -1.0 * h, 1.0])
        br = np.matrix([w, -1.0 * h, 1.0])
        tlp = derp * tl.transpose()
        trp = derp * tr.transpose()
        blp = derp * bl.transpose()
        brp = derp * br.transpose()
        return (float(tlp[0, 0]), float(tlp[1, 0])), (float(trp[0, 0]), float(
            trp[1, 0])), (float(blp[0, 0]), float(blp[1, 0])), (float(brp[0, 0]
            ), float(brp[1, 0]))
    
    def drawRect(self, layer=None, color=Color.DEFAULT, width=1, alpha=128):
        """
            **SUMMARY**
    
            Draws the bounding rectangle for the blob.
    
            **PARAMETERS**
    
            * *color* - The color to render the blob's box.
            * *alpha* - The alpha value of the rendered blob 0 = transparent 255 = opaque.
            * *width* - The width of the drawn blob in pixels
            * *layer* - if layer is not None, the blob is rendered to the layer versus the source image.
    
            **RETURNS**
    
            Returns None, this operation works on the supplied layer or the source image.
    
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-1].drawRect(color=Color.RED, width=-1,alpha=128)
            >>> img.show()
    
            """
        if layer is None:
            layer = self.image.dl()
        if width < 1:
            layer.rectangle(self.topLeftCorner(), (self.width(), self.height()),
                color, width, filled=True, alpha=alpha)
        else:
            layer.rectangle(self.topLeftCorner(), (self.width(), self.height()),
                color, width, filled=False, alpha=alpha)
    
    def drawMinRect(self, layer=None, color=Color.DEFAULT, width=1, alpha=128):
        """
            **SUMMARY**
    
            Draws the minimum bounding rectangle for the blob. The minimum bounding rectangle is the smallest
            rotated rectangle that can enclose the blob.
    
            **PARAMETERS**
    
            * *color* - The color to render the blob's box.
            * *alpha* - The alpha value of the rendered blob 0 = transparent 255 = opaque.
            * *width* - The width of the drawn blob in pixels
            * *layer* - If layer is not None, the blob is rendered to the layer versus the source image.
    
            **RETURNS**
    
            Returns none, this operation works on the supplied layer or the source image.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> for b in blobs:
            >>>      b.drawMinRect(color=Color.RED, width=-1,alpha=128)
            >>> img.show()
    
            """
        if layer is None:
            layer = self.image.dl()
        tl, tr, bl, br = self.minRect()
        layer.line(tl, tr, color, width=width, alpha=alpha, antialias=False)
        layer.line(bl, br, color, width=width, alpha=alpha, antialias=False)
        layer.line(tl, bl, color, width=width, alpha=alpha, antialias=False)
        layer.line(tr, br, color, width=width, alpha=alpha, antialias=False)
    
    def angle(self):
        """
            **SUMMARY**
    
            This method returns the angle between the horizontal and the minimum enclosing
            rectangle of the blob. The minimum enclosing rectangle IS NOT not the bounding box.
            Use the bounding box for situations where you need only an approximation of the objects
            dimensions. The minimum enclosing rectangle is slightly harder to maninpulate but
            gives much better information about the blobs dimensions.
    
            **RETURNS**
    
            Returns the angle between the minimum bounding rectangle and the horizontal.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blob[-1].angle()
    
            """
        retVal = 0.0
        if self.mMinRectangle[1][0] < self.mMinRectangle[1][1]:
            retVal = self.mMinRectangle[2]
        else:
            retVal = 90.0 + self.mMinRectangle[2]
        retVal = retVal + 90.0
        if retVal > 90.0:
            retVal = -180.0 + retVal
        return retVal
    
    def minRectX(self):
        """
            **SUMMARY**
    
            This is the x coordinate of the centroid for the minimum bounding rectangle
    
            **RETURNS**
    
            An integer that is the x position of the centrod of the minimum bounding rectangle.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].minRectX()
    
            """
        return self.mMinRectangle[0][0]
    
    def minRectY(self):
        """
            **SUMMARY**
    
            This is the y coordinate of the centroid for the minimum bounding rectangle
    
            **RETURNS**
    
            An integer that is the y position of the centrod of the minimum bounding rectangle.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].minRectY()
    
            """
        return self.mMinRectangle[0][1]
    
    def minRectWidth(self):
        """
            **SUMMARY**
    
            This is the width of the minimum bounding rectangle for the blob.
    
            **RETURNS**
    
            An integer that is the width of the minimum bounding rectangle for this blob.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].minRectWidth()
    
            """
        return self.mMinRectangle[1][0]
    
    def minRectHeight(self):
        """
            **SUMMARY**
    
            This is the height, in pixels, of the minimum bounding rectangle.
    
            **RETURNS**
    
            An integer that is the height of the minimum bounding rectangle for this blob.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> print blobs[-1].minRectHeight()
    
    
            """
        return self.mMinRectangle[1][1]
    
    def rectifyMajorAxis(self, axis=0):
        """
            **SUMMARY**
    
            Rectify the blob image and the contour such that the major
            axis is aligned to either horizontal=0 or vertical=1. This is to say, we take the blob,
            find the longest axis, and rotate the blob such that the axis is either vertical or horizontal.
    
            **PARAMETERS**
    
            * *axis* - if axis is zero we rotate the blobs to fit along the vertical axis, otherwise we use the horizontal axis.
    
            **RETURNS**
    
            This method works in place, i.e. it rotates the blob's internal data structures. This method is experimetnal.
            Use at your own risk.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-2].mImg.show()
            >>> blobs[-2].rectifyToMajorAxis(1)
            >>> blobs[-2].mImg.show()
    
            """
        finalRotation = self.angle()
        w = self.minRectWidth()
        h = self.minRectHeight()
        if w > h:
            finalRotation = finalRotation
        if axis > 0:
            finalRotation = finalRotation - 90
        self.rotate(finalRotation)
        return None
    
    def rotate(self, angle):
        """
            **SUMMARY**
    
            Rotate the blob given an  angle in degrees. If you use this method
            most of the blob elements will
            be rotated in place , however, this will "break" drawing back to the original image.
            To draw the blob create a new layer and draw to that layer. Positive rotations
            are counter clockwise.
    
            **PARAMETERS**
    
            * *angle* - A floating point angle in degrees. Positive is anti-clockwise.
    
            **RETURNS**
    
            .. Warning:
              Nothing. All rotations are performed in place. This modifies the blob's data
              and will break any image write back capabilities.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-2].mImg.show()
            >>> blobs[-2].rotate(90)
            >>> blobs[-2].mImg.show()
    
            """
        theta = 2 * np.pi * (angle / 360.0)
        mode = ''
        point = self.x, self.y
        self.mImg = self.mImg.rotate(angle, mode, point)
        self.mHullImg = self.mHullImg.rotate(angle, mode, point)
        self.mMask = self.mMask.rotate(angle, mode, point)
        self.mHullMask = self.mHullMask.rotate(angle, mode, point)
        self.mContour = map(lambda x: (x[0] * np.cos(theta) - x[1] * np.sin(
            theta), x[0] * np.sin(theta) + x[1] * np.cos(theta)), self.mContour)
        self.mConvexHull = map(lambda x: (x[0] * np.cos(theta) - x[1] * np.sin(
            theta), x[0] * np.sin(theta) + x[1] * np.cos(theta)), self.mConvexHull)
        if self.mHoleContour is not None:
            for h in self.mHoleContour:
                h = map(lambda x: (x[0] * np.cos(theta) - x[1] * np.sin(theta),
                    x[0] * np.sin(theta) + x[1] * np.cos(theta)), h)
    
    def drawAppx(self, color=Color.HOTPINK, width=-1, alpha=-1, layer=None):
        if self.mContourAppx is None or len(self.mContourAppx) == 0:
            return
        if not layer:
            layer = self.image.dl()
        if width < 1:
            layer.polygon(self.mContourAppx, color, width, True, True, alpha)
        else:
            layer.polygon(self.mContourAppx, color, width, False, True, alpha)
    
    def draw(self, color=Color.GREEN, width=-1, alpha=-1, layer=None):
        """
            **SUMMARY**
    
            Draw the blob, in the given color, to the appropriate layer
    
            By default, this draws the entire blob filled in, with holes.  If you
            provide a width, an outline of the exterior and interior contours is drawn.
    
            **PARAMETERS**
    
            * *color* -The color to render the blob as a color tuple.
            * *alpha* - The alpha value of the rendered blob 0=transparent 255=opaque.
            * *width* - The width of the drawn blob in pixels, if -1 then filled then the polygon is filled.
            * *layer* - A source layer, if layer is not None, the blob is rendered to the layer versus the source image.
    
            **RETURNS**
    
            This method either works on the original source image, or on the drawing layer provided.
            The method does not modify object itself.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-2].draw(color=Color.PUCE,width=-1,alpha=128)
            >>> img.show()
    
            """
        if not layer:
            layer = self.image.dl()
        if width == -1:
            maskred = cv.CreateImage(cv.GetSize(self.mMask._getGrayscaleBitmap(
                )), cv.IPL_DEPTH_8U, 1)
            maskgrn = cv.CreateImage(cv.GetSize(self.mMask._getGrayscaleBitmap(
                )), cv.IPL_DEPTH_8U, 1)
            maskblu = cv.CreateImage(cv.GetSize(self.mMask._getGrayscaleBitmap(
                )), cv.IPL_DEPTH_8U, 1)
            maskbit = cv.CreateImage(cv.GetSize(self.mMask._getGrayscaleBitmap(
                )), cv.IPL_DEPTH_8U, 3)
            cv.ConvertScale(self.mMask._getGrayscaleBitmap(), maskred, color[0] /
                255.0)
            cv.ConvertScale(self.mMask._getGrayscaleBitmap(), maskgrn, color[1] /
                255.0)
            cv.ConvertScale(self.mMask._getGrayscaleBitmap(), maskblu, color[2] /
                255.0)
            cv.Merge(maskblu, maskgrn, maskred, None, maskbit)
            masksurface = Image(maskbit).getPGSurface()
            masksurface.set_colorkey(Color.BLACK)
            if alpha != -1:
                masksurface.set_alpha(alpha)
            layer._mSurface.blit(masksurface, self.topLeftCorner())
        else:
            self.drawOutline(color, alpha, width, layer)
            self.drawHoles(color, alpha, width, layer)
    
    def drawOutline(self, color=Color.GREEN, alpha=255, width=1, layer=None):
        """
            **SUMMARY**
    
            Draw the blob contour the provided layer -- if no layer is provided, draw
            to the source image.
    
    
            **PARAMETERS**
    
            * *color* - The color to render the blob.
            * *alpha* - The alpha value of the rendered poly.
            * *width* - The width of the drawn blob in pixels, -1 then the polygon is filled.
            * *layer* - if layer is not None, the blob is rendered to the layer versus the source image.
    
    
            **RETURNS**
    
            This method either works on the original source image, or on the drawing layer provided.
            The method does not modify object itself.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-2].drawOutline(color=Color.GREEN,width=3,alpha=128)
            >>> img.show()
    
    
            """
        if layer is None:
            layer = self.image.dl()
        if width < 0:
            layer.polygon(self.mContour, color, filled=True, alpha=alpha)
        else:
            lastp = self.mContour[0]
            for nextp in self.mContour[1:]:
                layer.line(lastp, nextp, color, width=width, alpha=alpha,
                    antialias=False)
                lastp = nextp
            layer.line(self.mContour[0], self.mContour[-1], color, width=width,
                alpha=alpha, antialias=False)
    
    def drawHoles(self, color=Color.GREEN, alpha=-1, width=-1, layer=None):
        """
            **SUMMARY**
    
            This method renders all of the holes (if any) that are present in the blob.
    
            **PARAMETERS**
    
            * *color* - The color to render the blob's holes.
            * *alpha* - The alpha value of the rendered blob hole.
            * *width* - The width of the drawn blob hole in pixels, if w=-1 then the polygon is filled.
            * *layer* - If layer is not None, the blob is rendered to the layer versus the source image.
    
            **RETURNS**
    
            This method either works on the original source image, or on the drawing layer provided.
            The method does not modify object itself.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs(128)
            >>> blobs[-1].drawHoles(color=Color.GREEN,width=3,alpha=128)
            >>> img.show()
    
            """
        if self.mHoleContour is None:
            return
        if layer is None:
            layer = self.image.dl()
        if width < 0:
            for h in self.mHoleContour:
                layer.polygon(h, color, filled=True, alpha=alpha)
        else:
            for h in self.mHoleContour:
                lastp = h[0]
                for nextp in h[1:]:
                    layer.line((int(lastp[0]), int(lastp[1])), (int(nextp[0]),
                        int(nextp[1])), color, width=width, alpha=alpha,
                        antialias=False)
                    lastp = nextp
                layer.line(h[0], h[-1], color, width=width, alpha=alpha,
                    antialias=False)
    
    def drawHull(self, color=Color.GREEN, alpha=-1, width=-1, layer=None):
        """
            **SUMMARY**
    
            Draw the blob's convex hull to either the source image or to the
            specified layer given by layer.
    
            **PARAMETERS**
    
            * *color* - The color to render the blob's convex hull as an RGB triplet.
            * *alpha* - The alpha value of the rendered blob.
            * *width* - The width of the drawn blob in pixels, if w=-1 then the polygon is filled.
            * *layer* - if layer is not None, the blob is rendered to the layer versus the source image.
    
            **RETURNS**
    
            This method either works on the original source image, or on the drawing layer provided.
            The method does not modify object itself.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs(128)
            >>> blobs[-1].drawHull(color=Color.GREEN,width=3,alpha=128)
            >>> img.show()
    
            """
        if layer is None:
            layer = self.image.dl()
        if width < 0:
            layer.polygon(self.mConvexHull, color, filled=True, alpha=alpha)
        else:
            lastp = self.mConvexHull[0]
            for nextp in self.mConvexHull[1:]:
                layer.line(lastp, nextp, color, width=width, alpha=alpha,
                    antialias=False)
                lastp = nextp
            layer.line(self.mConvexHull[0], self.mConvexHull[-1], color, width=
                width, alpha=alpha, antialias=False)
    
    def drawMaskToLayer(self, layer=None, offset=(0, 0)):
        """
            **SUMMARY**
    
            Draw the actual pixels of the blob to another layer. This is handy if you
            want to examine just the pixels inside the contour.
    
            **PARAMETERS**
    
            * *layer* - A drawing layer upon which to apply the mask.
            * *offset* -  The offset from the top left corner where we want to place the mask.
    
            **RETURNS**
    
            This method either works on the original source image, or on the drawing layer provided.
            The method does not modify object itself.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs(128)
            >>> dl = DrawingLayer((img.width,img.height))
            >>> blobs[-1].drawMaskToLayer(layer = dl)
            >>> dl.show()
    
            """
        if layer is not None:
            layer = self.image.dl()
        mx = self.mBoundingBox[0] + offset[0]
        my = self.mBoundingBox[1] + offset[1]
        layer.blit(self.mImg, coordinates=(mx, my))
        return None
    
    def isSquare(self, tolerance=0.05, ratiotolerance=0.05):
        """
            **SUMMARY**
    
            Given a tolerance, test if the blob is a rectangle, and how close its
            bounding rectangle's aspect ratio is to 1.0.
    
            **PARAMETERS**
    
            * *tolerance* - A percentage difference between an ideal rectangle and our hull mask.
            * *ratiotolerance* - A percentage difference of the aspect ratio of our blob and an ideal square.
    
            **RETURNS**
    
            Boolean True if our object falls within tolerance, false otherwise.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs(128)
            >>> if(blobs[-1].isSquare() ):
            >>>     print "it is hip to be square."
    
            """
        if self.isRectangle(tolerance) and abs(1 - self.aspectRatio()
            ) < ratiotolerance:
            return True
        return False
    
    def isRectangle(self, tolerance=0.05):
        """
            **SUMMARY**
    
            Given a tolerance, test the blob against the rectangle distance to see if
            it is rectangular.
    
            **PARAMETERS**
    
            * *tolerance* - The percentage difference between our blob and its idealized bounding box.
    
            **RETURNS**
    
            Boolean True if the blob is withing the rectangle tolerage, false otherwise.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs(128)
            >>> if(blobs[-1].isRecangle() ):
            >>>     print "it is hip to be square."
    
            """
        if self.rectangleDistance() < tolerance:
            return True
        return False
    
    def rectangleDistance(self):
        """
            **SUMMARY**
    
            This compares the hull mask to the bounding rectangle.  Returns the area
            of the blob's hull as a fraction of the bounding rectangle.
    
            **RETURNS**
    
            The number of pixels in the blobs hull mask over the number of pixels in its bounding box.
    
            """
        blackcount, whitecount = self.mHullMask.histogram(2)
        return abs(1.0 - float(whitecount) / (self.minRectWidth() * self.
            minRectHeight()))
    
    def isCircle(self, tolerance=0.05):
        """
            **SUMMARY**
    
            Test circle distance against a tolerance to see if the blob is circlular.
    
            **PARAMETERS**
    
            * *tolerance* - the percentage difference between our blob and an ideal circle.
    
            **RETURNS**
    
            True if the feature is within tolerance for being a circle, false otherwise.
    
            """
        if self.circleDistance() < tolerance:
            return True
        return False
    
    def circleDistance(self):
        """
            **SUMMARY**
    
            Compare the hull mask to an ideal circle and count the number of pixels
            that deviate as a fraction of total area of the ideal circle.
    
            **RETURNS**
    
            The difference, as a percentage, between the hull of our blob and an idealized
            circle of our blob.
    
            """
        w = self.mHullMask.width
        h = self.mHullMask.height
        idealcircle = Image((w, h))
        radius = min(w, h) / 2
        idealcircle.dl().circle((w / 2, h / 2), radius, filled=True, color=
            Color.WHITE)
        idealcircle = idealcircle.applyLayers()
        netdiff = idealcircle - self.mHullMask + (self.mHullMask - idealcircle)
        numblack, numwhite = netdiff.histogram(2)
        return float(numwhite) / (radius * radius * np.pi)
    
    def centroid(self):
        """
            **SUMMARY**
    
            Return the centroid (mass-determined center) of the blob. Note that this is differnt from the bounding box center.
    
            **RETURNS**
    
            An (x,y) tuple that is the center of mass of the blob.
    
            **EXAMPLE**
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> img.drawCircle((blobs[-1].x,blobs[-1].y),10,color=Color.RED)
            >>> img.drawCircle((blobs[-1].centroid()),10,color=Color.BLUE)
            >>> img.show()
    
            """
        return self.m10 / self.m00, self.m01 / self.m00
    
    def radius(self):
        """
            **SUMMARY**
    
            Return the radius, the avg distance of each contour point from the centroid
            """
        return float(np.mean(spsd.cdist(self.mContour, [self.centroid()])))
    
    def hullRadius(self):
        """
            **SUMMARY**
    
            Return the radius of the convex hull contour from the centroid
            """
        return float(np.mean(spsd.cdist(self.mConvexHull, [self.centroid()])))
    
    @LazyProperty
    def mImg(self):
        retVal = cv.CreateImage((self.width(), self.height()), cv.IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        bmp = self.image.getBitmap()
        mask = self.mMask.getBitmap()
        tl = self.topLeftCorner()
        cv.SetImageROI(bmp, (tl[0], tl[1], self.width(), self.height()))
        cv.Copy(bmp, retVal, mask)
        cv.ResetImageROI(bmp)
        return Image(retVal)
    
    @LazyProperty
    def mMask(self):
        retVal = cv.CreateImage((self.width(), self.height()), cv.IPL_DEPTH_8U, 1)
        cv.Zero(retVal)
        l, t = self.topLeftCorner()
        cv.FillPoly(retVal, [[(p[0] - l, p[1] - t) for p in self.mContour]], (
            255, 255, 255), 8)
        holes = []
        if self.mHoleContour is not None:
            for h in self.mHoleContour:
                holes.append([(h2[0] - l, h2[1] - t) for h2 in h])
            cv.FillPoly(retVal, holes, (0, 0, 0), 8)
        return Image(retVal)
    
    @LazyProperty
    def mHullImg(self):
        retVal = cv.CreateImage((self.width(), self.height()), cv.IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        bmp = self.image.getBitmap()
        mask = self.mHullMask.getBitmap()
        tl = self.topLeftCorner()
        cv.SetImageROI(bmp, (tl[0], tl[1], self.width(), self.height()))
        cv.Copy(bmp, retVal, mask)
        cv.ResetImageROI(bmp)
        return Image(retVal)
    
    @LazyProperty
    def mHullMask(self):
        retVal = cv.CreateImage((self.width(), self.height()), cv.IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        thull = []
        l, t = self.topLeftCorner()
        cv.FillPoly(retVal, [[(p[0] - l, p[1] - t) for p in self.mConvexHull]],
            (255, 255, 255), 8)
        return Image(retVal)
    
    def hullImage(self):
        """
            **SUMMARY**
    
            The convex hull of a blob is the shape that would result if you snapped a rubber band around
            the blob. So if you had the letter "C" as your blob the convex hull would be the letter "O."
            This method returns an image where the source image around the convex hull of the blob is copied
            ontop a black background.
    
            **RETURNS**
            Returns a SimpleCV Image of the convex hull, cropped to fit.
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-1].hullImage().show()
    
            """
        return self.mHullImg
    
    def hullMask(self):
        """
            **SUMMARY**
    
            The convex hull of a blob is the shape that would result if you snapped a rubber band around
            the blob. So if you had the letter "C" as your blob the convex hull would be the letter "O."
            This method returns an image where the area of the convex hull is white and the rest of the image
            is black. This image is cropped to the size of the blob.
    
            **RETURNS**
    
            Returns a binary SimpleCV image of the convex hull mask, cropped to fit the blob.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-1].hullMask().show()
    
            """
        return self.mHullMask
    
    def blobImage(self):
        """
            **SUMMARY**
    
            This method automatically copies all of the image data around the blob and puts it in a new
            image. The resulting image has the size of the blob, with the blob data copied in place.
            Where the blob is not present the background is black.
    
            **RETURNS**
    
            Returns just the image of the blob (cropped to fit).
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-1].blobImage().show()
    
    
            """
        return self.mImg
    
    def blobMask(self):
        """
            **SUMMARY**
    
            This method returns an image of the blob's mask. Areas where the blob are present are white
            while all other areas are black. The image is cropped to match the blob area.
    
            **RETURNS**
    
            Returns a SimplecV image of the blob's mask, cropped to fit.
    
            **EXAMPLE**
    
            >>> img = Image("lenna")
            >>> blobs = img.findBlobs()
            >>> blobs[-1].blobMask().show()
    
    
    
            """
        return self.mMask
    
    def match(self, otherblob):
        """
            **SUMMARY**
    
            Compare the Hu moments between two blobs to see if they match.  Returns
            a comparison factor -- lower numbers are a closer match.
    
            **PARAMETERS**
    
            * *otherblob* - The other blob to compare this one to.
    
            **RETURNS**
    
            A single floating point value that is the match quality.
    
            **EXAMPLE**
    
            >>> cam = Camera()
            >>> img1 = cam.getImage()
            >>> img2 = cam.getImage()
            >>> b1 = img1.findBlobs()
            >>> b2 = img2.findBlobs()
            >>> for ba in b1:
            >>>     for bb in b2:
            >>>         print ba.match(bb)
    
            """
        mySigns = np.sign(self.mHu)
        myLogs = np.log(np.abs(self.mHu))
        myM = mySigns * myLogs
        otherSigns = np.sign(otherblob.mHu)
        otherLogs = np.log(np.abs(otherblob.mHu))
        otherM = otherSigns * otherLogs
        return np.sum(abs(1 / myM - 1 / otherM))
    
    def getFullMaskedImage(self):
        """
            Get the full size image with the masked to the blob
            """
        retVal = cv.CreateImage((self.image.width, self.image.height), cv.
            IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        bmp = self.image.getBitmap()
        mask = self.mMask.getBitmap()
        tl = self.topLeftCorner()
        cv.SetImageROI(retVal, (tl[0], tl[1], self.width(), self.height()))
        cv.SetImageROI(bmp, (tl[0], tl[1], self.width(), self.height()))
        cv.Copy(bmp, retVal, mask)
        cv.ResetImageROI(bmp)
        cv.ResetImageROI(retVal)
        return Image(retVal)
    
    def getFullHullMaskedImage(self):
        """
            Get the full size image with the masked to the blob
            """
        retVal = cv.CreateImage((self.image.width, self.image.height), cv.
            IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        bmp = self.image.getBitmap()
        mask = self.mHullMask.getBitmap()
        tl = self.topLeftCorner()
        cv.SetImageROI(retVal, (tl[0], tl[1], self.width(), self.height()))
        cv.SetImageROI(bmp, (tl[0], tl[1], self.width(), self.height()))
        cv.Copy(bmp, retVal, mask)
        cv.ResetImageROI(bmp)
        cv.ResetImageROI(retVal)
        return Image(retVal)
    
    def getFullMask(self):
        """
            Get the full sized image mask
            """
        retVal = cv.CreateImage((self.image.width, self.image.height), cv.
            IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        mask = self.mMask.getBitmap()
        tl = self.topLeftCorner()
        cv.SetImageROI(retVal, (tl[0], tl[1], self.width(), self.height()))
        cv.Copy(mask, retVal)
        cv.ResetImageROI(retVal)
        return Image(retVal)
    
    def getFullHullMask(self):
        """
            Get the full sized image hull mask
            """
        retVal = cv.CreateImage((self.image.width, self.image.height), cv.
            IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        mask = self.mHullMask.getBitmap()
        tl = self.topLeftCorner()
        cv.SetImageROI(retVal, (tl[0], tl[1], self.width(), self.height()))
        cv.Copy(mask, retVal)
        cv.ResetImageROI(retVal)
        return Image(retVal)
    
    def getHullEdgeImage(self):
        retVal = cv.CreateImage((self.width(), self.height()), cv.IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        tl = self.topLeftCorner()
        translate = [(cs[0] - tl[0], cs[1] - tl[1]) for cs in self.mConvexHull]
        cv.PolyLine(retVal, [translate], 1, (255, 255, 255))
        return Image(retVal)
    
    def getFullHullEdgeImage(self):
        retVal = cv.CreateImage((self.image.width, self.image.height), cv.
            IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        cv.PolyLine(retVal, [self.mConvexHull], 1, (255, 255, 255))
        return Image(retVal)
    
    def getEdgeImage(self):
        """
            Get the edge image for the outer contour (no inner holes)
            """
        retVal = cv.CreateImage((self.width(), self.height()), cv.IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        tl = self.topLeftCorner()
        translate = [(cs[0] - tl[0], cs[1] - tl[1]) for cs in self.mContour]
        cv.PolyLine(retVal, [translate], 1, (255, 255, 255))
        return Image(retVal)
    
    def getFullEdgeImage(self):
        """
            Get the edge image within the full size image.
            """
        retVal = cv.CreateImage((self.image.width, self.image.height), cv.
            IPL_DEPTH_8U, 3)
        cv.Zero(retVal)
        cv.PolyLine(retVal, [self.mContour], 1, (255, 255, 255))
        return Image(retVal)
    
    def __repr__(self):
        return 'SimpleCV.Features.Blob.Blob object at (%d, %d) with area %d' % (
            self.x, self.y, self.area())
    
    def _respacePoints(self, contour, min_distance=1, max_distance=5):
        p0 = np.array(contour[-1])
        min_d = min_distance ** 2
        max_d = max_distance ** 2
        contour = [p0] + contour[:-1]
        contour = contour[:-1]
        retVal = [p0]
        while len(contour) > 0:
            pt = np.array(contour.pop())
            dist = (p0[0] - pt[0]) ** 2 + (p0[1] - pt[1]) ** 2
            if dist > max_d:
                a = float(pt[0] - p0[0])
                b = float(pt[1] - p0[1])
                l = np.sqrt(a ** 2 + b ** 2)
                punit = np.array([a / l, b / l])
                pn = max_distance * punit + p0
                retVal.append((pn[0], pn[1]))
                contour.append(pt)
                p0 = pn
            elif dist > min_d:
                p0 = np.array(pt)
                retVal.append(pt)
        return retVal
    
    def _filterSCPoints(self, min_distance=3, max_distance=8):
        """
            Go through ever point in the contour and make sure
            that it is no less than min distance to the next point
            and no more than max_distance from the the next point.
            """
        completeContour = self._respacePoints(self.mContour, min_distance,
            max_distance)
        if self.mHoleContour is not None:
            for ctr in self.mHoleContour:
                completeContour = completeContour + self._respacePoints(ctr,
                    min_distance, max_distance)
        return completeContour
    
    def getSCDescriptors(self):
        if self._scdescriptors is not None:
            return self._scdescriptors, self._completeContour
        completeContour = self._filterSCPoints()
        descriptor = self._generateSC(completeContour)
        self._scdescriptors = descriptors
        self._completeContour = completeContour
        return descriptors, completeContour
    
    def _generateSC(self, completeContour, dsz=6, r_bound=[0.1, 2.1]):
        """
            Create the shape context objects.
            dsz - The size of descriptor as a dszxdsz histogram
            completeContour - All of the edge points as a long list
            r_bound - Bounds on the log part of the shape context descriptor
            """
        data = []
        for pt in completeContour:
            temp = []
            for b in completeContour:
                r = np.sqrt((b[0] - pt[0]) ** 2 + (b[1] - pt[1]) ** 2)
                if r == 0.0:
                    continue
                r = np.log10(r)
                theta = np.arctan2(b[0] - pt[0], b[1] - pt[1])
                if np.isfinite(r) and np.isfinite(theta):
                    temp.append((r, theta))
            data.append(temp)
        descriptors = []
        for d in data:
            test = np.array(d)
            hist, a, b = np.histogram2d(test[:, 0], test[:, 1], dsz, [r_bound,
                [np.pi * -1 / 2, np.pi / 2]], normed=True)
            hist = hist.reshape(1, dsz ** 2)
            if np.all(np.isfinite(hist[0])):
                descriptors.append(hist[0])
        self._scdescriptors = descriptors
        return descriptors
    
    def getShapeContext(self):
        """
            Return the shape context descriptors as a featureset. Corrently
            this is not used for recognition but we will perhaps use it soon.
            """
        derp = self.getSCDescriptors()
        descriptors, completeContour = self.getSCDescriptors()
        fs = FeatureSet()
        for i in range(0, len(completeContour)):
            fs.append(ShapeContextDescriptor(self.image, completeContour[i],
                descriptors[i], self))
        return fs
    
    def showCorrespondence(self, otherBlob, side='left'):
        """
            This is total beta - use at your own risk.
            """
        side = side.lower()
        myPts = self.getShapeContext()
        yourPts = otherBlob.getShapeContext()
        myImg = self.image.copy()
        yourImg = otherBlob.image.copy()
        myPts = myPts.reassignImage(myImg)
        yourPts = yourPts.reassignImage(yourImg)
        myPts.draw()
        myImg = myImg.applyLayers()
        yourPts.draw()
        yourImg = yourImg.applyLayers()
        result = myImg.sideBySide(yourImg, side=side)
        data = self.shapeContextMatch(otherBlob)
        mapvals = data[0]
        color = Color()
        for i in range(0, len(self._completeContour)):
            lhs = self._completeContour[i]
            idx = mapvals[i]
            rhs = otherBlob._completeContour[idx]
            if side == 'left':
                shift = rhs[0] + yourImg.width, rhs[1]
                result.drawLine(lhs, shift, color=color.getRandom(), thickness=1)
            elif side == 'bottom':
                shift = rhs[0], rhs[1] + myImg.height
                result.drawLine(lhs, shift, color=color.getRandom(), thickness=1)
            elif side == 'right':
                shift = rhs[0] + myImg.width, rhs[1]
                result.drawLine(lhs, shift, color=color.getRandom(), thickness=1)
            elif side == 'top':
                shift = lhs[0], lhs[1] + myImg.height
                result.drawLine(lhs, shift, color=color.getRandom(), thickness=1)
        return result
    
    def getMatchMetric(self, otherBlob):
        """
            This match metric is now deprecated.
            """
        data = self.shapeContextMatch(otherBlob)
        distances = np.array(data[1])
        sd = np.std(distances)
        x = np.mean(distances)
        min = np.min(distances)
        tmean = sps.tmean(distances, (min, x + sd))
        return tmean
    
<<<<<<< left_content.py
    def getConvexityDefects(self):
=======
    def getConvexityDefects(self, returnPoints=False):
>>>>>>> right_content.py
        """
            **SUMMARY**
    
            Get Convexity Defects of the contour.
    
            **PARAMETERS**
    
<<<<<<< left_content.py
            **RETURNS**
    
            FeatureSet - A FeatureSet of Line objects.
=======
            *returnPoints* - Bool(False). 
                             If False: Returns FeatureSet of Line(start point, end point) 
                             and Corner(far point)
                             If True: Returns a list of tuples
                             (start point, end point, far point)
            **RETURNS**
    
            FeatureSet - A FeatureSet of Line and Corner objects
                         OR
                         A list of (start point, end point, far point)
                         See PARAMETERS.
>>>>>>> right_content.py
    
            **EXAMPLE**
    
            >>> img = Image('lenna')
            >>> blobs = img.findBlobs()
            >>> blob = blobs[-1]
<<<<<<< left_content.py
            >>> feat = blob.getConvexityDefects()
            >>> feat.draw()
            >>> img.show()
=======
            >>> lines, farpoints = blob.getConvexityDefects()
            >>> lines.draw()
            >>> farpoints.draw(color=Color.RED, width=-1)
            >>> img.show()
    
            >>> points = blob.getConvexityDefects(returnPoints=True)
            >>> startpoints = zip(*points)[0]
            >>> endpoints = zip(*points)[0]
            >>> farpoints = zip(*points)[0]
            >>> print startpoints, endpoints, farpoints
>>>>>>> right_content.py
            """
    
        def cvFallback():
            chull = cv.ConvexHull2(self.mContour, cv.CreateMemStorage(),
                return_points=False)
            defects = cv.ConvexityDefects(self.mContour, chull, cv.
                CreateMemStorage())
<<<<<<< left_content.py
            features = FeatureSet([Line(self.image, (defect[0], defect[1])) for
                defect in defects])
            return features
=======
            points = [(defect[0], defect[1], defect[2]) for defect in defects]
            return points
>>>>>>> right_content.py
        try:
            import cv2
            if hasattr(cv2, 'convexityDefects'):
                hull = [self.mContour.index(x) for x in self.mConvexHull]
                hull = np.array(hull).reshape(len(hull), 1)
                defects = cv2.convexityDefects(np.array(self.mContour), hull)
                if isinstance(defects, type(None)):
                    warnings.warn(
                        'Unable to find defects. Returning Empty FeatureSet.')
                    defects = []
<<<<<<< left_content.py
                features = FeatureSet([Line(self.image, (self.mContour[defect[0
                    ][0]], self.mContour[defect[0][1]])) for defect in defects])
            else:
                features = cvFallback()
        except ImportError:
            features = cvFallback()
        return features
=======
                points = [(self.mContour[defect[0][0]], self.mContour[defect[0]
                    [1]], self.mContour[defect[0][2]]) for defect in defects]
            else:
                points = cvFallback()
        except ImportError:
            points = cvFallback()
        if returnPoints:
            return FeatureSet(points)
        else:
            lines = FeatureSet([Line(self.image, (start, end)) for start, end,
                far in points])
            farpoints = FeatureSet([Corner(self.image, far[0], far[1]) for 
                start, end, far in points])
            features = FeatureSet([lines, farpoints])
            return features
>>>>>>> right_content.py
    
    

