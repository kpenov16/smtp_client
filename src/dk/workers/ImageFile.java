package dk.workers;

//Kaloyan Penov: just a helper class to model an image
public class ImageFile {
    public String base64File = "";
    public String fileName = "";

    public ImageFile(String base64File, String fileName) {
        this.base64File = base64File;
        this.fileName = fileName;
    }
}
