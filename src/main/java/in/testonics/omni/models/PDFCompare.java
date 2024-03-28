package in.testonics.omni.models;

import in.testonics.omni.utils.FileUtils;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PDFCompare extends FileUtils {

    private boolean ENABLE_FONT_VALIDATION = false;
    private boolean ENABLE_FONT_SIZE_VALIDATION = false;
    private boolean FETCH_IMAGES = false;
    private String IMAGES_PATH = ".//target//";
    private Map<String, List<String>> mapExpectedPatterns = new HashMap<>();
    private List<String> expectedPatternList = new ArrayList<>();

    public JSONObject compare(String pathOfFile1, String pathOfFile2, int pageNumber) throws Exception {
        return compare(new File(pathOfFile1), new File(pathOfFile2), pageNumber);
    }

    //Compares all the files in a folder or single file
    public JSONObject compare(String fileOrFolderPath1, String fileOrFolderPath2) throws Exception {
        JSONObject jsonObject = new JSONObject();
        File file1 = new File(fileOrFolderPath1);
        File file2 = new File(fileOrFolderPath2);

        if (file1.isDirectory() && file2.isDirectory()) {
            File[] files = file1.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isFile()) {
                    JSONObject jsonObject1 = new JSONObject();
                    String fileName = file.getName();
                    File FileToCompare1 = new File(fileOrFolderPath1 + "//" + fileName);
                    File FileToCompare2 = new File(fileOrFolderPath2 + "//" + fileName);
                    if (!FileToCompare2.exists())
                        jsonObject1.put("Error", FileToCompare2.getAbsoluteFile() + " file not found");
                    else
                        jsonObject1 = compare(FileToCompare1, FileToCompare2);
                    try {
                        jsonObject.put(file.getName(), jsonObject1.get("Error"));
                    } catch (JSONException jsonException) {
                        jsonObject.put(file.getName(), jsonObject1.get("Mismatches"));
                    }
                }
            }
        } else {
            return compare(file1, file2);
        }
        return jsonObject;
    }

    public JSONObject compare(File pdfFile1, File pdfFile2) throws Exception {
        return compare(pdfFile1, pdfFile2, 0);
    }

    public JSONObject compare(File pdfFile1, File pdfFile2, int pageNumber) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        setColumnNames();
        System.out.println("Comparing PDF files : \nPDF 1: " + pdfFile1 + "\nPDF2 : " + pdfFile2);

        if (!getFileExtension(pdfFile1).equalsIgnoreCase("PDF") || !getFileExtension(pdfFile2).equalsIgnoreCase("PDF")) {
            jsonObject.put("Error", "Only PDF files are supported");
            return jsonObject;
        }

        PDDocument pdf1 = PDDocument.load(pdfFile1);
        PDDocument pdf2 = PDDocument.load(pdfFile2);
        PDPageTree pdf1pages = pdf1.getDocumentCatalog().getPages();
        PDPageTree pdf2pages = pdf2.getDocumentCatalog().getPages();
        int numberOfPages = pdf1pages.getCount();

        try {
            if ((pageNumber == 0) && (pdf1pages.getCount() != pdf2pages.getCount())) {
                String message = "Page Count Mismatch. Number of Page in PDF1 : " + pdf1pages.getCount() + " | Number of Page in PDF2 : " + pdf2pages.getCount();
                System.out.println(message);
                jsonObject.put("Error", message);
                return jsonObject;
            }

            //To validate only a specific page in PDF
            if (!(pageNumber == 0)) numberOfPages = 1;

            System.out.println("Total Number Of Page in PDF 1 : " + pdf1pages.getCount());
            System.out.println("Total Number Of Page in PDF 2 : " + pdf2pages.getCount());
            for (int i = 0; i < numberOfPages; i++) {
                int pageNumberToValidate = (i + 1);

                //Setting the expected patters on the page pageNumberToValidate
                expectedPatternList.clear();
                setExpectedPatternList(pageNumberToValidate);

                if (!(pageNumber == 0)) pageNumberToValidate = pageNumber;
                String pdf1PageText = getFileText(pdfFile1, pageNumberToValidate);
                String pdf2PageText = getFileText(pdfFile2, pageNumberToValidate);
                String[] pdf1PageTextLines = pdf1PageText.split("\n");
                String[] pdf2PageTextLines = pdf2PageText.split("\n");

                //PDF Validation
                if (pdf1PageTextLines.length != pdf2PageTextLines.length) {
                    JSONObject item = new JSONObject();
                    String message = "Number of lines are not on same on page # " + pageNumberToValidate + ". Hence no further validation done";
                    item.put("Error", message);
                    jsonArray.put(item);
                } else {
                    for (int j = 0; j < pdf1PageTextLines.length; j++) {
                        if (!pdf1PageTextLines[j].equals(pdf2PageTextLines[j])) {
                            if (!isExpectedPatternMatch(pdf1PageTextLines[j], pdf2PageTextLines[j])) {
                                JSONObject item = new JSONObject();
                                item.put("Page#", pageNumberToValidate);
                                item.put("Line#", j + 1);
                                item.put("PDF 1 Text", pdf1PageTextLines[j]);
                                item.put("PDF 2 Text", pdf2PageTextLines[j]);
                                jsonArray.put(item);
                            }
                        }
                    }
                }

            }
            jsonObject.put("Mismatches", jsonArray);
            System.out.println("PDF Validation completed");
        } finally {
            pdf1.close();
            pdf2.close();
        }
        return jsonObject;
    }

    private void setExpectedPatternList(int pageNumber) {
        if (mapExpectedPatterns != null) {
            if (mapExpectedPatterns.get("All") != null) {
                expectedPatternList.addAll(mapExpectedPatterns.get("All"));
            }
            if (mapExpectedPatterns.get(String.valueOf(pageNumber)) != null) {
                expectedPatternList.addAll(mapExpectedPatterns.get(String.valueOf(pageNumber)));
            }
        }
    }

    private boolean isExpectedPatternMatch(String line1, String line2) {
        if (expectedPatternList.isEmpty()) return false;

        Optional<String> matchedPatterns = expectedPatternList.stream().filter(pattern -> line1.trim().matches(pattern)).findFirst();
        if (matchedPatterns.isPresent()) {
            String matchedPattern = matchedPatterns.get();
            return line2.trim().matches(matchedPattern);
        } else {
            return false;
        }
    }

    public String getFileText(File pdfFile) throws Exception {
        return getFileText(pdfFile, 0);
    }

    public String getFileText(String pdfFile, int pageNumber) throws Exception {
        return getFileText(new File(pdfFile), pageNumber);
    }

    public String getFileTextAndImage(String pdfFile, int pageNumber) throws Exception {
        return getFileTextAndImage(new File(pdfFile), pageNumber);
    }

    public String getFileTextAndImage(File pdfFile, int pageNumber) throws Exception {
        //Fetches the images if FETCH_IMAGE flag is true
        String images = getImages(pdfFile, pageNumber).toString();
        String text = getFileText(pdfFile, pageNumber);
        return images + "\n" + text;
    }

    public String getFileText(File pdfFile, int pageNumber) throws Exception {

        try (PDDocument pdf = PDDocument.load(pdfFile)) {

            //Overwritten protected method to get font and size of the text
            PDFTextStripper pdfStripper = new PDFTextStripper() {
                String prevBaseFont = "";
                String prevBaseFontSize = "";

                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    StringBuilder builder = new StringBuilder();

                    for (TextPosition position : textPositions) {

                        //Base Font Validation
                        if (ENABLE_FONT_VALIDATION) {
                            String baseFont = position.getFont().getFontDescriptor().getFontName();
                            if (baseFont != null && !baseFont.equals(prevBaseFont)) {
                                builder.append('[').append(baseFont).append(']');
                                prevBaseFont = baseFont;
                            }
                        }

                        //Font Size Validation
                        if (ENABLE_FONT_SIZE_VALIDATION) {
                            String baseFontSize = String.valueOf(position.getFontSizeInPt());
                            if (!baseFontSize.equals(prevBaseFontSize)) {
                                builder.append('[').append(baseFontSize).append(']');
                                prevBaseFontSize = baseFontSize;
                            }
                        }

                        builder.append(position.getUnicode());
                    }
                    writeString(builder.toString());
                }
            };

            if (pageNumber != 0) {
                pdfStripper.setStartPage(pageNumber);
                pdfStripper.setEndPage(pageNumber);
            }

            pdfStripper.setSortByPosition(true);
            return pdfStripper.getText(pdf);
        }

    }

    public JSONObject getImages(File pdfFile, int pageNumber) throws Exception {
        JSONObject jsonObject = new JSONObject();

        if (!FETCH_IMAGES)
            return jsonObject;

        try (PDDocument pdfDocument = PDDocument.load(pdfFile)) {
            System.out.println("Extracting the image from the PDF at page number : " + pageNumber);
            PDPage page = pdfDocument.getPage(pageNumber - 1);
            PDResources pdResources = page.getResources();

            int imageCounter = 1;
            for (COSName c : pdResources.getXObjectNames()) {
                if (pdResources.isImageXObject(c)) {
                    PDXObject o = pdResources.getXObject(c);
                    String imageFilePath = IMAGES_PATH + System.nanoTime() + ".png";
                    jsonObject.put("Image" + imageCounter, imageFilePath);
                    File file = new File(imageFilePath);
                    ImageIO.write(((PDImageXObject) o).getImage(), "png", file);
                    imageCounter++;
                }
            }
        }
        return jsonObject;
    }

    //Filters the data based on the expected pattern added in the file at path expectedPatternFilePath
    public Map<String, List<String>> getExpectedPatterns(String expectedPatternFilePath, String pdfName) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        // Expected Pattern Data
        String jsonData = new String(Files.readAllBytes(Paths.get(expectedPatternFilePath)));
        try {
            // Parse the JSON data
            JSONObject json = new JSONObject(jsonData);
            // Get the child object (pages) for each file
            JSONObject pages = json.getJSONObject(pdfName);
            // Iterate over the child objects (pages) in each file
            for (String page : pages.keySet()) {
                List<String> pageList = new ArrayList<>();
                // Get the regex patterns for each page
                JSONObject regexPatterns = pages.getJSONObject(page);
                // Iterate over the regex patterns in each page
                for (String regex : regexPatterns.keySet()) {

                    String pattern = regexPatterns.getString(regex);
                    pageList.add(pattern);
                }
                String pageNumber = page.replace("Page#", "");
                map.put(pageNumber, pageList);
            }
        } catch (Exception exception) {
            System.out.println("Below exception occurred while fetching expected patterns. Hence, doing pdf comparison without expected patterns");
            System.out.println(exception.getMessage());
        }
        return map;
    }

    public PDFCompare setEnableFontValidation(boolean enableFontValidationFlag) {
        this.ENABLE_FONT_VALIDATION = enableFontValidationFlag;
        return this;
    }

    public PDFCompare setEnableFontSizeValidation(boolean enableFontSizeValidationFlag) {
        this.ENABLE_FONT_SIZE_VALIDATION = enableFontSizeValidationFlag;
        return this;
    }

    public PDFCompare setFetchImagesFlag(boolean fetchImagesFlag) {
        this.FETCH_IMAGES = fetchImagesFlag;
        return this;
    }

    public PDFCompare setImagesPath(String imageFolderPath) {
        this.IMAGES_PATH = imageFolderPath;
        return this;
    }

    public PDFCompare setExpectedPatterns(Map<String, List<String>> mapExpectedPatterns) {
        this.mapExpectedPatterns = mapExpectedPatterns;
        return this;
    }

}
