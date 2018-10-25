package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;


public class Controller {

    public Label imageInfo;
    public ImageView picture;
    public Button SaveAsPngButton,SaveAsBinButton;
    BufferedImage cykaImage;

    public byte[] pik4a;
    public int width, heigh;


    public int RGB888toRGB565(int color){
        color=color&0x00FFFFFF;

        int B=(color&0x0000FF);
        int G=(color&0x01FF00)>>8;
        int R=(color&0xF80000)>>16;

        int red = R>>3;
        int green = G>>2;
        int blue = B>>3;

        int red_value = red << 11;
        int green_value = green << 5;
        int blue_value = blue;

        return red_value|green_value|blue_value;
    }

    public String hex(int hexx){return Integer.toHexString(hexx);}

    public int RGB565toRGBA888(int color){

        int red_mask = 0x00F800;
        int green_mask = 0x0007E0;
        int blue_mask = 0x00001F;

        int red_value = (color & red_mask) >> 11;
        int green_value = (color & green_mask) >> 5;
        int blue_value = (color & blue_mask);

        int red = red_value<<3;
        int green = green_value<<2;
        int blue = blue_value<<3;

        return (0xFF000000 | (red<< 16) | (green << 8) | blue);
    }

    public void drawPik4a() throws IOException {

        boolean normalSize = true;
        picture.setImage(null);

        width = (pik4a[0] & 0x00FF) << 8 | (pik4a[1] & 0x00FF);
        heigh = (pik4a[2] & 0x00FF) << 8 | (pik4a[3] & 0x00FF);

        if (width > 240 || heigh > 320) {
            JOptionPane.showMessageDialog(null, "The image is too big!\nDo not open image more than: width>240 and height>320\nYou open: width=" + width + " and height=" + heigh
                    + "\nPlease, open another image(bin or png) file.", "Info: " + "image too big!", JOptionPane.INFORMATION_MESSAGE);
            normalSize = false;
            SaveAsPngButton.setDisable(true);
            SaveAsBinButton.setDisable(true);
        }
        if (normalSize) {
            WritableImage writableImage = new WritableImage(width, heigh);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            picture.setImage(writableImage);

            int pointer = 4;
            System.out.println("width=" + width + " height=" + heigh);
            imageInfo.setText("Bin file: width=" + width + " height=" + heigh);
            for (int i = 0; i < heigh; i++) {
                for (int j = 0; j < width; j++) {
                    int pixelCode = ((pik4a[pointer + 1] << 8) | pik4a[pointer] & 0xFF) & 0xFFFF;
                    int rgb = RGB565toRGBA888(pixelCode);

                    pixelWriter.setArgb(j, i, rgb);
                    pointer += 2;
                }
            }
            SaveAsPngButton.setDisable(false);
            SaveAsBinButton.setDisable(true);
        }


    }

  public void openBinFile() throws IOException {
      FileChooser fileChooser = new FileChooser();
      FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Bin files (*.bin)", "*.bin","*.BIN");
      fileChooser.getExtensionFilters().add(extFilter);
      fileChooser.setTitle("Open BIN File");
      File file = fileChooser.showOpenDialog(null);

      if(file!=null){
          Path fileLocation = Paths.get(file.getPath());
          pik4a = Files.readAllBytes(fileLocation);
          drawPik4a();
      }
  }

    public void openPngFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Open PNG File");
        File file = fileChooser.showOpenDialog(null);



