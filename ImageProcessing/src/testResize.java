import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class testResize {


    static BufferedImage createResizedCopy(Image originalImage,
                                                   int newWidth, int newHeight, String mode)
    {
        System.out.println("resizing...");
        int imageType = BufferedImage.TYPE_INT_ARGB;
        float alpha = 1f;
        int w, h;
        int imageWidth = originalImage.getWidth(null);
        int imageHeight = originalImage.getHeight(null);
        double ratio;
        if(mode.equals("W")){    // 넓이기준

            ratio = (double)newWidth/(double)imageWidth;
            w = (int)(imageWidth * ratio);
            h = (int)(imageHeight * ratio);

        }else if(mode.equals("H")){ // 높이기준

            ratio = (double)newHeight/(double)imageHeight;
            w = (int)(imageWidth * ratio);
            h = (int)(imageHeight * ratio);

        }else{ //설정값 (비율무시)

            w = newWidth;
            h = newHeight;
        }

        BufferedImage scaledBI = new BufferedImage(w, h, imageType);
        Graphics2D g = scaledBI.createGraphics();
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g.setComposite(ac);
        g.drawImage(originalImage, 0, 0, w, h, null);
        g.dispose();
        return scaledBI;
    }

    static BufferedImage addPadding(BufferedImage originalImage, int newWidth, int newHeight)
    {

        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Color empty = new Color(1, 1, 1, 0f);

        if(originalImage.getWidth() < newWidth){
            int needLength = newWidth - originalImage.getWidth();

            for(int j = 0; j < newHeight; j++)
            {
                int k = 0;

                for(int i = 0; i < newWidth; i++)
                {
                    if(i < needLength/2)
                        newImage.setRGB(i, j, empty.getRGB());
                    else if(k >= originalImage.getWidth())
                        newImage.setRGB(i, j, empty.getRGB());
                    else {
                        newImage.setRGB(i, j, originalImage.getRGB(k, j));
                        k++;
                    }
                }
            }
        }
        else if(originalImage.getHeight() < newHeight)
        {
            int needLength = newHeight - originalImage.getHeight();

            int k = 0;
            for(int j = 0; j < newHeight; j++)
            {
                for(int i = 0; i < newWidth; i++)
                {
                    if(j < needLength/2)
                        newImage.setRGB(i, j, empty.getRGB());
                    else if(k >= originalImage.getHeight())
                        newImage.setRGB(i, j, empty.getRGB());
                    else {
                        newImage.setRGB(i, j, originalImage.getRGB(i, k));
                        k++;
                    }
                }
            }
        }
        else
            return originalImage;

        return newImage;
    }


    public static void main(String[] args) {
        String imgOriginalPath= "D:/m2j97/Documents/IntelliJ_IDEA/ImageProcessing/Images/test3.png";           // 원본 이미지 파일명
        String imgTargetPath= "D:/m2j97/Documents/IntelliJ_IDEA/ImageProcessing/Images/test_resize.jpg";    // 새 이미지 파일명
        int newWidth = 144;                                  // 변경 할 넓이
        int newHeight = 144;                                 // 변경 할 높이

        Image image;

        try {
            // 원본 이미지 가져오기
            image = ImageIO.read(new File(imgOriginalPath));
            BufferedImage scaledImage = createResizedCopy(image, newWidth, newHeight, "H");

            //투명 패딩 코드 추가 필요
            BufferedImage resultImage = addPadding(scaledImage, newWidth, newHeight);

            File outFile = new File(imgTargetPath);
            ImageIO.write(resultImage, "PNG", outFile);
        }catch (Exception e){

        e.printStackTrace();

        }
    }
}
