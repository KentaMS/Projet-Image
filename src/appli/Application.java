package appli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Application {
	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat imgMat = Imgcodecs.imread("src/img/19.jpeg", Imgcodecs.IMREAD_ANYCOLOR);
		Mat greyMat = new Mat();
		greyMat = imgMat.clone();
		HighGui.imshow("normal img", greyMat);

		HighGui.imshow("Image à bords", openImage(binarization(detectEdgesSobel(sharpenImage(greyMat)), 13)));

		HighGui.waitKey();
	}
	
	public static Mat sharpenImage(Mat mSource) {
		Mat mSharpened = new Mat();
		
		Size size = new Size(0,0);
		Imgproc.GaussianBlur(mSource, mSharpened, size, 3);
		Core.addWeighted(mSource, 1.5, mSharpened, -0.5, 0, mSharpened);
		
		return mSharpened;
	}
	
	public static Mat detectEdgesSobel(Mat mSource) {
		Mat mSourceGray = new Mat();
		Mat mHorizontalGradient = new Mat();
		Mat mAbsoluteHorizontalGradient = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		Imgproc.Sobel(mSourceGray, mHorizontalGradient, CvType.CV_16S, 0, 1);
		Core.convertScaleAbs(mHorizontalGradient, mAbsoluteHorizontalGradient);
		
		return mAbsoluteHorizontalGradient;
	}
	
	public static Mat openImage(Mat mSource) {
		int horizontalSize = mSource.cols() / 35;
		Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalSize, 1));
		Imgproc.erode(mSource, mSource, horizontalStructure, new Point(-1, -1));
		Imgproc.dilate(mSource, mSource, horizontalStructure, new Point(-1, -1));
		
		return mSource;
	}
	
	public static Mat binarization(Mat mSource, int blockSize) {
		Mat binaryMat = new Mat();
		Mat mSourceGray = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		// blockSize must be odd
		Imgproc.adaptiveThreshold(mSourceGray, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, -60);
		return binaryMat;
	}
	
	private static Mat returnGrayIfNotGray(Mat mSource) {
		if (mSource.channels() != 1) {
			  Imgproc.cvtColor(mSource, mSource, Imgproc.COLOR_RGB2GRAY);
		}
		return mSource;
	}
}
