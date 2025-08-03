package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Image.fxml, responsible for displaying a static image.
 */
public class ImageController implements Initializable {

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Check if imageView is properly injected
        if (imageView == null) {
            System.err.println("❌ ImageView is null! Check your FXML file:");
            System.err.println("- Ensure the ImageView has fx:id=\"imageView\"");
            System.err.println("- Verify the FXML file is properly linked to this controller");
            System.err.println("- Check that the ImageView is properly declared in the FXML");

            // Try to create the ImageView programmatically as a fallback
            try {
                imageView = new ImageView();
                imageView.setFitHeight(1085.0);
                imageView.setFitWidth(685.0);
                imageView.setPreserveRatio(true);
                System.out.println("⚠️ Created ImageView programmatically as fallback");
            } catch (Exception e) {
                System.err.println("❌ Failed to create ImageView programmatically: " + e.getMessage());
                return;
            }
        }

        boolean imageLoaded = false;

        try {
            // Try to load from the resources first
            URL imageUrl = getClass().getResource("/image/optique_image_resized.png");

            if (imageUrl != null) {
                // If found in classpath
                Image image = new Image(imageUrl.toExternalForm());
                if (!image.isError()) {
                    imageView.setImage(image);
                    System.out.println("✅ Loaded image from resources.");
                    imageLoaded = true;
                }
            }

            // If not loaded from resources, try fallback
            if (!imageLoaded) {
                File file = new File("src/image/optique_image_resized.png");
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    if (!image.isError()) {
                        imageView.setImage(image);
                        System.out.println("✅ Loaded image from src/image/ via file path.");
                        imageLoaded = true;
                    }
                }
            }

            // If still not loaded, try additional fallback locations
            if (!imageLoaded) {
                String[] fallbackPaths = {
                        "image/optique_image_resized.png",
                        "out/production/Gestion_cabinet_ d'optique/image/optique_image_resized.png",
                        "resources/image/optique_image_resized.png"
                };

                for (String path : fallbackPaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        if (!image.isError()) {
                            imageView.setImage(image);
                            System.out.println("✅ Loaded image from fallback path: " + path);
                            imageLoaded = true;
                            break;
                        }
                    }
                }
            }

            if (!imageLoaded) {
                System.err.println("❌ Image not found in any location!");
                System.err.println("Please ensure 'optique_image_resized.png' exists in one of these locations:");
                System.err.println("- src/main/resources/image/");
                System.err.println("- src/image/");
                System.err.println("- image/");
                System.err.println("- out/production/Gestion_cabinet_ d'optique/image/");
            }

        } catch (Exception e) {
            System.err.println("An error occurred while loading the image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}