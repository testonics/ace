package TestJUnit;

import in.testonics.omni.models.PDFCompare;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;

public class FileCompareTest {

    String path = ".\\src\\test\\resources\\TestData\\";

    @Test
    public void InvalidFile() throws Exception {
        System.out.println("Invalid File Validation");
        PDFCompare pdfCompare = new PDFCompare();
        File file1 = new File(path + "PDF1.docx");
        File file2 = new File(path + "PDF2.pdf");
        JSONObject jsonObject = pdfCompare.compare(file1,file2);
        System.out.println(jsonObject);
    }

    @Test
    public void PageCountMismatch() throws Exception {
        System.out.println("Page Count Mismatch Validation");
        PDFCompare pdfCompare = new PDFCompare();
        File file1 = new File(path + "PDF1.pdf");
        File file2 = new File(path + "PDF2.pdf");
        JSONObject jsonObject = pdfCompare.compare(file1,file2);
        System.out.println(jsonObject);
    }

    @Test
    public void LineCountMismatch() throws Exception {
        System.out.println("Line Count Mismatch Validation");
        PDFCompare pdfCompare = new PDFCompare();
        File file1 = new File(path + "PDF1.pdf");
        File file2 = new File(path + "PDF2.pdf");
        JSONObject jsonObject = pdfCompare.compare(file1,file2,2);
        System.out.println(jsonObject);
    }

    @Test
    public void Mismatch() throws Exception {
        System.out.println("PDF Font, Size, Type and Text Mismatch validation");
        PDFCompare pdfCompare = new PDFCompare();
        pdfCompare.setEnableFontValidation(true);
        pdfCompare.setEnableFontSizeValidation(true);
        pdfCompare.setBoldItalicValidation(true);
        File file1 = new File(path + "PDF3.pdf");
        File file2 = new File(path + "PDF5.pdf");
        JSONObject jsonObject = pdfCompare.compare(file1,file2,1);
        System.out.println(jsonObject);
    }
}
