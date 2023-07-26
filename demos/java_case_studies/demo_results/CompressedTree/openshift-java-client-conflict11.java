package com.openshift.internal.client.httpclient;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import com.openshift.internal.client.RequestParameter;
import java.util.Map;
/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/



/**
 * @author Andre Dietisheim
 */
public interface IMediaType {

	public String getType();
	
<<<<<<< left_content.java
	public void write(RequestParameter[] parameters, OutputStream out) throws IOException;

	public void write(String name, String value, OutputStream out) throws IOException;
=======
	public String encodeParameters(Map<String, Object> parameters) throws EncodingException;
>>>>>>> right_content.java
	
	public void write(String name, List<String> values, OutputStream out) throws IOException;
}

