package com.openhtmltopdf.objects.pdf;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Charsets;
import org.w3c.dom.Element;
import com.openhtmltopdf.extend.OutputDevice;
import com.openhtmltopdf.pdfboxout.PdfBoxOutputDevice;
import com.openhtmltopdf.render.RenderingContext;

public class MergeBackgroundPdfDrawer extends PdfDrawerBase {

<<<<<<< left.java
  private final Map<PDFBoxDeviceReference, SoftReference<Map<String, PDFormXObject>>> formMap = new HashMap<>();
=======
>>>>>>> Unknown file: This is a bug in JDime.


  @Override public Map<Shape, String> drawObject(Element e, double x, double y, double width, double height, OutputDevice outputDevice, RenderingContext ctx, int dotsPerPixel) {
    if (!(outputDevice instanceof PdfBoxOutputDevice)) {
      return null;
    }
    PdfBoxOutputDevice pdfBoxOutputDevice = (PdfBoxOutputDevice) outputDevice;

<<<<<<< left.java
    if (map == null) {
      map = new HashMap<>();
      formMap.put(new PDFBoxDeviceReference(pdfBoxOutputDevice), new SoftReference<>(map));
    }
=======
>>>>>>> Unknown file: This is a bug in JDime.

    try {
      LayerUtility layerUtility = new LayerUtility(pdfBoxOutputDevice.getWriter());

<<<<<<< left.java
      if (pdFormXObject == null) {
        try (InputStream inputStream = new URL(url).openStream()) {
          PDFParser pdfParser = new PDFParser(new RandomAccessBuffer(inputStream));
          pdfParser.parse();
          pdFormXObject = layerUtility.importPageAsForm(pdfParser.getPDDocument(), pdfpage - 1);
          pdfParser.getPDDocument().close();
        }
        map.put(url, pdFormXObject);
      }
=======
      PDFormXObject pdFormXObject = importPageAsXForm(ctx, e, pdfBoxOutputDevice, layerUtility);
>>>>>>> right.java

      PDPage page = pdfBoxOutputDevice.getPage();
      layerUtility.wrapInSaveRestore(page);
      COSArray cosArray = (COSArray) page.getCOSObject().getDictionaryObject(COSName.CONTENTS);
      COSStream saveStateAndPlacePageBackgroundStream = (COSStream) cosArray.get(0);
      OutputStream saveAndPlaceStream = saveStateAndPlacePageBackgroundStream.createOutputStream();
      saveAndPlaceStream.write("q\n".getBytes(Charsets.US_ASCII));
      COSName name = page.getResources().add(pdFormXObject);
      name.writePDF(saveAndPlaceStream);
      saveAndPlaceStream.write(' ');
      saveAndPlaceStream.write("Do\n".getBytes(Charsets.US_ASCII));
      saveAndPlaceStream.write("Q\n".getBytes(Charsets.US_ASCII));
      saveAndPlaceStream.write("q\n".getBytes(Charsets.US_ASCII));
      saveAndPlaceStream.close();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    return null;
  }
}