        if(file!=null) {
            cykaImage = ImageIO.read(file);

            WritableImage writableImage = SwingFXUtils.toFXImage(cykaImage, null);
            picture.setImage(writableImage);

            System.out.println("wi="+(int)picture.getImage().getHeight()+ " hei:"+(int)picture.getImage().getWidth());


            heigh = (int)picture.getImage().getHeight();
            width = (int)picture.getImage().getWidth();


            if (width > 240 || heigh > 320) {
                JOptionPane.showMessageDialog(null, "The image is too big!\nDo not open image more than: width>240 and height>320\nYou open: width=" + width + " and height=" + heigh
                        + "\nPlease, open another image(bin or png) file.", "Info: " + "image too big!", JOptionPane.INFORMATION_MESSAGE);
                SaveAsPngButton.setDisable(true);
                SaveAsBinButton.setDisable(true);
            }else {

                imageInfo.setText("Jpeg file: width=" + width + " height=" + heigh);
                pik4a = new byte[(int) (picture.getImage().getWidth() * picture.getImage().getHeight() * 2) + 4];

                pik4a[0] = (byte) ((((int) picture.getImage().getWidth()) & 0xFF00) >> 8);
                pik4a[1] = (byte) (((int) picture.getImage().getWidth()) & 0x00FF);

                pik4a[2] = (byte) ((((int) picture.getImage().getHeight()) & 0xFF00) >> 8);
                pik4a[3] = (byte) (((int) picture.getImage().getHeight()) & 0x00FF);

                System.out.println(Integer.toHexString(pik4a[3]));

                System.out.println("w :" + hex(((pik4a[0] & 0xFF) << 8 | pik4a[1] & 0xFF) & 0xFFFF) + " h:" + hex(((pik4a[2] & 0xFF) << 8 | pik4a[3] & 0xFF) & 0xFFFF));
                SaveAsPngButton.setDisable(true);
                SaveAsBinButton.setDisable(false);
            }
        }
    }

  public void resovalka(MouseEvent event) throws IOException {
      System.out.println("Can`t touch this ;-)");
  }


  public void savePictureToPngFile() throws IOException {
      FileChooser fileChooser = new FileChooser();
      FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Save as png","*.png");
      fileChooser.getExtensionFilters().add(extFilter);
      fileChooser.setTitle("Save as png");
      File file = fileChooser.showSaveDialog(null);
      if(file!=null) {

        Image image = picture.getImage();
        BufferedImage bImage = SwingFXUtils.fromFXImage(image,null);

        if(ImageIO.write(bImage,"png",file)){
            JOptionPane.showMessageDialog(null, "File is saved!\n"+ file.getPath(), "Info", JOptionPane.INFORMATION_MESSAGE);
        }else{
            JOptionPane.showMessageDialog(null, "File is not saved!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }

      }
  }

  public void savePictureToBinFile(){

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Save as bin","*.bin");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Save as bin");
        File file;

        byte[] toFIle = new byte[pik4a.length];
        int pointer = 4;

      toFIle[0] = pik4a[0];
      toFIle[1] = pik4a[1];

      toFIle[2] = pik4a[2];
      toFIle[3] = pik4a[3];

      heigh = (int)picture.getImage().getHeight();
      width = (int)picture.getImage().getWidth();

      for (int i = 0; i < heigh; i++) {
          for (int j = 0; j < width; j++) {
              int PixelColor = (picture.getImage().getPixelReader().getArgb(j,i)&0x00FFFFFF);
              int sixteenBitColor = RGB888toRGB565(PixelColor);

              //System.out.println("pointer:"+pointer+" i:"+i+" j:"+j);
              toFIle[pointer+1] = (byte)((sixteenBitColor&0xFF00)>>8);
              toFIle[pointer] = (byte)(sixteenBitColor&0x00FF);
              pointer+=2;
          }
      }
      file = fileChooser.showSaveDialog(null);
      try (
              FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(toFIle);
              JOptionPane.showMessageDialog(null, "File is saved!\n"+ file.getPath(), "Info", JOptionPane.INFORMATION_MESSAGE);
      } catch (FileNotFoundException e) {
         // e.printStackTrace();
          JOptionPane.showMessageDialog(null, "File is not saved!", "Info", JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException e) {
          JOptionPane.showMessageDialog(null, "File is not saved!", "Info", JOptionPane.INFORMATION_MESSAGE);
         // e.printStackTrace();
      }
  }

  public void authorInfo(){
      JOptionPane.showMessageDialog(null, "Designed by Aleksey Smirnov in oct 2018\nvk.com/xylic", "Author Info", JOptionPane.INFORMATION_MESSAGE);
  }

  public void testCyka(){
      System.out.println("Closing app...");
      Platform.exit();
      System.exit(0);
  }

}
