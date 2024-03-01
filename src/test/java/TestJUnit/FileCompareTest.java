package TestJUnit;

import in.testonics.omni.models.PDFCompare;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;

public class FileCompareTest {

    PDFCompare pdfCompare = new PDFCompare();
    String path = ".\\src\\test\\resources\\TestData\\";

    @Test
    public void InvalidFile() throws Exception {
        System.out.println("Invalid File Validation");
        File file1 = new File(path + "PDF1.docx");
        File file2 = new File(path + "PDF2.pdf");
        JSONObject jsonObject = pdfCompare.compare(file1,file2);
        System.out.println(jsonObject);
    }

    @Test
    public void PageCountMismatch() throws Exception {
        System.out.println("Page Count Mismatch Validation");
        File file1 = new File(path + "PDF1.pdf");
        File file2 = new File(path + "PDF2.pdf");
        JSONObject jsonObject = pdfCompare.compare(file1,file2);
        System.out.println(jsonObject);
    }

    @Test
    public void LineCountMismatch() throws Exception {
        System.out.println("Line Count Mismatch Validation");
        File file1 = new File(path + "PDF1.pdf");
        File file2 = new File(path + "PDF2.pdf");
        JSONObject jsonObject = pdfCompare.compare(file1,file2,2);
        System.out.println(jsonObject);
    }

    @Test
    public void Mismatch() throws Exception {
        System.out.println("PDF Font, Size, Type and Text Mismatch validation");
        pdfCompare.setEnableFontValidation(true);
        pdfCompare.setEnableFontSizeValidation(true);
        JSONObject jsonObject = pdfCompare.compare(path + "PDF3.pdf",path + "PDF5.pdf",1);
        System.out.println(jsonObject);
    }

    @Test
    public void ExtractText() throws Exception {
        System.out.println("Extracting the text");
        String text = pdfCompare.setEnableFontValidation(true)
                                .setEnableFontSizeValidation(true)
                                .setFetchImagesFlag(true)
                                .setImagesPath(".//target//")
                                .getFileTextAndImage(path + "PDF-File-Sample_150kB.pdf",1);
        System.out.println("Extracted Text & Image from page 1 :" + text);
    }

//    @Test
//    public void CompareFolders() throws Exception {
//        System.out.println("Comparing all the PDFs in the folder with PDF Font, Size, Type and Text Mismatch validation");
//        pdfCompare.setEnableFontValidation(true);
//        pdfCompare.setEnableFontSizeValidation(true);
//        JSONObject jsonObject = pdfCompare.compare(path,path);
//        System.out.println(jsonObject);
//    }
}
