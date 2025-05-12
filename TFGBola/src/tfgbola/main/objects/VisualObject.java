/*
    Clase de objetos visuales
 */
package tfgbola.main.objects;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author jesus
 */
public class VisualObject implements Serializable {

    private BufferedImage img;

    public VisualObject() {
        try {
            img = ImageIO.read(getClass().getResource("/resources/nave-espacial.png"));
        } catch (IOException ex) {
            Logger.getLogger(VisualObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public VisualObject(BufferedImage image) {
        this.img = image;
    }

    public void pintar(Graphics g) {
    }

    public BufferedImage getImage() {
        return img;
    }

    public void setImage(BufferedImage img) {
        this.img = img;
    }

    public void setCarImage() {
        try {
            img = ImageIO.read(getClass().getResource("/resources/coche.png"));
        } catch (IOException ex) {
            Logger.getLogger(VisualObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
