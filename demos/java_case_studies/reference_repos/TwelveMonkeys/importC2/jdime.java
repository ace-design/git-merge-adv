package com.twelvemonkeys.servlet.image;
import com.twelvemonkeys.image.ImageUtil;
import com.twelvemonkeys.io.FastByteArrayOutputStream;
import com.twelvemonkeys.io.FileUtil;
import com.twelvemonkeys.servlet.OutputStreamAdapter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ImageServletResponseImplTestCase
 *
 * @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
 * @author last modified by $Author: haku $
 * @version $Id: //depot/branches/personal/haraldk/twelvemonkeys/release-2/twelvemonkeys-servlet/src/test/java/com/twelvemonkeys/servlet/image/ImageServletResponseImplTestCase.java#6 $
 */
public class ImageServletResponseImplTestCase {
  private static final String CONTENT_TYPE_BMP = "image/bmp";

  private static final String CONTENT_TYPE_FOO = "foo/bar";

  private static final String CONTENT_TYPE_GIF = "image/gif";

  private static final String CONTENT_TYPE_JPEG = "image/jpeg";

  private static final String CONTENT_TYPE_PNG = "image/png";

  private static final String CONTENT_TYPE_TEXT = "text/plain";

  private static final String IMAGE_NAME_PNG = "12monkeys-splash.png";

  private static final String IMAGE_NAME_GIF = "tux.gif";

  private static final String IMAGE_NAME_PNG_INDEXED = "star.png";

  private static final Dimension IMAGE_DIMENSION_PNG = new Dimension(300, 410);

  private static final Dimension IMAGE_DIMENSION_GIF = new Dimension(250, 250);

  private static final Dimension IMAGE_DIMENSION_PNG_INDEXED = new Dimension(199, 192);

  private static final int STREAM_DEFAULT_SIZE = 2000;

  private HttpServletRequest request;

  private ServletContext context;

  @Before public void init() throws Exception {
    request = mock(HttpServletRequest.class);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    context = mock(ServletContext.class);
    when(context.getResource("/" + IMAGE_NAME_PNG)).thenReturn(getClass().getResource(IMAGE_NAME_PNG));
    when(context.getResource("/" + IMAGE_NAME_GIF)).thenReturn(getClass().getResource(IMAGE_NAME_GIF));
    when(context.getResource("/" + IMAGE_NAME_PNG_INDEXED)).thenReturn(getClass().getResource(IMAGE_NAME_PNG_INDEXED));
    when(context.getMimeType("file.bmp")).thenReturn(CONTENT_TYPE_BMP);
    when(context.getMimeType("file.foo")).thenReturn(CONTENT_TYPE_FOO);
    when(context.getMimeType("file.gif")).thenReturn(CONTENT_TYPE_GIF);
    when(context.getMimeType("file.jpeg")).thenReturn(CONTENT_TYPE_JPEG);
    when(context.getMimeType("file.png")).thenReturn(CONTENT_TYPE_PNG);
    when(context.getMimeType("file.txt")).thenReturn(CONTENT_TYPE_TEXT);
    MockLogger mockLogger = new MockLogger();
    doAnswer(mockLogger).when(context).log(anyString());
    doAnswer(mockLogger).when(context).log(anyString(), any(Throwable.class));
    doAnswer(mockLogger).when(context).log(any(Exception.class), anyString());
  }

  private void fakeResponse(HttpServletRequest pRequest, ImageServletResponseImpl pImageResponse) throws IOException {
    String uri = pRequest.getRequestURI();
    int index = uri.lastIndexOf('/');
    assertTrue(uri, index >= 0);
    String name = uri.substring(index + 1);
    InputStream in = getClass().getResourceAsStream(name);
    if (in == null) {
      pImageResponse.sendError(HttpServletResponse.SC_NOT_FOUND, uri + " not found");
    } else {
      String ext = name.substring(name.lastIndexOf("."));
      pImageResponse.setContentType(context.getMimeType("file" + ext));
      pImageResponse.setContentLength(234);
      try {
        ServletOutputStream out = pImageResponse.getOutputStream();
        try {
          FileUtil.copy(in, out);
        }  finally {
          out.close();
        }
      }  finally {
        in.close();
      }
    }
  }

