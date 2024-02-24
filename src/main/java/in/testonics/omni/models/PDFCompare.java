package in.testonics.omni.models;

import in.testonics.omni.utils.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PDFCompare extends FileUtils {

    private boolean ENABLE_FONT_VALIDATION = false;
    private boolean ENABLE_FONT_SIZE_VALIDATION = false;
    private boolean ENABLE_BOLD_ITALIC_VALIDATION = false;

    public JSONObject compare(String pathOfFile1, String pathOfFile2, int pageNumber) throws Exception {
        return compare(new File(pathOfFile1),new File(pathOfFile2),pageNumber);
    }

    public JSONObject compare(String pathOfFile1, String pathOfFile2) throws Exception {
        return compare(new File(pathOfFile1),new File(pathOfFile2));
    }

    public JSONObject compare(File pdfFile1, File pdfFile2) throws Exception {
        return compare(pdfFile1,pdfFile2,0);
    }

    public JSONObject compare(File pdfFile1, File pdfFile2, int pageNumber) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        setColumnNames();
        System.out.println("Comparing PDF files : \nPDF 1: " + pdfFile1 + "\nPDF2 : " + pdfFile2);

        if (!getFileExtension(pdfFile1).equalsIgnoreCase("PDF") || !getFileExtension(pdfFile2).equalsIgnoreCase("PDF")){
            jsonObject.put("Error","Only PDF files are supported");
            return jsonObject;
        }

        PDDocument pdf1 = PDDocument.load(pdfFile1);
        PDDocument pdf2 = PDDocument.load(pdfFile2);
        PDPageTree pdf1pages = pdf1.getDocumentCatalog().getPages();
        PDPageTree pdf2pages = pdf2.getDocumentCatalog().getPages();
        int numberOfPages = pdf1pages.getCount();

        try {
            if ((pageNumber ==0) && (pdf1pages.getCount() != pdf2pages.getCount())) {
                String message = "Page Count Mismatch. Number of Page in PDF1 : " + pdf1pages.getCount() + " | Number of Page in PDF2 : " + pdf2pages.getCount();
                System.out.println(message);
                jsonObject.put("Error",message);
                return jsonObject;
            }

            //Overwritten protected method to get font and size of the text
            PDFTextStripper pdfStripper1 = new PDFTextStripper() {
                String prevBaseFont = "";
                String prevBaseFontSize = "";
                boolean preItalic = false;
                boolean preBold = false;
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    StringBuilder builder = new StringBuilder();

                    for (TextPosition position : textPositions) {

                        //Base Font Validation
                        if (ENABLE_FONT_VALIDATION){
                            String baseFont = position.getFont().getFontDescriptor().getFontName();
                            if (baseFont != null && !baseFont.equals(prevBaseFont)) {
                                builder.append('[').append(baseFont).append(']');
                                prevBaseFont = baseFont;
                            }
                        }

                        //Font Size Validation
                        if (ENABLE_FONT_SIZE_VALIDATION){
                            String baseFontSize = String.valueOf(position.getFontSizeInPt());
                            if (!baseFontSize.equals(prevBaseFontSize)) {
                                builder.append('[').append(baseFontSize).append(']');
                                prevBaseFontSize = baseFontSize;
                            }
                        }

                        //Bold Italic Validation
                        if (ENABLE_BOLD_ITALIC_VALIDATION){
                            boolean italic = position.getFont().getFontDescriptor().isItalic();
                            boolean bold = position.getFont().getFontDescriptor().isForceBold();
                            if (italic != preItalic || bold != preBold) {
                                builder.append('[').append("Italic/Bold").append(']');
                                preItalic = italic;
                                preBold = bold;
                            }
                        }

                        builder.append(position.getUnicode());
                    }
                    writeString(builder.toString());
                }
            };


            //Overwritten protected method to get font and size of the text
            PDFTextStripper pdfStripper2 = new PDFTextStripper() {
                String prevBaseFont = "";
                String prevBaseFontSize = "";
                boolean preItalic = false;
                boolean preBold = false;

                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    StringBuilder builder = new StringBuilder();

                    for (TextPosition position : textPositions) {

                        //Font Validation
                        if (ENABLE_FONT_VALIDATION){
                            String baseFont = position.getFont().getFontDescriptor().getFontName();
                            if (baseFont != null && !baseFont.equals(prevBaseFont)) {
                                builder.append('[').append(baseFont).append(']');
                                prevBaseFont = baseFont;
                            }
                        }

                        //Font Size validation
                        if (ENABLE_FONT_SIZE_VALIDATION){
                            String baseFontSize = String.valueOf(position.getFontSizeInPt());
                            if (!baseFontSize.equals(prevBaseFontSize)) {
                                builder.append('[').append(baseFontSize).append(']');
                                prevBaseFontSize = baseFontSize;
                            }
                        }

                        //Bold Italic Validation
                        if (ENABLE_BOLD_ITALIC_VALIDATION){
                            boolean italic = position.getFont().getFontDescriptor().isItalic();
                            boolean bold = position.getFont().getFontDescriptor().isForceBold();
                            if (italic != preItalic || bold != preBold) {
                                builder.append('[').append("Italic/Bold").append(']');
                                preItalic = italic;
                                preBold = bold;
                            }
                        }

                        builder.append(position.getUnicode());
                    }
                    writeString(builder.toString());
                }
            };

            //To validate only a specific page in PDF
            if (!(pageNumber==0)) numberOfPages = 1;

            System.out.println("Total Number Of Page in PDF 1 : " + pdf1pages.getCount());
            System.out.println("Total Number Of Page in PDF 2 : " + pdf2pages.getCount());
            for (int i = 0; i < numberOfPages; i++) {
                int pageNumberToValidate = (i+1);
                if (!(pageNumber==0)) pageNumberToValidate = pageNumber;
                pdfStripper1.setStartPage(pageNumberToValidate);
                pdfStripper1.setEndPage(pageNumberToValidate);
                pdfStripper2.setStartPage(pageNumberToValidate);
                pdfStripper2.setEndPage(pageNumberToValidate);
                String pdf1PageText = pdfStripper1.getText(pdf1);
                String pdf2PageText = pdfStripper2.getText(pdf2);
                String[] pdf1PageTextLines = pdf1PageText.split("\n");
                String[] pdf2PageTextLines = pdf2PageText.split("\n");


                //PDF Validation
                if (pdf1PageTextLines.length != pdf2PageTextLines.length) {
                    JSONObject item = new JSONObject();
                    String message = "Number of lines are not on same on page # " + pageNumberToValidate + ". Hence no further validation done";
                    item.put("Error",message);
                    jsonArray.put(item);
                } else {
                    for (int j = 0; j < pdf1PageTextLines.length; j++) {
                        if (!pdf1PageTextLines[j].equals(pdf2PageTextLines[j])) {
                            JSONObject item = new JSONObject();
                            item.put("Page#",pageNumberToValidate);
                            item.put("Line#",j+1);
                            item.put("PDF 1 Text",pdf1PageTextLines[j]);
                            item.put("PDF 2 Text",pdf2PageTextLines[j]);
                            jsonArray.put(item);
                        }
                    }
                }

            }
            jsonObject.put("Mismatches",jsonArray);
            System.out.println("PDF Validation completed");
        } finally {
            pdf1.close();
            pdf2.close();
        }
        return jsonObject;
    }

    public String getFileText(File pdfFile) throws Exception {
        return getFileText(pdfFile,0);
    }

    public String getFileText(String pdfFile, int pageNumber) throws Exception{
        return getFileText(new File(pdfFile),pageNumber);
    }

    public String getFileText(File pdfFile, int pageNumber) throws Exception{
        PDDocument pdf = PDDocument.load(pdfFile);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        if (pageNumber != 0){
            pdfStripper.setStartPage(pageNumber);
            pdfStripper.setEndPage(pageNumber);
        }
        return pdfStripper.getText(pdf);
    }

    public PDFCompare setEnableFontValidation(boolean enableFontValidationFlag){
        this.ENABLE_FONT_VALIDATION = enableFontValidationFlag;
        return this;
    }
    public PDFCompare setEnableFontSizeValidation(boolean enableFontSizeValidationFlag){
        this.ENABLE_FONT_SIZE_VALIDATION = enableFontSizeValidationFlag;
        return this;
    }
    public PDFCompare setBoldItalicValidation(boolean enableBoldItalicValidationFlag){
        this.ENABLE_BOLD_ITALIC_VALIDATION = enableBoldItalicValidationFlag;
        return this;
    }
}
