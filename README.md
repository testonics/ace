## ACE
Compares & extracts the text from given PDF(s)

*   Extract the text from a given PDF as a whole or a particular page

*   Compares the 2 given PDFs by text, font style, font size, font name

## Configuration
Below configurations can be updated based on your needs.

| *Property* | *Description*                                                             |
| --- |---------------------------------------------------------------------------|
| `ENABLE_FONT_VALIDATION` | Enables the font name validation if true. By detault false                |
| `ENABLE_FONT_SIZE_VALIDATION` | Enables the font size validation if true. By detault false                |
| `ENABLE_BOLD_ITALIC_VALIDATION` | Enables the font style (Italic/Bold) validation if true. By detault false |

## Usage

#### Maven
```xml
<dependency>
    <groupId>in.testonics.omni</groupId>
    <artifactId>ace</artifactId>
    <version>1.0.0</version>
</dependency>
```
#### Gradle
```groovy
compile 'in.testonics.omni:ace:1.0.0'
```

#### Compares the 2 given PDFs
```java
        //Returns the outout in Json Format
        System.out.println("PDF Font, Size, Type and Text Mismatch validation");
        PDFCompare pdfCompare = new PDFCompare();
        pdfCompare.setEnableFontValidation(true);
        pdfCompare.setEnableFontSizeValidation(true);
        pdfCompare.setBoldItalicValidation(true);
        JSONObject jsonObject = pdfCompare.compare("PDF3.pdf", "PDF5.pdf", 1);
        System.out.println(jsonObject);
```

#### Extract the text from a given PDF
```java
        public void ExtractText() throws Exception {
            System.out.println("Extracting the text");
            String text = pdfCompare.getFileText("PDF3.pdf",1);
            System.out.println("Extracted Text from page 1 :" + text);
        }
```


## License
This project is Apache License 2.0 - see the [LICENSE](LICENSE) file for details

## Release Notes

Can be found in [RELEASE_NOTES](RELEASE_NOTES.md).

## Code of Conduct
Please, follow [Code of Conduct](CODE_OF_CONDUCT.md) page.

#### Also if you're interesting - see my other repositories
*   [Visual](https://visual.testonics.in/) - Textual and Visual Comparison Of Images
*   [Keep-Alive](https://keepalive.testonics.in/) - Keeps the device awake (stops from sleeping) 