  @Test public void testBasicResponse() throws IOException {
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertEquals(IMAGE_DIMENSION_PNG.width, image.getWidth());
    assertEquals(IMAGE_DIMENSION_PNG.height, image.getHeight());
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).setContentType(CONTENT_TYPE_PNG);
    verify(response).getOutputStream();
  }

  @Test public void testNoOpResponse() throws IOException {
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    assertTrue("Data differs", Arrays.equals(FileUtil.read(getClass().getResourceAsStream(IMAGE_NAME_PNG)), out.toByteArray()));
    verify(response).setContentType(CONTENT_TYPE_PNG);
    verify(response).getOutputStream();
  }

  @Test public void testTranscodeResponsePNGToJPEG() throws IOException {
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    imageResponse.setOutputContentType("image/jpeg");
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    ByteArrayInputStream input = out.createInputStream();
    assertEquals(0xFF, input.read());
    assertEquals(0xD8, input.read());
    assertEquals(0xFF, input.read());
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(IMAGE_DIMENSION_PNG.width, outImage.getWidth());
    assertEquals(IMAGE_DIMENSION_PNG.height, outImage.getHeight());
    BufferedImage image = flatten(ImageIO.read(context.getResource("/" + IMAGE_NAME_PNG)), Color.BLACK);
    assertSimilarImage(image, outImage, 144f);
    verify(response).setContentType(CONTENT_TYPE_JPEG);
    verify(response).getOutputStream();
  }

  @Test public void testTranscodeResponsePNGToGIFWithQuality() throws IOException {
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    when(request.getAttribute(ImageServletResponse.ATTRIB_OUTPUT_QUALITY)).thenReturn(.5f);
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    imageResponse.setOutputContentType("image/gif");
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    ByteArrayInputStream stream = out.createInputStream();
    assertEquals('G', stream.read());
    assertEquals('I', stream.read());
    assertEquals('F', stream.read());
    assertEquals('8', stream.read());
    assertEquals('9', stream.read());
    assertEquals('a', stream.read());
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(IMAGE_DIMENSION_PNG.width, outImage.getWidth());
    assertEquals(IMAGE_DIMENSION_PNG.height, outImage.getHeight());
    BufferedImage image = ImageIO.read(context.getResource("/" + IMAGE_NAME_PNG));
    assertSimilarImageTransparent(image, outImage, 50f);
    verify(response).setContentType(CONTENT_TYPE_GIF);
    verify(response).getOutputStream();
  }

  @Test public void testTranscodeResponsePNGToGIF() throws IOException {
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    imageResponse.setOutputContentType("image/gif");
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(IMAGE_DIMENSION_PNG.width, outImage.getWidth());
    assertEquals(IMAGE_DIMENSION_PNG.height, outImage.getHeight());
    BufferedImage image = ImageIO.read(context.getResource("/" + IMAGE_NAME_PNG));
    assertSimilarImageTransparent(image, outImage, 50f);
    verify(response).setContentType(CONTENT_TYPE_GIF);
    verify(response).getOutputStream();
  }

  @Test public void testTranscodeResponseIndexColorModelGIFToJPEG() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_GIF);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    imageResponse.setOutputContentType("image/jpeg");
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    ByteArrayInputStream stream = out.createInputStream();
    assertEquals(0xFF, stream.read());
    assertEquals(0xD8, stream.read());
    assertEquals(0xFF, stream.read());
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(IMAGE_DIMENSION_GIF.width, outImage.getWidth());
    assertEquals(IMAGE_DIMENSION_GIF.height, outImage.getHeight());
    BufferedImage image = flatten(ImageIO.read(context.getResource("/" + IMAGE_NAME_GIF)), Color.WHITE);
    assertSimilarImage(image, outImage, 96f);
  }

  @Test public void testIndexedColorModelResizePNG() throws IOException {
    int[] algorithms = new int[] { Image.SCALE_DEFAULT, Image.SCALE_FAST, Image.SCALE_SMOOTH, Image.SCALE_REPLICATE, Image.SCALE_AREA_AVERAGING, 77 };
    for (int algorithm : algorithms) {
      Dimension size = new Dimension(100, 100);
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
      when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE_UNIFORM)).thenReturn(false);
      when(request.getAttribute(ImageServletResponse.ATTRIB_IMAGE_RESAMPLE_ALGORITHM)).thenReturn(algorithm);
      when(request.getContextPath()).thenReturn("/ape");
      when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG_INDEXED);
      FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
      HttpServletResponse response = mock(HttpServletResponse.class);
      when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
      ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
      fakeResponse(request, imageResponse);
      imageResponse.getImage();
      imageResponse.flush();
      assertTrue("Content has no data", out.size() > 0);
      ByteArrayInputStream inputStream = out.createInputStream();
      assertEquals(0x89, inputStream.read());
      assertEquals('P', inputStream.read());
      assertEquals('N', inputStream.read());
      assertEquals('G', inputStream.read());
      BufferedImage outImage = ImageIO.read(out.createInputStream());
      assertNotNull(outImage);
      assertEquals(size.width, outImage.getWidth());
      assertEquals(size.height, outImage.getHeight());
      BufferedImage read = ImageIO.read(context.getResource("/" + IMAGE_NAME_PNG_INDEXED));
      BufferedImage image = ImageUtil.createResampled(read, size.width, size.height, imageResponse.getResampleAlgorithmFromRequest());
      assertSimilarImageTransparent(image, outImage, 10f);
    }
  }

  private static BufferedImage flatten(final BufferedImage pImage, final Color pBackgroundColor) {
    BufferedImage image = ImageUtil.toBuffered(pImage, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    try {
      g.setComposite(AlphaComposite.DstOver);
      g.setColor(pBackgroundColor);
      g.fillRect(0, 0, pImage.getWidth(), pImage.getHeight());
    }  finally {
      g.dispose();
    }
    return image;
  }

  /**
     * Makes sure images are the same, taking JPEG artifacts into account.
     *
     * @param pExpected the expected image
     * @param pActual the actual image
     * @param pArtifactThreshold the maximum allowed difference between the expected and actual pixel value
     */
  private void assertSimilarImage(final BufferedImage pExpected, final BufferedImage pActual, final float pArtifactThreshold) {
    for (int y = 0; y < pExpected.getHeight(); y++) {
      for (int x = 0; x < pExpected.getWidth(); x++) {
        int expected = pExpected.getRGB(x, y);
        int actual = pActual.getRGB(x, y);
        float alpha = ((expected >> 24) & 0xff) / 255f;
        assertEquals(alpha * ((expected >> 16) & 0xff), (actual >> 16) & 0xff, pArtifactThreshold);
        assertEquals(alpha * ((expected >> 8) & 0xff), (actual >> 8) & 0xff, pArtifactThreshold);
        assertEquals(alpha * ((expected) & 0xff), actual & 0xff, pArtifactThreshold);
      }
    }
  }

  private void assertSimilarImageTransparent(final BufferedImage pExpected, final BufferedImage pActual, final float pArtifactThreshold) {
    IndexColorModel icm = pActual.getColorModel() instanceof IndexColorModel ? (IndexColorModel) pActual.getColorModel() : null;
    Object pixel = null;
    for (int y = 0; y < pExpected.getHeight(); y++) {
      for (int x = 0; x < pExpected.getWidth(); x++) {
        int expected = pExpected.getRGB(x, y);
        int actual = pActual.getRGB(x, y);
        if (icm != null) {
          int alpha = (expected >> 24) & 0xff;
          boolean transparent = alpha < 0x40;
          if (transparent) {
            int expectedLookedUp = icm.getRGB(icm.getTransparentPixel());
            assertRGBEquals(x, y, expectedLookedUp & 0xff000000, actual & 0xff000000, 0);
          } else {
            pixel = icm.getDataElements(expected, pixel);
            int expectedLookedUp = icm.getRGB(pixel);
            assertRGBEquals(x, y, expectedLookedUp & 0xffffff, actual & 0xffffff, pArtifactThreshold);
          }
        } else {
          assertRGBEquals(x, y, expected, actual, pArtifactThreshold);
        }
      }
    }
  }

  private void assertRGBEquals(int x, int y, int expected, int actual, float pArtifactThreshold) {
    int expectedA = (expected >> 24) & 0xff;
    int expectedR = (expected >> 16) & 0xff;
    int expectedG = (expected >> 8) & 0xff;
    int expectedB = expected & 0xff;
    try {
      assertEquals("Alpha", expectedA, (actual >> 24) & 0xff, pArtifactThreshold);
      assertEquals("RGB", 0, (Math.abs(expectedR - ((actual >> 16) & 0xff)) + Math.abs(expectedG - ((actual >> 8) & 0xff)) + Math.abs(expectedB - ((actual) & 0xff))) / 3.0, pArtifactThreshold);
    } catch (AssertionError e) {
      AssertionError assertionError = new AssertionError(String.format("@[%d,%d] expected: 0x%08x but was: 0x%08x", x, y, expected, actual));
      assertionError.initCause(e);
      throw assertionError;
    }
  }

  @Test public void testReplaceResponse() throws IOException {
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    imageResponse.setImage(image);
    imageResponse.setOutputContentType("image/bmp");
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    assertSimilarImage(image, outImage, 0);
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_BMP);
  }

  @Test public void testNotFoundInput() throws IOException {
    request = mock(HttpServletRequest.class);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/monkey-business.gif");
    HttpServletResponse response = mock(HttpServletResponse.class);
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
  }

  @Test public void testUnsupportedInput() throws IOException {
    assertFalse("Test is invalid, rewrite test", ImageIO.getImageReadersByFormatName("txt").hasNext());
    request = mock(HttpServletRequest.class);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/foo.txt");
    HttpServletResponse response = mock(HttpServletResponse.class);
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    try {
      imageResponse.setOutputContentType("image/png");
      imageResponse.flush();
      fail("Should throw IOException in case of unspupported input");
    } catch (IOException e) {
      String message = e.getMessage().toLowerCase();
      assertTrue("Wrong message: " + e.getMessage(), message.contains("transcode"));
      assertTrue("Wrong message: " + e.getMessage(), message.contains("reader"));
      assertTrue("Wrong message: " + e.getMessage(), message.contains("text"));
    }
  }

  @Test public void testUnsupportedOutput() throws IOException {
    assertFalse("Test is invalid, rewrite test", ImageIO.getImageWritersByFormatName("foo").hasNext());
    HttpServletResponse response = mock(HttpServletResponse.class);
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    try {
      imageResponse.setOutputContentType("application/xml+foo");
      imageResponse.flush();
      fail("Should throw IOException in case of unspupported output");
    } catch (IOException e) {
      String message = e.getMessage().toLowerCase();
      assertTrue("Wrong message: " + e.getMessage(), message.contains("transcode"));
      assertTrue("Wrong message: " + e.getMessage(), message.contains("writer"));
      assertTrue("Wrong message: " + e.getMessage(), message.contains("foo"));
    }
  }

  @Test public void testReadWithSourceRegion() throws IOException {
    Rectangle sourceRegion = new Rectangle(100, 100, 100, 100);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertEquals(sourceRegion.width, image.getWidth());
    assertEquals(sourceRegion.height, image.getHeight());
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithNonSquareSourceRegion() throws IOException {
    Rectangle sourceRegion = new Rectangle(100, 100, 100, 80);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertEquals(sourceRegion.width, image.getWidth());
    assertEquals(sourceRegion.height, image.getHeight());
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithCenteredUniformSourceRegion() throws IOException {
    Rectangle sourceRegion = new Rectangle(-1, -1, 300, 300);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI_UNIFORM)).thenReturn(true);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertEquals(sourceRegion.width, image.getWidth());
    assertEquals(sourceRegion.height, image.getHeight());
    BufferedImage original = ImageIO.read(getClass().getResource(IMAGE_NAME_PNG));
    assertNotNull(original);
    assertEquals(IMAGE_DIMENSION_PNG.width, original.getWidth());
    assertEquals(IMAGE_DIMENSION_PNG.height, original.getHeight());
    sourceRegion.setLocation((int) Math.round((IMAGE_DIMENSION_PNG.width - sourceRegion.getWidth()) / 2.0), (int) Math.round((IMAGE_DIMENSION_PNG.height - sourceRegion.getHeight()) / 2.0));
    for (int y = 0; y < sourceRegion.height; y++) {
      for (int x = 0; x < sourceRegion.width; x++) {
        assertEquals(original.getRGB(x + sourceRegion.x, y + sourceRegion.y), image.getRGB(x, y));
      }
    }
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithCenteredUniformNonSquareSourceRegion() throws IOException {
    Rectangle sourceRegion = new Rectangle(-1, -1, 410, 300);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI_UNIFORM)).thenReturn(true);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    imageResponse.flush();
    assertTrue("Image wider than bounding box", IMAGE_DIMENSION_PNG.width >= image.getWidth());
    assertTrue("Image taller than bounding box", IMAGE_DIMENSION_PNG.height >= image.getHeight());
    assertTrue("Image not maximized to bounding box", IMAGE_DIMENSION_PNG.width == image.getWidth() || IMAGE_DIMENSION_PNG.height == image.getHeight());
    double destAspect = sourceRegion.getWidth() / sourceRegion.getHeight();
    double srcAspect = IMAGE_DIMENSION_PNG.getWidth() / IMAGE_DIMENSION_PNG.getHeight();
    if (srcAspect >= destAspect) {
      assertEquals(IMAGE_DIMENSION_PNG.height, image.getHeight());
      assertEquals("Image width does not follow aspect", Math.round(IMAGE_DIMENSION_PNG.getHeight() * destAspect), image.getWidth());
    } else {
      assertEquals(IMAGE_DIMENSION_PNG.width, image.getWidth());
      assertEquals("Image height does not follow aspect", Math.round(IMAGE_DIMENSION_PNG.getWidth() / destAspect), image.getHeight());
    }
    BufferedImage original = ImageIO.read(getClass().getResource(IMAGE_NAME_PNG));
    assertNotNull(original);
    assertEquals(IMAGE_DIMENSION_PNG.width, original.getWidth());
    assertEquals(IMAGE_DIMENSION_PNG.height, original.getHeight());
    sourceRegion.setLocation((int) Math.round((IMAGE_DIMENSION_PNG.width - image.getWidth()) / 2.0), (int) Math.round((IMAGE_DIMENSION_PNG.height - image.getHeight()) / 2.0));
    sourceRegion.setSize(image.getWidth(), image.getHeight());
    for (int y = 0; y < sourceRegion.height; y++) {
      for (int x = 0; x < sourceRegion.width; x++) {
        assertEquals(original.getRGB(x + sourceRegion.x, y + sourceRegion.y), image.getRGB(x, y));
      }
    }
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithResize() throws IOException {
    Dimension size = new Dimension(100, 120);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertTrue("Image wider than bounding box", size.width >= image.getWidth());
    assertTrue("Image taller than bounding box", size.height >= image.getHeight());
    assertTrue("Image not maximized to bounding box", size.width == image.getWidth() || size.height == image.getHeight());
    if (size.width == image.getWidth()) {
      assertEquals(Math.round(size.getWidth() * IMAGE_DIMENSION_PNG.getWidth() / IMAGE_DIMENSION_PNG.getHeight()), image.getHeight());
    } else {
      assertEquals(Math.round(size.getHeight() * IMAGE_DIMENSION_PNG.getWidth() / IMAGE_DIMENSION_PNG.getHeight()), image.getWidth());
    }
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithNonUniformResize() throws IOException {
    Dimension size = new Dimension(150, 150);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE_UNIFORM)).thenReturn(false);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertEquals(size.width, image.getWidth());
    assertEquals(size.height, image.getHeight());
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithSourceRegionAndResize() throws IOException {
    Rectangle sourceRegion = new Rectangle(100, 100, 200, 200);
    Dimension size = new Dimension(100, 120);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertTrue("Image wider than bounding box", size.width >= image.getWidth());
    assertTrue("Image taller than bounding box", size.height >= image.getHeight());
    assertTrue("Image not maximized to bounding box", size.width == image.getWidth() || size.height == image.getHeight());
    if (size.width == image.getWidth()) {
      assertEquals(Math.round(size.getWidth() * sourceRegion.getWidth() / sourceRegion.getHeight()), image.getHeight());
    } else {
      assertEquals(Math.round(size.getHeight() * sourceRegion.getWidth() / sourceRegion.getHeight()), image.getWidth());
    }
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithSourceRegionAndNonUniformResize() throws IOException {
    Rectangle sourceRegion = new Rectangle(100, 100, 200, 200);
    Dimension size = new Dimension(150, 150);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE_UNIFORM)).thenReturn(false);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertEquals(size.width, image.getWidth());
    assertEquals(size.height, image.getHeight());
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithUniformSourceRegionAndResizeSquare() throws IOException {
    Rectangle sourceRegion = new Rectangle(-1, -1, 300, 300);
    Dimension size = new Dimension(100, 120);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI_UNIFORM)).thenReturn(true);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    imageResponse.flush();
    assertTrue("Image wider than bounding box", size.width >= image.getWidth());
    assertTrue("Image taller than bounding box", size.height >= image.getHeight());
    assertTrue("Image not maximized to bounding box", size.width == image.getWidth() || size.height == image.getHeight());
    if (size.width == image.getWidth()) {
      assertEquals("Image height does not follow aspect", Math.round(size.getWidth() / (sourceRegion.getWidth() / sourceRegion.getHeight())), image.getHeight());
    } else {
      System.out.println("size: " + size);
      System.out.println("image: " + new Dimension(image.getWidth(), image.getHeight()));
      assertEquals("Image width does not follow aspect", Math.round(size.getHeight() * (sourceRegion.getWidth() / sourceRegion.getHeight())), image.getWidth());
    }
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithNonSquareUniformSourceRegionAndResize() throws IOException {
    Rectangle sourceRegion = new Rectangle(-1, -1, 170, 300);
    Dimension size = new Dimension(150, 120);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI_UNIFORM)).thenReturn(true);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    imageResponse.flush();
    assertTrue("Image wider than bounding box", size.width >= image.getWidth());
    assertTrue("Image taller than bounding box", size.height >= image.getHeight());
    assertTrue("Image not maximized to bounding box", size.width == image.getWidth() || size.height == image.getHeight());
    if (size.width == image.getWidth()) {
      assertEquals("Image height does not follow aspect", Math.round(size.getWidth() / (sourceRegion.getWidth() / sourceRegion.getHeight())), image.getHeight());
    } else {
      assertEquals("Image width does not follow aspect", Math.round(size.getHeight() * (sourceRegion.getWidth() / sourceRegion.getHeight())), image.getWidth());
    }
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }

  @Test public void testReadWithAllNegativeSourceRegion() throws IOException {
    Rectangle sourceRegion = new Rectangle(-1, -1, -1, -1);
    Dimension size = new Dimension(100, 120);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI_UNIFORM)).thenReturn(true);
    when(request.getAttribute(ImageServletResponse.ATTRIB_AOI)).thenReturn(sourceRegion);
    when(request.getAttribute(ImageServletResponse.ATTRIB_SIZE)).thenReturn(size);
    when(request.getContextPath()).thenReturn("/ape");
    when(request.getRequestURI()).thenReturn("/ape/" + IMAGE_NAME_PNG);
    FastByteArrayOutputStream out = new FastByteArrayOutputStream(STREAM_DEFAULT_SIZE);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(new OutputStreamAdapter(out));
    ImageServletResponseImpl imageResponse = new ImageServletResponseImpl(request, response, context);
    fakeResponse(request, imageResponse);
    BufferedImage image = imageResponse.getImage();
    assertNotNull(image);
    assertTrue("Image wider than bounding box", size.width >= image.getWidth());
    assertTrue("Image taller than bounding box", size.height >= image.getHeight());
    assertTrue("Image not maximized to bounding box", size.width == image.getWidth() || size.height == image.getHeight());
    imageResponse.flush();
    assertTrue("Content has no data", out.size() > 0);
    BufferedImage outImage = ImageIO.read(out.createInputStream());
    assertNotNull(outImage);
    assertEquals(image.getWidth(), outImage.getWidth());
    assertEquals(image.getHeight(), outImage.getHeight());
    verify(response).getOutputStream();
    verify(response).setContentType(CONTENT_TYPE_PNG);
  }


