package com.openshift.internal.client.httpclient;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import com.openshift.internal.client.RequestParameter;

/**
 * @author Andre Dietisheim
 */
public interface IMediaType {
  public String getType();

  public void write(RequestParameter[] parameters, OutputStream out) throws IOException;


<<<<<<< Unknown file: This is a bug in JDime.
=======
  public String encodeParameters(Map<String, Object> parameters) throws EncodingException;
>>>>>>> right.java


  public void write(String name, String value, OutputStream out) throws IOException;

  public void write(String name, List<String> values, OutputStream out) throws IOException;
}