package com.openhtmltopdf.objects.pdf;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
<<<<<<< HEAD
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
=======
>>>>>>> ff947447
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

<<<<<<< HEAD
public class MergeBackgroundPdfDrawer implements FSObjectDrawer {
	private final Map<PDFBoxDeviceReference, SoftReference<Map<String, PDFormXObject>>> formMap = new HashMap<>();
=======
public class MergeBackgroundPdfDrawer extends PdfDrawerBase
{
>>>>>>> ff947447

    @Override
    public Map<Shape, String> drawObject(Element e, double x, double y, double width, double height,
            OutputDevice outputDevice, RenderingContext ctx, int dotsPerPixel)
    {

        /*
         * We can only do something if this is a PDF.
         */
        if (!(outputDevice instanceof PdfBoxOutputDevice))
            return null;

        PdfBoxOutputDevice pdfBoxOutputDevice = (PdfBoxOutputDevice) outputDevice;


<<<<<<< HEAD
		SoftReference<Map<String, PDFormXObject>> mapWeakReference = formMap
				.get(new PDFBoxDeviceReference(pdfBoxOutputDevice));
		Map<String, PDFormXObject> map = null;
		if (mapWeakReference != null)
			map = mapWeakReference.get();
		if (map == null) {
			map = new HashMap<>();
			formMap.put(new PDFBoxDeviceReference(pdfBoxOutputDevice), new SoftReference<>(map));
		}
		try {
			PDFormXObject pdFormXObject = map.get(url);
			LayerUtility layerUtility = new LayerUtility(pdfBoxOutputDevice.getWriter());
			if (pdFormXObject == null) {
				try (InputStream inputStream = new URL(url).openStream()){
					PDFParser pdfParser = new PDFParser(new RandomAccessBuffer(inputStream));
					pdfParser.parse();
					pdFormXObject = layerUtility.importPageAsForm(pdfParser.getPDDocument(), pdfpage - 1);
					pdfParser.getPDDocument().close();
				}
				map.put(url, pdFormXObject);
			}
			PDPage page = pdfBoxOutputDevice.getPage();
=======
        try
        {
            LayerUtility layerUtility = new LayerUtility(pdfBoxOutputDevice.getWriter());
            PDFormXObject pdFormXObject = importPageAsXForm(ctx,e, pdfBoxOutputDevice, layerUtility);
            PDPage page = pdfBoxOutputDevice.getPage();
>>>>>>> ff947447

            /*
             * This ensures that the Contents of the page is a COSArray. The first entry in
             * the array is just a save state (e.g. 'q'). We can override it to add the
             * XForm.
             */
            layerUtility.wrapInSaveRestore(page);
            COSArray cosArray = (COSArray) page.getCOSObject()
                    .getDictionaryObject(COSName.CONTENTS);
            COSStream saveStateAndPlacePageBackgroundStream = (COSStream) cosArray.get(0);
            OutputStream saveAndPlaceStream = saveStateAndPlacePageBackgroundStream
                    .createOutputStream();
            saveAndPlaceStream.write("q\n".getBytes(Charsets.US_ASCII));
            COSName name = page.getResources().add(pdFormXObject);
            name.writePDF(saveAndPlaceStream);
            saveAndPlaceStream.write(' ');
            saveAndPlaceStream.write("Do\n".getBytes(Charsets.US_ASCII));
            saveAndPlaceStream.write("Q\n".getBytes(Charsets.US_ASCII));
            saveAndPlaceStream.write("q\n".getBytes(Charsets.US_ASCII));
            saveAndPlaceStream.close();

<<<<<<< HEAD
		} catch (IOException e1) {
			e1.printStackTrace();
		}
=======
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        return null;
    }
>>>>>>> ff947447

}