<<<<<<< Unknown file: This is a bug in JDime.
=======
  private static class BlackLabel extends JLabel {
    private final Paint checkeredBG;

    private boolean opaque = true;

    public BlackLabel(final String text, final BufferedImage outImage) {
      super(text, new BufferedImageIcon(outImage), JLabel.CENTER);
      setOpaque(true);
      setBackground(Color.BLACK);
      setForeground(Color.WHITE);
      setVerticalAlignment(JLabel.CENTER);
      setVerticalTextPosition(JLabel.BOTTOM);
      setHorizontalTextPosition(JLabel.CENTER);
      checkeredBG = createTexture();
    }

    @Override public boolean isOpaque() {
      return opaque && super.isOpaque();
    }

    @Override protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics;
      int iconHeight = getIcon() == null ? 0 : getIcon().getIconHeight() + getIconTextGap();
      g.setPaint(checkeredBG);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(getBackground());
      g.fillRect(0, iconHeight, getWidth(), getHeight() - iconHeight);
      try {
        opaque = false;
        super.paintComponent(g);
      }  finally {
        opaque = true;
      }
    }

    private static Paint createTexture() {
      GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      BufferedImage pattern = graphicsConfiguration.createCompatibleImage(20, 20);
      Graphics2D g = pattern.createGraphics();
      try {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, pattern.getWidth(), pattern.getHeight());
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, pattern.getWidth() / 2, pattern.getHeight() / 2);
        g.fillRect(pattern.getWidth() / 2, pattern.getHeight() / 2, pattern.getWidth() / 2, pattern.getHeight() / 2);
      }  finally {
        g.dispose();
      }
      return new TexturePaint(pattern, new Rectangle(pattern.getWidth(), pattern.getHeight()));
    }
  }
>>>>>>> right.java


  private static class MockLogger implements Answer<Void> {
    public Void answer(InvocationOnMock invocation) throws Throwable {
      Object[] arguments = invocation.getArguments();
      String msg = (String) (arguments[0] instanceof String ? arguments[0] : arguments[1]);
      Throwable t = (Throwable) (arguments[0] instanceof Exception ? arguments[0] : arguments.length > 1 ? arguments[1] : null);
      System.out.println("mock-context: " + msg);
      if (t != null) {
        t.printStackTrace(System.out);
      }
      return null;
    }
  }
}