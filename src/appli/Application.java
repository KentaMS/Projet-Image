package appli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Application {
	
	// Initialisation des listes des différents niveaux du verre trouvés.
	public static ArrayList<Integer> objets = new ArrayList<>();
	public static ArrayList<Integer> objetsFiltres = new ArrayList<>();
	
	// Initialisation du niveau du liquide à 0%.
	public static int niveauLiquide = 0;
	
	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Lecture de l'image sélectionnée.
		Mat imgMat = Imgcodecs.imread("src/img/20.png", Imgcodecs.IMREAD_ANYCOLOR);
		
		// Conversion de l'image en niveaux de gris.
		Mat greyMat = new Mat();
		greyMat = imgMat.clone();
		HighGui.imshow("normal img", greyMat);

		Mat edgeImage;
		edgeImage = openImage(binarization(detectHorizontalEdgesSobel(sharpenImage(greyMat))));
		HighGui.imshow("Image à bords", edgeImage);
		
		objets = detectObjects(edgeImage);
		
		int threshold = (int) (edgeImage.rows() * 0.02);
		objetsFiltres = filterObjects(objets, threshold);

		System.out.println(getLiquidQuantity(objetsFiltres) + "%");

		HighGui.waitKey();
	}
	
	public static int getLiquidQuantity(ArrayList<Integer> lines) {
		if(lines.size() <= 4) {
			return 0;
		} else {
			float glassTop = lines.get(1);
			float waterTop = lines.get(3);
			float glassBottom = lines.get(4);
			
			float waterLevel = ((glassBottom - waterTop) / (glassBottom - glassTop)) * 100;

			return (int) waterLevel;
		}
	}
	
	/**
	 * Détecte les traits de l'image fournie et les range dans une liste.
	 * @param mSource L'image dont les traits sont à détecter.
	 * @return Une liste contenant les traits détectés.
	 */
	public static ArrayList<Integer> detectObjects(Mat mSource) {
		ArrayList<Integer> lines = new ArrayList<>();
		
		for(int i = 0; i < mSource.rows(); i++) {
			for(int j = 0; j < mSource.cols(); j++) {
				if(mSource.get(i, j)[0] == 255.0) {
					lines.add(i);
					break;
				}
			}
		}
		
		return lines;
	}
	
	/**
	 * Filtre les traits d'une liste fournie en regroupant les plus proches.
	 * @param lines La liste contenant les traits d'une image.
	 * @param threshold L'int représentant l'espace entre les lignes maximum.
	 * @return Une liste contenant les traits filtrés.
	 */
	public static ArrayList<Integer> filterObjects(ArrayList<Integer> lines, int threshold) {
		ArrayList<Integer> filteredObjects = new ArrayList<>();
		
		// Regroupe les objets les plus proches entre eux.
		ArrayList<Integer> seriesOfLines = new ArrayList<>();
		for(int line : lines) {
			if(seriesOfLines.isEmpty()) {
				seriesOfLines.add(line);
			} else {
				int lastOfSeries = seriesOfLines.get(seriesOfLines.size() - 1);
				if(line - lastOfSeries <= threshold) {
					seriesOfLines.add(line);
				} else {
					int sum = 0;
					for(int object : seriesOfLines) {
						sum += object;
					}
					filteredObjects.add(sum / seriesOfLines.size());
					seriesOfLines.clear();
					seriesOfLines.add(line);
				}
			}
		}
		int sum = 0;
		for(int object : seriesOfLines) {
			sum += object;
		}
		filteredObjects.add(sum / seriesOfLines.size());
		
		return filteredObjects;
	}
	
	/**
	 * Fonction rendant l'image plus nette afin de faciliter la détection de contours.
	 * @param mSource L'image que l'on souhaite rendre net.
	 * @return Une image plus nette.
	 */
	public static Mat sharpenImage(Mat mSource) {
		Mat mSharpened = new Mat();
		
		Size size = new Size(0,0);
		Imgproc.GaussianBlur(mSource, mSharpened, size, 3);
		Core.addWeighted(mSource, 1.5, mSharpened, -0.5, 0, mSharpened);
		
		return mSharpened;
	}
	
	/**
	 * Détecte les contours horizontaux de l'image en utilisant le procédé de Sobel.
	 * @param mSource L'image dont les contours sont à détecter.
	 * @return Une image représentant les contours horizontaux de l'image donnée.
	 */
	public static Mat detectHorizontalEdgesSobel(Mat mSource) {
		Mat mSourceGray = new Mat();
		Mat mHorizontalGradient = new Mat();
		Mat mAbsoluteHorizontalGradient = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		Imgproc.Sobel(mSourceGray, mHorizontalGradient, CvType.CV_16S, 0, 1);
		Core.convertScaleAbs(mHorizontalGradient, mAbsoluteHorizontalGradient);
		
		return mAbsoluteHorizontalGradient;
	}
	
	/**
	 * Applique une ouverture à une image.
	 * @param mSource L'image à ouvrir.
	 * @return Une image représentant l'image donnée ouverte.
	 */
	public static Mat openImage(Mat mSource) {
		int horizontalSize = mSource.cols() / 35;
		Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalSize, 1));
		Imgproc.erode(mSource, mSource, horizontalStructure, new Point(-1, -1));
		Imgproc.dilate(mSource, mSource, horizontalStructure, new Point(-1, -1));
		
		return mSource;
	}
	
	/**
	 * Applique une binarisation à une image.
	 * @param mSource L'image à binariser.
	 * @return Une image représentant la version binarisée de l'image donnée.
	 */
	public static Mat binarization(Mat mSource) {
		Mat binaryMat = new Mat();
		Mat mSourceGray = new Mat();
		mSourceGray = returnGrayIfNotGray(mSource);
		// blockSize must be odd
		int blockSize = 5;
		Imgproc.adaptiveThreshold(mSourceGray, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, -60);
		
		return binaryMat;
	}
	
	/**
	 * Transforme une image en niveaux de gris si elle ne l'est pas déjà.
	 * @param mSource L'image a transformer.
	 * @return Une image représentant la version en niveaux de gris de l'image donnée.
	 */
	private static Mat returnGrayIfNotGray(Mat mSource) {
		if (mSource.channels() != 1) {
			  Imgproc.cvtColor(mSource, mSource, Imgproc.COLOR_RGB2GRAY);
		}
		
		return mSource;
	}
}
